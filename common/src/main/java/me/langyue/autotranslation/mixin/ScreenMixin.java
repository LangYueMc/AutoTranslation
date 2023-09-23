package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import me.langyue.autotranslation.gui.ScreenManager;
import me.langyue.autotranslation.gui.widgets.AutoTranslationIcon;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow
    protected abstract <T extends GuiEventListener & Renderable> T addRenderableWidget(T guiEventListener);

    @Unique
    private final Screen autoTranslation$_this = (Screen) (Object) this;

    private AutoTranslationIcon icon;

    private void addIcon() {
        if (ScreenManager.isInBlacklist(autoTranslation$_this)) return;
        if (icon == null) {
            icon = new AutoTranslationIcon(autoTranslation$_this, 12, 12, ScreenManager.getScreenStatus(autoTranslation$_this));
        }
        this.addRenderableWidget(icon);
    }

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;triggerImmediateNarration(Z)V"))
    private void initMixin(Minecraft minecraft, int i, int j, CallbackInfo ci) {
        addIcon();
    }

    @Inject(method = "rebuildWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init()V", shift = At.Shift.AFTER))
    private void afterInit(CallbackInfo ci) {
        addIcon();
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void renderMixin(CallbackInfo ci) {
        if (icon == null) {
            addIcon();
        }
    }

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
