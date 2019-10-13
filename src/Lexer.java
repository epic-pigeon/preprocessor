import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private Collection<LexerToken> tokens;
    private String code;
    private Collection<Rule> rules;
    private Rule toSkip;
    private int position;

    public TokenHolder lex(String code, Collection<Rule> rules, Rule toSkip) {
        position = 0;
        tokens = new Collection<>();
        this.code = code;
        this.rules = rules;
        this.toSkip = toSkip;
        while (position < code.length()) {
            readNext();
        }
        return new TokenHolder(tokens);
    }

    private LexerToken readNext() {
            Matcher skipMatcher = toSkip.getMatches(code.substring(position));
            if (skipMatcher.find() && skipMatcher.start() == 0) {
                position += skipMatcher.group().length();
            }
        if (position >= code.length()) return null;

        for (Rule rule : rules) {
                Matcher matcher = rule.getMatches(code.substring(position));
                if (matcher.find() && matcher.start() == 0) {
                    position += matcher.group().length();
                    LexerToken token = new LexerToken(matcher.group(), rule);
                        tokens.add(token);
                        return token;
                }
        }

        System.out.println(code.substring(position));

        throw new RuntimeException("Lexing error");
    }
}