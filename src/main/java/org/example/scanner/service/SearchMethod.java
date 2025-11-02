package org.example.scanner.service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.example.scanner.entity.MethodResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class SearchMethod {

    public List<MethodResult> searchMethodInFiles(File file) throws IOException {
        // 让注释尽量“归属”到节点，允许前面有空行也归属
        ParserConfiguration cfg = new ParserConfiguration()
                .setAttributeComments(true)
                .setDoNotAssignCommentsPrecedingEmptyLines(false);
        JavaParser parser = new JavaParser(cfg);

        CompilationUnit cu = parser.parse(file).getResult()
                .orElseThrow(() -> new IOException("Parse failed: " + file));
        LexicalPreservingPrinter.setup(cu); // 保留原始格式用于 print()

        final String pkg = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");

        List<MethodResult> results = new ArrayList<>();
        cu.findAll(MethodDeclaration.class).forEach(m -> {
            MethodResult r = new MethodResult();
            r.classPath = file.getAbsolutePath();
            r.className = buildQualifiedClassName(m, pkg);
            r.methodName = m.getNameAsString();

            r.methodBody = m.getBody().map(LexicalPreservingPrinter::print).orElse("");
            r.methodStartLine = m.getBegin().map(p -> p.line).orElse(-1);
            r.methodEndLine   = m.getEnd().map(p -> p.line).orElse(-1);

            // 取方法头 Javadoc（优先已附着；否则在方法前邻近处兜底），允许 1 行空行间隔
            Optional<JavadocComment> jcOpt = findMethodJavadoc(cu, m, 1);

            // 原样（含 /** ... */）
            r.methodJavadoc = jcOpt.map(LexicalPreservingPrinter::print).orElse("");
            // 纯文本（去 /** */ 与星号）
            r.methodJavadocText = jcOpt.map(jc -> jc.parse().toText()).orElse("");

            results.add(r);
        });
        return results;
    }

    public void searchMethodInFilesPrint(File file) throws IOException {
        searchMethodInFiles(file).forEach(r -> {
            System.out.println(r);
            if (r.methodJavadoc != null && !r.methodJavadoc.trim().isEmpty()) {
                System.out.println("----- Javadoc (raw) -----");
                System.out.println(r.methodJavadoc);
                System.out.println("----- Javadoc (text) ----");
                System.out.println(r.methodJavadocText);
            }
        });
    }

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

    // —— 工具方法 ——

    /** 找“方法头 Javadoc”。未附着到节点时，向前找最近的 Javadoc，允许 allowedGapLines 行空行。 */
    private Optional<JavadocComment> findMethodJavadoc(CompilationUnit cu, MethodDeclaration m, int allowedGapLines) {
        if (m.getJavadocComment().isPresent()) return m.getJavadocComment();

        Optional<Comment> any = m.getComment();
        if (any.isPresent() && any.get() instanceof JavadocComment) {
            return Optional.of((JavadocComment) any.get());
        }

        int start = m.getBegin().map(p -> p.line).orElse(Integer.MAX_VALUE);
        return cu.getAllContainedComments().stream()
                .filter(c -> c instanceof JavadocComment && c.getEnd().isPresent())
                .sorted(Comparator.comparingInt(c -> -c.getEnd().get().line)) // 越靠近方法越先
                .map(c -> (JavadocComment) c)
                .filter(jc -> {
                    int end = jc.getEnd().get().line;
                    return end < start && (start - end) <= (allowedGapLines + 1);
                })
                .findFirst();
    }

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
