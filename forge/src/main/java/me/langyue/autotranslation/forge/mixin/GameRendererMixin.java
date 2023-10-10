package me.langyue.autotranslation.forge.mixin;

import me.langyue.autotranslation.ScreenTranslationHelper;
import me.langyue.autotranslation.gui.widgets.AutoTranslationIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @ModifyArg(method = "render(FJZ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
            ),
            index = 1
    )
    private GuiGraphics setGuiGraphics(GuiGraphics guiGraphics) {
        ScreenTranslationHelper.ready();
        AutoTranslationIcon.setArgs(guiGraphics, null, null, null);
        return guiGraphics;
    }

    @ModifyArg(method = "render(FJZ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
            ),
            index = 2
    )
    private int setMouseX(int mouseX) {
        AutoTranslationIcon.setArgs(null, mouseX, null, null);
        return mouseX;
    }

    @ModifyArg(method = "render(FJZ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
            ),
            index = 3
    )
    private int setMouseY(int mouseY) {
        AutoTranslationIcon.setArgs(null, null, mouseY, null);
        return mouseY;
    }

    @ModifyArg(method = "render(FJZ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
            ),
            index = 4
    )
    private float setMouseY(float partialTick) {
        AutoTranslationIcon.setArgs(null, null, null, partialTick);
        return partialTick;
    }
}
