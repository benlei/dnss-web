package dnss.model;

public class Level {
    private int level;
    private int requiredJobLevel;
    private int spCost;
    private int totalSPCost;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRequiredJobLevel() {
        return requiredJobLevel;
    }

    public void setRequiredJobLevel(int requiredJobLevel) {
        this.requiredJobLevel = requiredJobLevel;
    }

    public int getSpCost() {
        return spCost;
    }

    public void setSpCost(int spCost) {
        this.spCost = spCost;
    }

    public int getTotalSPCost() {
        return totalSPCost;
    }

    public void setTotalSPCost(int totalSPCost) {
        this.totalSPCost = totalSPCost;
    }
}
