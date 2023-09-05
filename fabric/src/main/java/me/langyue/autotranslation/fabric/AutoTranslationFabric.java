package me.langyue.autotranslation.fabric;

import me.langyue.autotranslation.AutoTranslation;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class AutoTranslationFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        AutoTranslation.init();
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> AutoTranslation.stop());
    }
}