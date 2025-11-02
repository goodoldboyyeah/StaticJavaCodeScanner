package org.example.scanner.service;

import org.example.scanner.entity.ConfigResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.*;

public class SearchProperties {
    // 统一入口：既支持传目录也支持传单个文件
    public List<ConfigResult> scanConfig(File path) {
        if (path == null) throw new IllegalArgumentException("path == null");
        if (path.isDirectory()) {
            return scanConfigInDir(path);
        } else {
            // 单文件：按后缀读取并封装为单元素列表
            try {
                ConfigResult r = scanConfigFile(path);
                List<ConfigResult> one = new ArrayList<>(1);
                one.add(r);
                return one;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

//    /** 扫描目录下的 .properties/.yaml/.yml 文件并返回结果列表（使用 XmlResult） */
//    public List<ConfigResult> scanConfigInDir(File rootDir) {
//        List<ConfigResult> list = new ArrayList<>();
//        try {
//            final Path root = rootDir.toPath();
//            walk(root)
//                    .filter(Files::isRegularFile)
//                    .filter(Files::isReadable)
//                    .filter(p -> {
//                        String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
//                        return name.endsWith(".properties") || name.endsWith(".yaml") || name.endsWith(".yml");
//                    })
//                    .forEach(p -> {
//                        try {
//                            list.add(scanConfigFile(p.toFile()));
//                        } catch (Exception e) {
//                            System.err.println("跳过文件（读取失败）: " + p + " -> " + e.getMessage());
//                        }
//                    });
//        } catch (IOException e) {
//            throw new RuntimeException("扫描目录失败: " + rootDir, e);
//        }
//        return list;
//    }

    /** 扫描目录下的 .properties/.yaml/.yml 文件并返回结果列表 */
    public List<ConfigResult> scanConfigInDir(File rootDir) {
        List<ConfigResult> list = new ArrayList<>();
        try {
            final Path root = rootDir.toPath();
            Files.walk(root)
                    .filter(Files::isRegularFile)    // 只要常规文件
                    .filter(Files::isReadable)       // 可读
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
                        return name.endsWith(".properties") || name.endsWith(".yaml") || name.endsWith(".yml");
                    })
                    .forEach(p -> {
                        try {
                            list.add(scanConfigFile(p.toFile()));
                        } catch (Exception e) {
                            System.err.println("跳过文件（读取失败）: " + p + " -> " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("扫描目录失败: " + rootDir, e);
        }
        return list;
    }



//    /**
//     * 根据后缀分发
//     */
//    public ConfigResult scanConfigFile(File file) throws IOException {
//        String name = file.getName().toLowerCase(Locale.ROOT);
//        if (name.endsWith(".properties")) {
//            return scanPropertiesFile(file);
//        } else { // .yaml / .yml
//            return scanYamlFile(file);
//        }
//    }

    /** 扫描单个配置文件（properties/yaml/yml） */
    public ConfigResult scanConfigFile(File file) throws IOException {
        Path p = file.toPath();
        if (!Files.isRegularFile(p)) {
            throw new IllegalArgumentException("scanConfigFile 仅接受文件，传入的是目录或非常规路径: " + file.getAbsolutePath());
        }
        String name = file.getName().toLowerCase(java.util.Locale.ROOT);
        if (name.endsWith(".properties")) {
            return scanPropertiesFile(file);
        } else if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            return scanYamlFile(file);
        } else {
            throw new IllegalArgumentException("不支持的文件类型: " + file.getAbsolutePath());
        }
    }
//    /** 读取 .properties（先 UTF-8，失败回退 ISO-8859-1） */
//    private ConfigResult scanPropertiesFile(File f) throws IOException {
//        ConfigResult r = new ConfigResult();
//        r.filePath = f.getAbsolutePath();
//        byte[] bytes = readAllBytes(f.toPath());
//        String text;
//        String encoding = "UTF-8";
//        text = new String(bytes, StandardCharsets.UTF_8);
//        r.encoding = encoding;
//        r.xmlContent = text;            // ← 原内容放这里
//        r.lineCount = countLines(text);
//        r.rootElement = "properties";   // ← 用来标识类型
//        r.elementCount = 0;             // 非 XML，置 0
//        return r;
//    }

    // 读取 .properties（先 UTF-8，失败回退 ISO-8859-1）
    private ConfigResult scanPropertiesFile(File f) throws IOException {
        ConfigResult r = new ConfigResult();
        r.filePath = f.getAbsolutePath();
        byte[] bytes = Files.readAllBytes(f.toPath());
        String text;
        String encoding = "UTF-8";
        text = new String(bytes, StandardCharsets.UTF_8);
        r.encoding = encoding;
        r.xmlContent = text;          // 原始内容
        r.lineCount = countLines(text);
        r.rootElement = "properties"; // 用作类型标识
        r.elementCount = 0;
        return r;
    }
//
//    /** 读取 .yaml/.yml（UTF-8） */
//    private ConfigResult scanYamlFile(File f) throws IOException {
//        ConfigResult r = new ConfigResult();
//        r.filePath = f.getAbsolutePath();
//        String text = new String(readAllBytes(f.toPath()), StandardCharsets.UTF_8);
//        r.encoding = "UTF-8";
//        r.xmlContent = text;          // ← 原内容放这里
//        r.lineCount = countLines(text);
//        r.rootElement = "yaml";       // ← 用来标识类型
//        r.elementCount = 0;           // 非 XML，置 0
//        return r;
//    }
// 读取 .yaml/.yml（UTF-8）
private ConfigResult scanYamlFile(File f) throws IOException {
    ConfigResult r = new ConfigResult();
    r.filePath = f.getAbsolutePath();
    String text = new String(Files.readAllBytes(f.toPath()), java.nio.charset.StandardCharsets.UTF_8);
    r.encoding = "UTF-8";
    r.xmlContent = text;        // 原始内容
    r.lineCount = countLines(text);
    r.rootElement = "yaml";     // 用作类型标识
    r.elementCount = 0;
    return r;
}

    /** 保存原始内容到 outDir（保留相对路径结构） */
    public void saveContentsToDir(List<ConfigResult> results, File rootDir, File outDir) {
        Path root = rootDir.toPath().toAbsolutePath().normalize();
        Path out = outDir.toPath().toAbsolutePath().normalize();
        results.forEach(r -> {
            try {
                Path src = Paths.get(r.filePath).toAbsolutePath().normalize();
                Path rel = root.relativize(src);
                Path dst = out.resolve(rel);
                createDirectories(dst.getParent());
                write(dst, r.xmlContent.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    /** 保存为 JSONL（每行一个 JSON 对象） */
    public void saveAsJsonl(List<ConfigResult> results, File jsonlFile) {
        try (BufferedWriter w = newBufferedWriter(jsonlFile.toPath(), StandardCharsets.UTF_8)) {
            for (ConfigResult r : results) {
                String line = "{"
                        + "\"filePath\":\"" + esc(r.filePath) + "\","
                        + "\"rootElement\":\"" + esc(r.rootElement) + "\"," // 这里存类型
                        + "\"elementCount\":" + r.elementCount + ","
                        + "\"lineCount\":" + r.lineCount + ","
                        + "\"encoding\":\"" + esc(r.encoding) + "\","
                        + "\"xmlContent\":\"" + esc(r.xmlContent) + "\""
                        + "}";
                w.write(line);
                w.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ---------- 工具 ----------

//    private int countLines(String s) {
//        if (s == null || s.isEmpty()) return 0;
//        int lines = 1;
//        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '\n') lines++;
//        return lines;
//    }
private int countLines(String s) {
    if (s == null || s.isEmpty()) return 0;
    int lines = 0;
    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.StringReader(s))) {
        while (br.readLine() != null) lines++;
    } catch (java.io.IOException ignore) {}
    return lines;
}


    private String esc(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int)c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }
}
