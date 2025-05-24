package rl.sage.rangerlevels.capability;

import java.util.HashMap;
import java.util.Map;

public class PlayerRewardsData implements IPlayerRewards {
    // Mapa interno donde la key será algo como "EveryLevel:Free" o "Exact:5:Master"
    private final Map<String, RewardStatus> statusMap = new HashMap<>();

    @Override
    public Map<String, RewardStatus> getStatusMap() {
        return statusMap;
    }

    @Override
    public void setStatus(String key, RewardStatus status) {
        statusMap.put(key, status);
    }
}
