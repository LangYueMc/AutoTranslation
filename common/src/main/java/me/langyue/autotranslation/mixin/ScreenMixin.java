package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.gui.ScreenManager;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Unique
    private final Screen autoTranslation$_this = (Screen) (Object) this;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void keyPressedMixin(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if (AutoTranslation.SCREEN_TRANSLATE_KEYMAPPING.matches(i, j)) {
            ScreenManager.toggleScreenStatus(autoTranslation$_this);
            cir.setReturnValue(true);
        }
    }
}
