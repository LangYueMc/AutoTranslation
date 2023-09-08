package me.langyue.autotranslation.config;

import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.translate.TranslatorManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@me.shedaniel.autoconfig.annotation.Config(name = AutoTranslation.MOD_ID)
public class Config implements ConfigData {

    @Comment("筛选模式\n可选值\n\tRESOURCE: 只要当前语言存在 key，就忽略这个key，无论是否翻译\n\tCORRECTION: 只要当前语言的 key 未翻译，就进行翻译，无论资源文件内是否存在")
    public FilterMode mode = FilterMode.RESOURCE;
    @Comment("您的语言的特征码，默认的是中日韩")
    public String yourLanguageFeature = "[\\u0800-\\u9fa5\\uac00-\\ud7ff]+";

    @Comment("翻译引擎，默认Google")
    public String translator = TranslatorManager.DEFAULT_TRANSLATOR;

    public enum FilterMode {
        RESOURCE,
        CORRECTION
    }

    @Comment("无需翻译文本, 支持正则, 不区分大小写")
    @ConfigEntry.Gui.Excluded
    public Set<String> noNeedForTranslation = new LinkedHashSet<>() {{
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
        add("Ko");
        add("fi");
        add("FPS");
        add("TPS");
        add("MSTP");
        add("ping");
    }};

    @Comment("是否在翻译后的文本里增加原文显示")
    public boolean appendOriginal = true;

    @Comment("忽略的命名空间, 支持正则")
    @ConfigEntry.Gui.Excluded
    public Set<String> excludedNamespace = new HashSet<>() {{
        add("minecraft");
        add("^fabric-.*");
        add("forge");
    }};

    @Comment("Google 翻译相关配置")
    @ConfigEntry.Gui.CollapsibleObject
    public Google google = new Google();
    @Comment("开启 DEBUG 模式，开启可能会有日志刷屏")
    public boolean debug = false;

    public static class Google {

        @Comment("Google 翻译备用域名，可以填镜像站，只要 API 跟谷歌相同就行")
        @ConfigEntry.Gui.RequiresRestart
        public String domain = "translate.google.com";

        @Comment("Google 服务器 IP，如果您所在地区无法直连域名，可以配置此项\n 参考 https://github.com/Ponderfly/GoogleTranslateIpCheck")
        @ConfigEntry.Gui.Excluded
        public Set<String> dns = new HashSet<>() {{
            add("64.233.189.191");
            add("108.177.97.100");
            add("216.239.32.40");
            add("74.125.196.113");
            add("142.251.171.90");
        }};
    }

    public static void init() {
        AutoConfig.register(Config.class, JanksonConfigSerializer::new);

        AutoTranslation.CONFIG = AutoConfig.getConfigHolder(Config.class).getConfig();
    }
}
