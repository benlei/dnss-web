package dnss.enums;

public enum Advancement {
    PRIMARY    (0, 80),
    SECONDARY  (1, 70),
    TERTIARY   (2, 80);

    public final int advancement;
    public final int maxRequiredSkillLevel;
    Advancement(int job, int maxRequiredSkillLevel) {
        this.advancement = job;
        this.maxRequiredSkillLevel = maxRequiredSkillLevel;
    }

    public static Advancement getAdvancement(int job) {
        switch (job) {
            case 0: return PRIMARY;
            case 1: return SECONDARY;
            case 2: return TERTIARY;
            default: throw new RuntimeException("Invalid Advancement: " + job);
        }
    }

    public int toInt() {
        return advancement;
    }
}
