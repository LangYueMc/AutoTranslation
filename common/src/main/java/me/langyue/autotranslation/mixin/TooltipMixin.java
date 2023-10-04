package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.TranslatorHelper;
import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Tooltip.class)
public class TooltipMixin {

    @Mutable
    @Shadow
    @Final
    private Component message;

    @Unique
    private @Nullable List<FormattedCharSequence> at$translatedTooltip;

    @Inject(method = "toCharSequence", at = @At("HEAD"), cancellable = true)
    private void toCharSequenceMixin(Minecraft minecraft, CallbackInfoReturnable<List<FormattedCharSequence>> cir) {
        MutableComponentAccessor componentAccessor = (MutableComponentAccessor) this.message;
        if (componentAccessor.at$shouldTranslate()) {
            if (ScreenTranslationHelper.shouldTranslate(minecraft.screen)) {
                if (componentAccessor.at$decomposedWith() != Language.getInstance()) {
                    this.at$translatedTooltip = null;
                }
                if (this.at$translatedTooltip != null) {
                    cir.setReturnValue(this.at$translatedTooltip);
                    return;
                }
                String content = this.message.getString();
                if (TranslatorHelper.shouldTranslate(content)) {
                    TranslatorHelper.translate(content, t -> {
                        if (t != null && !t.equals(content)) {
                            this.message = Component.literal(t);
                            at$translatedTooltip = Tooltip.splitTooltip(minecraft, Component.translatable(content));
                        }
                    });
                    if (this.at$translatedTooltip != null) {
                        cir.setReturnValue(this.at$translatedTooltip);
                    }
                }
            }
        }
    }
}
