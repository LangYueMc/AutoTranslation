package me.langyue.autotranslation.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.langyue.autotranslation.resource.ResourceManager;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class AutoTranslationCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("auto_translation")
                .then(Commands.literal("reload")
                        .executes(c -> {
                            ResourceManager.loadResource();
                            return feedback(c.getSource(), Component.translatable("commands.autotranslation.command.reloaded"), true);
                        })
                        .then(Commands.argument(ResourcePathArgument.NAME, StringArgumentType.word())
                                .suggests(ResourcePathArgument.getSuggests())
                                .executes(c -> {
                                    String namespace = c.getArgument(ResourcePathArgument.NAME, String.class);
                                    try {
                                        ResourceManager.loadResource(namespace);
                                    } catch (ResourceLocationException re) {
                                        return feedback(c.getSource(), Component.translatable("commands.autotranslation.command.error.invalid_namespace", namespace), false);
                                    } catch (Throwable e) {
                                        return feedback(c.getSource(), Component.translatable("commands.autotranslation.command.error.reload"), false);
                                    }
                                    return feedback(c.getSource(), Component.translatable("commands.autotranslation.command.reloaded"), true);
                                })
                        )
                ));
    }

    private static int feedback(CommandSourceStack source, Component component, boolean success) {
        Component feedback = Component.translatable("message.prefix.autotranslation").append(component);
        if (success) {
            source.sendSuccess(feedback, false);
            return 1;
        } else {
            source.sendFailure(feedback);
            return 0;
        }
    }
}
