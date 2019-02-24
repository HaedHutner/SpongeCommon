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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.tree.CommandNode;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.command.CommandHelper;
import org.spongepowered.common.command.parameter.SpongeParameterKey;
import org.spongepowered.common.mixin.core.brigadier.context.MixinCommandContext;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeCommandContext extends CommandContext<Cause> implements org.spongepowered.api.command.parameter.CommandContext {

    private final Multimap<SpongeParameterKey<?>, Object> argumentMultimap;

    @Nullable private MessageChannel channel;

    // null is not yet loaded, empty optional is no object found
    @Nullable private Optional<Subject> subject;
    @Nullable private Optional<Location> location;
    @Nullable private Optional<BlockSnapshot> blockSnapshot;

    public SpongeCommandContext(CommandContext<Cause> causeCommandContext) {
        super(causeCommandContext.getSource(),
                causeCommandContext.getInput(),
                ((MixinCommandContext) causeCommandContext).getArguments(),
                causeCommandContext.getCommand(),
                causeCommandContext.getNodes(),
                causeCommandContext.getRange(),
                causeCommandContext.getChild(),
                causeCommandContext.getRedirectModifier(),
                causeCommandContext.isForked());
        this.argumentMultimap = HashMultimap.create();
    }

    public SpongeCommandContext(
            Cause source,
            String input,
            Map<String, ParsedArgument<Cause, ?>> brigArguments,
            Multimap<SpongeParameterKey<?>, Object> arguments,
            Command<Cause> command,
            Map<CommandNode<Cause>, StringRange> nodes,
            StringRange range,
            @Nullable CommandContext<Cause> child,
            @Nullable RedirectModifier<Cause> modifier,
            boolean forks) {
        super(source, input, brigArguments, command, nodes, range, child, modifier, forks);
        this.argumentMultimap = arguments;
    }

    @Override
    public Cause getCause() {
        return getSource();
    }

    @Override
    public MessageChannel getTargetMessageChannel() {
        if (this.channel == null) {
            this.channel = CommandHelper.getTargetMessageChannel(getSource());
        }

        return this.channel;
    }

    @Override
    public Optional<Subject> getSubject() {
        if (this.subject == null) {
            this.subject = CommandHelper.getSubject(getSource());
        }

        return this.subject;
    }

    @Override
    public Optional<Location> getLocation() {
        if (this.location == null) {
            if (this.blockSnapshot != null && this.blockSnapshot.isPresent()) {
                this.location = CommandHelper.getLocation(getSource(), this.blockSnapshot.get());
            } else {
                this.location = CommandHelper.getLocation(getSource());
            }
        }

        return this.location;
    }

    @Override
    public Optional<BlockSnapshot> getTargetBlock() {
        if (this.blockSnapshot == null) {
            this.blockSnapshot = CommandHelper.getTargetBlock(getSource());
        }

        return this.blockSnapshot;
    }

    @Override public boolean hasAny(Parameter.Key<?> key) {
        return false;
    }

    @Override public <T> Optional<? extends T> getOne(Parameter.Key<T> key) {
        return Optional.empty();
    }

    @Override public <T> T requireOne(Parameter.Key<T> key) throws NoSuchElementException {
        return null;
    }

    @Override public <T> Collection<? extends T> getAll(Parameter.Key<T> key) {
        return null;
    }


}
