package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.TranslatorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public class FontMixin {

    @ModifyVariable(method = "renderText(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F", at = @At("HEAD"), ordinal = 0, argsOnly = true)
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
