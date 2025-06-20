// File: rl/sage/rangerlevels/items/altar/AltarAlmasUseHandler.java
package rl.sage.rangerlevels.items.altar;

import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.gui.altar.InvocationSelectionMenu;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.modificadores.*;
import rl.sage.rangerlevels.items.sacrificios.*;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class AltarAlmasUseHandler {

    /** Mapa de UUID de jugador a tick (gameTime) hasta el cual está en cooldown */
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock ev) {
        World world = ev.getWorld();
        if (world.isClientSide) return;
        if (!(world instanceof ServerWorld)) return;
        ServerWorld sworld = (ServerWorld) world;
        long currentTick = sworld.getGameTime();

        ServerPlayerEntity player = (ServerPlayerEntity) ev.getPlayer();
        UUID uuid = player.getUUID();
        Hand hand = ev.getHand();
        ItemStack altarStack = player.getItemInHand(hand);

        // 1) Solo continuamos si es el Altar de Almas
        String rid = altarStack.hasTag() && altarStack.getTag().contains(RangerItemDefinition.NBT_ID_KEY)
                ? altarStack.getTag().getString(RangerItemDefinition.NBT_ID_KEY)
                : null;
        if (!AltarAlmas.ID.equals(rid)) return;

        // Comprobar cooldown: 10s = 200 ticks
        Long nextAllowed = COOLDOWNS.get(uuid);
        if (nextAllowed != null && currentTick < nextAllowed) {
            long remainingTicks = nextAllowed - currentTick;
            long seconds = (remainingTicks + 19) / 20;
            player.sendMessage(
                    new StringTextComponent("§cDebes esperar " + seconds + "s para volver a usar el Altar de Almas."),
                    uuid
            );
            ev.setCanceled(true);
            ev.setCancellationResult(ActionResultType.CONSUME);
            syncSlot(player, hand);
            return;
        }

        ev.setCanceled(true);
        ev.setCancellationResult(ActionResultType.CONSUME);

        // 2) Encontrar glowstone central y validar estructura
        BlockPos clicked = ev.getPos();
        BlockPos center = findCenterGlowstone(world, clicked, ev.getFace());
        if (center == null) {
            player.sendMessage(new StringTextComponent("§cDebes clicar sobre la glowstone central de la estructura."), uuid);
            syncSlot(player, hand);
            return;
        }
        if (!checkStructure(sworld, center)) {
            player.sendMessage(new StringTextComponent("§cEstructura inválida para Altar de Almas."), uuid);
            syncSlot(player, hand);
            return;
        }

        // 3) Buscar una Esencia ligada en inventario
        ItemStack essence = findBoundEssence(player);
        if (essence == null) {
            player.sendMessage(new StringTextComponent("§cNo tienes una Esencia ligada válida en inventario."), uuid);
            return;
        }

        // 4) Extraer species e ID de Pokémon de la esencia
        CompoundNBT tag = essence.getTag();
        String species  = tag.getString("EsenciaSpecies");
        String storedId = tag.getString("EsenciaPokemonID");
        if (species.isEmpty() || storedId.isEmpty()) {
            player.sendMessage(new StringTextComponent("§cLa Esencia encontrada no es válida."), uuid);
            return;
        }

        // 5) Buscar todas las recetas válidas
        List<AltarRecipe> recipes = InvocationCraftHelper.getRecipesForAltar(AltarAlmas.ID);
        List<AltarRecipe> matches = new ArrayList<>();
        for (AltarRecipe recipe : recipes) {
            if (recipe.matches(player)) {
                matches.add(recipe);
            }
        }
        if (matches.isEmpty()) {
            player.sendMessage(new StringTextComponent("§cNo puedes invocar nada con esta esencia."), uuid);
            return;
        }

        // 6) Si solo hay una receta válida, proceder como antes:
        if (matches.size() == 1) {
            AltarRecipe chosen = matches.get(0);
            // Consumir esencia y altar:
            boolean ok = consumeIngredientsAndAltar(player, hand, chosen);
            if (!ok) {
                player.sendMessage(new StringTextComponent("§cError al consumir ingredientes."), uuid);
                syncSlot(player, hand);
                return;
            }
            // Crear y entregar modificador:
            executeInvocation(player, chosen, species, storedId, center);
            // Registrar cooldown
            COOLDOWNS.put(uuid, currentTick + 200);
            return;
        }

        // 7) Si hay más de una, abrir menú de selección.
        // Guardar contexto pendiente:
        PENDING_RECIPES.put(uuid, matches);
        PENDING_CONTEXT.put(uuid, new PendingInvocationContext(species, storedId, center, hand, world, currentTick));
        // Abrir menú de selección:
        InvocationSelectionMenu.open(player, matches);
    }
    public static boolean consumeIngredientsAndAltar(ServerPlayerEntity player, Hand hand, AltarRecipe recipe) {
        // 1) Consumir todos los ingredientes de la receta
        boolean ok = consumeRecipeIngredients(player, recipe);
        if (!ok) {
            // Mensaje de fallo si quieres
            return false;
        }
        // 2) Consumir el altar en la mano
        ItemStack altarStack = player.getItemInHand(hand);
        altarStack.shrink(1);
        syncSlot(player, hand);
        return true;
    }

    public static void executeInvocation(ServerPlayerEntity player, AltarRecipe chosen, String species, String storedId, BlockPos center) {
        UUID uuid = player.getUUID();
        World world = player.level;
        // Crear el modificador:
        ItemStack modifier;
        String path = chosen.getId().getPath();
        if (path.contains("tamano")) {
            if (path.contains("universal")) {
                // stack universal: usar resultSupplier o CustomItemRegistry
                modifier = chosen.getResultSupplier().get().copy();
            } else {
                modifier = ModificadorTamano.createForSpeciesAndId(species, storedId);
            }
        } else if (path.contains("naturaleza")) {
            if (path.contains("universal")) {
                modifier = chosen.getResultSupplier().get().copy();
            } else {
                modifier = ModificadorNaturaleza.createForSpeciesAndId(species, storedId);
            }
        } else if (path.contains("shiny")) {
            if (path.contains("universal")) {
                modifier = chosen.getResultSupplier().get().copy();
            } else {
                modifier = ModificadorShiny.createForSpeciesAndId(species, storedId);
            }
        } else if (path.contains("ivs")) {
            if (path.contains("universal")) {
                modifier = chosen.getResultSupplier().get().copy();
            } else {
                modifier = ModificadorIVs.createForSpeciesAndId(species, storedId);
            }
        } else {
            modifier = chosen.getResultSupplier().get().copy();
        }
        if (modifier.isEmpty()) {
            player.sendMessage(new StringTextComponent("§cError al crear el ítem invocado."), uuid);
            return;
        }
        // Intentar añadir al inventario o dropear:
        if (!player.inventory.add(modifier)) {
            ItemEntity drop = new ItemEntity(
                    world,
                    center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
                    modifier);
            world.addFreshEntity(drop);
        }
        // Mensaje, sonido y efectos:
        player.sendMessage(chosen.getSuccessMessage(), uuid);
        PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.WITHER_AMBIENT, SoundCategory.MASTER, 1.0f, 0.7f);
        PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.BEACON_DEACTIVATE, SoundCategory.MASTER, 1.0f, 1.2f);
        if (world instanceof ServerWorld) {
            InvocationEffects.spawnEpicVictory((ServerWorld)world, center);
        }
    }


    private static BlockPos findCenterGlowstone(World world, BlockPos pos, net.minecraft.util.Direction face) {
        if (world.getBlockState(pos).getBlock() == Blocks.GLOWSTONE) return pos;
        BlockPos adj = pos.relative(face);
        return world.getBlockState(adj).getBlock() == Blocks.GLOWSTONE ? adj : null;
    }

    public static void syncSlot(ServerPlayerEntity player, Hand hand) {
        int windowId  = 0;
        int slotIndex = hand == Hand.MAIN_HAND
                ? 36 + player.inventory.selected
                : 45;
        ItemStack current = hand == Hand.MAIN_HAND
                ? player.inventory.getItem(player.inventory.selected)
                : player.inventory.offhand.get(0);
        player.connection.send(new SSetSlotPacket(windowId, slotIndex, current));
        player.inventoryMenu.broadcastChanges();
    }

    public static ItemStack findBoundEssence(ServerPlayerEntity player) {
        for (ItemStack s : player.inventory.items) {
            if (s.isEmpty()) continue;
            String id = RangerItemDefinition.getIdFromStack(s);
            if ((EsenciaLegendaria.ID.equals(id) || EsenciaUltraente.ID.equals(id))
                    && s.hasTag() && s.getTag().contains("EsenciaPokemonID")) {
                return s;
            }
        }
        return null;
    }

    private static boolean checkStructure(ServerWorld world, BlockPos center) {
        // Capa 0
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = center.offset(dx, 0, dz);
                if (dx == 0 && dz == 0) {
                    if (world.getBlockState(p).getBlock() != Blocks.GLOWSTONE) return false;
                } else {
                    if (world.getBlockState(p).getBlock() != Blocks.RED_NETHER_BRICKS) return false;
                }
            }
        }
        // Capa 1: esquinas
        for (int dx : new int[]{-1, 1}) {
            for (int dz : new int[]{-1, 1}) {
                BlockPos p = center.offset(dx, 1, dz);
                if (world.getBlockState(p).getBlock() != Blocks.RED_NETHER_BRICK_WALL) return false;
            }
        }
        return true;
    }
    // Dentro de AltarAlmasUseHandler o en otra clase helper:
    public static class PendingInvocationContext {
        public final String species;
        public final String storedId;
        public final BlockPos center;
        public final Hand hand;
        public final World world;
        public final long currentTick;
        // Podrías agregar más datos si hace falta
        public PendingInvocationContext(String species, String storedId, BlockPos center, Hand hand, World world, long currentTick) {
            this.species = species;
            this.storedId = storedId;
            this.center = center;
            this.hand = hand;
            this.world = world;
            this.currentTick = currentTick;
        }
    }
    private static final Map<UUID, List<AltarRecipe>> PENDING_RECIPES = new ConcurrentHashMap<>();
    private static final Map<UUID, PendingInvocationContext> PENDING_CONTEXT = new ConcurrentHashMap<>();

    public static void clearPending(UUID uuid) {
        PENDING_RECIPES.remove(uuid);
        PENDING_CONTEXT.remove(uuid);
    }
    public static PendingInvocationContext getPendingContext(UUID uuid) {
        return PENDING_CONTEXT.get(uuid);
    }
    public static Long getCooldown(UUID uuid) {
        return COOLDOWNS.get(uuid);
    }
    public static void putCooldown(UUID uuid, long tick) {
        COOLDOWNS.put(uuid, tick);
    }
    /**
     * Intenta consumir de inventario del jugador los ingredientes de la receta.
     * @param player El jugador.
     * @param recipe La receta con su mapa ingredients: Map<String, Integer>.
     * @return true si se consumieron todas las cantidades requeridas; false si faltaba algo (no debería pasar si ya matches).
     */
    public static boolean consumeRecipeIngredients(ServerPlayerEntity player, AltarRecipe recipe) {
        Map<String, Integer> ingredients = recipe.getIngredients(); // asumo que hay un getter
        // Primero, vamos a comprobar que realmente tenemos suficientes (aunque recipe.matches(player) debería garantizarlo)
        // Pero para seguridad podemos omitir esta comprobación y confiar en matches.
        // Ahora, para cada ingrediente, eliminamos la cantidad necesaria:
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            String ingredientId = entry.getKey();
            int required = entry.getValue();
            int remaining = required;

            // Iterar por cada slot del inventario:
            for (int slot = 0; slot < player.inventory.items.size() && remaining > 0; slot++) {
                ItemStack stack = player.inventory.items.get(slot);
                if (stack.isEmpty()) continue;
                // Determinar si este stack corresponde al ingredientId
                // Podría ser un ítem definido en RangerItemDefinition (tu mod) o un ítem vanilla/pixelmon.
                boolean matches = false;
                // 1) Si es un ítem de tu mod con ID en NBT:
                String stackId = RangerItemDefinition.getIdFromStack(stack);
                if (ingredientId.equals(stackId)) {
                    matches = true;
                } else {
                    // 2) Si es un ítem vanilla/pixelmon por ResourceLocation:
                    ResourceLocation rl = stack.getItem().getRegistryName();
                    if (rl != null && ingredientId.equals(rl.toString())) {
                        matches = true;
                    }
                }
                if (!matches) continue;

                int canTake = Math.min(stack.getCount(), remaining);
                stack.shrink(canTake);
                remaining -= canTake;
                // Si el stack quedó en 0, Minecraft lo manejará y slot quedará vacío.
            }
            if (remaining > 0) {
                // No se consiguieron suficientes, esto indica inconsistencia. Podemos abortar o reportar.
                return false;
            }
        }
        return true;
    }
}
