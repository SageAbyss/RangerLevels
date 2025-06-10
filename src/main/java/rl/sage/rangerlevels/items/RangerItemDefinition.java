package rl.sage.rangerlevels.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.*;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.List;
import java.util.UUID;

/**
 * Clase base para cualquier “ítem” de RangerLevels que use un ítem vanilla + NBT.
 * Ahora incluye:
 *  - id (String) para identificarlo en el registry
 *  - baseItem (Item) que se reutiliza
 *  - tier (enum Tier) para saber su categoría
 *  - tierColor (TextFormatting) para colorear el nombre
 *  - displayName (String) que aparecerá como nombre del ítem
 *  - defaultLore (List<IFormattableTextComponent>) con las líneas de lore por defecto
 *  - HideFlags completos para que NO se muestren atributos extra
 *  - Tag único por instancia para evitar stacking
 */
public class RangerItemDefinition {
    public static final String NBT_ID_KEY     = "RangerID";
    public static final String NBT_TIER_KEY   = "RangerTier";
    private static final String NBT_UNIQUE    = "RangerUniqueTag";

    private final String id;
    private final Item baseItem;
    private final Tier tier;
    private final TextFormatting tierColor;
    private final String displayName;
    private final List<IFormattableTextComponent> defaultLore;

    /**
     * @param id            Identificador único (ej. "ticket_super")
     * @param baseItem      Ítem vanilla que se reutiliza (ej. Items.MAP)
     * @param tier          Tier al que pertenece (COMUN, RARO, EPICO, etc.)
     * @param tierColor     Color de texto para el nombre (TextFormatting)
     * @param displayName   Nombre legible que verá el jugador (ej. "Ticket Épico")
     * @param defaultLore   Lista de líneas de lore (componentes de texto) que se mostrarán
     */
    public RangerItemDefinition(
            String id,
            Item baseItem,
            Tier tier,
            TextFormatting tierColor,
            String displayName,
            List<IFormattableTextComponent> defaultLore
    ) {
        this.id           = id;
        this.baseItem     = baseItem;
        this.tier         = tier;
        this.tierColor    = tierColor;
        this.displayName  = displayName;
        this.defaultLore  = defaultLore;
    }

    public String getId() { return id; }
    public Item getBaseItem() { return baseItem; }
    public Tier getTier() { return tier; }
    public TextFormatting getTierColor() { return tierColor; }
    public String getDisplayName() { return displayName; }

    /**
     * Crea un ItemStack que:
     *  - Siempre tiene count = 1.
     *  - Incluye ID, tier, lore, hideflags.
     *  - Aplica tierColor al nombre.
     *  - Añade un UUID único para romper stacking.
     */
    public ItemStack createStack(int amount) {
        // 1) Base stack de 1
        ItemStack stack = new ItemStack(baseItem);
        stack.setCount(1);

        // 2) Tag NBT inicial
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString(NBT_ID_KEY, id);
        tag.putString(NBT_TIER_KEY, tier.name());

        // 3) Nombre coloreado con tierColor y degradado de Tier
        IFormattableTextComponent nombre = tier
                .applyGradient(displayName)
                .withStyle(style -> style
                        .withColor(tierColor)
                        .withItalic(false)
                );
        stack.setHoverName(nombre);

        // 4) Lore por defecto (si lo hay)
        if (defaultLore != null && !defaultLore.isEmpty()) {
            CompoundNBT display = tag.contains("display")
                    ? tag.getCompound("display")
                    : new CompoundNBT();
            ListNBT loreList = new ListNBT();
            for (IFormattableTextComponent line : defaultLore) {
                String json = ITextComponent.Serializer.toJson(line);
                loreList.add(StringNBT.valueOf(json));
            }
            display.put("Lore", loreList);
            tag.put("display", display);
        }

        // 5) Ocultar atributos/encantos/etc.
        NBTUtils.applyAllHideFlags(tag);

        // 6) Tag único para romper stacking
        tag.putUUID(NBT_UNIQUE, UUID.randomUUID());

        // 7) Guardar NBT y devolver
        stack.setTag(tag);
        return stack;
    }

    /** Extrae el Tier según el tag NBT “RangerTier”. */
    public static Tier getTierFromStack(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return null;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(NBT_TIER_KEY)) return null;
        try { return Tier.valueOf(tag.getString(NBT_TIER_KEY)); }
        catch (IllegalArgumentException e) { return null; }
    }

    /** Extrae el ID según el tag NBT “RangerID”. */
    public static String getIdFromStack(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return null;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(NBT_ID_KEY)) return null;
        return tag.getString(NBT_ID_KEY);
    }
}
