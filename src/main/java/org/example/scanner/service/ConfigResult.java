package org.example.scanner.service;

public class ConfigResult {
    public String filePath;
    public String rootElement;
    public int elementCount;
    public int lineCount;
    public String encoding;
    public String xmlContent;

    @Override
    public String toString() {
        return "XmlResult{" +
                "filePath='" + filePath + '\'' +
                ", rootElement='" + rootElement + '\'' +
                ", elementCount=" + elementCount +
                ", lineCount=" + lineCount +
                ", encoding='" + encoding + '\'' +
                '}';
    }
}
