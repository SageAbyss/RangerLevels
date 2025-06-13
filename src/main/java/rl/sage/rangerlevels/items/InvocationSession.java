package rl.sage.rangerlevels.items;

import com.pixelmonmod.pixelmon.init.registry.SoundRegistration;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.server.ServerWorld;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class InvocationSession {
    private final UUID playerUUID;
    private final BlockPos pos;
    private final ServerWorld world;
    private final AltarRecipe recipe;
    private final ItemStack altarStack;
    private int tickCounter = 0;

    private enum State { COUNTDOWN, CONSUME, FINALIZE, WAIT_DROP, DONE }
    private State state = State.COUNTDOWN;

    /** Lista “plana” de claves: cada unidad de ingrediente por separado */
    private final List<String> toConsume;
    private int consumeIndex = 0;
    private boolean failed = false;

    public InvocationSession(ServerPlayerEntity player, ItemStack altarStack,
                             AltarRecipe recipe, BlockPos pos) {
        this.playerUUID = player.getUUID();
        this.world      = (ServerWorld) player.level;
        this.pos        = pos;
        this.recipe     = recipe;
        this.altarStack = altarStack;

        // Construir lista plana de ingredientes
        List<String> flat = new ArrayList<>();
        for (Map.Entry<String,Integer> e : recipe.getIngredients().entrySet()) {
            for (int i = 0; i < e.getValue(); i++) {
                flat.add(e.getKey());
            }
        }
        this.toConsume = flat;

        // Anuncio global de inicio

        PlayerList ppl = world.getServer().getPlayerList();
        IFormattableTextComponent globalStart = InvocationMessageHelper.getRandomStartMessage(
                pos.getX(), pos.getY(), pos.getZ()
        );
        ppl.broadcastMessage(globalStart, ChatType.SYSTEM, playerUUID);

        // Registrar sesión
        InvocationManager.register(this);
    }

    public void tick() {
        tickCounter++;

        if (state != State.DONE) {
            spawnRitualParticles();
        }
        ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUUID(playerUUID);
        if (player == null) { state = State.DONE; return; }

        switch (state) {
            case COUNTDOWN: runCountdown(player); break;
            case CONSUME:   runConsume(player);   break;
            case FINALIZE:  runFinalize(player);  break;
            case WAIT_DROP:  runPostSuccessDrop(player); break;
            default: break;
        }
    }
    /** Genera lluvia de partículas PORTAL (moradas) y FLAME (fuego) en un área 5×5x3 alrededor de pos. */
    private void spawnRitualParticles() {
        // Cada 2 ticks
        if (tickCounter % 2 != 0) return;

        int count = 8;
        for (int i = 0; i < count; i++) {
            double offsetX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 8.0;
            double offsetZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 8.0;
            double offsetY = ThreadLocalRandom.current().nextDouble() * 3.0 + 0.5;
            double x = pos.getX() + 0.5 + offsetX;
            double y = pos.getY() + offsetY;
            double z = pos.getZ() + 0.5 + offsetZ;
            world.sendParticles(ParticleTypes.PORTAL, x, y, z, 1, 0, 0, 0, 0.0);
            world.sendParticles(ParticleTypes.FLAME,  x, y, z, 1, 0, 0, 0, 0.0);
        }
    }

    private void runCountdown(ServerPlayerEntity player) {
        if (tickCounter % 20 == 0) {
            int phase = 3 - (tickCounter / 20);
            if (phase >= 1) {
                // Título y sonido/partículas
                IFormattableTextComponent titleLine = InvocationMessageHelper.getRandomTitleMessage();
                player.connection.send(new STitlePacket(
                        STitlePacket.Type.TITLE,
                        titleLine,
                        0, 18, 0
                ));
                player.connection.send(new STitlePacket(
                        STitlePacket.Type.SUBTITLE,
                        new StringTextComponent("§6" + phase),
                        0, 18, 0
                ));
                world.sendParticles(
                        ParticleTypes.PORTAL,
                        pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5,
                        20, 1,1,1, 0.1
                );
            }
            if (tickCounter >= 60) {
                // 3s completados → repeler y pasar a CONSUME
                repelNearbyPlayers();
                state = State.CONSUME;
                tickCounter = 0;
            }
        }
    }

    /** Empuja al jugador ~5 bloques hacia atrás desde el altar */
    private void repelNearbyPlayers() {
        double radius = 10.0; // ahora 10 bloques
        AxisAlignedBB box = new AxisAlignedBB(
                pos.getX() - radius, pos.getY() - 1, pos.getZ() - radius,
                pos.getX() + radius, pos.getY() + 3, pos.getZ() + radius
        );
        List<ServerPlayerEntity> players = world.getEntitiesOfClass(ServerPlayerEntity.class, box);
        for (ServerPlayerEntity p : players) {
            double dx = p.getX() - (pos.getX() + 0.5);
            double dz = p.getZ() - (pos.getZ() + 0.5);
            double distH = Math.sqrt(dx*dx + dz*dz);
            if (distH < 0.01) {
                // evita división por cero
                dx = 1; dz = 0; distH = 1;
            }

            // Knockback horizontal igual a un arco nivel III (~3.0F)
            float knockbackStrength = 3.0f;
            // Aplica el knockback (LivingEntity.knockback)
            p.knockback(knockbackStrength, -dx/distH, -dz/distH);

            // Ajusta la componente vertical para elevar ligeramente
            // (por defecto knockback no eleva, sólo desplaza)
            p.setDeltaMovement(
                    p.getDeltaMovement().x,
                    0.6,   // altura de elevación, ajusta entre 0.5 – 1.0
                    p.getDeltaMovement().z
            );
            p.hurtMarked = true; // fuerza a recalcular movimiento en el servidor
        }
    }

    private void spawnFireExplosionWithRays() {
        // 1) Explosión “corta” en radio ~5 bloques (10×10 área horizontal)
        int radius = 5;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx*dx + dz*dz > radius*radius) continue;
                double x = pos.getX() + 0.5 + dx + ThreadLocalRandom.current().nextDouble();
                double y = pos.getY() + 1.0 + ThreadLocalRandom.current().nextDouble();
                double z = pos.getZ() + 0.5 + dz + ThreadLocalRandom.current().nextDouble();
                // Partícula de llama y humo
                world.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0, 0, 0, 0.0);
                world.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 1, 0, 0, 0, 0.0);
            }
        }

        // 2) Rayos de fuego: líneas de partículas que parten del centro y llegan hasta maxDist
        int numRays = 40; // cuántos rayos
        int maxDist = 35; // alcance en bloques
        for (int i = 0; i < numRays; i++) {
            double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);
            double dirX = Math.cos(angle);
            double dirZ = Math.sin(angle);
            // Para cada paso a lo largo de la línea
            for (int d = 1; d <= maxDist; d++) {
                double x = pos.getX() + 0.5 + dirX * d;
                double y = pos.getY() + 1.0;
                double z = pos.getZ() + 0.5 + dirZ * d;
                world.sendParticles(ParticleTypes.FLAME, x, y, z, 10, 0, 0, 0, 0.0);
                world.sendParticles(ParticleTypes.SMOKE, x, y, z, 7, 0, 0, 0, 0.0);
            }
        }
    }


    //PARTICULAS DE VICTORIA
    private void spawnEpicVictoryParticles() {
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 1.0;
        double centerZ = pos.getZ() + 0.5;

        ThreadLocalRandom rand = ThreadLocalRandom.current();

        // 1) Anillos concéntricos de END_ROD y CAMPFIRE_COSY_SMOKE
        int ringCount = 3;
        for (int ring = 1; ring <= ringCount; ring++) {
            double radius = ring * 2.0; // 2, 4, 6 bloques
            int points = 40;
            double y = centerY + ring * 0.3;
            for (int i = 0; i < points; i++) {
                double angle = 2 * Math.PI * i / points;
                double x = centerX + Math.cos(angle) * radius;
                double z = centerZ + Math.sin(angle) * radius;
                // Partícula brillante
                world.sendParticles(ParticleTypes.END_ROD,
                        x, y + (rand.nextDouble() - 0.5) * 0.5, z,
                        1, 0, 0, 0, 0.0);
                // Humo suave
                world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        x, y + rand.nextDouble() * 0.5, z,
                        1, 0, 0, 0, 0.0);
            }
        }

        // 2) Espiral ascendente de DRAGON_BREATH para efecto místico
        int spiralSteps = 60;
        double maxSpiralHeight = 4.0;
        for (int i = 0; i < spiralSteps; i++) {
            double t = i * (2 * Math.PI / spiralSteps) * 3; // 3 vueltas
            double fraction = (double) i / spiralSteps;
            double radius = 1.0 + fraction * 3.0; // de 1 a 4 bloques
            double x = centerX + Math.cos(t) * radius;
            double z = centerZ + Math.sin(t) * radius;
            double y = centerY + fraction * maxSpiralHeight;
            world.sendParticles(ParticleTypes.DRAGON_BREATH,
                    x, y, z,
                    1, 0, 0, 0, 0.0);
        }

        // 3) Explosión central de partículas mezcladas
        int centerBurst = 50;
        for (int i = 0; i < centerBurst; i++) {
            double offsetX = (rand.nextDouble() - 0.5) * 2.0;
            double offsetZ = (rand.nextDouble() - 0.5) * 2.0;
            double offsetY = rand.nextDouble() * 1.5;
            double x = centerX + offsetX;
            double y = centerY + offsetY;
            double z = centerZ + offsetZ;
            // Chispa de triunfo
            world.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    x, y, z,
                    1, 0, 0, 0, 0.0);
            // Toques de llama
            world.sendParticles(ParticleTypes.FLAME,
                    x, y, z,
                    1, 0, 0, 0, 0.0);
        }

        // 4) Pequeños rayos ascendentes aleatorios (columnas de portal)
        int verticalColumns = 10;
        for (int i = 0; i < verticalColumns; i++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double r = rand.nextDouble() * 3.0;
            double x0 = centerX + Math.cos(angle) * r;
            double z0 = centerZ + Math.sin(angle) * r;
            // sube desde Y=centerY hasta centerY+3 en 3 pasos
            for (int j = 0; j < 3; j++) {
                double y = centerY + j * 1.0;
                world.sendParticles(ParticleTypes.PORTAL,
                        x0, y, z0,
                        1, 0, 0, 0, 0.0);
            }
        }
    }
    private void runPostSuccessDrop(ServerPlayerEntity player) {
        // Espera 20 ticks (~1s)
        if (tickCounter < 20) return;

        // Ahora sí: genera el ítem o lo dropea si no cabe
        ItemStack result = recipe.getResultSupplier().get();
        boolean added = player.inventory.add(result);
        if (!added) {
            ItemEntity dropped = new ItemEntity(
                    world,
                    pos.getX() + 0.5,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.5,
                    result
            );
            // opcional: dropped.setDefaultPickupDelay();
            world.addFreshEntity(dropped);
        }
        // Consume el altar y termina la sesión
        altarStack.shrink(1);
        state = State.DONE;
    }

    private void runConsume(ServerPlayerEntity player) {
        // Cada 3s = 60 ticks
        if (tickCounter % 40 != 0) return;

        // 5% de fallo
        if (ThreadLocalRandom.current().nextDouble() < 0.05) {
            failed = true;
            state = State.FINALIZE;
            return;
        }

        // Consumir un solo ítem (unidad) de la lista plana
        String key = toConsume.get(consumeIndex);
        // Verificar tiene ingrediente; si no, FALLA sin devolver altar
        if (!recipe.hasIngredient(player, key)) {
            failed = true;
            state = State.FINALIZE;
            return;
        }
        recipe.consumeSingleIngredient(player, key, 1);

        // Rayo al altar
        LightningBoltEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt != null) {
            bolt.moveTo(pos.getX()+0.5, pos.getY()+2, pos.getZ()+0.5);
            world.addFreshEntity(bolt);
        }

        // Mensaje dinámico
        IFormattableTextComponent consumeMsg = InvocationMessageHelper
                .getRandomConsumeMessage(player.getName().getString(), key);
        player.sendMessage(consumeMsg, playerUUID);

        consumeIndex++;
        // Si ya consumimos todos los elementos planos, vamos a FINALIZE
        if (consumeIndex >= toConsume.size()) {
            state = State.FINALIZE;
            tickCounter = 0;
        }
    }

    private void runFinalize(ServerPlayerEntity player) {
        // Sonido y rayo
        world.playSound(null, pos, SoundEvents.GENERIC_EXPLODE,
                SoundCategory.BLOCKS, 1.0f, 0.5f);

        LightningBoltEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt != null) {
            bolt.moveTo(pos.getX()+0.5, pos.getY()+2, pos.getZ()+0.5);
            world.addFreshEntity(bolt);
        }

        PlayerList ppl = world.getServer().getPlayerList();
        if (failed) {
            // FALLO: explosión y efectos de fallo
            world.explode(null,
                    pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5,
                    10.0f, Explosion.Mode.DESTROY
            );
            spawnFireExplosionWithRays();

            IFormattableTextComponent failMsg = InvocationMessageHelper.getRandomFailureMessage();
            ppl.broadcastMessage(failMsg, ChatType.SYSTEM, playerUUID);

            PlayerSoundUtils.playSoundToAllPlayers(
                    Objects.requireNonNull(player.getServer()),
                    SoundEvents.ENDER_DRAGON_DEATH,
                    SoundCategory.PLAYERS,
                    1.0f,
                    0.5f
            );
            PlayerSoundUtils.playSoundToAllPlayers(
                    player.getServer(),
                    SoundEvents.ELDER_GUARDIAN_CURSE,
                    SoundCategory.PLAYERS,
                    1.0f,
                    0.7f
            );

            // Consumir inmediato en fallo y terminar
            altarStack.shrink(1);
            state = State.DONE;
        } else {
            // ÉXITO: anuncio + partículas + sonido
            IFormattableTextComponent successMsg = InvocationMessageHelper
                    .getRandomSuccessMessage(player.getName().getString());
            ppl.broadcastMessage(successMsg, ChatType.SYSTEM, playerUUID);

            PlayerSoundUtils.playSoundToPlayer(
                    player,
                    SoundEvents.END_PORTAL_SPAWN,
                    SoundCategory.MASTER,
                    1.0f,
                    0.5f
            );
            spawnEpicVictoryParticles();

            // Prepara el dropeo retrasado y sal de aquí
            state = State.WAIT_DROP;
            tickCounter = 0;
        }
        // <-- NO state = State.DONE aquí
    }

    public boolean isDone() {
        return state == State.DONE;
    }
    public BlockPos getPos() {
        return pos;
    }

    public boolean isInFinalizePhase() {
        return state == State.FINALIZE;
    }
}
