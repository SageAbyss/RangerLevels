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
 *  - Flag stackable: si es false, añade un UUID único para romper stacking; si es true, permite stackear y respeta amount.
 */
public class RangerItemDefinition {
    public static final String NBT_ID_KEY   = "RangerID";
    public static final String NBT_TIER_KEY = "RangerTier";
    private static final String NBT_UNIQUE  = "RangerUniqueTag";

    private final String id;
    private final Item baseItem;
    private final Tier tier;
    private final TextFormatting tierColor;
    private final String displayName;
    private final List<IFormattableTextComponent> defaultLore;
    private final boolean stackable;

    /**
     * Constructor principal.
     *
     * @param id            Identificador único (ej. "ticket_super")
     * @param baseItem      Ítem vanilla que se reutiliza (ej. Items.MAP)
     * @param tier          Tier al que pertenece (COMUN, RARO, EPICO, etc.)
     * @param tierColor     Color de texto para el nombre (TextFormatting)
     * @param displayName   Nombre legible que verá el jugador (ej. "Ticket Épico")
     * @param defaultLore   Lista de líneas de lore (componentes de texto) que se mostrarán
     * @param stackable     true si este ítem debe poder stackear; false para romper stacking con UUID
     */
    public RangerItemDefinition(
            String id,
            Item baseItem,
            Tier tier,
            TextFormatting tierColor,
            String displayName,
            List<IFormattableTextComponent> defaultLore,
            boolean stackable
    ) {
        this.id = id;
        this.baseItem = baseItem;
        this.tier = tier;
        this.tierColor = tierColor;
        this.displayName = displayName;
        this.defaultLore = defaultLore;
        this.stackable = stackable;
    }

    // Si hay lugares que llaman al constructor antiguo, podrías añadir un overload asumiendo stackable=false:
    public RangerItemDefinition(
            String id,
            Item baseItem,
            Tier tier,
            TextFormatting tierColor,
            String displayName,
            List<IFormattableTextComponent> defaultLore
    ) {
        this(id, baseItem, tier, tierColor, displayName, defaultLore, false);
    }

    public String getId() { return id; }
    public Item getBaseItem() { return baseItem; }
    public Tier getTier() { return tier; }
    public TextFormatting getTierColor() { return tierColor; }
    public String getDisplayName() { return displayName; }
    public boolean isStackable() { return stackable; }

    /**
     * Crea un ItemStack que:
     *  - Si stackable==true: count = amount.
     *  - Si stackable==false: count = 1 y añade UUID único para romper stacking.
     *  - Incluye ID, tier, lore, hideflags.
     *  - Aplica tierColor al nombre.
     */
    public ItemStack createStack(int amount) {
        ItemStack stack = new ItemStack(baseItem);
        if (stackable) {
            // permitimos hasta el máximo natural del item; asumimos amount <= maxStackSize externo
            stack.setCount(Math.max(1, amount));
        } else {
            stack.setCount(1);
        }

        // Tag NBT inicial
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString(NBT_ID_KEY, id);
        tag.putString(NBT_TIER_KEY, tier.name());

        // Nombre coloreado con tierColor y degradado de Tier
        IFormattableTextComponent nombre = tier
                .applyGradient(displayName)
                .withStyle(style -> style
                        .withColor(tierColor)
                        .withItalic(false)
                );
        stack.setHoverName(nombre);

        // Lore por defecto (si lo hay)
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

        // Ocultar atributos/encantos/etc.
        NBTUtils.applyAllHideFlags(tag);

        // Si NO es stackable, añadimos UUID para romper stacking
        if (!stackable) {
            tag.putUUID(NBT_UNIQUE, UUID.randomUUID());
        } else {
            // Si es stackable, aseguramos no llevar tag único
            tag.remove(NBT_UNIQUE);
        }

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
