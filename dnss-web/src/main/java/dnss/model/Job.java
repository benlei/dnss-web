package dnss.model;

import dnss.enums.Advancement;

public class Job {
    private String name;
    private String identifier;
    private Advancement advancement;
    private float spRatio1;
    private float spRatio2;
    private float spRatio3;
    private Job parent;
    private int[][] skillTree;

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

    public float getSpRatio1() {
        return spRatio1;
    }

    public void setSpRatio1(float spRatio1) {
        this.spRatio1 = spRatio1;
    }

    public float getSpRatio2() {
        return spRatio2;
    }

    public void setSpRatio2(float spRatio2) {
        this.spRatio2 = spRatio2;
    }

    public float getSpRatio3() {
        return spRatio3;
    }

    public void setSpRatio3(float spRatio3) {
        this.spRatio3 = spRatio3;
    }

    public Job getParent() {
        return parent;
    }

    public void setParent(Job parent) {
        this.parent = parent;
    }

    public int[][] getSkillTree() {
        return skillTree;
    }

    public void setSkillTree(int[][] skillTree) {
        this.skillTree = skillTree;
    }
}
