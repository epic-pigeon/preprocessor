import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preprocessor {
    public static String preprocess(String code) {
        /*AtomicInteger i = new AtomicInteger(0);
        Supplier<Character> charAccessor = () -> i.get() < code.length() ? code.charAt(i.getAndIncrement()) : null;
        Supplier<Character> checkNextAccessor = () -> i.get() < code.length() ? code.charAt(i.get() + 1) : null;
        Runnable reverter = i::getAndDecrement;
        Character c;
        StringBuilder result = new StringBuilder();
        Supplier<String> spaceSkipper = () -> {
            reverter.run();
            StringBuilder res = new StringBuilder();
            Character _c;
            while ((_c = charAccessor.get()) != null && Character.isWhitespace(_c)) res.append(_c);
            return res.toString();
        };
        List<List<MacroElement>> macros = new ArrayList<>();
        while ((c = charAccessor.get()) != null) {
            if (c == '#') {
                Character _c = checkNextAccessor.get();
                if (_c == null || _c == '#') {
                    result.append('#');
                    if (_c != null) c = charAccessor.get();
                } else {
                    String totalCommand = "#";
                    StringBuilder commandBuilder = new StringBuilder();
                    while ((c = charAccessor.get()) != null && Character.isAlphabetic(c)) {
                        commandBuilder.append(c);
                    }
                    String command = commandBuilder.toString();
                    totalCommand += command;
                    if (command.equals("macro")) {
                        // TODO
                        //reverter.run();
                        totalCommand += spaceSkipper.get();


                    }
                }
            } else if (c == '\'' || c == '"' || c == '`') {
                char stringSeparator = c;
                StringBuilder str = new StringBuilder(String.valueOf(c));
                while ((c = charAccessor.get()) != null && c != stringSeparator) {
                    str.append(c);
                    if (c == '\\') {
                        if ((c = charAccessor.get()) != null) str.append(c);
                    }
                }
                if (c != null) str.append(c);
                result.append(str.toString());
            } else {
                result.append((char) c);
            }
        }
        return result.toString();*/

        TokenHolder tokenHolder = new Lexer().lex(code, new Collection<>(
                new Rule(Pattern.compile("[$_a-zA-Z][$_a-zA-Z0-9]*"), "IDENTIFIER"),
                new Rule(Pattern.compile("\\("), "LEFT_PAREN"),
                new Rule(Pattern.compile("\\)"), "RIGHT_PAREN"),
                new Rule(Pattern.compile("\\["), "LEFT_SQUARE_PAREN"),
                new Rule(Pattern.compile("\\]"), "RIGHT_SQUARE_PAREN"),
                new Rule(Pattern.compile("\\{"), "LEFT_CURLY_PAREN"),
                new Rule(Pattern.compile("\\}"), "RIGHT_CURLY_PAREN"),
                new Rule(Pattern.compile("\\,"), "COMMA"),
                new Rule(Pattern.compile("\\;"), "SEMICOLON"),
                new Rule(Pattern.compile("->"), "THIN_ARROW"),
                new Rule(Pattern.compile("=>"), "FAT_ARROW"),
                new Rule(Pattern.compile("%[$_a-zA-Z0-9]+(\\[\\d*.\\d*])?%"), "DIRECTIVE_IDENTIFIER"),
                new Rule(Pattern.compile("[+=\\-*/%!~|&^<>.]"), "OPERATOR"),
                new Rule(Pattern.compile("('([^']|(\\\\.))*')|(\"([^\"]|(\\\\.))*\")|(`([^`]|(\\\\.))*`)"), "STRING"),
                new Rule(Pattern.compile("#[$_a-zA-Z]+"), "DIRECTIVE")

        ), new Rule(Pattern.compile("\\s+"), "kar"));

        List<Macro> macros = new Collection<>();
        List<LexerToken> resultingList = new Collection<>();

        while (tokenHolder.hasNext()) {
            LexerToken token = tokenHolder.next();
            if (token.getRule().getName().equals("DIRECTIVE")) {
                if (token.getValue().substring(1).equals("macro")) {
                    List<LexerToken> onError = new Collection<>(token);
                    List<MacroElement> macro = new Collection<>();
                    List<MacroElement> replacement = new Collection<>();
                    boolean error = false;
                    try {
                        LexerToken current = tokenHolder.next();
                        onError.add(current);
                        if (!current.getRule().getName().equals("LEFT_CURLY_PAREN")) throw new RuntimeException();
                        { // macro
                            int count = 1;
                            while (count > 0) {
                                current = tokenHolder.next();
                                onError.add(current);
                                if (current.getRule().getName().equals("RIGHT_CURLY_PAREN")) {
                                    count--;
                                    if (count > 0) {
                                        macro.add(new Token(current.getValue()));
                                    }
                                } else if (current.getRule().getName().equals("LEFT_CURLY_PAREN")) {
                                    macro.add(new Token(current.getValue()));
                                    count++;
                                } else if (current.getRule().getName().equals("DIRECTIVE_IDENTIFIER")) {
                                    String identifier = current.getValue().substring(1);
                                    identifier = identifier.substring(0, identifier.length() - 1);
                                    Matcher nameMatcher = Pattern.compile("[$_a-zA-Z0-9]+").matcher(identifier);
                                    nameMatcher.find();
                                    String name = nameMatcher.group();
                                    identifier = identifier.substring(name.length());
                                    if (identifier.length() > 0) {
                                        identifier = identifier.substring(1, identifier.length() - 1);
                                        int minLength = 0;
                                        int maxLength = Integer.MAX_VALUE;
                                        Matcher minMatcher = Pattern.compile("^[0-9]+").matcher(identifier);
                                        if (minMatcher.find()) {
                                            String group = minMatcher.group();
                                            minLength = Integer.valueOf(group);
                                            identifier = identifier.substring(group.length());
                                        }
                                        char separator = identifier.charAt(0);
                                        identifier = identifier.substring(1);
                                        Matcher maxMatcher = Pattern.compile("^[0-9]+").matcher(identifier);
                                        if (maxMatcher.find()) {
                                            String group = maxMatcher.group();
                                            maxLength = Integer.valueOf(group);
                                            //identifier = identifier.substring(group.length());
                                        }
                                        macro.add(new VarArgs(minLength, maxLength, name, separator));
                                    } else {
                                        macro.add(new Variable(name));
                                    }
                                } else {
                                    macro.add(new Token(current.getValue()));
                                }
                            }
                        }
                        current = tokenHolder.next();
                        onError.add(current);
                        if (!current.getRule().getName().equals("FAT_ARROW")) throw new RuntimeException();
                        current = tokenHolder.next();
                        onError.add(current);
                        if (!current.getRule().getName().equals("LEFT_CURLY_PAREN")) throw new RuntimeException();
                        { // replacement
                            int count = 1;
                            while (count > 0) {
                                current = tokenHolder.next();
                                onError.add(current);
                                if (current.getRule().getName().equals("RIGHT_CURLY_PAREN")) {
                                    count--;
                                    if (count > 0) {
                                        replacement.add(new Token(current.getValue()));
                                    }
                                } else if (current.getRule().getName().equals("LEFT_CURLY_PAREN")) {
                                    replacement.add(new Token(current.getValue()));
                                    count++;
                                } else if (current.getRule().getName().equals("DIRECTIVE_IDENTIFIER")) {
                                    String identifier = current.getValue().substring(1);
                                    identifier = identifier.substring(0, identifier.length() - 1);
                                    Matcher nameMatcher = Pattern.compile("[$_a-zA-Z0-9]+").matcher(identifier);
                                    nameMatcher.find();
                                    String name = nameMatcher.group();
                                    identifier = identifier.substring(name.length());
                                    if (identifier.length() > 0) {
                                        identifier = identifier.substring(1, identifier.length() - 1);
                                        int minLength = 0;
                                        int maxLength = Integer.MAX_VALUE;
                                        Matcher minMatcher = Pattern.compile("^[0-9]+").matcher(identifier);
                                        if (minMatcher.find()) {
                                            String group = minMatcher.group();
                                            minLength = Integer.valueOf(group);
                                            identifier = identifier.substring(group.length());
                                        }
                                        char separator = identifier.charAt(0);
                                        identifier = identifier.substring(1);
                                        Matcher maxMatcher = Pattern.compile("^[0-9]+").matcher(identifier);
                                        if (maxMatcher.find()) {
                                            String group = maxMatcher.group();
                                            maxLength = Integer.valueOf(group);
                                            //identifier = identifier.substring(group.length());
                                        }
                                        replacement.add(new VarArgs(minLength, maxLength, name, separator));
                                    } else {
                                        replacement.add(new Variable(name));
                                    }
                                } else {
                                    replacement.add(new Token(current.getValue()));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        resultingList.addAll(onError);
                        error = true;
                    }

                    if (!error) {
                        macros.add(new Macro(macro, replacement));
                    }
                } else {
                    resultingList.add(token);
                }
            } else {
                // TODO for Anton: check and replace macros
                resultingList.add(token);
            }
        }

        //System.out.println(macros);

        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < resultingList.size(); i++)
            resultBuilder.append(resultingList.get(i).getValue()).append(" ");
        return resultBuilder.toString();
    }
}
