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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.Completions;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SpongeWrappedArgumentType<T> implements ArgumentType<T>, ValueParameter<T> {

    private final ArgumentType<T> wrappedType;

    public SpongeWrappedArgumentType(ArgumentType<T> wrappedType) {
        this.wrappedType = wrappedType;
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        return this.wrappedType.parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return this.wrappedType.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return this.wrappedType.getExamples();
    }

    @Override
    public Optional<T> getValue(ArgumentReader args, org.spongepowered.api.command.parameter.CommandContext.Builder context)
            throws ArgumentParseException {
        try {
            return Optional.ofNullable(parse((StringReader) args));
        } catch (CommandSyntaxException e) {
            throw new ArgumentParseException(Text.of(e.getMessage()), e, e.getInput(), e.getCursor());
        }

    }

    @Override
    public void complete(Completions.Builder completionBuilder, org.spongepowered.api.command.parameter.CommandContext context) {
        listSuggestions((CommandContext) context, (SuggestionsBuilder) completionBuilder);
    }

}
