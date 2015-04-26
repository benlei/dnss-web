package dnss.model;

public class Skill {
    private int id;
    private String sprite;
    private int icon;
    private Level[] levels;
    private int level;
    private int maxLevel;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSprite() {
        if (! isDefault() && level == 0) {
            return sprite + "_b";
        }

        return sprite;
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public Level[] getLevels() {
        return levels;
    }

    public void setLevels(Level[] levels) {
        this.levels = levels;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevelForCap(int cap) {
        Level max = null;
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].getRequiredJobLevel() <= cap) {
                max = levels[i];
            }
        }

        if (max != null) {
            maxLevel = max.getLevel();
        }
    }

    public Level getLevel(int level) {
        if (level <= 0 || level > levels.length) {
            return null;
        }

        return levels[--level];
    }

    public boolean isDefault() {
        return levels[0].getRequiredJobLevel() == 1;
    }

    public int getLevel() {
        if (isDefault()) {
            return level + 1;
        }

        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getSpriteXY() {
        int x = (icon % 10) * - 50;
        int y = (icon / 10) * - 50;
        return x+"px "+y+"px";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof Skill) {
            return id == ((Skill) obj).id;
        }

        return false;
    }
}
