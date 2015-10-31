package dnss.model;

public class Skill {
    private int id;
    private String sprite;
    private int icon;
    private Level[] levels;
    private int level;
    private int spMaxLevel;

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
        return levels.length - spMaxLevel;
    }

    public int getSpMaxLevel() {
        return spMaxLevel;
    }

    public void setSpMaxLevel(int spMaxLevel) {
        this.spMaxLevel = spMaxLevel;
    }

    public Level getLevel(int level) {
        if (level <= 0 || level > levels.length) {
            return null;
        }

        return levels[level-1];
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

    public int getUsedSP() {
        Level l = getLevel(getLevel());
        return l == null ? 0 : l.getTotalSPCost();
    }
}
