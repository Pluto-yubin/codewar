package code3kyu;

import java.util.Hashtable;
import java.lang.StringBuilder;
import java.util.Optional;

class Attribute {

    public static final int
            IDENTIFIER  = 256,
            LAMBDAOP    = 257;

}

class Token {

    protected int tag;

    public Token(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return this.tag;
    }

}

class WordToken extends Token {

    private String lexeme;

    public WordToken(String lexeme) {
        super(Attribute.IDENTIFIER);
        this.lexeme = lexeme;
    }

    public String getLexeme() {
        return this.lexeme;
    }

}

class Tokenizer {

    private final String input;

    private int position = 0;

    private final Hashtable<String, WordToken> keywordTable;

    public Tokenizer(String input) throws IllegalArgumentException {
        if (input == null) throw new IllegalArgumentException();
        this.input = input;
        keywordTable = new Hashtable<>();
        reserveKeyword(new WordToken("in"));
        reserveKeyword(new WordToken("out"));
    }

    private void reserveKeyword(WordToken token) {
        this.keywordTable.put(token.getLexeme(), token);
    }

    private boolean isNotEof() {
        return input.length() != position;
    }

    private boolean isEof() {
        return !isNotEof();
    }

    private void SkipWhiteSpaces() {
        while (isNotEof() && Character.isWhitespace(input.charAt(position))) {
            position++;
        }
    }

    private WordToken readIdentifier() {
        StringBuilder buffer = new StringBuilder();

        while (isNotEof() && (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
            buffer.append(input.charAt(position));
            position++;
        }

        return Optional.ofNullable(keywordTable.get(buffer.toString())).orElse(new WordToken(buffer.toString()));
//        WordToken token = this.keywordTable.get(buffer.toString());
//        if (token != null) return token;
//        return new WordToken(buffer.toString());
    }

    private Token readLambdaOp() throws IllegalArgumentException {
        position++;
        if (isNotEof() && input.charAt(position) == '>') {
            position++;
            return new Token(Attribute.LAMBDAOP);
        }
        throw new IllegalArgumentException();
    }

    public Token nextToken() {
        SkipWhiteSpaces();
        if (isEof()) return null;
        if (Character.isLetter(input.charAt(position)) || input.charAt(position) == '_') return readIdentifier();
        if (input.charAt(position) == '-') return readLambdaOp();
        if (isNotEof()) {
            char symbol = input.charAt(position);
            position++;
            return new Token(symbol);
        }
        return null;
    }
}


public class TypeTranspilerBestPractice {

    public static void main(String[] args) {
        System.out.println(new TypeTranspilerBestPractice("A<A<A<A>,A<A<A>>>,A>").transpile());
    }

    private final Tokenizer tokenizer;

    private Token currentToken;

    public TypeTranspilerBestPractice(String s) {
        this.tokenizer = new Tokenizer(s);
        this.currentToken = tokenizer.nextToken();
    }

    private void match(int tag) throws UnsupportedOperationException, IllegalArgumentException {
        if (currentToken == null) throw new UnsupportedOperationException();

        if (currentToken.getTag() == tag) {
            currentToken = tokenizer.nextToken();
            return;
        }
        throw new IllegalArgumentException();
    }

    public String transpile() {
        try {
            String transpiled = type();
            if (currentToken != null) return null;
            return transpiled;
        }
        catch (Exception ex) {
            return null;
        }
    }

    private String type() {
        if (currentToken.getTag() == '(') return functionType();
        else return userType();
    }

    private String functionType() {
        StringBuilder functionType = new StringBuilder();

        match('(');
        functionType.append(parameters());
        match(')');
        match(Attribute.LAMBDAOP);
        functionType.append(type()).append('>');
        return functionType.toString();
    }

    private String parameters() {
        if (currentToken.getTag() == ')') return "Function0" + "<";

        int paramsCount = 1;
        StringBuilder params = new StringBuilder();
        params.append(type());
        if (currentToken.getTag() == ',') {
            while (currentToken.getTag() == ',') {
                match(',');
                paramsCount++;
                params.append(',').append(type());
            }
        }
        return "Function" + paramsCount + "<" + params + ",";
    }

    private String userType() {
        if (currentToken.getTag() != Attribute.IDENTIFIER) {
            throw new IllegalArgumentException();
        }

        StringBuilder userType = new StringBuilder();

        userType.append(simpleUserType());
        if (currentToken != null && currentToken.getTag() == '.') {
            match('.');
            userType.append('.').append(userType());
        }
        return userType.toString();
    }

    private String simpleUserType() {
        StringBuilder simpleUserType = new StringBuilder();

        simpleUserType.append(name());
        if (currentToken != null && currentToken.getTag() == '<') {
            match('<');
            simpleUserType.append('<').append(typeParams()).append('>');
            match('>');
        }
        return simpleUserType.toString();
    }

    private String name() {
        if (currentToken.getTag() == Attribute.IDENTIFIER) {
            String name = ((WordToken)currentToken).getLexeme();
            match(Attribute.IDENTIFIER);
            return name.equals("Int") ? "Integer" : name.equals("Unit") ? "Void" : name;
        }
        throw new IllegalArgumentException();
    }

    private String typeParams() {
        StringBuilder params = new StringBuilder();
        params.append(typeParam());

        if (currentToken != null && currentToken.getTag() == ',') {
            match(',');
            params.append(',').append(typeParams());
        }
        return params.toString();
    }

    private String typeParam() {
        if (currentToken.getTag() == '*') {
            match('*');
            return "?";
        }

        if (currentToken.getTag() == Attribute.IDENTIFIER) {
            String word = ((WordToken)currentToken).getLexeme();

            if (word.equals("in") || word.equals("out")) {
                match(Attribute.IDENTIFIER);
                if (currentToken.getTag() != Attribute.IDENTIFIER) {
                    return word;
                }
                return word.equals("in") ? "? super " + type() : "? extends " + type();
            }
        }
        return type();
    }

}