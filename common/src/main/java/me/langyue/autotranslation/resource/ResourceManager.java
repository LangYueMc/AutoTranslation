package me.langyue.autotranslation.resource;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.command.ResourcePathArgument;
import me.langyue.autotranslation.translate.TranslatorManager;
import me.langyue.autotranslation.util.FileUtils;
import net.minecraft.ResourceLocationException;
import net.minecraft.locale.Language;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ResourceManager {
    private static final String ref = "_ref.json";

    public static final Multimap<String, String> UNLOAD_KEYS = LinkedListMultimap.create();
    public static final Multimap<String, String> UNKNOWN_KEYS = LinkedListMultimap.create();
    private final static Object syncLock = new Object();
    private static final Map<String, String> AUTO_KEYS = new ConcurrentHashMap<>();

    /**
     * 用于存放无 key 翻译
     */
    private static final Map<String, String> NO_KEY_TRANS_STORE = new ConcurrentHashMap<>();
    /**
     * 存放无 key 翻译文件的命名空间
     */
    public static final String NO_KEY_TRANS_STORE_NAMESPACE = "_at_store";

    /**
     * 翻译 json 必须格式化，不然可能出问题
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static long id = 0;

    private static Language language;

    private static ScheduledExecutorService timer = null;

    public static void init() {
        if (timer == null) {
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(ResourceManager::save, 5, 5, TimeUnit.MINUTES);
        }
    }

    public static void setLanguage(Language language) {
        ResourceManager.save();
        ResourceManager.language = language;
    }

    public static void createPackMeta() {
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", 15);
        pack.addProperty("description", language.getOrDefault("pack.mcmeta.description"));
        JsonObject mcmeta = new JsonObject();
        mcmeta.add("pack", pack);
        write(AutoTranslation.ROOT.resolve("pack.mcmeta").toFile(), GSON.toJson(mcmeta));
    }

    public static void initResource() {
        if (language == null) {
            throw new RuntimeException("Unready!");
        }
        createPackMeta();
        ResourcePathArgument.addExamples(UNKNOWN_KEYS.keySet());
        UNKNOWN_KEYS.forEach(UNLOAD_KEYS::put);
        loadResource();
        UNLOAD_KEYS.keySet().forEach(namespace -> {
            Collection<String> keys = UNLOAD_KEYS.get(namespace);
            if (keys.isEmpty()) return;
            JsonObject jsonObject = new JsonObject();
            keys.forEach(key -> jsonObject.addProperty(key, language.getOrDefault(key)));
            // 写入参考文件
            String json = GSON.toJson(jsonObject);
            write(namespace, ref, json);
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

    public static void loadResource(String namespace) {
        Path file = AutoTranslation.ROOT.resolve(namespace).resolve(AutoTranslation.getLanguage() + ".json");
        if (Files.exists(file)) {
            JsonObject jsonObject = read(file);
            if (jsonObject == null) return;
            jsonObject.asMap().forEach((k, v) -> {
                String t = v.getAsString();
                if (namespace.equals(NO_KEY_TRANS_STORE_NAMESPACE) || UNKNOWN_KEYS.containsValue(k)) {
                    // NO_KEY_TRANS_STORE_NAMESPACE 全存，其他的只有在 UNKNOWN_KEYS 里存在的才缓存
                    TranslatorManager.setCache(k, t);
                }
                NO_KEY_TRANS_STORE.remove(k);
                UNLOAD_KEYS.remove(namespace, k);
            });
            AutoTranslation.LOGGER.info("Resource loaded: " + file);
        } else {
            throw new ResourceLocationException("Invalid resource path: " + file);
        }
    }

    public static void loadResource(String... namespaces) {
        if (namespaces == null || namespaces.length == 0) {
            namespaces = new HashSet<>(UNKNOWN_KEYS.keySet()) {{
                add(NO_KEY_TRANS_STORE_NAMESPACE);
            }}.toArray(new String[]{});
        }
        for (String ns : namespaces) {
            try {
                loadResource(ns);
            } catch (ResourceLocationException e) {
                AutoTranslation.LOGGER.warn(e.getMessage());
            }
        }
    }

    public static void noKeyTranslate(String en, String translation) {
        if (StringUtils.isNotBlank(en) && StringUtils.isNotBlank(translation)) {
            NO_KEY_TRANS_STORE.put(en, translation);
        }
    }

    public static void save() {
        if (language == null) return;
        if (!NO_KEY_TRANS_STORE.isEmpty()) {
            AutoTranslation.debug("Saving no_key_translation");
            write(NO_KEY_TRANS_STORE_NAMESPACE, AutoTranslation.getLanguage() + ".json", GSON.toJson(NO_KEY_TRANS_STORE));
            loadResource(NO_KEY_TRANS_STORE_NAMESPACE);
        }
    }

    private static void translatorAndAppendJson(String namespace, JsonObject object) {
        Set<String> remove = new HashSet<>();
        TranslatorManager.translate(GSON.toJson(object), false, s -> {
            synchronized (syncLock) {
                String result = s;
                for (Map.Entry<String, String> entry : AUTO_KEYS.entrySet()) {
                    if (result.contains(entry.getValue() + "\"")) {
                        remove.add(entry.getKey());
                        result = result.replace(entry.getValue() + "\"", entry.getKey() + "\"");
                    }
                }
                remove.forEach(AUTO_KEYS::remove);
                remove.clear();
                write(namespace, AutoTranslation.getLanguage() + ".json", result);
                loadResource(namespace);
            }
        });
    }

    private static String generateAutoKey(String key) {
        if (AUTO_KEYS.containsKey(key)) {
            return AUTO_KEYS.get(key);
        }
        if (AUTO_KEYS.isEmpty()) {
            id = 0;
        }
        String autoKey = "trans.auto." + String.format("%05d", (++id));
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
    private static void write(String dir, String fileName, String json) {
        synchronized (syncLock) {
            Path file = AutoTranslation.ROOT.resolve(dir).resolve(fileName);
            JsonObject current;
            try {
                current = GSON.fromJson(json, JsonObject.class);
            } catch (Throwable e) {
                AutoTranslation.LOGGER.error("Json format error, write to {}", file, e);
                write(AutoTranslation.ROOT.resolve(dir).resolve("_auto.json").toFile(), json);
                return;
            }
            JsonObject original = read(file);
            if (original == null) {
                // 文件不存在，或者格式不正确
                original = current;
            } else if (current != null) {
                for (Map.Entry<String, JsonElement> entry : current.entrySet()) {
                    original.add(entry.getKey(), entry.getValue());
                }
            }
            if (original == null) return;
            write(file.toFile(), GSON.toJson(original));
        }
    }

    private static void write(File file, String content) {
        synchronized (syncLock) {
            FileUtils.write(file.toPath(), content);
        }
    }

    private static JsonObject read(Path path) {
        synchronized (syncLock) {
            if (!Files.exists(path)) {
                return null;
            }
            String readString = null;
            try {
                readString = Files.readString(path);
            } catch (Exception e) {
                AutoTranslation.LOGGER.error("Read file ({}) failed", path, e);
            }
            JsonObject jsonObject = null;
            if (StringUtils.isNotBlank(readString)) {
                try {
                    jsonObject = GSON.fromJson(readString, JsonObject.class);
                } catch (JsonSyntaxException e) {
                    AutoTranslation.LOGGER.error("The original file ({}) format is incorrect", path, e);
                }
            }
            return jsonObject;
        }
    }
}
