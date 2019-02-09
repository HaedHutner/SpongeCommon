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
import org.spongepowered.api.data.LocatableSnapshot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeCommandContext<S> extends CommandContext<S> implements org.spongepowered.api.command.parameter.CommandContext {

    private final Multimap<String, ParsedArgument<S, ?>> argumentMultimap;
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
            Multimap<String, ParsedArgument<S, ?>> arguments,
            Command<S> command,
            Map<CommandNode<S>, StringRange> nodes,
            StringRange range,
            @Nullable CommandContext<S> child,
            @Nullable RedirectModifier<S> modifier,
            boolean forks,
            Cause cause,
            boolean completing) {
        super(source, input, ImmutableMap.of(), command, nodes, range, child, modifier, forks);
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
            this.channel = this.cause.getContext().get(EventContextKeys.MESSAGE_CHANNEL)
                    .orElseGet(() -> this.cause.first(MessageReceiver.class).map(MessageChannel::fixed).orElse(null));
            if (this.channel == null) {
                this.channel = MessageChannel.TO_CONSOLE;
            }
        }

        return this.channel;
    }

    @Override
    public Optional<Subject> getSubject() {
        if (this.subject == null) {
            this.subject = this.cause.getContext().get(EventContextKeys.SUBJECT);
            if (!this.subject.isPresent()) {
                this.subject = this.cause.first(Subject.class);
            }
        }

        return this.subject;
    }

    @Override
    public Optional<Location> getLocation() {
        if (this.location == null) {
            this.location = getTargetBlock().flatMap(LocatableSnapshot::getLocation);
            if (!this.location.isPresent()) {
                this.location = this.cause.first(Locatable.class).map(Locatable::getLocation);
            }
        }

        return this.location;
    }

    @Override
    public Optional<BlockSnapshot> getTargetBlock() {
        if (this.blockSnapshot == null) {
            this.blockSnapshot = this.cause.getContext().get(EventContextKeys.BLOCK_HIT);
            if (!this.blockSnapshot.isPresent()) {
                this.blockSnapshot = this.cause.first(BlockSnapshot.class);
            }
        }

        return this.blockSnapshot;
    }

    @Override
    public boolean isCompletion() {
        return this.completing;
    }

    @Override
    public boolean hasAny(String key) {
        return false;
    }

    @Override
    public <V> V getArgument(String name, Class<V> clazz) {
        // Get all elements with the given name
        this.argumentMultimap.get(name);
        return super.getArgument(name, clazz);
    }

    @Override
    public <T> Optional<T> getOne(Parameter.Value<T> parameter) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> getOne(String key) {
        return Optional.empty();
    }

    @Override
    public <T> T requireOne(Parameter.Value<T> parameter) throws NoSuchElementException {
        return null;
    }

    @Override
    public <T> T requireOne(String key) throws NoSuchElementException {
        return null;
    }

    @Override
    public <T> Collection<T> getAll(Parameter.Value<T> parameter) throws NoSuchElementException {
        return null;
    }

    @Override public <T> Collection<T> getAll(String key) {
        return null;
    }

    @Override public <T> void putEntry(Parameter.Value<T> parameter, T value) throws NoSuchElementException {

    }

    @Override public void putEntry(Text key, Object object) {

    }

    @Override public void putEntry(String key, Object object) {

    }

    @Override public State getState() {
        return null;
    }

    @Override public void setState(State state) {

    }
}
