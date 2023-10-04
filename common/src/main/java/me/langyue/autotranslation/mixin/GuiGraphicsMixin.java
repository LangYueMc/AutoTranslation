package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.TranslatorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

    @ModifyVariable(method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private String drawStringMixin(String string) {
        return at$getTranslate(string);
    }

    @Unique
    private String at$getTranslate(String content) {
        if (ScreenTranslationHelper.shouldTranslate(Minecraft.getInstance().screen)) {
            if (TranslatorHelper.shouldTranslate(content)) {
                String t = TranslatorHelper.translate(content, null);
                if (t != null) {
                    return t;
                }
            }
        }
        return content;
    }
}
