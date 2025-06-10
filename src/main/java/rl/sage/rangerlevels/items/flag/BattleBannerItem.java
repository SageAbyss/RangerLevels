package rl.sage.rangerlevels.items.flag;

import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.config.ItemsConfig;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Bandera de Batalla (Battle Banner) con usos limitados por tier.
 *
 * Clave: createStack siempre resetea el NBT de usos a initialUses, sin heredar valores previos.
 */
public class BattleBannerItem extends RangerItemDefinition {
    public static final String ID = "battle_banner";
    private static final String NBT_USES_KEY = "BattleBannerUses";

    private final int initialUses;

    public BattleBannerItem(Tier tier, int initialUses) {
        super(
                ID + "_" + tier.name().toLowerCase(),
                Items.BLACK_BANNER,
                tier,
                null,
                " ✦ Bandera de Batalla ✦",
                null
        );
        this.initialUses = initialUses;
        CustomItemRegistry.register(this);
    }

    /**
     * Cuando se crea un ItemStack “nuevo” de esta bandera, forzamos NBT limpio:
     * - super.createStack(amount) inicializa ID/Tier/lore base/uniqueUUID/etc.
     * - Luego sobreescribimos el tag de usos a initialUses.
     */
    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        // Forzar NBT limpio de usos: siempre initialUses
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putInt(NBT_USES_KEY, initialUses);
        stack.setTag(tag);
        // Reconstruir lore acorde a initialUses
        return updateLore(stack);
    }

    /**
     * Reconstruye el lore según el NBT actual de usos en el stack.
     * Usa this.initialUses sólo en createStack; aquí se lee el tag.
     */
    public ItemStack updateLore(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        int uses = tag.contains(NBT_USES_KEY) ? tag.getInt(NBT_USES_KEY) : 0;

        // Obtener duración desde config según este tier
        ItemsConfig.BannerConfig cfg = ItemsConfig.get().battleBanner;
        int durationMin;
        switch (getTier()) {
            case EPICO:   durationMin = cfg.durationEpic;   break;
            case ESTELAR: durationMin = cfg.durationStellar; break;
            case MITICO:  durationMin = cfg.durationMythic;  break;
            default:      durationMin = cfg.durationDefault;  break;
        }
        double radius;
        switch (getTier()) {
            case EPICO:   radius = cfg.radiusEpic;    break;
            case ESTELAR: radius = cfg.radiusStellar; break;
            case MITICO:  radius = cfg.radiusMythic;  break;
            default:      radius = cfg.radiusDefault;  break;
        }

        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7✦ Crea un Área de Batalla controlada al colocarla"));
        lore.add(new StringTextComponent("§7✦ +50% exp en dicha área en todo tipo de batallas"));
        lore.add(new StringTextComponent("§7✦ Duración: §e" + durationMin + " minutos"));
        lore.add(new StringTextComponent("§7✦ Radio de activación: §e" + radius + " bloques"));
        lore.add(new StringTextComponent("§7✦ Usos restantes: §e" + uses));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Tier: ").append(getTier().getColor()));

        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : lore) {
            loreList.add(StringNBT.valueOf(IFormattableTextComponent.Serializer.toJson(line)));
        }
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        display.put("Lore", loreList);
        tag.put("display", display);

        // Aplicar hideflags
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }

    /** Obtiene usos restantes desde el ItemStack. */
    public static int getUsosRestantes(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        return tag.contains(NBT_USES_KEY) ? tag.getInt(NBT_USES_KEY) : 0;
    }

    /** Decrementa un uso en el ItemStack (solo si > 0). */
    public static void decrementarUso(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        int u = tag.contains(NBT_USES_KEY) ? tag.getInt(NBT_USES_KEY) : 0;
        if (u > 0) {
            tag.putInt(NBT_USES_KEY, u - 1);
            stack.setTag(tag);
        }
    }

    /**
     * Crea un ItemStack con un número específico de usos y lore actualizado.
     * Usado para devolver la bandera tras romper o expirar cuando quedan usosLeft>0.
     */
    public static ItemStack createWithUses(Tier tier, int uses, int amount) {
        // Se crea nuevo stack “limpio”
        ItemStack stack = CustomItemRegistry.create(ID + "_" + tier.name().toLowerCase(), amount);
        // Forzamos NBT de usos = uses
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putInt(NBT_USES_KEY, uses);
        stack.setTag(tag);
        // Reconstruir lore según uses
        // Obtener la definición registrada para este ID:
        RangerItemDefinition def = CustomItemRegistry.getDefinition(ID + "_" + tier.name().toLowerCase());
        if (def instanceof BattleBannerItem) {
            return ((BattleBannerItem) def).updateLore(stack);
        } else {
            // No debería ocurrir
            return stack;
        }
    }

    /**
     * Crea un ItemStack “nuevo” sin heredar NBT previo (inicializa usos a initialUses).
     * Equivale a createStack(amount).
     */
    public static ItemStack createForTier(Tier tier, int amount) {
        return CustomItemRegistry.create(ID + "_" + tier.name().toLowerCase(), amount);
    }
}
