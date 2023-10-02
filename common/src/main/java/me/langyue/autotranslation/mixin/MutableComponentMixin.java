package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import me.langyue.autotranslation.gui.ScreenManager;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MutableComponent.class)
public class MutableComponentMixin implements MutableComponentAccessor {

    @Mutable
    @Shadow
    @Final
    private ComponentContents contents;

    @Shadow
    private @Nullable Language decomposedWith;

    @Unique
    private boolean at$shouldTranslate = false;

    @Unique
    private ComponentContents at$translatedContents;

    @Unique
    private FormattedCharSequence at$translatedVisualOrderText;

    @Inject(method = "create", at = @At("RETURN"))
    private static void initMixin(ComponentContents componentContents, CallbackInfoReturnable<MutableComponent> cir) {
        boolean shouldTranslate = false;
        if (componentContents instanceof LiteralContents literalContents) {
            String text = literalContents.text();
            shouldTranslate = TranslatorManager.shouldTranslate(text);
        }
        ((MutableComponentAccessor) cir.getReturnValue()).at$shouldTranslate(shouldTranslate);
    }

    @Inject(method = "getContents", at = @At("HEAD"), cancellable = true)
    private void getContentsMixin(CallbackInfoReturnable<ComponentContents> cir) {
        if (!this.at$shouldTranslate) return;
        if (Minecraft.getInstance() != null && ScreenManager.shouldTranslate(Minecraft.getInstance().screen)) {
            if (this.decomposedWith != Language.getInstance()) {
                this.at$translatedContents = null;
            }
            if (this.at$translatedContents != null) {
                cir.setReturnValue(this.at$translatedContents);
                return;
            }
            if (this.contents instanceof LiteralContents literalContents) {
                String text = literalContents.text();
                if (TranslatorManager.shouldTranslate(text)) {
                    TranslatorManager.translate(text, translate -> at$translatedContents = new TranslatableContents(text, null, TranslatableContents.NO_ARGS));
                }
            }
        }
    }

    @Inject(method = "getVisualOrderText", at = @At("HEAD"), cancellable = true)
    private void getVisualOrderTextMixin(CallbackInfoReturnable<FormattedCharSequence> cir) {
        if (!this.at$shouldTranslate) return;
        if (this.at$translatedContents == null) return;
        if (Minecraft.getInstance() != null && ScreenManager.shouldTranslate(Minecraft.getInstance().screen)) {
            if (this.decomposedWith != Language.getInstance()) {
                this.at$translatedVisualOrderText = null;
            }
            if (this.at$translatedVisualOrderText != null) {
                cir.setReturnValue(this.at$translatedVisualOrderText);
                return;
            }
            this.at$translatedVisualOrderText = Language.getInstance().getVisualOrder(MutableComponent.create(this.at$translatedContents));
        }
    }

    @Override
    public boolean at$shouldTranslate() {
        return this.at$shouldTranslate;
    }

    @Override
    public void at$shouldTranslate(boolean shouldTranslate) {
        this.at$shouldTranslate = shouldTranslate;
    }
}
