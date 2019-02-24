package org.spongepowered.common.command.parameter;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Completions;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.common.command.CommandHelper;

import java.util.concurrent.CompletableFuture;

public class SpongeValueCompleter implements SuggestionProvider<Cause>, ValueCompleter {

    private final ValueCompleter completer;

    public SpongeValueCompleter(ValueCompleter completer) {
        this.completer = completer;
    }

    @Override
    public void complete(Completions.Builder completionBuilder, CommandContext context) {
        this.completer.complete(completionBuilder, context);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(com.mojang.brigadier.context.CommandContext<Cause> context, SuggestionsBuilder builder)
            throws CommandSyntaxException {
        complete((Completions.Builder) builder, CommandHelper.fromBrig(context));
        return builder.buildFuture();
    }

}
