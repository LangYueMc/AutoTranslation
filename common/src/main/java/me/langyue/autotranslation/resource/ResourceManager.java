package me.langyue.autotranslation.resource;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ResourceManager {

    private static final String root = "AutoTranslation";
    private static final String ref = "_ref.json";

    public static final Multimap<String, String> UNKNOWN_KEYS = LinkedListMultimap.create();

    private static String gameDirectory;

    private static Language language;

    public static void init() {
        try {
            gameDirectory = Minecraft.getInstance().gameDirectory.getPath();
            // 创建根目录和缓存目录
            FileUtil.createDirectoriesSafe(Path.of(gameDirectory, root));
        } catch (Throwable e) {
            AutoTranslation.LOGGER.error("Root directory creation failed", e);
        }
    }

    public static void setLanguage(Language language) {
        ResourceManager.language = language;
    }

    public static void initResource() {
        if (language == null) {
            throw new RuntimeException("Unready!");
        }
        UNKNOWN_KEYS.keySet().forEach(namespace -> {
            Collection<String> keys = UNKNOWN_KEYS.get(namespace);
            if (keys.isEmpty()) return;
            JsonObject jsonObject = new JsonObject();
            keys.forEach(key -> {
                jsonObject.addProperty(key, language.getOrDefault(key));
            });
            // 写入参考文件
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
            writeFile(namespace, ref, json, false);
            // 写入翻译文件
            // TODO 文件整个翻译时 key 要随机生成，然后替换，Google还是会翻译 key 的
            int maxLength = TranslatorManager.getTranslator().maxLength();
            if (json.length() < maxLength) {
                TranslatorManager.translate(json, t -> writeFile(namespace, AutoTranslation.getLanguage() + ".json", t, false));
            } else {
                int length = 10;
                JsonObject chunk = new JsonObject();
                for (String key : keys) {
                    String value = jsonObject.get(key).getAsString();
                    int rowLength = key.length() + value.length() + 20;
                    if (length + rowLength > maxLength) {
                        // 达到分片大小
                        translatorAndAppendJson(namespace, chunk);
                        chunk = new JsonObject();
                        length = 10;
                    }
                    chunk.addProperty(key, language.getOrDefault(key));
                    length += rowLength;
                }
                if (chunk.size() > 0) {
                    translatorAndAppendJson(namespace, chunk);
                }
            }
        });
    }

    private static void translatorAndAppendJson(String namespace, JsonObject object) {
        TranslatorManager.translate(new GsonBuilder().setPrettyPrinting().create().toJson(object), t -> writeFile(namespace, AutoTranslation.getLanguage() + ".json", t, true));
    }

    /**
     * 写入文件
     *
     * @param dir      目录
     * @param fileName 文件名
     * @param json     必须是 json 格式的内容
     * @param append   是否追加
     */
    public static void writeFile(String dir, String fileName, String json, boolean append) {
        Writer writer = null;
        Path file = Path.of(gameDirectory, root, dir, fileName);
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

        if (append) {
            // 删除之前的最后一行 } 125
            String readString;
            try {
                readString = Files.readString(file);
            } catch (Exception e) {
                AutoTranslation.LOGGER.error("Append file ({}) failed", file, e);
                return;
            }
            if (readString != null && readString.trim().length() > 0) {
                // 删除本次的第一行 { 123
                int lastIndexOf = readString.lastIndexOf('}');
                if (lastIndexOf > 0) {
                    readString = readString.substring(0, lastIndexOf);
                }
                json = readString + "," + json.substring(json.indexOf(123) + 1);
            }
        }
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file.toFile()), StandardCharsets.UTF_8);
            writer.write(json);
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
