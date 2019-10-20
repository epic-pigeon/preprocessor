import java.util.List;

public class VarArgsValue extends MacroValue {
    private List<List<LexerToken>> value;

    public VarArgsValue(List<List<LexerToken>> value) {
        this.value = value;
    }

    public List<List<LexerToken>> getValue() {
        return value;
    }

    public void setValue(List<List<LexerToken>> value) {
        this.value = value;
    }
}
