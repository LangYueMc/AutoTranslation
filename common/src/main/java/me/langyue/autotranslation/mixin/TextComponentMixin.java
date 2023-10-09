package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.TranslatorHelper;
import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextComponent.class)
public abstract class TextComponentMixin extends BaseComponent implements MutableComponentAccessor {

    @Unique
    private @Nullable Language at$decomposedWith;

    @Shadow
    @Final
    private String text;
    @Unique
    private String at$translatedContents;

    @Unique
    private boolean at$shouldTranslate = true;

    @Unique
    private FormattedCharSequence at$translatedVisualOrderText;

    @Shadow
    public abstract @NotNull TextComponent plainCopy();

    @Inject(method = "getContents", at = @At("HEAD"), cancellable = true)
    private void getContentsMixin(CallbackInfoReturnable<String> cir) {
        if (!this.at$shouldTranslate()) return;
        try {
            if (ScreenTranslationHelper.shouldTranslate(Minecraft.getInstance().screen)) {
                if (this.at$decomposedWith != Language.getInstance()) {
                    this.at$translatedContents = null;
                }
                if (this.at$translatedContents != null) {
                    cir.setReturnValue(this.at$translatedContents);
                    return;
                }
                if (TranslatorHelper.shouldTranslate(text)) {
                    TranslatorHelper.translate(text, translate -> at$translatedContents = translate);
                    if (this.at$translatedContents != null) {
                        cir.setReturnValue(this.at$translatedContents);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public @NotNull FormattedCharSequence getVisualOrderText() {
        if (this.at$shouldTranslate) {
            if (ScreenTranslationHelper.shouldTranslate(Minecraft.getInstance().screen)) {
                if (this.at$decomposedWith != Language.getInstance()) {
                    this.at$translatedVisualOrderText = null;
                    this.at$decomposedWith = Language.getInstance();
                }
                if (this.at$translatedVisualOrderText != null) {
                    return this.at$translatedVisualOrderText;
                }
                if (this.at$translatedContents != null) {
                    this.at$translatedVisualOrderText = Language.getInstance().getVisualOrder(new TextComponent(this.at$translatedContents));
                }
            }
        }
        return super.getVisualOrderText();
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
