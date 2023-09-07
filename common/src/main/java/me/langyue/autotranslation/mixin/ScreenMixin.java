package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import me.langyue.autotranslation.gui.ScreenManager;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Unique
    private final Screen autoTranslation$_this = (Screen) (Object) this;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void keyPressedMixin(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if (AutoTranslation.SCREEN_TRANSLATE_KEYMAPPING.matches(i, j)) {
            ScreenManager.toggleScreenStatus(autoTranslation$_this);
            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "setTooltipForNextRenderPass(Lnet/minecraft/network/chat/Component;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Tooltip;splitTooltip(Lnet/minecraft/client/Minecraft;Lnet/minecraft/network/chat/Component;)Ljava/util/List;"))
    private List<FormattedCharSequence> setTooltipForNextRenderPassMixin(Minecraft minecraft, Component component) {
        if (((MutableComponentAccessor) (Object) component).isLiteral()) {
            if (ScreenManager.shouldTranslate(minecraft.screen)) {
                String content = component.getString();
                if (TranslatorManager.shouldTranslate(content, content)) {
                    String t = TranslatorManager.translate(content, null);
                    if (t != null && !t.equals(content)) {
                        component = Component.literal(content);
                        ((MutableComponentAccessor) (Object) component).setTranslated(true);
                    }
                }
            }
        }
        return Tooltip.splitTooltip(minecraft, component);
    }
}
