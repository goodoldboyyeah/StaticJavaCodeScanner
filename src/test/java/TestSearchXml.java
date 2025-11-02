import lombok.extern.slf4j.Slf4j;
import org.example.scanner.service.ConfigResult;
import org.example.scanner.entity.SearchProperties;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

@Slf4j
public class TestSearchXml {
    @Test
    public void test() throws Exception {
        SearchProperties searchXMl = new SearchProperties();
//        File file = new File("D:\\A_document\\Java\\warehouse-master\\warehouse-master");
        List<ConfigResult> results = searchXMl.scanConfig(new File("D:\\A_document\\Java\\warehouse-master\\warehouse-master"));
//        searchXMl.scanConfigFile(file);
        results.forEach(System.out::println);
        log.info("success");
    }
}
