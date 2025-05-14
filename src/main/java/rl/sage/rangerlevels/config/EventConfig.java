package rl.sage.rangerlevels.config;

import java.util.List;

public class EventConfig {
    private boolean enable;
    private int[] expRange;
    private boolean requiresPermission;
    private List<String> permissions;
    private SpecificRangePermissions specificRangePermissions;

    // Campos nuevos para BeatWild
    private int[] expRangeWild;
    private int[] expRangeShiny;
    private int[] expRangeLegendary;
    private int[] expRangeBoss;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int[] getExpRange() {
        return expRange;
    }

    public void setExpRange(int[] expRange) {
        this.expRange = expRange;
    }

    public boolean isRequiresPermission() {
        return requiresPermission;
    }

    public void setRequiresPermission(boolean requiresPermission) {
        this.requiresPermission = requiresPermission;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public SpecificRangePermissions getSpecificRangePermissions() {
        return specificRangePermissions;
    }

    public void setSpecificRangePermissions(SpecificRangePermissions specificRangePermissions) {
        this.specificRangePermissions = specificRangePermissions;
    }

    /**
     * Rango por defecto para Pokémon salvaje "normal".
     * Si no está configurado, usa expRange genérico.
     */
    public int[] getExpRangeWild() {
        return expRangeWild != null ? expRangeWild : expRange;
    }

    public void setExpRangeWild(int[] expRangeWild) {
        this.expRangeWild = expRangeWild;
    }

    /**
     * Rango para Pokémon shiny.
     * Si no está configurado, usa expRangeWild o expRange genérico.
     */
    public int[] getExpRangeShiny() {
        if (expRangeShiny != null) {
            return expRangeShiny;
        }
        // fallback a wild o genérico
        return expRangeWild != null ? expRangeWild : expRange;
    }

    public void setExpRangeShiny(int[] expRangeShiny) {
        this.expRangeShiny = expRangeShiny;
    }

    /**
     * Rango para Pokémon legendario o Ultra Beast.
     * Si no está configurado, usa expRangeWild o expRange genérico.
     */
    public int[] getExpRangeLegendary() {
        if (expRangeLegendary != null) {
            return expRangeLegendary;
        }
        // fallback a wild o genérico
        return expRangeWild != null ? expRangeWild : expRange;
    }

    public void setExpRangeLegendary(int[] expRangeLegendary) {
        this.expRangeLegendary = expRangeLegendary;
    }

    /**
     * Rango para Boss Pokémon.
     * Si no está configurado, usa expRangeWild o expRange genérico.
     */
    public int[] getExpRangeBoss() {
        if (expRangeBoss != null) {
            return expRangeBoss;
        }
        // fallback a wild o genérico
        return expRangeWild != null ? expRangeWild : expRange;
    }

    public void setExpRangeBoss(int[] expRangeBoss) {
        this.expRangeBoss = expRangeBoss;
    }
}
