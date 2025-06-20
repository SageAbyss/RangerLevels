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

/**
 * Esencia de Ultraente: guarda species y Personality en NBT,
 * pero en el lore solo muestra la especie (no muestra el Personality).
 */
public class EsenciaUltraente extends RangerItemDefinition {
    public static final String ID = "esencia_ultraente";
    private static final String NBT_SPECIES_KEY = "EsenciaSpecies";
    private static final String NBT_POKEMON_ID  = "EsenciaPokemonID";

    public EsenciaUltraente() {
        super(
                ID,
                PixelmonItems.blue_flute,
                Tier.EPICO,
                TextFormatting.AQUA,
                "✦ Esencia Ultraente ✦",
                // Lore base: indicación genérica; la parte específica (especie) la añadimos en createForSpeciesAndId
                java.util.Arrays.asList(
                        new StringTextComponent("§7Usa esta esencia para"),
                        new StringTextComponent("§7conseguir modificadores"),
                        new StringTextComponent("§7de ADN para este Pokémon.")
                )
        );
        CustomItemRegistry.register(this);
    }

    /**
     * Crea la esencia ligada a una instancia concreta, usando Personality en NBT pero sin mostrarla en el lore.
     *
     * @param speciesName  El nombre de especie para mostrar (por ejemplo localizedName o un nombre legible).
     * @param personality  El identificador único (Personality/UUID); nunca debe ser null pues el llamador debe asegurar fallback.
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

        // 2) Guardar personality en NBT (se asume no null; pero protegemos)
        if (personality != null && !personality.isEmpty()) {
            tag.putString(NBT_POKEMON_ID, personality);
        } else {
            // Si de algún modo llegara null o vacío, usar un placeholder o evitar putString(null)
            tag.putString(NBT_POKEMON_ID, "none");
        }

        // 3) Hover name: mostrar especie legible
        IFormattableTextComponent name = new StringTextComponent("✦ Esencia de ")
                .append(new StringTextComponent(speciesName != null ? speciesName : "Pokémon")
                        .setStyle(Style.EMPTY.withColor(TextFormatting.GOLD)))
                .append(new StringTextComponent(" ✦"))
                .setStyle(Style.EMPTY.withItalic(false));
        stack.setHoverName(name);

        // 4) Lore: solo mostrar la especie, NO mostrar el Personality
        java.util.List<IFormattableTextComponent> lore = new java.util.ArrayList<>();
        lore.add(new StringTextComponent("§7Esta esencia pertenece solo a:"));
        lore.add(new StringTextComponent("§e" + (speciesName != null ? speciesName : "Desconocido")));
        // Si quisieras incluir más líneas generales, agrégalas aquí, pero NO incluir la línea del ID/personality.

        // Escribimos el lore en la etiqueta "display"
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        // 5) Ocultar flags innecesarios para que no muestre NBT al usuario
        NBTUtils.applyAllHideFlags(tag);

        stack.setTag(tag);
        return stack;
    }
}
