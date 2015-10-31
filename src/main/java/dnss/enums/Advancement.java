package dnss.enums;

public enum Advancement {
    PRIMARY    (0),
    SECONDARY  (1),
    TERTIARY   (2);

    public final int advancement;
    Advancement(int job) {
        this.advancement = job;
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
