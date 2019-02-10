package org.spongepowered.common.command.brigadier.context;

import com.google.common.collect.ImmutableMap;
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
import org.spongepowered.common.command.parameter.SpongeParameterKey;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeCommandContext<S> extends CommandContext<S> implements org.spongepowered.api.command.parameter.CommandContext {

    private final Multimap<SpongeParameterKey<?>, Object> argumentMultimap;
    private final Cause cause;
    private final boolean completing;

    @Nullable private MessageChannel channel;

    // null is not yet loaded, empty optional is no object found
    @Nullable private Optional<Subject> subject;
    @Nullable private Optional<Location> location;
    @Nullable private Optional<BlockSnapshot> blockSnapshot;

    public SpongeCommandContext(
            S source,
            String input,
            Map<String, ParsedArgument<S, ?>> brigArguments,
            Multimap<SpongeParameterKey<?>, Object> arguments,
            Command<S> command,
            Map<CommandNode<S>, StringRange> nodes,
            StringRange range,
            @Nullable CommandContext<S> child,
            @Nullable RedirectModifier<S> modifier,
            boolean forks,
            Cause cause,
            boolean completing) {
        super(source, input, brigArguments, command, nodes, range, child, modifier, forks);
        this.cause = cause;
        this.completing = completing;
        this.argumentMultimap = arguments;
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
