package me.langyue.autotranslation.fabric;

import me.langyue.autotranslation.AutoTranslation;
import net.fabricmc.api.ModInitializer;

public class AutoTranslationFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        AutoTranslation.init();
    }
}