public class Token extends MacroElement {
    private String value;
    private Rule rule;

    public Token(String value, Rule rule) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Rule getRule() {
        return rule;
    }

    @Override
    public String toString() {
        return "TOKEN '" + value + "'";
    }
}
