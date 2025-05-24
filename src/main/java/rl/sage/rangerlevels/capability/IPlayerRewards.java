package rl.sage.rangerlevels.capability;

import java.util.Map;

public interface IPlayerRewards {
    /** Devuelve el mapa <claveRecompensa, estado> */
    Map<String, RewardStatus> getStatusMap();

    /** Establece el estado para la clave indicada */
    void setStatus(String key, RewardStatus status);
}
