package rl.sage.rangerlevels.config;

/**
 * Representa la sección "levels" del Config.yml,
 * permitiendo establecer el nivel y experiencia inicial,
 * así como el nivel máximo alcanzable.
 */
public class LevelsConfig {
    /** Nivel y experiencia de inicio */
    public Starting starting = new Starting();
    /** Nivel máximo alcanzable */
    public Max max = new Max();

    /** Valores por defecto */
    public static LevelsConfig createDefault() {
        LevelsConfig cfg = new LevelsConfig();
        cfg.starting.level = 1;
        cfg.starting.experience = 0;
        cfg.max.level = 100;
        return cfg;
    }

    /** Clase interna para el bloque "starting" */
    public static class Starting {
        /** Nivel inicial */
        public int level = 1;
        /** Experiencia con la que inicia */
        public int experience = 0;

        public int getLevel() {
            return level;
        }

        public int getExperience() {
            return experience;
        }
    }

    /** Clase interna para el bloque "max" */
    public static class Max {
        /** Nivel máximo */
        public int level = 100;

        public int getLevel() {
            return level;
        }
    }

    // ===== Getters de conveniencia =====
    /** Devuelve el nivel inicial */
    public int getStartingLevel() {
        return starting.getLevel();
    }

    /** Devuelve la experiencia inicial */
    public int getStartingExperience() {
        return starting.getExperience();
    }

    /** Devuelve el nivel máximo */
    public int getMaxLevel() {
        return max.getLevel();
    }
}
