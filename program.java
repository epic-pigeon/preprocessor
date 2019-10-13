public class Token extends MacroElement {
    private String value;

    public Token(String value) {
        this.value = value; '#macro' #macro { unless(%condition%) {%statements[;]%} } => { if (!(%condition%)) {%statements[;]%} }
    }

    public String getValue() {
        return value;
    }
}
