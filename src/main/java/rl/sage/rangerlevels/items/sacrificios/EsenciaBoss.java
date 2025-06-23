// File: rl/sage/rangerlevels/items/sacrificios/EsenciaBoss.java
package rl.sage.rangerlevels.items.sacrificios;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.ArrayList;
import java.util.List;

public class EsenciaBoss extends RangerItemDefinition {
    public static final String ID = "esencia_boss";
    private static final String NBT_SPECIES_KEY = "EsenciaSpecies";
    private static final String NBT_POKEMON_ID  = "EsenciaPokemonID";

    public EsenciaBoss() {
        super(
                ID,
                PixelmonItems.yellow_flute,
                Tier.MITICO,               // o el tier que tenga sentido
                TextFormatting.LIGHT_PURPLE,
                "✦ Esencia de Boss ✦",
                getDefaultLore(),
                true
        );
        CustomItemRegistry.register(this);
    }

    private static List<IFormattableTextComponent> getDefaultLore() {
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Usa esta esencia especial de boss"));
        lore.add(new StringTextComponent("§7para obtener, vía otro método,"));
        lore.add(new StringTextComponent("§7las esencias que ya conoces."));
        lore.add(new StringTextComponent("§7Se obtiene de bosses al sacrificarlos"));
        lore.add(new StringTextComponent("§7o con 10% probabilidad al derrotarlos."));
        return lore;
    }

    /**
     * Crea la esencia ligada a una instancia concreta, usando Personality en NBT pero sin mostrarla en el lore.
     *
     * @param speciesName  El nombre de especie para mostrar.
     * @param personality  El identificador único; nunca debe ser null.
     * @return ItemStack de la esencia
     */
    public static ItemStack createForSpeciesAndId(String speciesName, String personality) {
        RangerItemDefinition def = CustomItemRegistry.getDefinition(ID);
        ItemStack stack = def.createStack(1);
        CompoundNBT tag = stack.getOrCreateTag();

        // 1) Guardar especie en NBT
        if (speciesName != null && !speciesName.isEmpty()) {
            tag.putString(NBT_SPECIES_KEY, speciesName);
        } else {
            tag.putString(NBT_SPECIES_KEY, "unknown");
        }

        // 2) Guardar personality en NBT (fallback ya garantizado en llamador)
        if (personality != null && !personality.isEmpty()) {
            tag.putString(NBT_POKEMON_ID, personality);
        } else {
            tag.putString(NBT_POKEMON_ID, "none");
        }

        // 3) Hover name: mostrar especie legible
        IFormattableTextComponent name = new StringTextComponent("✦ Esencia de ")
                .append(new StringTextComponent(speciesName != null ? speciesName : "Boss")
                        .setStyle(Style.EMPTY.withColor(TextFormatting.LIGHT_PURPLE)))
                .append(new StringTextComponent(" ✦"))
                .setStyle(Style.EMPTY.withItalic(false));
        stack.setHoverName(name);

        // 4) Lore: solo mostrar la especie
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Esta esencia proviene del boss:"));
        lore.add(new StringTextComponent("§e" + (speciesName != null ? speciesName : "Desconocido")));
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        // 5) Ocultar flags NBT para el usuario
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
