package me.langyue.autotranslation.translate;

import com.mojang.authlib.GameProfile;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.resource.ResourceManager;
import me.langyue.autotranslation.translate.google.Google;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.locale.Language;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class TranslatorManager {

    public static final String DEFAULT_TRANSLATOR = "Google";

    private static final Pattern enPattern = Pattern.compile("[a-zA-Z]{2,}");
    private static final Pattern tagPattern = Pattern.compile("([^\\s:]+:)+([^\\s.]+\\.)*[^\\s.]+");
    private static Pattern langPattern = null;

    /**
     * 临时黑名单，如果不需要翻译，可以在渲染前加入黑名单，等渲染时需要翻译的时候会自动移除，防止内存占用过高
     */
    private static final List<String> blacklist = Collections.synchronizedList(new ArrayList<>());

    private static final Map<String, Supplier<ITranslator>> _TRANSLATOR_MAP = new LinkedHashMap<>() {{
        put(DEFAULT_TRANSLATOR, Google::getInstance);
    }};

    private static final Map<String, ITranslator> _TRANSLATOR_INSTANCES = new HashMap<>();

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    public static void init() {
        setTranslator(AutoTranslation.CONFIG.translator);
        TranslateThreadPool.init();
        langPattern = Pattern.compile(AutoTranslation.CONFIG.yourLanguageFeature);
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

    public static void registerTranslator(String name, Supplier<ITranslator> getInstance) {
        _TRANSLATOR_MAP.put(name, getInstance);
    }

    public static ITranslator getTranslator() {
        return _TRANSLATOR_INSTANCES.get(AutoTranslation.CONFIG.translator);
    }

    public static ITranslator getTranslator(String name) {
        if (StringUtils.isBlank(name)) return null;
        return _TRANSLATOR_INSTANCES.get(name);
    }

    public static void addBlacklist(String key) {
        blacklist.add(key);
    }

    public static boolean shouldTranslate(String key, String content) {
        if (AutoTranslation.getLanguage().equals(Language.DEFAULT)) {
            // 当前语言就是默认语言，无需翻译
            return false;
        }
        if (blacklist.remove(key)) {
            // 如果在临时黑名单里，则不翻译，并且从临时黑名单移除
            return false;
        }
        if (CACHE.containsKey(key)) {
            // 如果有缓存，那肯定是需要翻译的，调用翻译接口会直接从缓存拿
            return true;
        }
        if (langPattern != null && langPattern.matcher(content).find()) {
            // 已有当前语言的字符
            return false;
        }
        if (tagPattern.matcher(content).matches()) {
            // TAG / ID 不翻译
            return false;
        }
        String _t = content.replaceAll("(§[0-9a-rA-R])|(%[a-hsxont]x?)|(\\\\\\S)|([^a-zA-Z\\s]+)", " ").toLowerCase();
        for (String p : AutoTranslation.CONFIG.noNeedForTranslation) {
            _t = _t.replaceAll(p.toLowerCase(), " ");
        }
        ServerData server = Minecraft.getInstance().getCurrentServer();
        if (server != null && server.players != null) {
            for (GameProfile gameProfile : server.players.sample()) {
                _t = _t.replaceAll(gameProfile.getName(), " ");
            }
        }
        return enPattern.matcher(_t.trim()).find();
    }

    /**
     * 异步翻译文本
     *
     * @param en       要翻译的语言文件 value
     * @param callback 回调方法
     * @return 翻译后的文本, 因为是异步的，可能为空
     */
    public static String translate(String en, Consumer<String> callback) {
        return translate(en, AutoTranslation.CONFIG.appendOriginal, callback);
    }

    /**
     * 异步翻译文本
     *
     * @param en             要翻译的语言文件 value
     * @param appendOriginal 添加原文
     * @param callback       回调方法
     * @return 翻译后的文本, 因为是异步的，可能为空
     */
    public static String translate(String en, boolean appendOriginal, Consumer<String> callback) {
        return translate(en, en, appendOriginal, false, callback);
    }

    /**
     * 异步翻译文本
     *
     * @param en              要翻译的语言文件 value
     * @param appendOriginal  添加原文
     * @param appendToNewLine 添加原文到新的一行
     * @param callback        回调方法
     * @return 翻译后的文本, 因为是异步的，可能为空
     */
    public static String translate(String en, boolean appendOriginal, boolean appendToNewLine, Consumer<String> callback) {
        return translate(en, en, appendOriginal, appendToNewLine, callback);
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
        return translate(key, en, AutoTranslation.CONFIG.appendOriginal, false, callback);
    }


    /**
     * 异步翻译文本
     *
     * @param key             要翻译的语言文件 key，仅支持语言文件未翻译的
     * @param en              要翻译的语言文件 value
     * @param appendOriginal  添加原文
     * @param appendToNewLine 添加原文到新的一行
     * @param callback        回调方法
     * @return 翻译后的文本, 因为是异步的，可能为空
     */
    public static String translate(String key, String en, boolean appendOriginal, boolean appendToNewLine, Consumer<String> callback) {
        String original = appendOriginal ?
                ((appendToNewLine ? "\n" : " ") + "§7* (" + en.replaceAll("%", "%%").replaceAll("%%%%", "%%") + ")")
                : "";
        if (CACHE.containsKey(key)) {
            String translation = CACHE.get(key) + original;
            if (callback != null) {
                callback.accept(translation);
            }
            return translation;
        }
        TranslateThreadPool.offer(key, en, t -> {
            String translation = t + original;
            if (callback != null) {
                callback.accept(translation);
            }
        });
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
     * @param en  要翻译的语言文件 value, 可以是json，但必须格式化，单行翻译容易出问题
     * @return 翻译后的文本
     */
    public static String translateSync(String key, String en) {
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }
        String translation = getTranslator().translate(en, AutoTranslation.getLanguage(), "en");
        if (key.trim().startsWith("{") && key.trim().endsWith("}")) {
            // json 格式，可能会翻译出问题，替换下全角“”：，
            // json 格式不加缓存，直接返回
            return translation
                    // key 前的
                    .replaceAll("\r?\n +[“”]", "\n\"")
                    // key 后
                    .replaceAll("[\"“”][^\"“”:： ]*[:：] *", "\": ")
                    // value 前
                    .replaceAll("[:：] *[\"“”]", ": \"")
                    // value 后
                    .replaceAll("[\"“”][,，] *\r?\n", "\",\n")
                    .replaceAll("[\"“”][^\"“”]*\r?\n *}", "\"\n}");
        }
        if (key.equals(en)) {
            ResourceManager.noKeyTranslate(en, translation);
        }
        return setCache(key, translation);
    }

    public static String setCache(String key, String value) {
        if (key == null || value == null) return value;
        if (!key.trim().startsWith("{") && !key.trim().endsWith("}")) {
            // json 格式是直接翻译的文件，不缓存
            CACHE.put(key, value);
        }
        return value;
    }

    public static boolean hasCache(String key) {
        if (key == null) return false;
        return CACHE.containsKey(key);
    }
}
