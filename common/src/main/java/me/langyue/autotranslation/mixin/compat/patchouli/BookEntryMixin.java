package me.langyue.autotranslation.mixin.compat.patchouli;

import me.langyue.autotranslation.gui.ScreenManager;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import vazkii.patchouli.client.book.BookEntry;

@Mixin(BookEntry.class)
public class BookEntryMixin {

    @ModifyArg(method = "getName", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
    private String getNameMixin(String string) {
        if (ScreenManager.shouldTranslate(Minecraft.getInstance().screen)) {
            if (TranslatorManager.shouldTranslate(string)) {
                String t = TranslatorManager.translate(string, null);
                if (t != null && !t.equals(string)) {
                    return t;
                }
            }
        }
        return string;
    }
}
