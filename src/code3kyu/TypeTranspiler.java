package code3kyu;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @auther Zhang Yubin
 * @date 2023/10/2 15:48
 */
public class TypeTranspiler {
    public TypeTranspiler(String s) {
        // Your code here!
    }

    public String transpile() {
        // Your code here too!
        return null;
    }
}

class TypeParser {

    private static final String FUNCTION_SIGNAL = "->";

    private static final String USER_TYPE_SIGNAL = "<";

    static String parseType(String type) {
        if (isFunctionType(type)) {
            return new FunctionType(type).parse();
        } else if (isUserType(type)) {
            return new UserType(type).parse();
        } else {
            return new Name(type).parse();
        }
    }

    static boolean isFunctionType(String type) {
        return type.contains(FUNCTION_SIGNAL);
    }

    static boolean isUserType(String type) {
        return type.contains(USER_TYPE_SIGNAL);
    }
}

interface Type {
    String parse();
}

class FunctionType implements Type {

    String functionName;

    Parameters parameters;

    public String getFunctionName() {
    }

    public FunctionType(String function) {
        if (function.contains(",")) {
            functionName = "function2";
        }
    }

    @Override
    public String parse() {
        return null;
    }
}

class Parameters implements Type {

    public Parameters(String parameter) {
        parameters = new LinkedList<>();
        String[] params = parameter.split(",");
        IntStream.range(1, params.length).forEach(i -> parameters.add(new Parameters(params[i])));
        type = params[0];
    }

    public List<Parameters> getParameters() {
        return parameters;
    }

    public String getType() {
        return type;
    }

    String type;

    List<Parameters> parameters;


    @Override
    public String parse() {
        return null;
    }
}

class UserType implements Type {

    private String userType;

    private Name name;


    public UserType(String userType) {
        this.userType = userType;
    }

    @Override
    public String parse() {
        return null;
    }
}

class TypeParam implements Type {
    String type;

    String extend;

    @Override
    public String parse() {
        if (type.equals("*")) {
            return "?";
        } else if (extend == null) {
            return TypeParser.parseType(type);
        } else {
            return String.format("? %s %s", extend.equals("in") ? "extends" : "super", TypeParser.parseType(type));
        }

    }

    public TypeParam(String type) {
        extend = type.contains("in") ? "in" : type.contains("out") ? "out" : null;
        this.type = extend == null ? type : type.split(" ")[1];
    }

}

class Name implements Type {

    static Map<String, String> keywordMap = new HashMap<>();

    static {
        keywordMap.put("Int", "Integer");
    }

    static String regex = "^[a-zA-Z_][a-zA-Z\\d_]*$";
    private String name;

    public Name(String name) {
        this.name = name;
    }

    @Override
    public String parse() {
        return Optional.ofNullable(keywordMap.get(name)).orElseGet(() -> {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(name);
            return matcher.matches() ? name : null;
        });
    }
}