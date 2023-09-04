package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import me.langyue.autotranslation.gui.ScreenManager;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Tooltip.class)
public class TooltipMixin {

    @Mutable
    @Shadow
    @Final
    private Component message;

    @Shadow
    private @Nullable List<FormattedCharSequence> cachedTooltip;

    @Redirect(method = "splitTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;"))
    private static List<FormattedCharSequence> splitTooltipMixin(Font instance, FormattedText formattedText, int i) {
        if (formattedText instanceof Component component) {
            if (((MutableComponentAccessor) (Object) component).isLiteral()) {
                if (ScreenManager.shouldTranslate(Minecraft.getInstance().screen)) {
                    String content = component.getString();
                    if (TranslatorManager.shouldTranslate(content, content)) {
                        String t = TranslatorManager.translate(content, null);
                        if (t != null && !t.equals(content)) {
                            return instance.split(Component.literal(t), i);
                        }
                    }
                }
            }
        }
        return instance.split(formattedText, i);
    }

    @Inject(method = "toCharSequence", at = @At("HEAD"))
    private void toCharSequenceMixin(Minecraft minecraft, CallbackInfoReturnable<List<FormattedCharSequence>> cir) {
        if (((MutableComponentAccessor) (Object) this.message).isLiteral()) {
            if (ScreenManager.shouldTranslate(minecraft.screen)) {
                String content = this.message.getString();
                if (TranslatorManager.shouldTranslate(content, content)) {
                    TranslatorManager.translate(content, t -> {
                        if (t != null && !t.equals(content)) {
                            this.message = Component.literal(t);
                            this.cachedTooltip = Tooltip.splitTooltip(minecraft, this.message);
                        }
                    });
                }
            }
        }
    }
}
