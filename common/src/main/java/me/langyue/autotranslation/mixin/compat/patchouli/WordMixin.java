package me.langyue.autotranslation.mixin.compat.patchouli;

import com.mojang.blaze3d.vertex.PoseStack;
import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import vazkii.patchouli.client.book.text.Word;

@Mixin(Word.class)
public class WordMixin {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lvazkii/patchouli/client/book/text/Word;isClusterHovered(DD)Z"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void renderMixin(PoseStack ms, Font font, Style styleOverride, int mouseX, int mouseY, CallbackInfo ci, MutableComponent toRender) {
        // 帕秋莉拆解过的句子就不翻译了，在拆解之前就有翻译
        if (toRender instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }
}
