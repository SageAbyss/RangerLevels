package rl.sage.rangerlevels.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import java.util.List;

public class NBTUtils {

    /**
     * Aplica los bits de HideFlags especificados sobre el tag NBT dado.
     * - Si ya existían HideFlags, los mezcla con OR.
     */
    public static void applyHideFlags(CompoundNBT tag, int flags) {
        int hide = tag.contains("HideFlags") ? tag.getInt("HideFlags") : 0;
        hide |= flags;
        tag.putInt("HideFlags", hide);
    }

    /**
     * Aplica **todos** los flags posibles de ocultamiento y elimina información extra como el tag de Pixelmon.
     */
    public static void applyAllHideFlags(CompoundNBT tag) {
        if (tag.contains("pixelmon")) {
            tag.remove("pixelmon");
        }
        int allFlags = 1 | 2 | 4 | 8 | 16 | 32 | 64 | 128;
        applyHideFlags(tag, allFlags);
    }

    /**
     * Escribe la lista de lore en el CompoundNBT 'display'.
     * Reemplaza cualquier lore previo.
     */
    public static void writeLore(CompoundNBT display, List<IFormattableTextComponent> lore) {
        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : lore) {
            // Serializamos cada componente a JSON
            String json = ITextComponent.Serializer.toJson(line);
            loreList.add(StringNBT.valueOf(json));
        }
        display.put("Lore", loreList);
    }
}
