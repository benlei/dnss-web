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
        if (cap < 1 || cap > sp.length) {
            return -1;
        }

        return sp[cap-1];
    }
}
