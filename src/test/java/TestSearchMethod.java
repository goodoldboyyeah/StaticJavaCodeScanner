import lombok.extern.slf4j.Slf4j;
import org.example.scanner.entity.MethodResult;
import org.example.scanner.service.SearchMethod;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

@Slf4j
public class TestSearchMethod {

    @Test
    public void test() throws Exception {
        SearchMethod ser = new SearchMethod();
        File file = new File("D:\\A_document\\StaticCodeScan\\Static_Code_Scanning\\src\\main\\java\\org\\example\\scanner\\service\\SearchAnnotation.java");

        List<MethodResult> results = ser.searchMethodInFiles(file);
        log.info("扫描方法结果"+results);
    }

}
