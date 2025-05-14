package rl.sage.rangerlevels.capability;

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
    public boolean addExp(int amount) {
        exp += amount;
        return checkLevelUp();
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
        // Curva progresiva: por ejemplo, 50 * lvl²
        return 50 * lvl * lvl;
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
