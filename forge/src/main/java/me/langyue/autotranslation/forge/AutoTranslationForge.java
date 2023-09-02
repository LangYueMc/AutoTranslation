package me.langyue.autotranslation.forge;

import dev.architectury.platform.forge.EventBuses;
import me.langyue.autotranslation.AutoTranslation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AutoTranslation.MOD_ID)
public class AutoTranslationForge {
    public AutoTranslationForge() {
		// Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(AutoTranslation.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
            AutoTranslation.init();
    }
}