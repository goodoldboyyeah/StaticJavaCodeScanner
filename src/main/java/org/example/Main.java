package org.example;

import org.example.scanner.entity.MethodResult;
import org.example.scanner.service.SearchAnnotation;

import java.io.File;
import java.util.List;

public final class Main {

    public static void main(String[] args) throws Exception {
        SearchAnnotation ser = new SearchAnnotation();
        File file = new File("src/main/java");
        List<MethodResult> resultList = ser.searchAnnotationInDir(file,"Override");

    }
}
