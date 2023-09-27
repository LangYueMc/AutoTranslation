package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Gui.class)
public class GuiMixin {

    @ModifyArg(remap = false, method = "render", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
    public Component drawStringModify(Component component) {
        if (component == null) return null;
        MutableComponentAccessor componentAccessor = (MutableComponentAccessor) component;
        if (componentAccessor.isLiteral()) {
            componentAccessor.setTranslated(true);
        }
        return component;
    }
}
