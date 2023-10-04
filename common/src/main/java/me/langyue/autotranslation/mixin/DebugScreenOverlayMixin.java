package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.TranslatorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
@Deprecated
public class DebugScreenOverlayMixin {

    @Inject(method = "renderLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void renderLinesMixin(GuiGraphics guiGraphics, List<String> list, boolean bl, CallbackInfo ci, int i, int j, String string, int k, int l, int m) {
        // DEBUG (F3) 屏幕内容不翻译
        if (ScreenTranslationHelper.shouldTranslate(Minecraft.getInstance().screen)) {
            TranslatorHelper.addBlacklist(string);
        }
    }
}
