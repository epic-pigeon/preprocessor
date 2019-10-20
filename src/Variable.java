public class Variable extends MacroElement {
    private String name;
    private int minTokens, maxTokens;
    private boolean canBeEmpty = false;

    public boolean canBeEmpty() {
        return canBeEmpty;
    }

    public Variable(String name, int minTokens, int maxTokens) {
        this.name = name;
        this.minTokens = minTokens;
        this.maxTokens = maxTokens;
    }

    public Variable(String name, int minTokens, int maxTokens, boolean canBeEmpty) {
        this.name = name;
        this.minTokens = minTokens;
        this.maxTokens = maxTokens;
        this.canBeEmpty = canBeEmpty;
    }

    public String getName() {
        return name;
    }

    public int getMinTokens() {
        return minTokens;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    @Override
    public String toString() {
        return "VARIABLE " + name + "(" + (minTokens > 0 ? String.valueOf(minTokens) : "")
                + "-"
                + (maxTokens < Integer.MAX_VALUE ? String.valueOf(maxTokens) : "")
                + ")";
    }
}
