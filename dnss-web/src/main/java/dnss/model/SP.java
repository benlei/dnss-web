package dnss.model;

public class SP {
    private int[] sp;

    public int[] getSp() {
        return sp;
    }

    public void setSp(int[] sp) {
        this.sp = sp;
    }

    public int forCap(int cap) {
        if (cap < 1 || sp.length < cap) {
            throw new RuntimeException("Invalid level cap passed in as argument.");
        }

        return sp[cap-1];
    }

    public int getLatestCap() {
        return sp.length;
    }

    public boolean isLastestCap(int cap) {
        return cap == getLatestCap();
    }
}
