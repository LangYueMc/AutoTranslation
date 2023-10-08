package me.langyue.autotranslation.fabric.mixin;

import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.gui.widgets.AutoTranslationIcon;
import net.minecraft.client.gui.GuiGraphics;
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
                    target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
            )
    )
    public void renderScreenPost(Screen instance, GuiGraphics guiGraphics, int i, int j, float f) {
        ScreenTranslationHelper.ready();
        instance.renderWithTooltip(guiGraphics, i, j, f);
        if (!AutoTranslation.CONFIG.icon.display) return;
        if (ScreenTranslationHelper.hideIcon(instance)) return;
        AutoTranslationIcon.getInstance().render(guiGraphics, i, j, f);
        if (instance.children().contains(AutoTranslationIcon.getInstance())) return;
        try {
            ((List<GuiEventListener>) instance.children()).add(AutoTranslationIcon.getInstance());
        } catch (Throwable ignored) {
        }
    }
}
