package code2kyu;

/**
 * @auther Zhang Yubin
 * @date 2023/9/23 21:56
 */
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WhitespaceInterpreterTest {

    @Test
    public void testPush() {
        System.out.println("Testing push, output of numbers 0 through 3");
        String[][] tests = {
                {"   \t\n\t\n \t\n\n\n", "1"},
                {"   \t \n\t\n \t\n\n\n", "2"},
                {"   \t\t\n\t\n \t\n\n\n", "3"},
                {"    \n\t\n \t\n\n\n", "0"}
        };
        for (String[] test : tests) {
            assertEquals(test[1], WhitespaceInterpreter.execute(test[0], null));
        }
    }

    @Test
    public void testOutNumbers() {
        System.out.println("Testing ouput of numbers -1 through -3");
        String[][] tests = {
                {"  \t\t\n\t\n \t\n\n\n", "-1"},
                {"  \t\t \n\t\n \t\n\n\n", "-2"},
                {"  \t\t\t\n\t\n \t\n\n\n", "-3"},
        };
        for (String[] test : tests) {
            assertEquals(test[1], WhitespaceInterpreter.execute(test[0], null));
        }
    }

    @Test(expected = Exception.class)
    public void testFlowEdge() {
        System.out.println("Testing simple flow control edge case");
        WhitespaceInterpreter.execute("", null);
    }

    @Test
    public void testOutLetters() {
        System.out.println("Testing output of letters A through C");
        String[][] tests = {
                {"   \t     \t\n\t\n  \n\n\n", "A"},
                {"   \t    \t \n\t\n  \n\n\n", "B"},
                {"   \t    \t\t\n\t\n  \n\n\n", "C"},
        };
        for (String[] test : tests) {
            assertEquals(test[1], WhitespaceInterpreter.execute(test[0], null));
        }
    }

    @Test
    public void testOutLettersWithComments() {
        System.out.println("Testing output of letters A through C with comments");
        String[][] tests = {
                {"blahhhh   \targgggghhh     \t\n\t\n  \n\n\n", "A"},
                {" I heart \t  cats  \t \n\t\n  \n\n\n", "B"},
                {"   \t  welcome  \t\t\n\t\n to the\nnew\nworld\n", "C"},
        };
        for (String[] test : tests) {
            assertEquals(test[1], WhitespaceInterpreter.execute(test[0], null));
        }
    }

    @Test
    public void testStack() {
        System.out.println("Testing stack functionality");
        String[][] tests = {
                {"   \t\t\n   \t\t\n\t\n \t\t\n \t\n\n\n", "33"},
                {"   \t\t\n \n \t\n \t\t\n \t\n\n\n", "33"},
                {"   \t\n   \t \n   \t\t\n \t  \t \n\t\n \t\n\n\n", "1"},
                {"   \t\n   \t \n   \t\t\n \t  \t\n\t\n \t\n\n\n", "2"},
                {"   \t\n   \t \n   \t\t\n \t   \n\t\n \t\n\n\n", "3"},
                {"   \t\t\n   \t \n \n\t\t\n \t\t\n \t\n\n\n", "32"},
                {"   \t\t\n   \t \n \n\t \n\n\t\n \t\n\n\n", "2"},
                {"   \t\t\n   \t \n   \t\n   \t  \n   \t\t \n   \t \t\n   \t\t\t\n \n\t \t\n \t\t\n\t\n \t\t\n \t\t\n \t\t\n \t\n\n\n", "5123"},
        };
        for (String[] test : tests) {
            assertEquals(test[1], WhitespaceInterpreter.execute(test[0], null));
        }
    }
}