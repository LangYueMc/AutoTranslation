package me.langyue.autotranslation.translate;

import com.mojang.authlib.GameProfile;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.translate.google.Google;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class TranslatorManager {

    public static final String DEFAULT_TRANSLATOR = "Google";

    private static final Pattern enPattern = Pattern.compile("([a-zA-Z]{2,} *)+");
    private static final Pattern tagPattern = Pattern.compile("#([^:\\s]+:?)+");

    private static final Map<String, Supplier<ITranslator>> _TRANSLATOR_MAP = new LinkedHashMap<>() {{
        put(DEFAULT_TRANSLATOR, Google::getInstance);
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
                translator.init();
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

    public static boolean shouldTranslate(String key, String content) {
        if (CACHE.containsKey(key)) {
            // 如果有缓存，那肯定是需要翻译的，调用翻译接口会直接从缓存拿
            return true;
        }
        if (tagPattern.matcher(content).matches()) {
            // TAG 不翻译
            return false;
        }
        String _t = content.replaceAll("(§[0-9a-rA-R])|(%[a-hsxont%]x?)|(\\\\\\S)|([^a-zA-Z\\s]+)", " ").toLowerCase();
        for (String p : AutoTranslation.CONFIG.noNeedForTranslation) {
            _t = _t.replaceAll(p.toLowerCase(), " ");
        }
        ServerData server = Minecraft.getInstance().getCurrentServer();
        if (server != null && server.players != null) {
            for (GameProfile gameProfile : server.players.sample()) {
                _t = _t.replaceAll(gameProfile.getName(), " ");
            }
        }
        return enPattern.matcher(_t.trim()).matches();
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
            String translate = CACHE.get(key);
            if (AutoTranslation.CONFIG.appendOriginal) {
                translate += " §7(" + en + ")";
            }
            if (callback != null) {
                callback.accept(translate);
            }
            return translate;
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
        return setCache(key, getTranslator().translate(en, AutoTranslation.getLanguage(), "en"));
    }

    public static String setCache(String key, String value) {
        if (key == null || value == null) return value;
        if (!key.trim().startsWith("{") && !key.trim().endsWith("}")) {
            // json 格式是直接翻译的文件，不缓存
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
