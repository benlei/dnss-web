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
    private int usedSP = -1;

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
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                if (skillTree[i][j] != null) {
                    skillTree[i][j].setMaxLevelForCap(this.maxSkillRequiredLevel);
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof Job) {
            return identifier.equals(((Job) obj).identifier);
        }

        return false;
    }

    public Skill getSkill(int row, int col) {
        return skillTree[row][col];
    }

    public Skill getSkill(int position) {
        return getSkill(position / 4, position % 4);
    }

    public int getUsedSP() {
        if (usedSP != -1) {
            return usedSP;
        }

        int total = 0;
        for (int i = 0; i < skillTree.length; i++) {
            for (int j = 0; j < skillTree[i].length; j++) {
                if (skillTree[i][j] != null) {
                    total += skillTree[i][j].getUsedSP();
                }
            }
        }

        usedSP = total;
        return total;
    }

    public boolean isCompactable() {
        return skillTree.length == 6 &&
                skillTree[5][0] == null && skillTree[5][1] == null && skillTree[5][2] == null && skillTree[5][3] == null;
    }

    private boolean canCompactThirdCol() {
        for (int i = 0; i < skillTree.length; i++) {
            if (skillTree[i][2] != null) {
                return false;
            }
        }

        return true;
    }

    private boolean canCompactFourthCol() {
        for (int i = 0; i < skillTree.length; i++) {
            if (skillTree[i][3] != null) {
                return false;
            }
        }

        return true;
    }

    public void compactSkillTree() {
        if (isCompactable()) {
            Skill[][] newSkillTree = new Skill[5][4]; // 5 rows, 4 cols
            newSkillTree[0] = skillTree[0];
            newSkillTree[1] = skillTree[1];
            newSkillTree[2] = skillTree[2];
            newSkillTree[3] = skillTree[3];
            newSkillTree[4] = skillTree[4];
            skillTree = newSkillTree;
        }


        if (advancement == Advancement.TERTIARY) {
            boolean canCompactThirdCol = canCompactThirdCol();
            boolean canCompactFourthCol = canCompactFourthCol();
            if (canCompactThirdCol || canCompactFourthCol) {
                for (int i = 0; i < skillTree.length; i++) {
                    if (canCompactThirdCol) {
                        skillTree[i] = new Skill[]{skillTree[i][0], skillTree[i][1]};
                    } else {
                        skillTree[i] = new Skill[]{skillTree[i][0], skillTree[i][1], skillTree[i][2]};
                    }
                }
            }
        }
    }

    public int getColSize() {
        return skillTree[0].length;
    }
}
