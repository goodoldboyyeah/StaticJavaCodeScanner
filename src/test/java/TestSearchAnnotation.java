import lombok.extern.slf4j.Slf4j;
import org.example.scanner.entity.MethodResult;
import org.example.scanner.service.SearchAnnotation;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

@Slf4j
public class TestSearchAnnotation {
    @Test
    public void test() throws FileNotFoundException {
        SearchAnnotation ser = new SearchAnnotation();
        File file = new File("D:\\A_document\\StaticCodeScan\\Static_Code_Scanning\\src\\main\\java\\org\\example\\Found.java");
        List<MethodResult> resultList = ser.searchAnnotation(file,"Deprecated");
        log.info("success");
    }
}
