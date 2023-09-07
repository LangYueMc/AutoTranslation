package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import me.langyue.autotranslation.gui.ScreenManager;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MutableComponent.class)
public class MutableComponentMixin implements MutableComponentAccessor {


    @Unique
    private final Component autoTranslation$_this = (MutableComponent) (Object) this;
    @Unique
    public boolean autoTranslation$translated = false;
    @Shadow
    private FormattedCharSequence visualOrderText;

    @Unique
    public boolean autoTranslation$isLiteral = false;
    @Mutable
    @Shadow
    @Final
    private ComponentContents contents;

    @Inject(method = "create", at = @At("RETURN"))
    private static void initMixin(ComponentContents componentContents, CallbackInfoReturnable<MutableComponent> cir) {
        MutableComponentAccessor accessor = (MutableComponentAccessor) (Object) cir.getReturnValue();
        accessor.isLiteral(componentContents instanceof LiteralContents);
        accessor.setTranslated(componentContents instanceof TranslatableContents);
    }

//    @Redirect(method = "getVisualOrderText", at = @At(value = "INVOKE", target = "Lnet/minecraft/locale/Language;getVisualOrder(Lnet/minecraft/network/chat/FormattedText;)Lnet/minecraft/util/FormattedCharSequence;"))
//    private FormattedCharSequence getVisualOrderTextMixin(Language instance, FormattedText formattedText) {
//        autoTranslation$translateComponent(instance);
//        return instance.getVisualOrder(formattedText);
//    }

    @Inject(method = "getVisualOrderText", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void getVisualOrderTextReturnMixin(CallbackInfoReturnable<FormattedCharSequence> cir, Language language) {
        autoTranslation$translateComponent(language);
    }

    @Unique
    private void autoTranslation$translateComponent(Language language) {
        if (autoTranslation$translated) {
            return;
        }
        if (ScreenManager.shouldTranslate(Minecraft.getInstance().screen)) {
            String content = autoTranslation$_this.getString();
            if (TranslatorManager.shouldTranslate(content, content)) {
                TranslatorManager.translate(content, translate -> {
                    MutableComponent component = Component.literal(translate);
                    this.visualOrderText = language.getVisualOrder(component);
                    this.contents = component.getContents();
                });
            }
        }
        autoTranslation$translated = true;
    }

    @Override
    public void isLiteral(boolean isLiteral) {
        this.autoTranslation$isLiteral = isLiteral;
    }

    @Override
    public boolean isLiteral() {
        return autoTranslation$isLiteral;
    }

    @Override
    public boolean isTranslated() {
        return autoTranslation$translated;
    }

    @Override
    public void setTranslated(boolean translated) {
        this.autoTranslation$translated = translated;
    }
}
