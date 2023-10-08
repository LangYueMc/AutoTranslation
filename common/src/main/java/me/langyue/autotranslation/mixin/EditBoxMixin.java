package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.ScreenTranslationHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
public class EditBoxMixin {

//    @ModifyArg(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"))
//    private String drawStringMixin(String s) {
//        TranslatorHelper.addBlacklist(s);
//        return s;
//    }
//
//    @ModifyArg(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
//    private Component drawComponentMixin(Component component) {
//        if (component instanceof MutableComponent mutableComponent) {
//            ((MutableComponentAccessor) mutableComponent).at$shouldTranslate(false);
//        }
//        return component;
//    }

    @Inject(method = "renderWidget", at = @At("HEAD"))
    private void drawStringBeforeMixin(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        ScreenTranslationHelper.unready();
    }

    @Inject(method = "renderWidget", at = @At("RETURN"))
    private void drawStringAfterMixin(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        ScreenTranslationHelper.ready();
    }
}
