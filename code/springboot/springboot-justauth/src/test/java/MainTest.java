/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年08月28日 12:57
 */
public class MainTest {

    public static void main(String[] args) {
        String words = "hello java,hello php";
        System.out.println("原始字符串是'"+words+"'");
        System.out.println("replace(\"l\",\"D\")结果："+words.replace("l","D"));
        System.out.println("replace(\"hello\",\"你好\")结果："+words.replace("hello","你好 "));
    }
}
