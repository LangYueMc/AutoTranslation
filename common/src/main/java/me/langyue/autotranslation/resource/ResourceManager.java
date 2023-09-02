package me.langyue.autotranslation.resource;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import dev.architectury.platform.Platform;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.FileUtil;
import net.minecraft.locale.Language;
import org.apache.commons.lang3.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ResourceManager {

    private static final Path root = Platform.getGameFolder().resolve("AutoTranslation");
    private static final String ref = "_ref.json";

    public static final Multimap<String, String> UNKNOWN_KEYS = LinkedListMultimap.create();

    private static final Map<String, String> AUTO_KEYS = new LinkedHashMap<>();

    /**
     * 翻译 json 必须格式化，不然可能出问题
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static long id = 0;

    private static Language language;

    public static void init() {
        try {
            // 创建根目录和缓存目录
            FileUtil.createDirectoriesSafe(root);
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
            String json = GSON.toJson(jsonObject);
            writeFile(namespace, ref, json);
            // 写入翻译文件
            int maxLength = TranslatorManager.getTranslator().maxLength();
            JsonObject chunk = new JsonObject();
            int length = 2; // 前后的{}
            for (String key : keys) {
                String autoKey = generateAutoKey(key);
                String value = jsonObject.get(key).getAsString();
                int rowLength = autoKey.length() + value.length() + 20; // 引号逗号回车等， 给20很富裕
                if (length + rowLength > maxLength) {
                    // 达到分片大小
                    translatorAndAppendJson(namespace, chunk);
                    chunk = new JsonObject();
                    length = 10;
                }
                chunk.addProperty(autoKey, language.getOrDefault(key));
                length += rowLength;
            }
            if (chunk.size() > 0) {
                translatorAndAppendJson(namespace, chunk);
            }
        });
    }

    private static void translatorAndAppendJson(String namespace, JsonObject object) {
        Set<String> remove = new HashSet<>();
        TranslatorManager.translate(new GsonBuilder().setPrettyPrinting().create().toJson(object), s -> {
            String result = s;
            for (Map.Entry<String, String> entry : AUTO_KEYS.entrySet()) {
                if (result.contains(entry.getValue() + "\"")) {
                    remove.add(entry.getKey());
                    result = result.replace(entry.getValue() + "\"", entry.getKey() + "\"");
                }
            }
            remove.forEach(AUTO_KEYS::remove);
            remove.clear();
            writeFile(namespace, AutoTranslation.getLanguage() + ".json", result);
        });
    }

    public static void loadResource(String... namespaces) {
        if (namespaces == null || namespaces.length == 0) {
            namespaces = UNKNOWN_KEYS.keySet().toArray(new String[]{});
        }

    }

    private static String generateAutoKey(String key) {
        if (AUTO_KEYS.containsKey(key)) {
            return AUTO_KEYS.get(key);
        }
        if (AUTO_KEYS.isEmpty()) {
            id = 0;
        }
        String autoKey = "trans.auto.$" + String.format("%05d", (++id));
        AUTO_KEYS.put(key, autoKey);
        return autoKey;
    }

    public static String getAutoKey(String key) {
        if (AUTO_KEYS.containsKey(key)) {
            return AUTO_KEYS.get(key);
        }
        return key;
    }

    /**
     * 写入文件
     *
     * @param dir      目录
     * @param fileName 文件名
     * @param json     必须是 json 格式的内容
     */
    public static void writeFile(String dir, String fileName, String json) {
        Writer writer = null;
        Path file = root.resolve(dir).resolve(fileName);
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
        String readString = null;
        try {
            readString = Files.readString(file);
        } catch (Exception e) {
            AutoTranslation.LOGGER.error("Read file ({}) failed", file, e);
        }
        JsonObject jsonObject = new JsonObject();
        if (StringUtils.isNotBlank(readString)) {
            try {
                jsonObject = GSON.fromJson(readString, JsonObject.class);
            } catch (JsonSyntaxException e) {
                AutoTranslation.LOGGER.error("The original file ({}) format is incorrect", file, e);
            }
        }
        try {
            for (Map.Entry<String, JsonElement> entry : GSON.fromJson(json, JsonObject.class).entrySet()) {
                jsonObject.addProperty(entry.getKey(), entry.getValue().getAsString());
            }
        } catch (Throwable e) {
            file = root.resolve(dir).resolve("_auto.json");
            AutoTranslation.LOGGER.error("Json format error, write to {}", file, e);
        }
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file.toFile()), StandardCharsets.UTF_8);
            writer.write(GSON.toJson(jsonObject));
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
