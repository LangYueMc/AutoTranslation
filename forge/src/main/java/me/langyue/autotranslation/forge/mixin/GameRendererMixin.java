package me.langyue.autotranslation.forge.mixin;

import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.gui.widgets.AutoTranslationIcon;
import net.minecraft.client.gui.GuiGraphics;
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
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
            )
    )
    public void renderScreenPost(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ScreenTranslationHelper.ready();
        ForgeHooksClient.drawScreen(screen, guiGraphics, mouseX, mouseY, partialTick);
        if (!AutoTranslation.CONFIG.icon.display) return;
        if (ScreenTranslationHelper.hideIcon(screen)) return;
        AutoTranslationIcon.getInstance().render(guiGraphics, mouseX, mouseY, partialTick);
        if (screen.children().contains(AutoTranslationIcon.getInstance())) return;
        try {
            ((List<GuiEventListener>) screen.children()).add(AutoTranslationIcon.getInstance());
        } catch (Throwable ignored) {
        }
    }
}
