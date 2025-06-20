// File: rl/sage/rangerlevels/items/sacrificios/EsenciaLegendaria.java
package rl.sage.rangerlevels.items.sacrificios;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
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
import java.util.Arrays;
import java.util.List;

public class EsenciaLegendaria extends RangerItemDefinition {
    public static final String ID = "esencia_legendaria";
    private static final String NBT_SPECIES_KEY = "EsenciaSpecies";
    private static final String NBT_POKEMON_ID  = "EsenciaPokemonID";

    public EsenciaLegendaria() {
        super(
                ID,
                PixelmonItems.red_flute,
                Tier.EPICO,
                TextFormatting.GOLD,
                "✦ Esencia Legendaria ✦",
                Arrays.asList(
                        new StringTextComponent("§7Usa esta esencia para"),
                        new StringTextComponent("§7conseguir modificadores"),
                        new StringTextComponent("§7de ADN para este Pokémon.")
                )
        );
        CustomItemRegistry.register(this);
    }

    /** Crea esencia ligada a una instancia concreta, usando Personality */
    public static ItemStack createForSpeciesAndId(String speciesName, String personality) {
        RangerItemDefinition def = CustomItemRegistry.getDefinition(ID);
        ItemStack stack = def.createStack(1);
        CompoundNBT tag = stack.getOrCreateTag();

        // Guardar especie
        if (speciesName != null && !speciesName.isEmpty()) {
            tag.putString(NBT_SPECIES_KEY, speciesName);
        } else {
            tag.putString(NBT_SPECIES_KEY, "unknown");
        }
        // Guardar personality (ya no null porque en llamador garantizamos fallback)
        tag.putString(NBT_POKEMON_ID, personality);

        // Hover name con especie
        IFormattableTextComponent name = new StringTextComponent("✦ Esencia de ")
                .append(new StringTextComponent(speciesName)
                        .setStyle(Style.EMPTY.withColor(TextFormatting.GOLD)))
                .append(new StringTextComponent(" ✦"))
                .setStyle(Style.EMPTY.withItalic(false));
        stack.setHoverName(name);

        // Lore: sólo incluimos especie, no mostramos personality
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Esta esencia pertenece solo a:"));
        lore.add(new StringTextComponent("§e" + speciesName));
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }

}
