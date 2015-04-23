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
        for (Advancement e : Advancement.values()) {
            if (e.advancement == job) {
                return e;
            }
        }

        throw new RuntimeException("Invalid Advancement: " + job);
    }

    public int toInt() {
        return advancement;
    }
}
