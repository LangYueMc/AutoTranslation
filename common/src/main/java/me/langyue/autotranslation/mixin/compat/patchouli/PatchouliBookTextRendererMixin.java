package me.langyue.autotranslation.mixin.compat.patchouli;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import me.langyue.autotranslation.gui.ScreenManager;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vazkii.patchouli.client.book.gui.BookTextRenderer;
import vazkii.patchouli.client.book.text.BookTextParser;
import vazkii.patchouli.client.book.text.TextLayouter;
import vazkii.patchouli.client.book.text.Word;

import java.util.List;

@Mixin(BookTextRenderer.class)
public class PatchouliBookTextRendererMixin {

    @Mutable
    @Shadow
    @Final
    private List<Word> words;
    @Unique
    private TextLayouter autoTranslation$textLayouter;
    @Unique
    private BookTextParser autoTranslation$parser;

    @ModifyVariable(method = "<init>(Lvazkii/patchouli/client/book/gui/GuiBook;Lnet/minecraft/network/chat/Component;IIIII)V", at = @At(value = "INVOKE", target = "Lvazkii/patchouli/client/book/text/TextLayouter;layout(Lnet/minecraft/client/gui/Font;Ljava/util/List;)V"))
    private TextLayouter layoutMixin(TextLayouter textLayouter) {
        autoTranslation$textLayouter = textLayouter;
        return textLayouter;
    }

    @ModifyVariable(method = "<init>(Lvazkii/patchouli/client/book/gui/GuiBook;Lnet/minecraft/network/chat/Component;IIIII)V", at = @At(value = "INVOKE", target = "Lvazkii/patchouli/client/book/text/BookTextParser;parse(Lnet/minecraft/network/chat/Component;)Ljava/util/List;"))
    private BookTextParser parseMixin(BookTextParser bookTextParser) {
        autoTranslation$parser = bookTextParser;
        return bookTextParser;
    }

    @ModifyArg(method = "<init>(Lvazkii/patchouli/client/book/gui/GuiBook;Lnet/minecraft/network/chat/Component;IIIII)V", at = @At(value = "INVOKE", target = "Lvazkii/patchouli/client/book/text/BookTextParser;parse(Lnet/minecraft/network/chat/Component;)Ljava/util/List;"))
    private Component parseMixin(Component text) {
        if (text instanceof MutableComponent mutableComponent) {
//            if (((MutableComponentAccessor) (Object) mutableComponent).isTranslated()) {
//                return mutableComponent;
//            }
            if (ScreenManager.shouldTranslate(Minecraft.getInstance().screen)) {
                String content = mutableComponent.getString();
                if (TranslatorManager.shouldTranslate(content, content)) {
                    String t = TranslatorManager.translate(content, translate -> {
                        if (autoTranslation$textLayouter == null || autoTranslation$parser == null) return;
                        MutableComponent component = Component.literal(translate);
                        autoTranslation$textLayouter.layout(Minecraft.getInstance().font, autoTranslation$parser.parse(component));
                        this.words = autoTranslation$textLayouter.getWords();
                        ((MutableComponentAccessor) (Object) mutableComponent).setTranslated(true);
                        ((MutableComponentAccessor) (Object) component).setTranslated(true);
                    });
                    if (t != null && !t.equals(content)) {
                        return Component.literal(t);
                    }
                }
            }
        }
        return text;
    }
}
