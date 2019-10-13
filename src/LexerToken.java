public class LexerToken {
    private String value;
    private Rule rule;

    public LexerToken(String value, Rule rule) {
        this.value = value;
        this.rule = rule;
    }

    public String getValue() {
        return value;
    }

    public Rule getRule() {
        return rule;
    }

    public String toString() {
        return rule.getName() + " '" + value.replaceAll("\\r?\\n", "<new line>") + "'";
    }

}
