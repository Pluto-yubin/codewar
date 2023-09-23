package code2kyu;

/**
 * @auther Zhang Yubin
 * @date 2023/9/12 23:32
 */
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

public class WhitespaceInterpreter {

    private static final char SPACE = 's';

    private static final char TAB = 't';

    private static final char LINE_FEED = 'n';

    private static Stack<Integer> stack;

    private static String result = "";

    private static final String ERROR = "error";

    private static Map<Integer, Integer> heap;

    private static Map<Integer, String> labels;

    private static BufferedReader reader;

    private static InputStream inputStream;

    private static OutputStream outputStream;

    private static String callerCode;

    // transforms space characters to ['s','t','n'] chars;
    public static String unbleach(String code) {
        return code != null ? code.replace(' ', 's').replace('\t', 't').replace('\n', 'n') : null;
    }

    public static void main(String[] args) {
//        System.out.println(parseBinaryStringToNumber("1101"));
//        System.out.println(execute("   \t\n\t\n \t\n\n\n", null));
//        System.out.println(execute("   \t\t\n\t\n \t\n\n\n", null));
//        System.out.println(execute("    \n\t\n \t\n\n\n", null)); // 0
//        System.out.println(execute("blahhhh   \targgggghhh     \t\n\t\n  \n\n\n", null)); // A
        System.out.println(execute("   \t\t\n   \t \n \n\t\t\n \t\t\n \t\n\n\n", null));
    }

    public static String execute(String code, InputStream input, OutputStream output) {
        outputStream = output;
        return execute(code, input);
    }

    /**
     * [space]: Stack Manipulation
     * [tab][space]: Arithmetic
     * [tab][tab]: Heap Access
     * [tab][line-feed]: Input/Output
     * [line-feed]: Flow Control
     * @param code
     * @param input
     * @return
     */
    public static String execute(String code, InputStream input) {
        if (code.length() == 0) {
            throw new RuntimeException();
        }

        code = unbleach(code.replaceAll("[^ \t\n]", ""));
        Map<String, Function<String, String>> IMPmap = new HashMap<>();
        IMPmap.put("s", WhitespaceInterpreter::manipulateStack);
        IMPmap.put("n",WhitespaceInterpreter::flowControl);
        IMPmap.put("ts", WhitespaceInterpreter::arithmetic);
        IMPmap.put("tn", WhitespaceInterpreter::streamControl);
        IMPmap.put("tt", WhitespaceInterpreter::interpretHeap);

        stack = new Stack<>();
        heap = new HashMap<>();
        inputStream = input;
        if (input != null) {
            reader = new BufferedReader(new InputStreamReader(input));
        }
        while (code.length() != 0) {
            if (code.charAt(0) == TAB) {
                code = IMPmap.get(code.substring(0, 2)).apply(code.substring(2));
            } else {
                code = IMPmap.get(code.substring(0, 1)).apply(code.substring(1));
            }

            if (ERROR.equals(code)) {
                throw new RuntimeException();
            }
        }

        String temp = result;
        result = "";
        return temp;
    }

    /**
     * [space][space]: Pop a and b, then push b+a.
     * [space][tab]: Pop a and b, then push b-a.
     * [space][line-feed]: Pop a and b, then push b*a.
     * [tab][space]: Pop a and b, then push b/a*. If a is zero, throw an error.
     * *Note that the result is defined as the floor of the quotient.
     * [tab][tab]: Pop a and b, then push b%a*. If a is zero, throw an error.
     * *Note that the result is defined as the remainder after division and sign (+/-) of the divisor (a).
     * @param code
     * @return
     */
    private static String arithmetic(String code) {
        Integer a = stack.pop();
        Integer b = stack.pop();
        String instruct = code.substring(0, 2);
        switch (instruct) {
            case "ss":
                stack.push(a + b);
                break;
            case "st":
                stack.push(b - a);
                break;
            case "sn":
                stack.push(a * b);
                break;
            case "ts":
                stack.push(b / a);
                break;
            case "tt":
                stack.push(b % a);
                break;
        }

        return code.substring(2);
    }

    /**
     * [space][space] (label): Mark a location in the program with label n.
     * [space][tab] (label): Call a subroutine with the location specified by label n.
     * [space][line-feed] (label): Jump unconditionally to the position specified by label n.
     * [tab][space] (label): Pop a value off the stack and jump to the label specified by n if the value is zero.
     * [tab][tab] (label): Pop a value off the stack and jump to the label specified by n if the value is less than zero.
     * [tab][line-feed]: Exit a subroutine and return control to the location from which the subroutine was called.
     * [line-feed][line-feed]: Exit the program.
     * @param code
     * @return
     */
    private static String flowControl(String code) {
        String instruct = code.substring(0, 2);
        code = code.substring(2);
        switch (instruct) {
            case "ss":
                int label = parseLabel(code);
                code = code.substring(0, code.indexOf(LINE_FEED));
                labels.put(label, code);
                break;
            case "st":
                label = parseLabel(code);
                String subRoutine = labels.get(label);
                callerCode = code.substring(0, code.indexOf(LINE_FEED));
                execute(subRoutine, inputStream);
                break;
            case "sn":
                label = parseLabel(code);
                execute(labels.get(label), inputStream);
                break;
            case "ts":
                label = parseLabel(code);
                code = code.substring(0, code.indexOf(LINE_FEED));
                if (stack.pop() == 0) {
                    execute(labels.get(label), inputStream);
                    return "";
                }
                break;
            case "tt":
                label = parseLabel(code);
                code = code.substring(0, code.indexOf(LINE_FEED));
                if (stack.pop() <= 0) {
                    execute(labels.get(label), inputStream);
                    return "";
                }
                break;
            case "tn":
                execute(callerCode, inputStream);
                return "";
            case "nn":
                return "";
        }

        return code;

    }

