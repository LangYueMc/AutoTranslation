package me.langyue.autotranslation.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.resource.ResourceManager;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

public class AutoTranslationCommands {

    private static Supplier<Integer> unconfirmed;

    private static final int waitingTime = 30;

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
                )
                .then(Commands.literal("pack_resource")
                        .executes(c -> {
                            packResource(c.getSource(), false);
                            return 1;
                        })
                        .then(Commands.literal("full")
                                .executes(c -> {
                                    packResource(c.getSource(), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("increment")
                                .executes(c -> {
                                    if (ResourceManager.UNKNOWN_KEYS.isEmpty()) {
                                        feedback(c.getSource(), Component.translatable("commands.autotranslation.command.pack_resource.none"), false);
                                        return 0;
                                    }
                                    packResource(c.getSource(), true);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("confirm")
                        .executes(c -> {
                            if (unconfirmed == null) {
                                return feedback(c.getSource(), Component.translatable("commands.autotranslation.command.unconfirmed.empty"), false);
                            }
                            Integer result = unconfirmed.get();
                            unconfirmed = null;
                            return result;
                        })
                )
        );
    }

    private static void packResource(CommandSourceStack source, boolean increment) {
        waitingForConfirm(() -> {
            try {
                ResourceManager.packResource(increment);
            } catch (Throwable e) {
                AutoTranslation.LOGGER.warn(e.getMessage(), e);
                return feedback(source, Component.translatable("commands.autotranslation.command.error.pack_resource"), false);
            }
            return feedback(source, Component.translatable("commands.autotranslation.command.pack_resource.packed"), true);
        }, source, Component.translatable("commands.autotranslation.command.pack_resource.unconfirmed"));
    }

    private static void waitingForConfirm(final Supplier<Integer> supplier, CommandSourceStack source, Component tip) {
        unconfirmed = supplier;
        if (tip != null) {
            feedback(source, tip, true);
        }
        feedback(source, Component.translatable("commands.autotranslation.command.tip.toBeConfirmed", waitingTime), true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (unconfirmed != null) {
                    unconfirmed = null;
                    feedback(source, Component.translatable("commands.autotranslation.command.tip.canceled"), false);
                }
            }
        }, waitingTime * 1000);
    }

    private static int feedback(CommandSourceStack source, Component component, boolean success) {
        Component feedback = Component.translatable("message.prefix.autotranslation").append(component);
        if (success) {
            source.sendSuccess(() -> feedback, false);
            return 1;
        } else {
            source.sendFailure(feedback);
            return 0;
        }
    }
}
