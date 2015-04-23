package dnss.model;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Jobs implements Iterable<Job> {
    private Job primary;
    private Job secondary;
    private Job tertiary;

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
            private Job job = primary;
            public boolean hasNext() {
                return job != null;
            }

            public Job next() {
                Job curr = job;
                if (curr != null) {
                    switch (curr.getAdvancement()) {
                        case PRIMARY:
                            job = secondary;
                            break;
                        case SECONDARY:
                            job = tertiary;
                            break;
                        case TERTIARY:
                            job = null;
                            break;
                    }
                }

                return curr;
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
}
