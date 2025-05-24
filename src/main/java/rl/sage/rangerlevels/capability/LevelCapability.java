package rl.sage.rangerlevels.capability;

import rl.sage.rangerlevels.config.ExpConfig;

import java.util.ArrayList;
import java.util.List;

public class LevelCapability implements ILevel {
    private int level = 1;
    private int exp = 0;
    private float playerMultiplier = 1.0f; // nuevo campo

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    @Override
    public int getExp() {
        return exp;
    }

    @Override
    public void setExp(int exp) {
        this.exp = Math.max(0, exp);
        checkLevelUp();
    }

    @Override
    public List<Integer> addExp(int amount) {
        exp += amount;
        List<Integer> nivelesSubidos = new ArrayList<>();
        while (true) {
            int siguiente = level + 1;
            int needed = getExpNeededFor(siguiente);
            if (exp >= needed && level < ExpConfig.get().getMaxLevel()) {
                exp -= needed;
                level++;
                nivelesSubidos.add(level);
            } else {
                break;
            }
        }
        return nivelesSubidos;
    }

    @Override
    public List<Integer> addLevel(int amount) {
        List<Integer> niveles = new ArrayList<>();
        int maxLvl = ExpConfig.get().getMaxLevel();

        int relExp = exp; // Guardamos la experiencia relativa actual

        for (int i = 0; i < amount; i++) {
            int current = getLevel();
            if (current >= maxLvl) break;

            level++; // Subimos nivel directamente
            niveles.add(level); // Registramos el nuevo nivel
        }

        exp = relExp; // Restauramos la experiencia relativa que tenía el jugador

        return niveles;
    }





    private boolean checkLevelUp() {
        boolean leveledUp = false;
        while (true) {
            int needed = getExpNeededFor(level + 1);
            if (exp >= needed) {
                exp -= needed;
                level++;
                leveledUp = true;
            } else {
                break;
            }
        }
        return leveledUp;
    }

    private int getExpNeededFor(int lvl) {
        // Curva progresiva: 18% mas cada nivel
        return ExpConfig.get().getLevels().getExpForLevel(lvl);
    }

    // Métodos nuevos para el multiplicador

    @Override
    public float getPlayerMultiplier() {
        return playerMultiplier;
    }

    @Override
    public void setPlayerMultiplier(float multiplier) {
        this.playerMultiplier = Math.max(0f, multiplier);
    }
}
