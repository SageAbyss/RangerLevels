package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.capability.ILevel;
import rl.sage.rangerlevels.pass.PassManager;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfoUtils {

    public static ItemStack getInfoItem(ServerPlayerEntity player, int slotIndex) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        CompoundNBT tag = head.getOrCreateTag();
        tag.putString("MenuButtonID", "info");
        tag.putInt("MenuSlot", slotIndex);

        // Solo el nombre del jugador
        ITextComponent nameComp = new StringTextComponent(player.getName().getString());
        CompoundNBT display = new CompoundNBT();
        display.putString("Name", ITextComponent.Serializer.toJson(nameComp));

        // Obtener datos de ILevel
        ILevel cap = player.getCapability(ILevel.CAPABILITY).orElse(null);
        int lvl  = cap != null ? cap.getLevel() : 0;
        int exp  = cap != null ? cap.getExp()   : 0;
        int next = 50 * (lvl + 1) * (lvl + 1);
        int perc = next > 0 ? (int)(exp * 100.0 / next) : 0;

        // Barra de progreso de 20 caracteres
        int bars   = 20;
        int filled = perc * bars / 100;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < bars; i++) {
            if (i < filled) {
                bar.append(TextFormatting.GREEN).append("░");
            } else {
                bar.append(TextFormatting.DARK_GRAY).append("░");
            }
        }

        // Lore
        List<ITextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent(TextFormatting.GRAY + "Nivel: "
                + TextFormatting.WHITE + lvl));
        lore.add(new StringTextComponent(TextFormatting.GRAY + "Exp: "
                + TextFormatting.AQUA + exp
                + TextFormatting.WHITE + "/"
                + TextFormatting.AQUA + next
                + TextFormatting.GRAY + " ["
                + TextFormatting.GREEN + perc + "%"
                + TextFormatting.GRAY + "]"));
        lore.add(new StringTextComponent(bar.toString()));
        lore.add(PassManager.getPass(player).getGradientDisplayName()
                .append(new StringTextComponent(TextFormatting.GRAY + " (Pase Actual)")));

        // Serializar lore
        ListNBT loreList = new ListNBT();
        for (ITextComponent line : lore) {
            loreList.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(line)));
        }
        display.put("Lore", loreList);

        tag.put("display", display);
        head.setTag(tag);
        return head;
    }
}
