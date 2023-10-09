package me.langyue.autotranslation.mixin.compat.patchouli;

import com.mojang.blaze3d.vertex.PoseStack;
import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.TranslatorHelper;
import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.patchouli.client.book.gui.BookTextRenderer;
import vazkii.patchouli.client.book.text.BookTextParser;
import vazkii.patchouli.client.book.text.TextLayouter;
import vazkii.patchouli.client.book.text.Word;

import java.util.ArrayList;
import java.util.List;

/**
 * 这个 Mixin 没用了，手册文字固定翻译是因为上游代码直接用了 Component.translatable()
 */
@Mixin(BookTextRenderer.class)
@Deprecated
public class PatchouliBookTextRendererMixin {

    @Mutable
    @Shadow
    @Final
    private List<Word> words;
    @Unique
    private TextLayouter at$textLayouter;
    @Unique
    private BookTextParser at$parser;
    @Unique
    private final List<Word> at$originalWords = new ArrayList<>();
    @Unique
    private final List<Word> at$translatedWords = new ArrayList<>();
    @Unique
    private boolean at$isTranslatedWords = false;
    @Unique
    private TextComponent at$text;

    @ModifyVariable(method = "<init>(Lvazkii/patchouli/client/book/gui/GuiBook;Lnet/minecraft/network/chat/Component;IIIII)V", at = @At(value = "INVOKE", target = "Lvazkii/patchouli/client/book/text/TextLayouter;layout(Lnet/minecraft/client/gui/Font;Ljava/util/List;)V"))
    private TextLayouter layoutMixin(TextLayouter textLayouter) {
        at$textLayouter = textLayouter;
        return textLayouter;
    }

    @ModifyVariable(method = "<init>(Lvazkii/patchouli/client/book/gui/GuiBook;Lnet/minecraft/network/chat/Component;IIIII)V", at = @At(value = "INVOKE", target = "Lvazkii/patchouli/client/book/text/BookTextParser;parse(Lnet/minecraft/network/chat/Component;)Ljava/util/List;"))
    private BookTextParser parseMixin(BookTextParser bookTextParser) {
        at$parser = bookTextParser;
        return bookTextParser;
    }

    @ModifyArg(method = "<init>(Lvazkii/patchouli/client/book/gui/GuiBook;Lnet/minecraft/network/chat/Component;IIIII)V", at = @At(value = "INVOKE", target = "Lvazkii/patchouli/client/book/text/BookTextParser;parse(Lnet/minecraft/network/chat/Component;)Ljava/util/List;"))
    private Component parseMixin(Component text) {
        if (text instanceof TextComponent textComponent) {
            MutableComponentAccessor accessor = (MutableComponentAccessor) textComponent;
            boolean temp = accessor.at$shouldTranslate();
            accessor.at$shouldTranslate(false);
            at$text = (TextComponent) textComponent.copy();
            accessor.at$shouldTranslate(temp);
        }
        return text;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void renderMixin(PoseStack ms, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.at$text == null || this.at$textLayouter == null || this.at$parser == null) return;
        MutableComponentAccessor text = (MutableComponentAccessor) this.at$text;
        if (!text.at$shouldTranslate()) return;
        if (this.at$originalWords.isEmpty()) {
            this.at$originalWords.addAll(this.words);
        }
        if (ScreenTranslationHelper.shouldTranslate(Minecraft.getInstance().screen)) {
//            if (text.at$decomposedWith() != Language.getInstance()) {
//                this.at$translatedWords.clear();
//            }
            if (this.at$translatedWords.isEmpty()) {
                String content = this.at$text.getString();
                if (TranslatorHelper.shouldTranslate(content)) {
                    TranslatorHelper.translate(content, t -> {
                        if (t != null && !t.equals(content)) {
                            this.at$textLayouter.layout(Minecraft.getInstance().font, this.at$parser.parse(new TranslatableComponent(content)));
                            this.at$translatedWords.addAll(this.at$textLayouter.getWords());
                        } else {
                            this.at$translatedWords.addAll(this.at$originalWords);
                        }
                    });
                }
            }
            if (this.at$translatedWords.isEmpty()) {
                text.at$shouldTranslate(false);
                return;
            }
            if (this.at$isTranslatedWords) return;
            this.words.clear();
            this.words.addAll(this.at$translatedWords);
            this.at$isTranslatedWords = true;
            return;
        }
        if (!this.at$isTranslatedWords) return;
        this.words.clear();
        this.words.addAll(this.at$originalWords);
        this.at$isTranslatedWords = false;
    }
}
