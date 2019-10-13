import java.util.List;
import java.util.Objects;

public class Macro {
    private List<MacroElement> macro;
    private List<MacroElement> replacement;

    public Macro(List<MacroElement> macro, List<MacroElement> replacement) {
        this.macro = macro;
        this.replacement = replacement;
    }

    public List<MacroElement> getMacro() {
        return macro;
    }

    public List<MacroElement> getReplacement() {
        return replacement;
    }

    @Override
    public String toString() {
        return "Macro{" +
                "macro=" + macro +
                ", replacement=" + replacement +
                '}';
    }
}
