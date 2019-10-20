import java.util.List;

public class VariableValue extends MacroValue {
    private List<LexerToken> value;

    public VariableValue(List<LexerToken> value) {
        this.value = value;
    }

    public List<LexerToken> getValue() {
        return value;
    }
}
