package me.langyue.autotranslation.config;

import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.translate.TranslatorManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@me.shedaniel.autoconfig.annotation.Config(name = AutoTranslation.MOD_ID)
public class Config implements ConfigData {

    public enum FilterMode {
        RESOURCE,
        CORRECTION
    }

    public enum ScreenArea {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        MIDDLE_LEFT,
        MIDDLE_CENTER,
        MIDDLE_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT
    }

    @Comment("筛选模式\n  RESOURCE: 只要当前语言存在 key，就忽略这个key，无论是否翻译\n  CORRECTION: 只要当前语言的 key 未翻译，就进行翻译，无论资源文件内是否存在")
    public FilterMode mode = FilterMode.RESOURCE;

    @Comment("英语特征")
    public String enFeature = "([A-Z]?[a-z]{2,}\\s*)+";

    @Comment("您的语言的特征，默认的是中日韩")
    public String yourLanguageFeature = "[\\u0800-\\u9fa5\\uac00-\\ud7ff]+";

    @Comment("翻译引擎，默认 Google，如果未安装其他翻译引擎，请勿修改本项")
    public String translator = TranslatorManager.DEFAULT_TRANSLATOR;

    @Comment("屏幕翻译排除原版屏幕")
    @ConfigEntry.Gui.NoTooltip
    public boolean ignoreOriginalScreen = true;

    @Comment("是否在翻译后的文本里增加原文显示")
    public boolean appendOriginal = true;

    @Comment("无需翻译文本, 支持正则, 不区分大小写")
    public List<String> wordBlacklist = new ArrayList<>();

    @Comment("开启 DEBUG 模式，开启可能会有日志刷屏")
    public boolean debug = false;

    @Comment("忽略的命名空间, 支持正则")
    @ConfigEntry.Gui.Excluded
    public Set<String> excludedNamespace = new HashSet<>() {{
        add("minecraft");
        add("^fabric-.*");
        add("forge");
    }};

    @Comment("翻译图标配置")
    @ConfigEntry.Gui.NoTooltip
    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("icon")
    public Icon icon = new Icon();

    public static class Icon {

        @Comment("是否显示图标")
        public boolean display = true;

        @Comment("显示位置")
        public ScreenArea displayArea = ScreenArea.TOP_RIGHT;

        @Comment("X 轴偏移量")
        public int offsetX = 0;

        @Comment("Y 轴偏移量")
        public int offsetY = 0;
    }

    @Comment("Google 翻译相关配置")
    @ConfigEntry.Gui.NoTooltip
    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("google")
    @ConfigEntry.Gui.PrefixText
    public Google google = new Google();

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
