package rl.sage.rangerlevels.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

public interface ILevel {
    Capability<ILevel> CAPABILITY = null;

    int getLevel();
    void setLevel(int level);

    int getExp();
    void setExp(int experience);

    /** Añade XP y retorna true si ha subido de nivel */
    boolean addExp(int amount);

    /** Obtiene el multiplicador personal del jugador */
    float getPlayerMultiplier();
    void setPlayerMultiplier(float multiplier);
}
