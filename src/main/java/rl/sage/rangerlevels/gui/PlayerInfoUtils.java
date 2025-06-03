package rl.sage.rangerlevels.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.*;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.capability.PassCapabilities;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.pass.PassType;
import rl.sage.rangerlevels.util.GradientText;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfoUtils {

    /**
     * Construye un ItemStack de cabeza de jugador que muestra:
     * - La skin del jugador.
     * - Su nivel, experiencia y barra de progreso (tomados de su capability real).
     * - Su pase actual.
     */
    public static ItemStack getInfoItem(ServerPlayerEntity player, int slotIndex) {
        // 1) Creamos la cabeza de jugador
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        CompoundNBT tag = head.getOrCreateTag();
        tag.putString("MenuButtonID", "info");
        tag.putInt("MenuSlot", slotIndex);

        // 2) Rellenamos el GameProfile con las propiedades de skin
        GameProfile profile = player.getGameProfile();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        MinecraftSessionService service = server.getSessionService();
        service.fillProfileProperties(profile, true);

        // 3) Serializamos todo el GameProfile (incluye textures) en SkullOwner
        CompoundNBT ownerNBT = NBTUtil.writeGameProfile(new CompoundNBT(), profile);
        tag.put("SkullOwner", ownerNBT);

        // 4) Display name: el nombre real del jugador CON degradado amarillo→naranja pastel,
        //    sin cursiva ni negrita.
        String playerName = player.getName().getString();
        IFormattableTextComponent gradientName = GradientText.of(
                playerName + " ",
                "#FFD54F",  // amarillo pastel
                "#FFA726"   // naranja pastel
        );
        // Eliminamos cursiva/negrita en el componente:
        Style noItalicNoBold = Style.EMPTY.withItalic(false).withBold(false);
        gradientName.setStyle(noItalicNoBold);

        CompoundNBT display = new CompoundNBT();
        display.putString("Name", ITextComponent.Serializer.toJson(gradientName));

        // 5) Leemos nivel y experiencia **actuales** del jugador
        final int[] lvlExp = new int[]{0, 0};
        LevelProvider.get(player).ifPresent(cap -> {
            lvlExp[0] = cap.getLevel();
            lvlExp[1] = cap.getExp();
        });
        int lvl = lvlExp[0];
        int exp = lvlExp[1];

        // 6) Calculamos la experiencia necesaria para el siguiente nivel
        int next = ExpConfig.get().getLevels().getExpForLevel(lvl + 1);
        int perc = next > 0 ? (int) (exp * 100.0 / next) : 0;

        // 7) Construimos la barra de progreso
        int bars = 20;
        int filled = perc * bars / 100;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < bars; i++) {
            bar.append(i < filled
                    ? TextFormatting.GREEN + "░"
                    : TextFormatting.DARK_GRAY + "░");
        }

        // 8) Obtenemos el tipo de pase
        int tier = PassCapabilities.get(player).getTier();
        PassType passType = PassType.values()[tier];

        // 9) Montamos el lore con valores reales
        List<ITextComponent> lore = new ArrayList<>();

        // 9a) Nivel
        lore.add(new StringTextComponent(
                TextFormatting.GRAY + "Nivel: " +
                        TextFormatting.WHITE + lvl
        ));

        // 9b) Exp
        lore.add(new StringTextComponent(
                TextFormatting.GRAY + "Exp: " +
                        TextFormatting.AQUA + exp +
                        TextFormatting.WHITE + "/" +
                        TextFormatting.AQUA + next +
                        TextFormatting.GRAY + " [" +
                        TextFormatting.GREEN + perc + "%" +
                        TextFormatting.GRAY + "]"
        ));

        // 9c) Barra de progreso
        lore.add(new StringTextComponent(bar.toString()));

        // 9d) Pase actual: usamos gradient tal cual, pero quitamos cursiva si tuviera
        IFormattableTextComponent passDisplay = passType.getGradientDisplayName().copy();
        passDisplay.setStyle(Style.EMPTY.withItalic(false).withBold(false));
        passDisplay.append(new StringTextComponent(
                TextFormatting.GRAY + " (Pase Actual)"
        ));
        lore.add(passDisplay);

        // 10) Serializamos el lore a NBT
        ListNBT loreList = new ListNBT();
        for (ITextComponent line : lore) {
            loreList.add(StringNBT.valueOf(
                    ITextComponent.Serializer.toJson(line)
            ));
        }
        display.put("Lore", loreList);

        // 11) Aplicamos el display completo y devolvemos la cabeza
        tag.put("display", display);
        head.setTag(tag);
        return head;
    }
}
