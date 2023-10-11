package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.TranslatorHelper;
import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MutableComponent.class)
public abstract class MutableComponentMixin implements MutableComponentAccessor, Component {

    @Mutable
    @Shadow
    @Final
    private ComponentContents contents;

    @Shadow
    private @Nullable Language decomposedWith;

    @Shadow
    private Style style;
    @Shadow
    @Final
    private List<Component> siblings;

    @Unique
    private boolean at$shouldTranslate = false;

    @Unique
    private ComponentContents at$translatedContents;

    @Unique
    private FormattedCharSequence at$translatedVisualOrderText;

    @Inject(method = "create", at = @At("RETURN"))
    private static void initMixin(ComponentContents componentContents, CallbackInfoReturnable<MutableComponent> cir) {
        ((MutableComponentAccessor) cir.getReturnValue()).at$shouldTranslate(componentContents instanceof LiteralContents);
    }

    @Inject(method = "getContents", at = @At("HEAD"), cancellable = true)
    private void getContentsMixin(CallbackInfoReturnable<ComponentContents> cir) {
        if (!this.at$shouldTranslate) return;
        try {
            if (ScreenTranslationHelper.shouldTranslate(Minecraft.getInstance().screen)) {
                if (this.decomposedWith != Language.getInstance()) {
                    this.at$translatedContents = null;
                }
                if (this.at$translatedContents != null) {
                    cir.setReturnValue(this.at$translatedContents);
                    return;
                }
                if (this.contents instanceof LiteralContents literalContents) {
                    String text = literalContents.text();
                    if (TranslatorHelper.shouldTranslate(text)) {
                        TranslatorHelper.translate(text, translate -> at$translatedContents = new LiteralContents(translate));
                        if (this.at$translatedContents != null) {
                            cir.setReturnValue(this.at$translatedContents);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    @Inject(method = "getVisualOrderText", at = @At("HEAD"), cancellable = true)
    private void getVisualOrderTextMixin(CallbackInfoReturnable<FormattedCharSequence> cir) {
        if (!this.at$shouldTranslate) return;
        if (this.at$translatedContents == null) return;
        try {
            if (ScreenTranslationHelper.shouldTranslate(Minecraft.getInstance().screen)) {
                if (this.decomposedWith != Language.getInstance()) {
                    this.at$translatedVisualOrderText = null;
                }
                if (this.at$translatedVisualOrderText != null) {
                    cir.setReturnValue(this.at$translatedVisualOrderText);
                    return;
                }
                this.at$translatedVisualOrderText = Language.getInstance().getVisualOrder(MutableComponent.create(this.at$translatedContents));
            }
        } catch (Throwable ignored) {
        }
    }

    @ModifyArg(method = "getVisualOrderText", at = @At(value = "INVOKE", target = "Lnet/minecraft/locale/Language;getVisualOrder(Lnet/minecraft/network/chat/FormattedText;)Lnet/minecraft/util/FormattedCharSequence;"))
    private FormattedText getVisualOrderTextMixin(FormattedText var1) {
        MutableComponent component = this.copy();
        ((MutableComponentAccessor) component).at$shouldTranslate(false);
        return component;
    }

    @Override
    public boolean at$shouldTranslate() {
        return this.at$shouldTranslate;
    }

    @Override
    public void at$shouldTranslate(boolean shouldTranslate) {
        this.at$shouldTranslate = shouldTranslate;
    }

    @Override
    public Language at$decomposedWith() {
        return this.decomposedWith;
    }

    @Override
    public @NotNull MutableComponent copy() {
        boolean temp = this.at$shouldTranslate;
        this.at$shouldTranslate(false);
        MutableComponent component = MutableComponent.create(this.getContents());
        this.at$shouldTranslate(temp);
        component.setStyle(this.style);
        for (Component sibling : this.siblings) {
            if (sibling instanceof MutableComponent mutableComponent) {
                MutableComponentAccessor accessor = (MutableComponentAccessor) mutableComponent;
                temp = accessor.at$shouldTranslate();
                accessor.at$shouldTranslate(false);
                component.append(mutableComponent.copy());
                accessor.at$shouldTranslate(temp);
            }
        }
        return component;
    }

    @Override
    public @NotNull MutableComponent plainCopy() {
        boolean temp = this.at$shouldTranslate;
        this.at$shouldTranslate(false);
        MutableComponent component = MutableComponent.create(this.getContents());
        this.at$shouldTranslate(temp);
        return component;
    }
}
