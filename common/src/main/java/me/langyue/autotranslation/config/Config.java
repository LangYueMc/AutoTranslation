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

    @Comment("翻译引擎，默认Google")
    public String translator = TranslatorManager.DEFAULT_TRANSLATOR;

    @Comment("无需翻译文本")
    public List<String> noNeedForTranslation = new ArrayList<>() {{
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
        add("PayPal");
        add("Ko-fi");
        add("Crowdin");
        add("Reddit");
        add("Liberapay");
        add("Coindrop");
        add("QQ");
    }};

    @Comment("是否在翻译后的文本里增加原文显示")
    @ConfigEntry.Gui.RequiresRestart
    public boolean appendOriginal = true;

    @Comment("忽略的命名空间")
    public Set<String> excludedNamespace = new HashSet<>() {{
        add("minecraft");
        add("fabric-registry-sync-v0");
    }};

    public static void init() {
        AutoConfig.register(Config.class, JanksonConfigSerializer::new);
        AutoTranslation.CONFIG = AutoConfig.getConfigHolder(Config.class).getConfig();
    }
}
