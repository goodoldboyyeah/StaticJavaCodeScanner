import lombok.extern.slf4j.Slf4j;
import org.example.scanner.entity.MethodResult;
import org.example.scanner.service.SearchMethod;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

@Slf4j
public class TestSearchMethodInDir {

    @Test
    public void test()  {
        SearchMethod ser = new SearchMethod();
        File file = new File("src/main/java");

        List<MethodResult> results = ser.searchMethodsInDir(file);
        log.info("扫描方法结果"+results);
    }

}
