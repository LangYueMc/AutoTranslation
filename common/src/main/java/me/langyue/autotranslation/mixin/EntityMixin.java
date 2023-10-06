package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "setCustomName", at = @At("HEAD"))
    private void setCustomNameMixin(Component component, CallbackInfo ci) {
        if (component instanceof MutableComponent mutableComponent) {
            ((MutableComponentAccessor) mutableComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "getName", at = @At("RETURN"))
    private void getNameMixin(CallbackInfoReturnable<Component> cir) {
        if (cir.getReturnValue() instanceof MutableComponent mutableComponent) {
            ((MutableComponentAccessor) mutableComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "getTypeName", at = @At("RETURN"))
    private void getTypeNameMixin(CallbackInfoReturnable<Component> cir) {
        if (cir.getReturnValue() instanceof MutableComponent mutableComponent) {
            ((MutableComponentAccessor) mutableComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"))
    private void getDisplayNameMixin(CallbackInfoReturnable<Component> cir) {
        if (cir.getReturnValue() instanceof MutableComponent mutableComponent) {
            ((MutableComponentAccessor) mutableComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "sendSystemMessage", at = @At("HEAD"))
    private void sendSystemMessageMixin(Component component, CallbackInfo ci) {
        if (component instanceof MutableComponent mutableComponent) {
            ((MutableComponentAccessor) mutableComponent).at$shouldTranslate(false);
        }
    }

}
