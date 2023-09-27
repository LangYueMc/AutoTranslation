package me.langyue.autotranslation.forge.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import me.langyue.autotranslation.gui.ScreenManager;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeGui.class)
public abstract class ForgeGuiMixin extends Gui {

    public ForgeGuiMixin(Minecraft arg, ItemRenderer arg2) {
        super(arg, arg2);
    }

    @ModifyArg(remap = false, method = "renderHUDText", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"))
    public String renderHUDTextDrawStringPre(String msg) {
        if (ScreenManager.shouldTranslate(Minecraft.getInstance().screen)) {
            TranslatorManager.addBlacklist(msg);
        }
        return msg;
    }

    @Inject(remap = false, method = "renderRecordOverlay", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"))
    public void renderRecordOverlayDrawStringPre(int width, int height, float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (this.overlayMessageString == null) return;
        MutableComponentAccessor componentAccessor = (MutableComponentAccessor) this.overlayMessageString;
        if (componentAccessor.isLiteral()) {
            componentAccessor.setTranslated(true);
        }
    }

    @Inject(remap = false, method = "renderTitle", at = @At(value = "INVOKE", target =
            "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V"))
    public void renderTitleDrawStringPre(int width, int height, float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (this.title == null) return;
        MutableComponentAccessor title = (MutableComponentAccessor) this.title;
        if (title.isLiteral()) {
            title.setTranslated(true);
        }
        if (this.subtitle == null) return;
        MutableComponentAccessor subtitle = (MutableComponentAccessor) this.subtitle;
        if (subtitle.isLiteral()) {
            subtitle.setTranslated(true);
        }
    }
}
