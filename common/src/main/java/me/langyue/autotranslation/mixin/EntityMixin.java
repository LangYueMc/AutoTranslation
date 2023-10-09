package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "setCustomName", at = @At("HEAD"))
    private void setCustomNameMixin(Component component, CallbackInfo ci) {
        if (component instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "getName", at = @At("RETURN"))
    private void getNameMixin(CallbackInfoReturnable<Component> cir) {
        if (cir.getReturnValue() instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "getTypeName", at = @At("RETURN"))
    private void getTypeNameMixin(CallbackInfoReturnable<Component> cir) {
        if (cir.getReturnValue() instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"))
    private void getDisplayNameMixin(CallbackInfoReturnable<Component> cir) {
        if (cir.getReturnValue() instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "sendMessage", at = @At("HEAD"))
    private void sendMessageMixin(Component component, UUID uUID, CallbackInfo ci) {
        if (component instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }

}
