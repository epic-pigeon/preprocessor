public class Token extends MacroElement {
    private String value;

    public Token(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "TOKEN '" + value + "'";
    }
}
