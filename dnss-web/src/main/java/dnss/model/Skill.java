package dnss.model;

public class Skill {
    private int id;
    private String sprite;
    private String icon;
    private Level[] levels;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSprite() {
        return sprite;
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Level[] getLevels() {
        return levels;
    }

    public void setLevels(Level[] levels) {
        this.levels = levels;
    }

    public Level getMaxLevel(int cap) {
        Level max = null;
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].getRequiredJobLevel() <= cap) {
                max = levels[i];
            }
        }

        return max;
    }

    public Level getLevel(int level) {
        if (isDefault() && level == 0) {
            level = 1;
        } else if (isDefault()) {
          level++;
        }

        if (level <= 0 || level > levels.length) {
            return null;
        }

        return levels[--level];
    }

    public boolean isDefault() {
        return levels[0].getRequiredJobLevel() == 1;
    }

}
