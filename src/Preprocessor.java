import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preprocessor {
    private static Collection<Rule> rules = new Collection<>(
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
            new Rule(Pattern.compile("##"), "CONCAT"),
            new Rule(Pattern.compile("%[$_a-zA-Z0-9]+(\\[\\d*.\\d*])?(\\(\\d*-\\d*\\))?[!?]?%"), "DIRECTIVE_IDENTIFIER"),
            new Rule(Pattern.compile("[+=\\-*/%!~|&^<>.]"), "OPERATOR"),
            new Rule(Pattern.compile("('([^']|(\\\\.))*')|(\"([^\"]|(\\\\.))*\")|(`([^`]|(\\\\.))*`)"), "STRING"),
            new Rule(Pattern.compile("#[$_a-zA-Z]+"), "DIRECTIVE")

    );

    private static Rule toSkip = new Rule(Pattern.compile("\\s+"), "kar");

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

        TokenHolder tokenHolder = new Lexer().lex(code, rules, toSkip);

        Collection<Macro> macros = new Collection<>();
        List<LexerToken> resultingList = new Collection<>();

        while (tokenHolder.hasNext()) {
            LexerToken token = tokenHolder.next();
            if (token != null) {
                if (token.getRule() != null && token.getRule().getName().equals("DIRECTIVE")) {
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
                                            macro.add(new Token(current.getValue(), current.getRule()));
                                        }
                                    } else if (current.getRule().getName().equals("LEFT_CURLY_PAREN")) {
                                        macro.add(new Token(current.getValue(), current.getRule()));
                                        count++;
                                    } else if (current.getRule().getName().equals("DIRECTIVE_IDENTIFIER")) {
                                        String identifier = current.getValue().substring(1);
                                        boolean canBeEmpty = false;
                                        if (identifier.charAt(identifier.length() - 1) == '!' ||
                                            identifier.charAt(identifier.length() - 1) == '?') {
                                            canBeEmpty = identifier.charAt(identifier.length() - 1) == '?';
                                            identifier = identifier.substring(0, identifier.length() - 1);
                                        }
                                        identifier = identifier.substring(0, identifier.length() - 1);
                                        Matcher nameMatcher = Pattern.compile("[$_a-zA-Z0-9]+").matcher(identifier);
                                        nameMatcher.find();
                                        String name = nameMatcher.group();
                                        identifier = identifier.substring(name.length());
                                        if (identifier.length() > 0) {
                                            if (identifier.charAt(0) == '[') {
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
                                                    identifier = identifier.substring(group.length());
                                                }
                                                int minPerArg = 0, maxPerArg = Integer.MAX_VALUE;
                                                if (identifier.length() > 0) {
                                                    identifier = identifier.substring(2);
                                                    minMatcher = Pattern.compile("^[0-9]+").matcher(identifier);
                                                    if (minMatcher.find()) {
                                                        String group = minMatcher.group();
                                                        minPerArg = Integer.valueOf(group);
                                                        identifier = identifier.substring(group.length());
                                                    }
                                                    identifier = identifier.substring(1);
                                                    maxMatcher = Pattern.compile("^[0-9]+").matcher(identifier);
                                                    if (maxMatcher.find()) {
                                                        String group = maxMatcher.group();
                                                        maxPerArg = Integer.valueOf(group);
                                                        identifier = identifier.substring(group.length());
                                                    }
                                                }
                                                //System.out.println(identifier);
                                                macro.add(new VarArgs(minLength, maxLength, minPerArg, maxPerArg, name, separator, canBeEmpty));
                                            } else if (identifier.charAt(0) == '(') {
                                                int minLength = 0, maxLength = Integer.MAX_VALUE;
                                                Matcher minMatcher = Pattern.compile("^[0-9]+").matcher(identifier);
                                                if (minMatcher.find()) {
                                                    String group = minMatcher.group();
                                                    minLength = Integer.valueOf(group);
                                                    identifier = identifier.substring(group.length());
                                                }
                                                identifier = identifier.substring(1);
                                                Matcher maxMatcher = Pattern.compile("^[0-9]+").matcher(identifier);
                                                if (maxMatcher.find()) {
                                                    String group = maxMatcher.group();
                                                    maxLength = Integer.valueOf(group);
                                                    //identifier = identifier.substring(group.length());
                                                }
                                                macro.add(new Variable(name, minLength, maxLength, canBeEmpty));
                                            } else {
                                                macro.add(new Variable(name, 0, Integer.MAX_VALUE, identifier.charAt(0) == '?'));
                                            }
                                        } else {
                                            macro.add(new Variable(name, 0, Integer.MAX_VALUE));
                                        }
                                    } else {
                                        macro.add(new Token(current.getValue(), current.getRule()));
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
                                            replacement.add(new Token(current.getValue(), current.getRule()));
                                        }
                                    } else if (current.getRule().getName().equals("LEFT_CURLY_PAREN")) {
                                        replacement.add(new Token(current.getValue(), current.getRule()));
                                        count++;
                                    } else if (current.getRule().getName().equals("DIRECTIVE_IDENTIFIER")) {
                                        String identifier = current.getValue().substring(1);
                                        identifier = identifier.substring(0, identifier.length() - 1);
                                        Matcher nameMatcher = Pattern.compile("[$_a-zA-Z0-9]+").matcher(identifier);
                                        nameMatcher.find();
                                        String name = nameMatcher.group();
                                        identifier = identifier.substring(name.length());
                                        if (identifier.length() > 0) {
                                            if (identifier.charAt(0) == '[') {
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
                                                    identifier = identifier.substring(group.length());
                                                }
                                                int minPerArg = 0, maxPerArg = Integer.MAX_VALUE;
                                                if (identifier.length() > 0) {
                                                    identifier = identifier.substring(2);
                                                    minMatcher = Pattern.compile("^[0-9]+").matcher(identifier);
                                                    if (minMatcher.find()) {
                                                        String group = minMatcher.group();
                                                        minPerArg = Integer.valueOf(group);
                                                        identifier = identifier.substring(group.length());
                                                    }
                                                    identifier = identifier.substring(1);
                                                    maxMatcher = Pattern.compile("^[0-9]+").matcher(identifier);
                                                    if (maxMatcher.find()) {
                                                        String group = maxMatcher.group();
                                                        maxPerArg = Integer.valueOf(group);
                                                        identifier = identifier.substring(group.length());
                                                    }
                                                }
                                                //System.out.println(identifier);
                                                replacement.add(new VarArgs(minLength, maxLength, minPerArg, maxPerArg, name, separator));
                                            } else if (identifier.charAt(0) == '(') {
                                                int minLength = 0, maxLength = Integer.MAX_VALUE;
                                                identifier = identifier.substring(1, identifier.length() - 1);
                                                Matcher minMatcher = Pattern.compile("^[0-9]+").matcher(identifier);
                                                if (minMatcher.find()) {
                                                    String group = minMatcher.group();
                                                    minLength = Integer.valueOf(group);
                                                    identifier = identifier.substring(group.length());
                                                }
                                                identifier = identifier.substring(1);
                                                Matcher maxMatcher = Pattern.compile("^[0-9]+").matcher(identifier);
                                                if (maxMatcher.find()) {
                                                    String group = maxMatcher.group();
                                                    maxLength = Integer.valueOf(group);
                                                    //identifier = identifier.substring(group.length());
                                                }
                                                replacement.add(new Variable(name, minLength, maxLength));
                                            } else throw new RuntimeException("Wrong identifier format");
                                        } else {
                                            replacement.add(new Variable(name, 0, Integer.MAX_VALUE));
                                        }
                                    } else if (current.getRule().getName().equals("CONCAT")) {
                                        replacement.add(new Concat());
                                    } else {
                                        replacement.add(new Token(current.getValue(), current.getRule()));
                                    }
                                }
                                //System.out.println(replacement);
                            }
                            if (macro.size() < 1 || !(macro.get(0) instanceof Token))
                                throw new RuntimeException("Invalid macro");
                        } catch (Exception e) {
                            e.printStackTrace();
                            resultingList.addAll(onError);
                            error = true;
                        }

                        if (!error) {
                            //System.out.println(macro);
                            //System.out.println(replacement);
                            macros.add(new Macro(macro, replacement));
                        }
                    } else {
                        resultingList.add(token);
                    }
                } else {
                    final LexerToken _token = token;
                    if (macros.some(macro -> ((Token) macro.getMacro().get(0)).getValue().equals(_token.getValue()))) {
                        int occurencies = macros.count(macro -> ((Token) macro.getMacro().get(0)).getValue().equals(_token.getValue()));
                        int tries = 0;
                        List<Exception> exceptions = new Collection<>();
                        int pos = tokenHolder.getPosition();
                        List<LexerToken> onError = new Collection<>();
                        boolean success = false;
                        while (tries < occurencies) {
                            Macro __macro = macros.findCount(tries, _macro -> ((Token) _macro.getMacro().get(0)).getValue().equals(_token.getValue()));
                            List<MacroElement> macro = __macro.getMacro();
                            List<MacroElement> replacement = __macro.getReplacement();
                            onError = new Collection<>(token);
                            try {
                                Map<String, MacroValue> valueMap = new HashMap<>();
                                for (int i = 1; i < macro.size(); i++) {
                                    MacroElement element = macro.get(i);
                                    if (element instanceof Token) {
                                        token = tokenHolder.next();
                                        onError.add(token);
                                        if (!token.getValue().equals(((Token) element).getValue()))
                                            throw new RuntimeException("Token '" + ((Token) element).getValue() + "' cannot be applied to '" + token.getValue() + "'");
                                    } else if (element instanceof Variable) {
                                        List<LexerToken> variableValue = new ArrayList<>();
                                        if (tokenHolder.lookUp() != null && tokenHolder.lookUp().getValue().equals("(")) {
                                            int parenCounter = 1;
                                            LexerToken __tok = tokenHolder.next();
                                            variableValue.add(__tok);
                                            onError.add(__tok);
                                            while (parenCounter > 0) {
                                                LexerToken _tok = tokenHolder.next();
                                                if (_tok.getValue().equals("(")) parenCounter++;
                                                if (_tok.getValue().equals(")")) parenCounter--;
                                                variableValue.add(_tok);
                                                onError.add(_tok);
                                            }
                                        } else if (i + 1 < macro.size() && macro.get(i + 1) instanceof Token) {
                                            String tok = ((Token) macro.get(i + 1)).getValue();
                                            //System.out.println(tokenHolder.getTokens().slice(tokenHolder.getPosition()));
                                            //System.out.println(tok);
                                            while (!tokenHolder.lookUp().getValue().equals(tok)) {
                                                LexerToken __tok = tokenHolder.next();
                                                //System.out.println(__tok);
                                                variableValue.add(__tok);
                                                onError.add(__tok);
                                            }
                                            //System.out.println();
                                        } else throw new RuntimeException("Cannot read variable value");
                                        if (variableValue.size() == 0 && !((Variable) element).canBeEmpty()) {
                                            throw new RuntimeException(((Variable) element).getName() + " is strict and cannot be empty");
                                        }
                                        Variable variable = (Variable) element;
                                        if (variableValue.size() < variable.getMinTokens() || variableValue.size() > variable.getMaxTokens()) {
                                            throw new RuntimeException(((Variable) element).getName() + " cannot contain " + variableValue.size() + " tokens");
                                        }
                                        valueMap.put(((Variable) element).getName(), new VariableValue(variableValue));
                                    } else if (element instanceof VarArgs) {
                                        VarArgs varArgs = (VarArgs) element;
                                        List<List<LexerToken>> varargsValue = new ArrayList<>();
                                        //System.out.println(i);
                                        //System.out.println(macro);
                                        if (i + 1 < macro.size() && macro.get(i + 1) instanceof Token) {
                                            String tok = ((Token) macro.get(i + 1)).getValue();
                                            while (!tokenHolder.lookUp().getValue().equals(tok)) {
                                                List<LexerToken> value = new ArrayList<>();

                                                if (tokenHolder.lookUp() != null && tokenHolder.lookUp().getValue().equals("(")) {
                                                    int parenCounter = 1;
                                                    LexerToken __tok = tokenHolder.next();
                                                    value.add(__tok);
                                                    onError.add(__tok);
                                                    while (parenCounter > 0) {
                                                        LexerToken _tok = tokenHolder.next();
                                                        if (_tok.getValue().equals("(")) parenCounter++;
                                                        if (_tok.getValue().equals(")")) parenCounter--;
                                                        value.add(_tok);
                                                        onError.add(_tok);
                                                    }
                                                    onError.add(tokenHolder.lookUp());
                                                    assert tokenHolder.next().getValue().equals("" + varArgs.getSeparator());

                                                } else {
                                                    while (!tokenHolder.lookUp().getValue().equals("" + varArgs.getSeparator()) &&
                                                            !tokenHolder.lookUp().getValue().equals(tok)) {
                                                        LexerToken __tok = tokenHolder.next();
                                                        value.add(__tok);
                                                        onError.add(__tok);
                                                    }
                                                    if (tokenHolder.lookUp().getValue().equals("" + varArgs.getSeparator()))
                                                        onError.add(tokenHolder.next());
                                                }
                                                //System.out.println(varArgs);
                                                if (value.size() == 0 && !varArgs.canBeEmpty()) {
                                                    throw new RuntimeException(varArgs.getName() + " is strict, arg cannot be empty");
                                                } else if (value.size() < varArgs.getMinPerArg() || value.size() > varArgs.getMaxPerArg()) {
                                                    throw new RuntimeException("Invalid amount of parameters for " + varArgs.getName());
                                                }
                                                varargsValue.add(value);
                                            }
                                        } else throw new RuntimeException("Cannot read varargs value");
                                        if (varargsValue.size() < varArgs.getMinArgs() || varargsValue.size() > varArgs.getMaxArgs()) {
                                            throw new RuntimeException("Unacceptable number of args " + varargsValue.size() + " (from " + varArgs.getMinArgs() + " to " + varArgs.getMaxArgs() + " expected)");
                                        }
                                        valueMap.put(varArgs.getName(), new VarArgsValue(varargsValue));
                                    }
                                }

                                List<LexerToken> tokenReplacement = new Collection<>();
                                for (MacroElement element : replacement) {
                                    if (element instanceof Token) {
                                        tokenReplacement.add(lexerTokenFrom((Token) element));
                                    } else if (element instanceof Variable) {
                                        List<LexerToken> value = ((VariableValue) valueMap.get(((Variable) element).getName())).getValue();
                                        Variable variable = (Variable) element;
                                        int min = variable.getMinTokens(), max = Math.min(value.size(), variable.getMaxTokens());
                                        tokenReplacement.addAll(new Collection<>(value).slice(min, max - 1));
                                    } else if (element instanceof VarArgs) {
                                        VarArgs varArgs = ((VarArgs) element);
                                        List<List<LexerToken>> value = ((VarArgsValue) valueMap.get(varArgs.getName())).getValue();
                                        int max = varArgs.getMaxArgs() == Integer.MAX_VALUE ? value.size() : varArgs.getMaxArgs();
                                        //System.out.println(value);
                                        value = new Collection<>(value).slice(varArgs.getMinArgs(), max - 1);
                                        //System.out.println(value);
                                        for (int i = 0; i < value.size(); i++) {
                                            if (i != 0) {
                                                tokenReplacement.add(new LexerToken("" + varArgs.getSeparator(), null));
                                            }
                                            int minArgs = varArgs.getMinPerArg(), maxArgs = Math.min(varArgs.getMaxPerArg(), value.get(i).size());
                                            //System.out.println(value.get(i));
                                            //System.out.println(minArgs);
                                            //System.out.println(maxArgs);
                                            //System.out.println(new Collection<>(value.get(i)).slice(minArgs, maxArgs - 1));
                                            tokenReplacement.addAll(new Collection<>(value.get(i)).slice(minArgs, maxArgs - 1));
                                        }
                                        //System.out.println(tokenReplacement);
                                    } else if (element instanceof Concat) {
                                        tokenReplacement.add(null);
                                    }
                                }

                                tokenHolder.appendOnPointer(tokenReplacement);
                                success = true;
                                break;
                            } catch (Exception e) {
                                //e.printStackTrace();
                                //resultingList.addAll(onError);
                                exceptions.add(e);
                                tries++;
                                onError = new Collection<>(_token);
                                tokenHolder.setPosition(pos);
                            }
                        }
                        if (!success) {
                            resultingList.addAll(onError);
                            System.err.println("Error: no macro suits expression because of errors:");
                            exceptions.forEach(Throwable::printStackTrace);
                        }
                    } else resultingList.add(token);
                }
            } else resultingList.add(null);
        }

        //System.out.println(macros);

        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < resultingList.size(); i++) {
            LexerToken token = resultingList.get(i);
            if (i != 0) {
                LexerToken prevToken = resultingList.get(i - 1);
                if ((token != null && prevToken != null) && (
                        token.getRule() == null || prevToken.getRule() == null ||
                        (prevToken.getRule().getName().equals("OPERATOR") && token.getRule().getName().equals("OPERATOR")) ||
                        (prevToken.getRule().getName().equals("IDENTIFIER") && token.getRule().getName().equals("IDENTIFIER"))
                )) {
                    resultBuilder.append(" ");
                }
            }
            if (token != null) resultBuilder.append(token.getValue());
        }
        return resultBuilder.toString();
    }

    private static LexerToken lexerTokenFrom(Token token) {
        return new LexerToken(token.getValue(), token.getRule());
    }
}
