import java.util.Iterator;

public class TokenHolder {
    private Collection<LexerToken> tokens;
    private int position;

    public TokenHolder(Collection<LexerToken> tokens) {
        this.tokens = tokens;
        position = 0;
    }

    public Collection<LexerToken> getTokens() {
        return tokens;
    }

    public void setTokens(Collection<LexerToken> tokens) {
        this.tokens = tokens;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[\n    ");
        for (int i = 0; i < getTokens().size(); i++) {
            if (i != 0) stringBuilder.append(",\n    ");
            stringBuilder.append(tokens.get(i).toString());
        }
        stringBuilder.append("\n]");
        return stringBuilder.toString();
    }

    public boolean hasNext() {
        return tokens.size() > position;
    }

    public LexerToken next() {
        return tokens.get(position++);
    }

    public LexerToken lookUp() {
        return lookUp(0);
    }

    public LexerToken lookUp(int i) {
        return tokens.size() > position + i ? tokens.get(position + i) : null;
    }

    public Iterator<LexerToken> iterator() {
        return tokens.iterator();
    }
}