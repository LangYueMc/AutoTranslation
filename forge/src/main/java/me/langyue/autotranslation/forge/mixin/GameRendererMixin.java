package me.langyue.autotranslation.forge.mixin;

import me.langyue.autotranslation.ScreenTranslationHelper;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render(FJZ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
            )
    )
    public void renderScreenPost(float f, long l, boolean bl, CallbackInfo ci) {
        ScreenTranslationHelper.ready();
    }
}
