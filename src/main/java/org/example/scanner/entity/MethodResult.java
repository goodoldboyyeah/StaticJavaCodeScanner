package org.example.scanner.entity;

public class MethodResult {

    public String classPath;
    /** 完整类名（含包名与内部类链，如 com.a.b.Outer.Inner） */
    public String className;
    /** 方法名（不含签名） */
    public String methodName;

    public String methodBody;

    public String getMethodBody() {
        return methodBody;
    }

    public void setMethodBody(String methodBody) {
        this.methodBody = methodBody;
    }

    /** 方法开始/结束行 */
    public int methodStartLine;
    public int methodEndLine;

    @Override
    public String toString() {
        return "Result{" +
                "classPath='" + classPath + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodBody='" + methodBody + '\'' +
                ", methodStartLine=" + methodStartLine +
                ", methodEndLine=" + methodEndLine +
                '}';
    }
}
