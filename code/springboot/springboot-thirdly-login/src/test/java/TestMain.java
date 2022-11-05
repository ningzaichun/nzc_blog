import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年08月28日 12:58
 */
public class TestMain {

    public static void main(String[] args) {
        String words = " https://gitee.com/oauth/authorize?client_id={client_id}&redirect_uri={redirect_uri}&response_type=code";
        System.out.println("原始字符串是'" + words + "'");
        System.out.println("replace(\"l\",\"D\")结果：" + words.replace("{client_id}", "http://127.0.0.1:8089/oauth/gitee/callback"));
        words=words.replace("{client_id}", "http://127.0.0.1:8089/oauth/gitee/callback");
        System.out.println("原始字符串是'" + words + "'");
        System.out.println("replace(\"l\",\"D\")结果：" + words.replace("{client_id}", "http://127.0.0.1:8089/oauth/gitee/callback"));
//        System.out.println("replace(\"hello\",\"你好\")结果：" + words.replace("hello", "你好 "));
    }
}
