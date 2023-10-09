package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin {

    @Inject(method = "setDisplayName", at = @At("HEAD"))
    private void setCustomNameMixin(Component component, CallbackInfo ci) {
        if (component instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "setPlayerPrefix", at = @At("HEAD"))
    private void setPlayerPrefixMixin(Component component, CallbackInfo ci) {
        if (component instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "setPlayerSuffix", at = @At("HEAD"))
    private void setPlayerSuffixMixin(Component component, CallbackInfo ci) {
        if (component instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"))
    private void getDisplayNameMixin(CallbackInfoReturnable<Component> cir) {
        if (cir.getReturnValue() instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "formatNameForTeam", at = @At("HEAD"))
    private static void formatNameForTeamMixin(Team team, Component component, CallbackInfoReturnable<MutableComponent> cir) {
        if (component instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "formatNameForTeam", at = @At("RETURN"))
    private static void formatNameForTeamReturnMixin(Team team, Component component, CallbackInfoReturnable<MutableComponent> cir) {
        if (cir.getReturnValue() instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }

}
