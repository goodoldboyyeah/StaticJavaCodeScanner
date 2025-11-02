package org.example.scanner.entity;

public class MethodResult {
    public String classPath;
    public String className;
    public String methodName;
    public String methodBody;
    public int methodStartLine;
    public int methodEndLine;

    // 新增：方法头 Javadoc（原样，含 /** ... */）与纯文本
    public String methodJavadoc;      // 原样
    public String methodJavadocText;  // 纯文本（无 /** */、无星号）

    @Override
    public String toString() {
        return "MethodResult{" +
                "classPath='" + classPath + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodStartLine=" + methodStartLine +
                ", methodEndLine=" + methodEndLine +
                ", hasJavadoc=" + (methodJavadoc != null && !methodJavadoc.trim().isEmpty()) +
                ", javadocPreview='" + preview(methodJavadocText) + "'" +
                '}';
    }

    private String preview(String s) {
        if (s == null) return "";
        String one = s.replaceAll("\\s+", " ").trim();
        return one.length() > 120 ? one.substring(0, 120) + " ..." : one;
    }
}
