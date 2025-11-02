package org.example.scanner.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.example.scanner.entity.MethodResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SearchAnnotation {

    public List<MethodResult> searchAnnotation(File file, String annotation) throws FileNotFoundException {
        CompilationUnit cu = StaticJavaParser.parse(file);
        // 需要先 setup 才能用 LexicalPreservingPrinter.print(...)
        LexicalPreservingPrinter.setup(cu);

        final String pkg = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");
        List<MethodResult> results = new ArrayList<>();

        // ===== 1) 方法上的注解 =====
        cu.findAll(MethodDeclaration.class).stream()
                .filter(m -> hasAnnotation(m, annotation))
                .forEach(m -> {
                    MethodResult r = new MethodResult();
                    r.classPath = file.getAbsolutePath();
                    r.className = buildQualifiedClassName(m, pkg);
                    r.methodName = m.getNameAsString();
                    r.methodBody = m.getBody().map(LexicalPreservingPrinter::print).orElse("");
                    r.methodStartLine = m.getBegin().map(p -> p.line).orElse(-1);
                    r.methodEndLine   = m.getEnd().map(p -> p.line).orElse(-1);
                    results.add(r);
                });

        // ===== 2) 字段上的注解 =====
        // 注意：一个 FieldDeclaration 可能声明多个变量：@A int a,b;
        cu.findAll(FieldDeclaration.class).stream()
                .filter(fd -> hasAnnotation(fd, annotation))
                .forEach(fd -> {
                    for (VariableDeclarator var : fd.getVariables()) {
                        MethodResult r = new MethodResult();
                        r.classPath = file.getAbsolutePath();
                        r.className = buildQualifiedClassName(fd, pkg);
                        // 复用 Result 的 methodName 字段来存放“字段名”
                        r.methodName = var.getNameAsString();
                        // 这里存整条字段声明（含注解、修饰符、类型与所有变量），如需只存该变量可改为 var.toString()
                        r.methodBody = LexicalPreservingPrinter.print(fd);
                        // 起止行号按整条字段声明
                        r.methodStartLine = fd.getBegin().map(p -> p.line).orElse(-1);
                        r.methodEndLine   = fd.getEnd().map(p -> p.line).orElse(-1);
                        results.add(r);
                    }
                });

        return results;
    }

    /** 递归扫描目录下所有 .java 文件 */
    public List<MethodResult> searchAnnotationInDir(File rootDir, String annotation) {
        List<MethodResult> all = new ArrayList<>();
        try {
            Files.walk(rootDir.toPath())
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        try {
                            all.addAll(searchAnnotation(p.toFile(), annotation));
                        } catch (FileNotFoundException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException("扫描目录失败: " + rootDir, e);
        }
        return all;
    }

    /** 兼容简单名与全限定名 */
    private boolean hasAnnotation(NodeWithAnnotations<?> n, String annotation) {
        return n.getAnnotations().stream().anyMatch(a -> {
            String name = a.getNameAsString();
            return name.equals(annotation) || name.endsWith("." + annotation);
        });
    }

    /** 计算“包名 + 外部类.内部类...”完整类名，例如 com.a.b.Outer.Inner */
    private String buildQualifiedClassName(Node node, String pkg) {
        List<String> classNames = new ArrayList<>();
        Node cur = node.getParentNode().orElse(null);
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
