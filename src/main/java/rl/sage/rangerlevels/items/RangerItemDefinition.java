package rl.sage.rangerlevels.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

/**
 * Clase base para cualquier “ítem” de RangerLevels que use un ítem vanilla + NBT.
 * Ahora incluye:
 *  - id (String) para identificarlo en el registry
 *  - baseItem (Item) que se reutiliza
 *  - tier (enum Tier) para saber su categoría
 *  - tierColor (TextFormatting) para colorear el nombre
 *  - displayName (String) que aparecerá como nombre del ítem
 *  - defaultLore (List<IFormattableTextComponent>) con las líneas de lore por defecto
 */
public class RangerItemDefinition {
    public static final String NBT_TIER_KEY = "RangerTier";

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
        this.id = id;
        this.baseItem = baseItem;
        this.tier = tier;
        this.tierColor = tierColor;
        this.displayName = displayName;
        this.defaultLore = defaultLore;
    }

    public String getId() {
        return id;
    }

    public static final String NBT_ID_KEY = "RangerID";


    public Item getBaseItem() {
        return baseItem;
    }

    public Tier getTier() {
        return tier;
    }

    public TextFormatting getTierColor() {
        return tierColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Crea un ItemStack con la cantidad indicada, marcándolo en NBT con “RangerTier”,
     * asignando el nombre coloreado y agregando el lore definido en defaultLore.
     */
    public ItemStack createStack(int amount) {
        ItemStack stack = new ItemStack(baseItem, amount);

        // 1) Guardar NBT igual que antes...
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString(NBT_ID_KEY, id);
        tag.putString(NBT_TIER_KEY, tier.name());

        // 2) ------------------ Aquí cambiamos ------------------
        // En lugar de usar TextFormatting y concatenar:
        // IFormattableTextComponent nombreColoreado = new StringTextComponent(tierColor + displayName);
        // ahora pintamos el displayName con degradado:
        IFormattableTextComponent nombreColoreado = tier.applyGradient(displayName);
        stack.setHoverName(nombreColoreado);
        // --------------------------------------------------------

        // 3) Lore (si existe)
        if (defaultLore != null && !defaultLore.isEmpty()) {
            CompoundNBT displayTag = tag.getCompound("display");
            ListNBT loreList = new ListNBT();
            for (IFormattableTextComponent line : defaultLore) {
                String json = ITextComponent.Serializer.toJson(line);
                loreList.add(StringNBT.valueOf(json));
            }
            displayTag.put("Lore", loreList);
            tag.put("display", displayTag);
        }

        // 4) Finalmente, setTag y return
        stack.setTag(tag);
        return stack;
    }


    /**
     * Dado un ItemStack, intenta extraer el Tier según el tag NBT “RangerTier”.
     * @param stack ItemStack a inspeccionar
     * @return Tier si coincide; null si no contiene el tag o no es válido
     */
    public static Tier getTierFromStack(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return null;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(NBT_TIER_KEY)) return null;
        try {
            return Tier.valueOf(tag.getString(NBT_TIER_KEY));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Dado un ItemStack, intenta extraer el ID según el tag NBT “RangerID”.
     * @param stack ItemStack a inspeccionar
     * @return ID (ej. "ticket_nivel") si existe; null si no está presente o inválido
     */
    public static String getIdFromStack(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return null;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(NBT_ID_KEY)) return null;
        return tag.getString(NBT_ID_KEY);
    }

}
