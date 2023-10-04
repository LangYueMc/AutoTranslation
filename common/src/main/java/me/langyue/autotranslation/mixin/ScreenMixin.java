package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.accessor.ScreenAccessor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin implements ScreenAccessor {

    @Unique
    private boolean at$shouldTranslate = false;

    @Unique
    private boolean at$showIcon = false;

    @Unique
    private final Screen autoTranslation$_this = (Screen) (Object) this;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void keyPressedMixin(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if (AutoTranslation.SCREEN_TRANSLATE_KEYMAPPING.matches(i, j)) {
            ScreenTranslationHelper.toggleScreenStatus(autoTranslation$_this);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("HEAD"))
    private void initMixin(CallbackInfo ci) {
        ScreenTranslationHelper.initScreenStatus(autoTranslation$_this);
    }

    @Override
    public boolean at$shouldTranslate() {
        return this.at$shouldTranslate;
    }

    @Override
    public void at$shouldTranslate(boolean shouldTranslate) {
        this.at$shouldTranslate = shouldTranslate;
    }

    @Override
    public boolean at$showIcon() {
        return this.at$showIcon;
    }

    @Override
    public void at$showIcon(boolean showIcon) {
        this.at$showIcon = showIcon;
    }
}
