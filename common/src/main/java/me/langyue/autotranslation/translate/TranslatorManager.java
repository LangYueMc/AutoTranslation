package me.langyue.autotranslation.translate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.translate.impl.Google;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TranslatorManager {

    public static final String DEFAULT_TRANSLATOR = "Google";

    private static final Map<String, Supplier<ITranslator>> _TRANSLATOR_MAP = new LinkedHashMap<>() {{
        put(DEFAULT_TRANSLATOR, Google::new);
    }};

    private static final Map<String, ITranslator> _TRANSLATOR_INSTANCES = new HashMap<>();

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    public static void init() {
        setTranslator(AutoTranslation.CONFIG.translator);
        TranslateThreadPool.init();
    }

    public static void setTranslator(String name) {
        if (!_TRANSLATOR_INSTANCES.containsKey(name)) {
            ITranslator translator = _TRANSLATOR_MAP.get(name).get();
            if (translator == null) {
                AutoTranslation.LOGGER.error("Unknown translator: {}", name);
                setTranslator(DEFAULT_TRANSLATOR);
            } else {
                _TRANSLATOR_INSTANCES.put(name, translator);
            }
        }
    }

    public static void registerTranslator(String name, Supplier<ITranslator> newInstance) {
        _TRANSLATOR_MAP.put(name, newInstance);
    }

    public static ITranslator getTranslator() {
        return _TRANSLATOR_INSTANCES.get(AutoTranslation.CONFIG.translator);
    }

    public static ITranslator getTranslator(String name) {
        if (StringUtils.isBlank(name)) return null;
        return _TRANSLATOR_INSTANCES.get(name);
    }

    /**
     * 异步翻译文本
     *
     * @param en       要翻译的语言文件 value
     * @param callback 回调方法
     * @return 翻译后的文本, 因为是异步的，可能为空
     */
    public static String translate(String en, Consumer<String> callback) {
        return translate(en, en, callback);
    }


    /**
     * 异步翻译文本
     *
     * @param key      要翻译的语言文件 key，仅支持语言文件未翻译的
     * @param en       要翻译的语言文件 value
     * @param callback 回调方法
     * @return 翻译后的文本, 因为是异步的，可能为空
     */
    public static String translate(String key, String en, Consumer<String> callback) {
        if (CACHE.containsKey(key)) {
            if (callback != null) {
                callback.accept(CACHE.get(key));
            }
            return CACHE.get(key);
        }
        TranslateThreadPool.offer(key, en, callback);
        return null;
    }

    /**
     * 翻译文本，线程阻塞的
     *
     * @param en 要翻译的语言文件 value
     * @return 翻译后的文本
     */
    public static String translateSync(String en) {
        return translateSync(en, en);
    }

    /**
     * 翻译文本，线程阻塞的, 尽量使用 translate(String key, String en)
     *
     * @param key 要翻译的语言文件 key，仅支持语言文件未翻译的
     * @param en  要翻译的语言文件 value
     * @return 翻译后的文本
     */
    public static String translateSync(String key, String en) {
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }
        return setCache(key, getTranslator().translate(en, AutoTranslation.getLanguage()));
    }

    private static String setCache(String key, String value) {
        if (key == null || value == null) return value;
        if (key.trim().startsWith("{")) {
            // json 格式
            try {
                new Gson()
                        .fromJson(value, JsonObject.class)
                        .asMap()
                        .forEach((s, jsonElement) -> CACHE.put(s, jsonElement.getAsString()));
            } catch (Throwable ignored) {
            }
        } else {
            CACHE.put(key, value);
        }
        return value;
    }

    private static String getCache(String key) {
        return CACHE.get(key);
    }

    public static void deleteCache(String key) {
        CACHE.remove(key);
    }
}
