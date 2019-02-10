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
package org.spongepowered.common.command.brigadier.context;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.command.parameter.SpongeParameterKey;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeCommandContextBuilder<S> extends CommandContextBuilder<S>
        implements org.spongepowered.api.command.parameter.CommandContext.Builder {

    // The Sponge command system allows for multiple arguments to be stored under the same key.
    private final LinkedHashMultimap<SpongeParameterKey<?>, Object> arguments = LinkedHashMultimap.create();
    private final Cause cause;
    private final boolean completing;

    @Nullable private MessageChannel channel;

    // null is not yet loaded, empty optional is no object found
    @Nullable private Optional<Subject> subject;
    @Nullable private Optional<Location> location;
    @Nullable private Optional<BlockSnapshot> blockSnapshot;

    public SpongeCommandContextBuilder(CommandDispatcher<S> dispatcher, S source, int start, Cause cause, boolean completing) {
        super(dispatcher, source, start);
        this.cause = cause;
        this.completing = completing;
    }

    @Override
    public SpongeCommandContextBuilder<S> withArgument(String name, ParsedArgument<S, ?> argument) {
        // With mods, they'll do anything, so we'll store it under the "Object" class.
        this.arguments.put(new SpongeParameterKey<>(name, Object.class), argument.getResult());
        super.withArgument(name, argument); // for getArguments and any mods that use this.
        return this;
    }

    public SpongeCommandContextBuilder<S> copy() {
        final SpongeCommandContextBuilder<S> copy =
                new SpongeCommandContextBuilder<>(getDispatcher(), getSource(), getRange().getStart(), this.cause, this.completing);
        IMixinCommandContextBuilder<S> mixinCommandContextBuilder = (IMixinCommandContextBuilder<S>) this;
        IMixinCommandContextBuilder<S> copyMixinCommandContextBuilder = (IMixinCommandContextBuilder<S>) copy;
        copy.arguments.putAll(this.arguments);
        copy.withChild(getChild());
        copy.withCommand(getCommand());
        copyMixinCommandContextBuilder.putArguments(getArguments());
        copyMixinCommandContextBuilder.setRedirectModifier(mixinCommandContextBuilder.getRedirectModifier());
        copyMixinCommandContextBuilder.setFork(mixinCommandContextBuilder.isForks());
        copyMixinCommandContextBuilder.setStringRange(copy.getRange());
        return copy;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public MessageChannel getTargetMessageChannel() {
        if (this.channel == null) {
            this.channel = CommandContextHelper.getTargetMessageChannel(this.cause);
        }

        return this.channel;
    }

    @Override
    public Optional<Subject> getSubject() {
        if (this.subject == null) {
            this.subject = CommandContextHelper.getSubject(this.cause);
        }

        return this.subject;
    }

    @Override
    public Optional<Location> getLocation() {
        if (this.location == null) {
            if (this.blockSnapshot != null && this.blockSnapshot.isPresent()) {
                this.location = CommandContextHelper.getLocation(this.cause, this.blockSnapshot.get());
            } else {
                this.location = CommandContextHelper.getLocation(this.cause);
            }
        }

        return this.location;
    }

    @Override
    public Optional<BlockSnapshot> getTargetBlock() {
        if (this.blockSnapshot == null) {
            this.blockSnapshot = CommandContextHelper.getTargetBlock(this.cause);
        }

        return this.blockSnapshot;
    }

    @Override
    public boolean isCompletion() {
        return this.completing;
    }

    @Override
    public boolean hasAny(Parameter.Key<?> key) {
        return this.arguments.containsKey(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<? extends T> getOne(Parameter.Key<T> key) {
        SpongeParameterKey<T> spongeParameterKey = SpongeParameterKey.getSpongeKey(key);
        Collection<?> collection = getFrom(spongeParameterKey);
        if (collection.size() > 1) {
            throw new IllegalArgumentException("More than one entry was found for " + spongeParameterKey.toString());
        }

        return Optional.ofNullable((T) collection.iterator().next());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T requireOne(Parameter.Key<T> key) throws NoSuchElementException, IllegalArgumentException {
        SpongeParameterKey<T> spongeParameterKey = SpongeParameterKey.getSpongeKey(key);
        Collection<?> collection = getFrom(spongeParameterKey);
        if (collection.size() > 1) {
            throw new IllegalArgumentException("More than one entry was found for " + spongeParameterKey.toString());
        } else if (collection.isEmpty()) {
            throw new NoSuchElementException("No entry was found for " + spongeParameterKey.toString());
        }

        return (T) collection.iterator().next();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<? extends T> getAll(Parameter.Key<T> key) {
        return (Collection<? extends T>) getFrom(SpongeParameterKey.getSpongeKey(key));
    }

    private Collection<?> getFrom(SpongeParameterKey<?> key) {
        Collection<?> collection = this.arguments.get(key);
        if (collection == null) {
            return ImmutableSet.of();
        }

        return collection;
    }

    @Override
    public <T> void putEntry(Parameter.Key<T> key, T object) {
        this.arguments.put(SpongeParameterKey.getSpongeKey(key), object);
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public void setState(State state) {

    }

    @Override
    public CommandContext<S> build(final String input) {
        final CommandContextBuilder<S> child = getChild();
        // TODO: this might not be needed for the derived class, come back when mixins are working
        final IMixinCommandContextBuilder<S> mixinCommandContextBuilder = (IMixinCommandContextBuilder<S>) this;
        return new SpongeCommandContext<S>(
                getSource(),
                input,
                getArguments(),
                ImmutableMultimap.copyOf(this.arguments),
                getCommand(),
                getNodes(),
                getRange(),
                child == null ? null : child.build(input),
                mixinCommandContextBuilder.getRedirectModifier(),
                mixinCommandContextBuilder.isForks(),
                this.cause,
                this.completing);
    }

    @Override
    public Builder reset() {
        return null;
    }

}
