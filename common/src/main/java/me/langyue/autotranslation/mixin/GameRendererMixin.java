package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.ScreenTranslationHelper;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render(FJZ)V", at = @At("HEAD"))
    public void renderScreenPre(float f, long l, boolean bl, CallbackInfo ci) {
        // forge 修改了 GameRenderer 的 Screen Renderer，所以只能分开写
        ScreenTranslationHelper.unready();
    }
}
