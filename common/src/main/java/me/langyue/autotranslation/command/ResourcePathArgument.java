package me.langyue.autotranslation.command;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.langyue.autotranslation.resource.ResourceManager;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;
import java.util.LinkedHashSet;

public class ResourcePathArgument {

    public static final String NAME = "namespace";

    private static SuggestionProvider<CommandSourceStack> suggests;
    private static final Collection<String> NAMESPACES = new LinkedHashSet<>() {{
        add(ResourceManager.NO_KEY_TRANS_STORE_NAMESPACE);
    }};

    public static void addExamples(Collection<String> examples) {
        NAMESPACES.addAll(examples);
        suggests = null;
    }

    public static SuggestionProvider<CommandSourceStack> getSuggests() {
        if (suggests == null) {
            suggests = (context, builder) -> {
                NAMESPACES.forEach(builder::suggest);
                return builder.buildFuture();
            };
        }
        return suggests;
    }
}
