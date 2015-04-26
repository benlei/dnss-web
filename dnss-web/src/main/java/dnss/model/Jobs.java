package dnss.model;

import dnss.enums.Advancement;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Jobs implements Iterable<Job> {
    private Job primary;
    private Job secondary;
    private Job tertiary;
    private int level;
    private int maxSP;

    public Job getPrimary() {
        return primary;
    }

    public void setPrimary(Job primary) {
        this.primary = primary;
    }

    public Job getSecondary() {
        return secondary;
    }

    public void setSecondary(Job secondary) {
        this.secondary = secondary;
    }

    public Job getTertiary() {
        return tertiary;
    }

    public void setTertiary(Job tertiary) {
        this.tertiary = tertiary;
    }

    public Iterator<Job> iterator() {
        return new Iterator<Job>() {
            private int i = 0;
            public boolean hasNext() {
                return i < 3;
            }

            public Job next() {
                switch (i++) {
                    case 0: return primary;
                    case 1: return secondary;
                    case 2: return tertiary;
                }

                return null;
            }
        };
    }

    public Iterator<Job> getIterator() {
        return iterator();
    }

    public void forEach(Consumer<? super Job> action) {
        // do nothing
    }

    public Spliterator<Job> spliterator() {
        return null; // not implementing
    }

    public int getMaxSP() {
        return maxSP;
    }

    public void setMaxSP(int maxSP) {
        setMaxSP(maxSP, tertiary.getSpRatio());
    }

    public void setMaxSP(int maxSP, float[] ratios) {
        this.maxSP = maxSP;
        for (Job job : this) {
            if (job != null) {
                job.setMaxSP((int) (ratios[job.getAdvancement().advancement] * maxSP));
            }
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;

        for (Job job : this) {
            if (job != null) {
                job.setMaxSkillRequiredLevel(level);
            }
        }
    }

    public void setJob(Job job) {
        switch (job.getAdvancement()) {
            case PRIMARY: setPrimary(job); break;
            case SECONDARY: setSecondary(job); break;
            case TERTIARY: setTertiary(job); break;
        }
    }

    public Job getJob(Advancement advancement) {
        switch (advancement) {
            case PRIMARY: return primary;
            case SECONDARY: return secondary;
            case TERTIARY: return tertiary;
        }

        return null;
    }

    public boolean isValid() {
        if (primary == null && secondary == null && tertiary == null) {
            return false;
        }

        if ((primary == null && secondary == null) ||
                (primary == null && tertiary == null) ||
                (secondary == null && tertiary == null)) {
            return true;
        }

        if (primary == null || secondary == null || tertiary == null) {
            return (primary == null && tertiary.getParent().equals(secondary)) ||
                    (secondary == null && tertiary.getParent().getParent().equals(primary)) ||
                    (tertiary == null && secondary.getParent().equals(primary));
        }

        return tertiary.getParent().equals(secondary) && secondary.getParent().equals(primary);
    }

    public int getNumJobs() {
        int sum = 0;
        for (Job j : this) {
            if (j != null) {
                sum++;
            }
        }

        return sum;
    }
}
