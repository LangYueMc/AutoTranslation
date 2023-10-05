package me.langyue.autotranslation.forge.mixin;

import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.TranslatorHelper;
import me.langyue.autotranslation.accessor.MutableComponentAccessor;
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
@Deprecated
public abstract class ForgeGuiMixin extends Gui {

    public ForgeGuiMixin(Minecraft arg, ItemRenderer arg2) {
        super(arg, arg2);
    }

    @ModifyArg(remap = false, method = "renderHUDText", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"))
    public String renderHUDTextDrawStringPre(String msg) {
        if (ScreenTranslationHelper.shouldTranslate(Minecraft.getInstance().screen)) {
            TranslatorHelper.addBlacklist(msg);
        }
        return msg;
    }

    @Inject(remap = false, method = "renderRecordOverlay", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"))
    public void renderRecordOverlayDrawStringPre(int width, int height, float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (this.overlayMessageString == null) return;
        ((MutableComponentAccessor) this.overlayMessageString).at$shouldTranslate(false);
    }

    @Inject(remap = false, method = "renderTitle", at = @At(value = "INVOKE", target =
            "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V"))
    public void renderTitleDrawStringPre(int width, int height, float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (this.title == null) return;
        ((MutableComponentAccessor) this.title).at$shouldTranslate(false);
        if (this.subtitle == null) return;
        ((MutableComponentAccessor) this.subtitle).at$shouldTranslate(false);
    }
}
