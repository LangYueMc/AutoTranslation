package me.langyue.autotranslation.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.gui.widgets.AutoTranslationIcon;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Redirect(method = "render(FJZ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"
            )
    )
    private void renderScreenPost(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ScreenTranslationHelper.ready();
        ForgeHooksClient.drawScreen(screen, poseStack, mouseX, mouseY, partialTick);
        if (ScreenTranslationHelper.hideIcon(screen)) return;
        AutoTranslationIcon autoTranslationIcon = AutoTranslationIcon.getInstance();
        if (!AutoTranslation.CONFIG.icon.alwaysDisplay && !ScreenTranslationHelper.getScreenStatus(screen)) {
            screen.children().remove(autoTranslationIcon);
            return;
        }
        autoTranslationIcon.render(poseStack, mouseX, mouseY, partialTick);
        if (screen.children().contains(autoTranslationIcon)) return;
        try {
            ((List<GuiEventListener>) screen.children()).add(autoTranslationIcon);
        } catch (Throwable ignored) {
        }
    }
}
