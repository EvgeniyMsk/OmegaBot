package ou;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.regex.Pattern;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
public class OmegaTest {

    @Test
    public void test() {
        String REGEXP_PERSON = "^[А-ЯЁ][а-яё]*([-][А-ЯЁ][а-яё]*)?\\s[А-ЯЁ][а-яё]*\\s[А-ЯЁ][а-яё]*\\s\\d{2}(?<!00).\\d{2}(?<!00).\\d{4}(?<!0000)$";
        System.out.println("Логвинов Евгений Вячеславович 12.01.1993".matches(REGEXP_PERSON));
    }
}
