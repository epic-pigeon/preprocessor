public class VarArgs extends MacroElement {
    private int minArgs, maxArgs, minPerArg, maxPerArg;
    private String name;
    private char separator;
    private boolean canBeEmpty = false;

    public VarArgs(int minArgs, int maxArgs, int minPerArg, int maxPerArg, String name, char separator) {
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.minPerArg = minPerArg;
        this.maxPerArg = maxPerArg;
        this.name = name;
        this.separator = separator;
    }

    public VarArgs(int minArgs, int maxArgs, int minPerArg, int maxPerArg, String name, char separator, boolean canBeEmpty) {
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.minPerArg = minPerArg;
        this.maxPerArg = maxPerArg;
        this.name = name;
        this.separator = separator;
        this.canBeEmpty = canBeEmpty;
    }

    public boolean canBeEmpty() {
        return canBeEmpty;
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

    public int getMinPerArg() {
        return minPerArg;
    }

    public int getMaxPerArg() {
        return maxPerArg;
    }



    @Override
    public String toString() {
        return "VARARGS " + name
                + "[" + (minArgs > 0                 ? String.valueOf(minArgs) : "")
                + separator
                      + (maxArgs < Integer.MAX_VALUE ? String.valueOf(maxArgs) : "")
                + "]"
                + "(" + (minPerArg > 0                 ? String.valueOf(minPerArg) : "")
                + "-"
                      + (maxPerArg < Integer.MAX_VALUE ? String.valueOf(maxPerArg) : "")
                + ")" +  (canBeEmpty ? '?' : '!');
    }
}
