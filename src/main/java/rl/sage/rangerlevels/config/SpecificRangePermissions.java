package rl.sage.rangerlevels.config;

import java.util.List;

public class SpecificRangePermissions {
    private boolean enable;
    private List<String> permissions;

    public boolean isEnable() {
        return enable;
    }
    public void setEnable(boolean enable) {
        this.enable = enable;
    }
    public List<String> getPermissions() {
        return permissions;
    }
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
