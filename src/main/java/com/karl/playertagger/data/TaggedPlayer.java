package com.karl.playertagger.data;

public class TaggedPlayer {
    private String name;
    private String uuid;
    private String gameMode;   // BedWars, SkyWars, Duels, UHC, etc.
    private String customInfo;  // 3.4 Reach, Fireball God, etc.
    private String colorCode;   // e.g. "§c", "§6", "§d", "§b", "§a", "§e", "§7"
    private String icon;        // e.g. "☠", "⚡", "☄", "⚔", "🛡", "🎯"
    private String category;    // "Cheater", "Sweat", "BedWars", "Custom", etc.
    private int threatLevel;    // 1 to 5
    private long addedTime;
    private boolean isShared;   // true if synced from cloud

    public TaggedPlayer() {}

    public TaggedPlayer(String name, String gameMode, String customInfo, String colorCode, String icon, String category, int threatLevel, boolean isShared) {
        this.name = name;
        this.gameMode = gameMode == null ? "" : gameMode.trim();
        this.customInfo = customInfo == null ? "" : customInfo.trim();
        this.colorCode = (colorCode == null || colorCode.isEmpty()) ? "§c" : colorCode;
        this.icon = icon == null ? "" : icon.trim();
        this.category = (category == null || category.isEmpty()) ? "Custom" : category;
        this.threatLevel = threatLevel < 1 ? 1 : Math.min(threatLevel, 5);
        this.addedTime = System.currentTimeMillis();
        this.isShared = isShared;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getGameMode() {
        return gameMode == null ? "" : gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode == null ? "" : gameMode.trim();
    }

    public String getCustomInfo() {
        return customInfo == null ? "" : customInfo;
    }

    public void setCustomInfo(String customInfo) {
        this.customInfo = customInfo == null ? "" : customInfo.trim();
    }

    public String getColorCode() {
        return colorCode == null ? "§c" : colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getIcon() {
        return icon == null ? "" : icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCategory() {
        return category == null ? "Custom" : category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(int threatLevel) {
        this.threatLevel = threatLevel;
    }

    public long getAddedTime() {
        return addedTime;
    }

    public void setAddedTime(long addedTime) {
        this.addedTime = addedTime;
    }

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }

    /**
     * Builds the overhead sub-badge line.
     * e.g. "§c[☠ BedWars - 3.4 Reach Cheater]§r"
     */
    public String getFormattedBadge() {
        StringBuilder content = new StringBuilder();
        
        if (gameMode != null && !gameMode.trim().isEmpty()) {
            content.append(gameMode.trim());
        }
        if (customInfo != null && !customInfo.trim().isEmpty()) {
            if (content.length() > 0) {
                content.append(" • ");
            }
            content.append(customInfo.trim());
        }

        if (content.length() == 0) {
            if (icon != null && !icon.trim().isEmpty()) {
                return colorCode + "[" + icon.trim() + "]§r";
            }
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(colorCode).append("[");
        if (icon != null && !icon.trim().isEmpty()) {
            sb.append(icon.trim()).append(" ");
        }
        sb.append(content).append("]§r");
        return sb.toString();
    }

    /**
     * Formats player name in their chosen color.
     */
    public String getFormattedName() {
        return getColorCode() + name + "§r";
    }

    /**
     * Prefix for Tab-List.
     */
    public String getTabPrefix() {
        if (icon != null && !icon.trim().isEmpty()) {
            return getColorCode() + "[" + icon.trim() + "] ";
        }
        return getColorCode();
    }
}
