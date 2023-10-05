package code3kyu;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @auther Zhang Yubin
 * @date 2023/10/2 15:48
 * @link <a href="https://www.codewars.com/kata/59a6949d398b5d6aec000007/solutions/java">...</a>
 */
public class TypeTranspiler {

    String code;

    public TypeTranspiler(String s) {
        System.out.println(s);
        code = s;
    }

    public String transpile() {
        try {
            return TypeParser.parseType(code.trim());
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static void main(String[] args) {
//        System.out.println(new TypeTranspiler("A.123").transpile());
//        System.out.println(new TypeTranspiler("*<A>").transpile());
//        System.out.println(new Parameters("A, B, C").parse());
//        System.out.println(new TypeTranspiler("A<in A>").transpile());
//        System.out.println(new TypeTranspiler("Array<out CSharp, out Java>").transpile());
//        System.out.println(new TypeTranspiler("A<").transpile());
//        System.out.println(new TypeTranspiler("ArrayList<out in>").transpile());
//        System.out.println(new TypeTranspiler("A<A<A>>").transpile());
//        System.out.println(new TypeTranspiler("Int.Compare").transpile());
//        System.out.println(new TypeTranspiler("A.").transpile());
//        System.out.println(new TypeTranspiler("A<A<A<A>,A<A<A>>>,A>").transpile());
//        System.out.println(new TypeTranspiler(" A < A > ").transpile());
//        System.out.println(new TypeTranspiler("(A) -> B").transpile());
//        System.out.println(new TypeTranspiler("(A, B) -> C").transpile());
//        System.out.println(new TypeTranspiler("((A) -> B, (B) -> C) -> (A) -> C").transpile());
//        System.out.println(new TypeTranspiler("() -> ()").transpile());
//        System.out.println(new TypeTranspiler("() -> A").transpile());
//        System.out.println(new TypeTranspiler("A  . A . B < C >").transpile());
//        System.out.println(new TypeTranspiler("A < B <in  > >").transpile());
    }
}

class TypeParser {

    private static final String FUNCTION_SIGNAL = "->";

    private static final String USER_TYPE_SIGNAL = "<";

    private static final String DOT = ".";

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
        return type.contains(USER_TYPE_SIGNAL) || type.contains(DOT);
    }
}

interface Type {
    String parse();
}

class FunctionType implements Type {

    String functionName;

    Parameters parameters;

    String type;

    public String getFunctionName() {
        return functionName;
    }

    public FunctionType(String function) {
        String[] func = Utils.splitFunction(function);
        String params = Utils.readParams(func[0], '(', ')');
        this.type = func[1].trim();
        parameters = new Parameters(params);

        if (function.contains(",")) {
            functionName = "Function" + params.split(",").length;
        } else if (params.isEmpty()) {
            functionName = "Function0";
        } else {
            functionName = "Function1";
        }

    }

    @Override
    public String parse() {
        String paramsValue = parameters.parse();
        if (paramsValue.isBlank()) {
            return String.format("%s<%s>", getFunctionName(), TypeParser.parseType(type));
        }
        return String.format("%s<%s,%s>", getFunctionName(), paramsValue, TypeParser.parseType(type));

    }
}

class Parameters implements Type {

    String type;

    List<Parameters> parameters;


    public Parameters(String parameter) {
        if (parameter.isBlank()) {
            type = "";
        } else {
            parameters = new LinkedList<>();
            String[] params = parameter.split(",");
            if (params.length > 1 && !params[1].isBlank()) {
                IntStream.range(1, params.length).forEach(i -> parameters.add(new Parameters(params[i].trim())));
            }
            type = params[0].trim();
        }
    }

    public List<Parameters> getParameters() {
        return parameters;
    }

    public String getType() {
        return type;
    }


    @Override
    public String parse() {
        if (type.isBlank()) {
            return "";
        } else if (parameters.isEmpty()) {
            return TypeParser.parseType(type);
        } else {
            return String.format("%s,%s", TypeParser.parseType(type), parameters.stream().map(Parameters::parse).collect(Collectors.joining(",")));
        }
    }
}


class UserType implements Type {

    String userType;

    public UserType(String userType) {
        this.userType = userType;
    }

    @Override
    public String parse() {
        if (userType.charAt(userType.length() - 1) == '.') {
            return null;
        }

        return Arrays.stream(userType.split("\\.")).map(s -> new SimpleUserType(s.trim()).parse().trim()).collect(Collectors.joining("."));
    }
}
class SimpleUserType implements Type {

    String userType;


    public SimpleUserType(String userType) {
        this.userType = userType;
    }

    @Override
    public String parse() {
        if (!userType.contains("<")) {
            return new Name(userType).parse();
        }

        String name = new Name(Utils.readName(userType)).parse();
        String typeParamValue = Arrays.stream(Utils.splitByComma(Utils.readParams(userType, '<', '>'))).map(s -> new TypeParam(s.trim()).parse()).collect(Collectors.joining(","));

        return String.format("%s<%s>", name, typeParamValue);
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
            return String.format("? %s %s", extend.equals("in") ? "super" : "extends", TypeParser.parseType(type));
        }

    }

    public TypeParam(String type) {
        if (TypeParser.isUserType(type)) {
            this.type = type;
        } else {
            String[] split = type.split(" ");
            if (split.length == 1) {
                this.type = type;
            } else {
                extend = split[0];
                this.type = split[1];
            }
        }
    }

}

class Name implements Type {

    static Map<String, String> keywordMap = new HashMap<>();

    static {
        keywordMap.put("Int", "Integer");
        keywordMap.put("Unit", "Void");
    }

    static String regex = "^[a-zA-Z_][a-zA-Z\\d_]*$";
    private String name;

    public Name(String name) {
        this.name = name;
    }

    @Override
    public String parse() {
        return Optional.ofNullable(Optional.ofNullable(keywordMap.get(name)).orElseGet(() -> {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(name);
            return matcher.matches() ? name : null;
        })).orElseThrow(RuntimeException::new);
    }

    }

class Utils {
    static String readName(String s) {
        return s.substring(0, s.indexOf("<")).trim();
    }

    static String[] splitFunction(String s) {
        String params = readParams(s, '(', ')');
        int idx = s.indexOf('-', params.length() + 2);
        return new String[]{s.substring(0, idx), s.substring(idx + 2)};
    }

    static String readParams(String s, char start, char end) {
        if (s.indexOf(start) == -1) {
            if (s.indexOf(end) == -1) {
                return "";
            } else {
                throw new RuntimeException();
            }
        }

        int indexStart = Integer.MAX_VALUE, indexEnd = 0;
        Stack<Character> stack = new Stack<>();

        for (int i = s.indexOf(start); i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == start) {
                stack.push(c);
                indexStart = Math.min(indexStart, i);
            } else if (c == end) {
                stack.pop();
                indexEnd = Math.max(indexEnd, i);
            }

            if (stack.empty()) {
                return s.substring(indexStart + 1, indexEnd).trim();
            }
        }

        throw new RuntimeException();
    }

    static String[] splitByComma(String s) {
        if (!s.contains("<")) {
            return s.split(",");
        }

        for (int i = s.length() - 1; i >= 0; i -= 1) {
            if (s.charAt(i) == ',') {
                return new String[]{s.substring(0, i), s.substring(i + 1)};
            }
        }

        return new String[]{s};
    }
}