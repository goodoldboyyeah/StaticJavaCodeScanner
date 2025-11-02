import lombok.extern.slf4j.Slf4j;
import org.example.scanner.entity.MethodResult;
import org.example.scanner.service.SearchAnnotation;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

@Slf4j
public class TestSearchAnnotationInDir {
    @Test
    public void test()  {
        SearchAnnotation ser = new SearchAnnotation();
        File file = new File("src/main/java");
        List<MethodResult> resultList = ser.searchAnnotationInDir(file,"Override");
        log.info("递归扫描所有Java文件注解结果："+resultList);
    }
}
