package dnss.enums;

public enum Advancement {
    PRIMARY    (0),
    SECONDARY  (1),
    TERTIARY   (2),
    QUATERNARY (3),
    QUINARY    (4);

    public final int job;
    Advancement(int job) {
        this.job = job;
    }

    public static Advancement getAdvancement(int job) {
        for (Advancement adv : Advancement.values()) {
            if (adv.job == job) {
                return adv;
            }
        }

        throw new RuntimeException("Invalid Advancement: " + job);
    }
}
