public class VarArgs extends MacroElement {
    private int minArgs, maxArgs;
    private String name;
    private char separator;

    public VarArgs(int minArgs, int maxArgs, String name, char separator) {
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.name = name;
        this.separator = separator;
    }

    public int getMinArgs() {
        return minArgs;
    }

    public int getMaxArgs() {
        return maxArgs;
    }

    public String getName() {
        return name;
    }

    public char getSeparator() {
        return separator;
    }

    @Override
    public String toString() {
        return "VARARGS " + name
                + "[" + (minArgs > 0                 ? String.valueOf(minArgs) : "")
                + separator
                      + (maxArgs < Integer.MAX_VALUE ? String.valueOf(maxArgs) : "")
                + "]";
    }
}
