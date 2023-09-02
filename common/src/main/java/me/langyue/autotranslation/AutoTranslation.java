package me.langyue.autotranslation;

import me.langyue.autotranslation.config.Config;
import me.langyue.autotranslation.resource.ResourceManager;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoTranslation {
    public static final Logger LOGGER = LoggerFactory.getLogger("AutoTranslation");
    public static final String MOD_ID = "autotranslation";
    public static Config CONFIG = null;

    public static void init() {
        Config.init();
        TranslatorManager.init();
        ResourceManager.init();
    }

    public static String getLanguage() {
        return Minecraft.getInstance().options.languageCode;
    }
}
