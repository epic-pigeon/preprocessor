import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {
    private Pattern pattern;
    private String name;

    public Rule(Pattern pattern, String name) {
        this.pattern = pattern;
        this.name = name;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getName() {
        return name;
    }

    public Matcher getMatches(String string) {
        return pattern.matcher(string);
    }


}
