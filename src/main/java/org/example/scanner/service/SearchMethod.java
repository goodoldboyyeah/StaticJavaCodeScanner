package org.example.scanner.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.example.scanner.entity.MethodResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SearchMethod {
    public List<MethodResult> searchMethodInFiles(File file) throws IOException {
        CompilationUnit cu = StaticJavaParser.parse(file);
        final String pkg = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");

        List<MethodResult> results = new ArrayList<>();
        cu.findAll(MethodDeclaration.class).forEach(m -> {
            MethodResult r = new MethodResult();
            r.classPath = file.getAbsolutePath();
            r.className = buildQualifiedClassName(m, pkg);
            r.methodName = m.getNameAsString();
            r.methodBody = m.getBody()
                    .map(b -> LexicalPreservingPrinter.print(b)) // 包含花括号
                    .orElse("");
            // 起止行号（若缺失则置为 -1）——兼容 Java 8/不同版本 JavaParser
            r.methodStartLine = m.getBegin().map(p -> p.line).orElse(-1);
            r.methodEndLine   = m.getEnd().map(p -> p.line).orElse(-1);

            results.add(r);
        });
        return results;
    }

    /**
     * 保留一个“打印”版（行为类似你原来的 void 方法）
     */
    public void searchMethodInFilesPrint(File file) throws IOException {
        searchMethodInFiles(file).forEach(System.out::println);
    }

    /**
     * 递归扫描目录下所有 .java 文件
     */
    public List<MethodResult> searchMethodsInDir(File rootDir) {
        List<MethodResult> all = new ArrayList<>();
        try {
            Files.walk(rootDir.toPath())
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        try {
                            all.addAll(searchMethodInFiles(p.toFile()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("扫描目录失败: " + rootDir, e);
        }
        return all;
    }

    /**
     * 计算“包名 + 外部类.内部类...”完整类名，例如 com.a.b.Outer.Inner
     */
    private String buildQualifiedClassName(MethodDeclaration m, String pkg) {
        List<String> classNames = new ArrayList<>();
        Node cur = m.getParentNode().orElse(null);
        while (cur != null) {
            if (cur instanceof ClassOrInterfaceDeclaration) {
                classNames.add(0, ((ClassOrInterfaceDeclaration) cur).getNameAsString());
            }
            cur = cur.getParentNode().orElse(null);
        }
        String simple = String.join(".", classNames);
        return pkg.isEmpty() ? simple : pkg + "." + simple;
    }
}
