package me.langyue.autotranslation.mixin.compat.patchouli;

import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.TranslatorHelper;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import vazkii.patchouli.client.book.BookEntry;

@Mixin(BookEntry.class)
public class BookEntryMixin {

    @ModifyArg(method = "getName", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
    private String getNameMixin(String string) {
        if (ScreenTranslationHelper.shouldTranslate(Minecraft.getInstance().screen)) {
            if (TranslatorHelper.shouldTranslate(string)) {
                String t = TranslatorHelper.translate(string, null);
                if (t != null && !t.equals(string)) {
                    return t;
                }
            }
        }
        return string;
    }
}
