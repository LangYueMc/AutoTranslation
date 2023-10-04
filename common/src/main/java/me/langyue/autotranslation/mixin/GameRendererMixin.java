package me.langyue.autotranslation.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.gui.widgets.AutoTranslationIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    Minecraft minecraft;

    @Unique
    private AutoTranslationIcon autoTranslationIcon;

    @Inject(method = "render(FJZ)V", at = @At("HEAD"))
    public void renderScreenPre(float f, long l, boolean bl, CallbackInfo ci) {
        // forge 修改了 GameRenderer 的 Screen Renderer，所以只能分开写
        ScreenTranslationHelper.unready();
    }

    @Inject(method = "render(FJZ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;flush()V"
            ),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    public void renderScreenPost(float f, long l, boolean bl, CallbackInfo ci, int i, int j, Window window, Matrix4f matrix4f, PoseStack poseStack, GuiGraphics guiGraphics) {
        if (!AutoTranslation.CONFIG.icon.display) return;
        if (ScreenTranslationHelper.hideIcon(minecraft.screen)) return;
        if (autoTranslationIcon == null) {
            autoTranslationIcon = new AutoTranslationIcon(12, 12, false);
        }
        try {
            ((List<GuiEventListener>) minecraft.screen.children()).add(autoTranslationIcon);
        } catch (Throwable ignored) {
        }
        autoTranslationIcon.render(guiGraphics, i, j, minecraft.getDeltaFrameTime());
    }
}
