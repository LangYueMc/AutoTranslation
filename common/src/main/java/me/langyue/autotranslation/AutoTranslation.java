package me.langyue.autotranslation;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import me.langyue.autotranslation.command.AutoTranslationCommands;
import me.langyue.autotranslation.config.Config;
import me.langyue.autotranslation.gui.ScreenManager;
import me.langyue.autotranslation.resource.ResourceManager;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class AutoTranslation {
    public static final Logger LOGGER = LoggerFactory.getLogger("AutoTranslation");
    public static final String MOD_ID = "autotranslation";

    public static final Path ROOT = Platform.getGameFolder().resolve("AutoTranslation");
    public static Config CONFIG = null;

    public static final KeyMapping SCREEN_TRANSLATE_KEYMAPPING = new KeyMapping(
            "key.autotranslation.screen_translate", // The translation key of the name shown in the Controls screen
            InputConstants.Type.KEYSYM, // This key mapping is for Keyboards by default
            -1, // The default keycode
            "category.autotranslation" // The category translation key used to categorize in the Controls screen
    );

    public static void init() {
        Config.init();
        TranslatorManager.init();
        ResourceManager.init();
        ScreenManager.init();
        KeyMappingRegistry.register(SCREEN_TRANSLATE_KEYMAPPING);
        CommandRegistrationEvent.EVENT.register((dispatcher, dedicated, ignored) -> AutoTranslationCommands.register(dispatcher));
    }

    public static void stop() {
        ScreenManager.saveConfig();
        ResourceManager.save();
    }

    public static String getLanguage() {
        Minecraft instance = Minecraft.getInstance();
        if (instance == null) return Language.DEFAULT;
        return instance.options.languageCode;
    }

    public static void debug(String var1, Object... var2) {
        if (CONFIG.debug) {
            LOGGER.info(var1, var2);
        }
    }
}
