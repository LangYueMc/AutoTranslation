package me.langyue.autotranslation.util;

import me.langyue.autotranslation.AutoTranslation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileUtils {

    public static List<String> readToList(Path file) {
        if (!Files.exists(file)) return null;
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (Throwable e) {
            AutoTranslation.LOGGER.error("Read file ({}) failed", file, e);
        }
        return lines;
    }

    public static void write(Path file, Collection<String> lines) {
        write(file, String.join("\n", lines));
    }

    public static void write(Path file, String content) {
        if (!Files.exists(file.getParent())) {
            try {
                Files.createDirectories(file.getParent());
            } catch (Throwable e) {
                AutoTranslation.LOGGER.error("Create directories ({}) failed", file.getParent(), e);
                return;
            }
        }
        if (!Files.exists(file)) {
            try {
                Files.createFile(file);
            } catch (Throwable e) {
                AutoTranslation.LOGGER.error("Create file ({}) failed", file, e);
                return;
            }
        }
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file.toFile()), StandardCharsets.UTF_8);
            writer.write(content);
        } catch (Throwable e) {
            AutoTranslation.LOGGER.error("Writing file failed", e);
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException ignored) {
            }
        }
    }
}
