package dnss.model;

import dnss.enums.Advancement;

public class Job {
    private String name;
    private String identifier;
    private Advancement advancement;
    private float[] spRatio;
    private Job parent;
    private Skill[][] skillTree;
    private int maxSP;
    private int maxSkillRequiredLevel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Advancement getAdvancement() {
        return advancement;
    }

    public void setAdvancement(int advancement) {
        this.advancement = Advancement.getAdvancement(advancement);
    }

    public float[] getSpRatio() {
        return spRatio;
    }

    public void setSpRatio(float[] spRatio) {
        this.spRatio = spRatio;
    }

    public Job getParent() {
        return parent;
    }

    public void setParent(Job parent) {
        this.parent = parent;
    }

    public Skill[][] getSkillTree() {
        return skillTree;
    }

    public void setSkillTree(Skill[][] skillTree) {
        this.skillTree = skillTree;
    }

    public int getMaxSP() {
        return maxSP;
    }

    public void setMaxSP(int maxSP) {
        this.maxSP = maxSP;
    }

    public int getMaxSkillRequiredLevel() {
        return maxSkillRequiredLevel;
    }

    public void setMaxSkillRequiredLevel(int maxSkillRequiredLevel) {
        this.maxSkillRequiredLevel = Math.min(advancement.maxRequiredSkillLevel, maxSkillRequiredLevel);
    }
}