    /**
     * [space][space]: Pop a value off the stack and output it as a character.
     * [space][tab]: Pop a value off the stack and output it as a number.
     * [tab][space]: Read a character from input, a, Pop a value off the stack, b, then store the ASCII value of a at heap address b.
     * [tab][tab]: Read a number from input, a, Pop a value off the stack, b, then store a at heap address b.
     * @param code
     * @return
     */
    private static String streamControl(String code) {
        String instruct = code.substring(0, 2);
        try {
            switch (instruct) {
                case "ss":
                    char val = (char) stack.pop().intValue();
                    result += val;
                    if (outputStream != null) {
                        outputStream.write(stack.peek());
                    }
                    break;
                case "st":
                    if (outputStream != null) {
                        outputStream.write(stack.peek());
                    }
                    result += stack.pop();
                    break;
                case "ts":
                    int a = reader.read();
                    Integer b = stack.pop();
                    heap.put(b, a);
                    break;
                case "tt":
                    a = Integer.parseInt(reader.readLine());
                    b = stack.pop();
                    heap.put(b, a);
                    break;
            }

        } catch (Throwable throwable) {
            return ERROR;
        }

        return code.substring(2);

    }

    /**
     * [space]: Pop a and b, then store a at heap address b.
     * [tab]: Pop a and then push the value at heap address a onto the stack.
     * @param code
     * @return
     */
    private static String interpretHeap(String code) {
        if (code.charAt(0) == SPACE) {
            Integer a = stack.pop();
            Integer b = stack.pop();
            heap.put(b, a);
        } else if (code.charAt(0) == TAB) {
            stack.push(heap.get(stack.pop()));
        }
        return code.substring(1);
    }

    /**
     * Numbers begin with a [sign] symbol. The sign symbol is either [tab] -> negative, or [space] -> positive.
     * Numbers end with a [terminal] symbol: [line-feed].
     * Between the sign symbol and the terminal symbol are binary digits [space] -> binary-0, or [tab] -> binary-1.
     * A number expression [sign][terminal] will be treated as zero.
     * The expression of just [terminal] should throw an error. (The Haskell implementation is inconsistent about this.)
     * example: {"  \t\t \n\t\n \t\n\n\n", "-2"},
     * @param code
     * @return
     */
    private static Integer parseNumber(String code) {
        if (LINE_FEED == code.charAt(0)) {
            throw new RuntimeException();
        }

        boolean isPositive = code.charAt(0) == SPACE;

        StringBuilder binaryCode = new StringBuilder();
        for (int i = 1; i < code.toCharArray().length; i++) {
            char c = code.charAt(i);
            if (LINE_FEED == c) {
                int result = parseBinaryStringToNumber(binaryCode.toString());
                return isPositive ? result : -1 * result;
            }

            binaryCode.append(SPACE == c ? '0' : '1');
        }

        return 0;
    }

    private static int parseBinaryStringToNumber(String code) {
        int index = code.indexOf('1');
        if (index < 0) {
            return 0;
        }

        code = code.substring(index);
        int result = 0;
        for (int i = 0; i < code.toCharArray().length; i++) {
            result += (code.charAt(i) - '0') * Math.pow(2, code.length() - i - 1);
        }

        Integer.parseInt("1", 2);
        return result;
    }


    /**
     * Labels begin with any number of [tab] and [space] characters.
     *
     * Labels end with a terminal symbol: [line-feed].
     * Unlike with numbers, the expression of just [terminal] is valid.
     * Labels must be unique.
     * A label may be declared either before or after a command that refers to it
     * @param code
     * @return
     */
    private static int parseLabel(String code) {
        int index = code.indexOf(LINE_FEED);
        return parseBinaryStringToNumber(code.substring(0, index));
    }

    /**
     * [space] (number): Push n onto the stack.
     * [tab][space] (number): Duplicate the nth value from the top of the stack and push onto the stack.
     * [tab][line-feed] (number): Discard the top n values below the top of the stack from the stack. (For n<**0** or **n**>=stack.length, remove everything but the top value.)
     * [line-feed][space]: Duplicate the top value on the stack.
     * [line-feed][tab]: Swap the top two value on the stack.
     * [line-feed][line-feed]: Discard the top value on the stack.
     * @param code
     */
    private static String manipulateStack(String code) {
        char param1 = code.charAt(0);
        char param2 = code.charAt(1);

        try {
            Integer peek = stack.empty() ? 0 : stack.peek();
            if (param1 == SPACE) {
                stack.push(parseNumber(code.substring(1)));
            } else if (param1 == TAB) {
                if (param2 == SPACE) {
                    int n = parseNumber(code.substring(2));
                    stack.push(stack.get(stack.size() - n - 1));
                } else if (param2 == LINE_FEED) {
                    int n = parseNumber(code.substring(2));
                    n = n < 0 || n >= stack.size() ? stack.size() : n;
                    while (n-- >= 0) {
                        stack.pop();
                    }
                    stack.push(peek);
                }
            } else if (param1 == LINE_FEED) {
                switch (param2) {
                    case SPACE:
                        stack.push(peek);
                        break;
                    case TAB:
                        Integer v1 = stack.pop();
                        Integer v2 = stack.pop();
                        stack.push(v1);
                        stack.push(v2);
                        break;
                    case LINE_FEED:
                        stack.pop();
                        break;
                }
                return code.substring(2);
            }
        } catch (Throwable throwable) {
            return ERROR;
        }

        return code.substring(code.indexOf('n') + 1);

    }

}
