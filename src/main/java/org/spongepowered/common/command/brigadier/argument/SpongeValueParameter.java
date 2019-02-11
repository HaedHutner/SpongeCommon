/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.command.brigadier.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Completions;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.ValueUsage;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContext;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SpongeValueParameter<T> implements ArgumentType<T>, ValueParameter<T> {

    private final ValueParser<? extends T> parser;
    private final ValueCompleter completer;
    private final ValueUsage usage;

    public static <S> SpongeValueParameter<S> fromValueParameter(ValueParameter<? extends S> parameter) {
        return new SpongeValueParameter<>(parameter, parameter, parameter);
    }

    public static <S> SpongeValueParameter<S> fromParser(ValueParser<? extends S> parser, ValueCompleter completer, ValueUsage usage) {
        return new SpongeValueParameter<>(parser, completer, usage);
    }

    private SpongeValueParameter(ValueParser<? extends T> parser, ValueCompleter completer, ValueUsage usage) {
        this.parser = parser;
        this.completer = completer;
        this.usage = usage;
    }

    @Override
    public Optional<T> getValue(ArgumentReader args, CommandContext.Builder context) throws ArgumentParseException {
        return (Optional<T>) this.parser.getValue(args, context);
    }

    @Override
    public void complete(Completions.Builder completions, CommandContext context) {
        this.completer.complete(completions, context);
    }

    @Override
    public Text getUsage(Cause cause, Text key) {
        return this.usage.getUsage(cause, key);
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        // If we end up here, we have a bot of a problem. We're gonna get the current cause
        // and hope for the best
        Cause cause = Sponge.getCauseStackManager().getCurrentCause();
        return null; // TODO: A fallback. S will be ICommandSource, we also need to keep a copy of the dispatcher somewhere...
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(com.mojang.brigadier.context.CommandContext<S> context, SuggestionsBuilder builder) {
        if (context instanceof SpongeCommandContext) {
            complete((Completions.Builder) builder, (CommandContext) context);
        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return null; // TODO: enable examples in the API
    }

}
