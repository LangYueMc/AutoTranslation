package me.langyue.autotranslation;

import me.langyue.autotranslation.resource.ResourceManager;
import me.langyue.autotranslation.translate.TranslateThreadPool;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.locale.Language;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class TranslatorHelper {

    private static final Pattern tagPattern = Pattern.compile("(([^\\s:]+:)+([^\\s.]+\\.)*[^\\s.]+)|(([^\\s.]+\\.)+[^\\s.]+)");
    private static Pattern enPattern = Pattern.compile("[A-Z]?[a-z]+\\s([A-Z]?[a-z]+\\s*)+");
    private static Pattern langPattern = null;

    /**
     * 临时黑名单，如果不需要翻译，可以在渲染前加入黑名单，等渲染时需要翻译的时候会自动移除，防止内存占用过高
     */
    private static final List<String> blacklist = Collections.synchronizedList(new ArrayList<>());

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();
    public static Set<String> noNeedForTranslation = new LinkedHashSet<>() {{
        // 按键
        add("Esc");
        add("Tab");
        add("Caps");
        add("Ins");
        add("Del");
        add("Home");
        add("End");
        add("Ctrl");
        add("Shift");
        add("Alt");
        // 其他单词
        add("Java");
        add("Minecraft");
        add("MC");
        add("Modrinth");
        add("CurseForge");
        add("Fabric");
        add("Forge");
        add("Discord");
        add("Patreon");
        add("Mastodon");
        add("YouTube");
        add("Twitch");
        add("Twitter");
        add("PayPal");
        add("Crowdin");
        add("Reddit");
        add("Liberapay");
        add("Coindrop");
        add("QQ");
        add("Ko-fi");
        add("FPS");
        add("TPS");
        add("MSTP");
        add("ping");
        add("max");
        add("min");
        add("avg");
        add("id");
        add("eg");
        add("Server");

        // 进度
        add("remaining");

        // FTB // 这几个如果单词出现基本上都是跟了数字的，并且频繁变动，所以误杀也没关系
        add("Cursor");
        add("Center:");
        add("Selected:");
    }};

    public static void init() {
        enPattern = Pattern.compile(AutoTranslation.CONFIG.enFeature);
        langPattern = Pattern.compile(AutoTranslation.CONFIG.yourLanguageFeature);
        noNeedForTranslation.addAll(AutoTranslation.CONFIG.wordBlacklist);
    }

    public static void addBlacklist(String key) {
        blacklist.add(key);
    }

    public static boolean shouldTranslate(String content) {
        return shouldTranslate(content, content);
    }

    public static boolean shouldTranslate(String key, String content) {
        if (blacklist.remove(content)) {
            // 如果在临时黑名单里，则不翻译，并且从临时黑名单移除
            return false;
        }
        if (AutoTranslation.getLanguage().equals(Language.DEFAULT)) {
            // 当前语言就是默认语言，无需翻译
            return false;
        }
        if (content == null || content.trim().length() < 2) {
            // 两个有效字符以下的不翻译
            return false;
        }
        if (content.trim().startsWith("* (")) {
            // 本 Mod 显示原文的格式，某些 Mod 分词可能会把原文分出去单独显示，所以过滤下
            return false;
        }
        if (!enPattern.matcher(content).find()) {
            // 没有英文
            return false;
        }
        if (langPattern != null && langPattern.matcher(content).find()) {
            // 已有当前语言的字符
            return false;
        }
        if (tagPattern.matcher(content).matches()) {
            // TAG / ID 不翻译
            return false;
        }
        if (CACHE.containsKey(key)) {
            // 如果有缓存，那肯定是需要翻译的，调用翻译接口会直接从缓存拿
            return true;
        }
        String _t = content
                .replaceAll("(§[0-9a-rA-R])", "")
                .replaceAll(tagPattern.pattern(), "")
                .replaceAll("[@#*&]\\S+", "")
                .toLowerCase();
        for (String p : noNeedForTranslation) {
            _t = _t.replaceAll(p.toLowerCase(), " ");
        }
//        ServerData server = Minecraft.getInstance().getCurrentServer();
//        if (server != null && server.players != null) {
//            for (GameProfile gameProfile : server.players.sample()) {
//                _t = _t.replaceAll(gameProfile.getName(), " ");
//            }
//        }
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
        return translate(en, en, appendOriginal, callback);
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
        return translate(key, en, AutoTranslation.CONFIG.appendOriginal, callback);
    }


    /**
     * 异步翻译文本
     *
     * @param key            要翻译的语言文件 key，仅支持语言文件未翻译的
     * @param en             要翻译的语言文件 value
     * @param appendOriginal 添加原文
     * @param callback       回调方法
     * @return 翻译后的文本, 因为是异步的，可能为空
     */
    public static String translate(String key, String en, boolean appendOriginal, Consumer<String> callback) {
        if (CACHE.containsKey(key)) {
            String translation = append(CACHE.get(key), en, appendOriginal);
            if (callback != null) {
                callback.accept(translation);
            }
            return translation;
        }
        TranslateThreadPool.offer(key, en, t -> {
            if (callback != null) {
                callback.accept(append(t, en, appendOriginal));
            }
        });
        return null;
    }

    private static String append(String translation, String original, boolean appendOriginal) {
        if (!appendOriginal) return translation;
        if (!CACHE.containsKey(original)) {
            // 占位符处理
            original = original.replaceAll("%%", "%").replaceAll("%", "%%");
        }
        if (!translation.contains("\n")) {
            return translation + " §7* (" + original + ")";
        }
        StringBuilder r = new StringBuilder();
        String[] t = translation.split("\n");
        String[] o = original.split("\n");
        if (t.length != o.length) {
            return translation + " §7* (" + original + ")";
        }
        for (int i = 0; i < t.length; i++) {
            r.append(t[i]).append(" §7* (").append(o[i]).append(')');
            if (i < t.length - 1) {
                r.append("\n");
            }
        }
        return r.toString();
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
        String translation = TranslatorManager.getTranslator().translate(en, AutoTranslation.getLanguage(), "en");
        if (key.startsWith(ResourceManager.BATCH_TRANSLATION)) {
            // json 格式，可能会翻译出问题，替换下全角“”：，
            // json 格式不加缓存，直接返回
            return translation
                    .replaceAll("(\\s).?[\"“”](.+)[\"“”].?[:：].? *.?[\"“”](.+)[\"“”][^,，\\s]?", "$1\"$2\": \"$3\"")
                    .replaceAll("(\")，", "$1,");
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
