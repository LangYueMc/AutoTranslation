package me.langyue.autotranslation.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.gui.widgets.AutoTranslationIcon;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Redirect(method = "render(FJZ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"
            )
    )
    public void renderScreenPost(Screen instance, PoseStack poseStack, int i, int j, float f) {
        ScreenTranslationHelper.ready();
        instance.render(poseStack, i, j, f);
        if (ScreenTranslationHelper.hideIcon(instance)) return;
        AutoTranslationIcon autoTranslationIcon = AutoTranslationIcon.getInstance();
        if (!AutoTranslation.CONFIG.icon.alwaysDisplay && !ScreenTranslationHelper.getScreenStatus(instance)) {
            instance.children().remove(autoTranslationIcon);
            return;
        }
        autoTranslationIcon.render(poseStack, i, j, f);
        if (instance.children().contains(autoTranslationIcon)) return;
        try {
            ((List<GuiEventListener>) instance.children()).add(autoTranslationIcon);
        } catch (Throwable ignored) {
        }
    }
}
