package dnss.model;

public class Build {
    final private static String BUILD_STR = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz+";
    private StringBuilder build = new StringBuilder("------------------------------------------------------------------------");

    public Build(String build) {
        if (build != null) {
            int len = Math.min(build.length(), this.build.length());
            for (int i = 0; i < len; i++) {
                directPut(i, build.charAt(i));
            }
        }
    }

    public int get(int position){
        return BUILD_STR.indexOf(build.charAt(position));
    }

    public void put(int position, int level) {
        directPut(position, BUILD_STR.charAt(level));
    }

    private void directPut(int position, char c) {
        if (BUILD_STR.indexOf(c) == -1) {
            throw new RuntimeException(c + " is not a valid build character");
        }

        build.setCharAt(position, c);
    }

    @Override
    public String toString() {
        return build.toString();
    }
}
