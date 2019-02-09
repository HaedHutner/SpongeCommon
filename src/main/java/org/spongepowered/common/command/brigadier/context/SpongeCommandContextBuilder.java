package org.spongepowered.common.command.brigadier.context;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import org.spongepowered.api.event.cause.Cause;

public class SpongeCommandContextBuilder<S> extends CommandContextBuilder<S> {

    // The Sponge command system allows for multiple arguments to be stored under the same key.
    private final LinkedHashMultimap<String, ParsedArgument<S, ?>> arguments = LinkedHashMultimap.create();
    private final Cause cause;
    private final boolean completing;

    public SpongeCommandContextBuilder(CommandDispatcher<S> dispatcher, S source, int start, Cause cause, boolean completing) {
        super(dispatcher, source, start);
        this.cause = cause;
        this.completing = completing;
    }

    @Override
    public SpongeCommandContextBuilder<S> withArgument(String name, ParsedArgument<S, ?> argument) {
        this.arguments.put(name, argument);
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
        copyMixinCommandContextBuilder.setRedirectModifier(mixinCommandContextBuilder.getRedirectModifier());
        copyMixinCommandContextBuilder.setFork(mixinCommandContextBuilder.isForks());
        copyMixinCommandContextBuilder.setStringRange(copy.getRange());
        return copy;
    }

    @Override
    public CommandContext<S> build(final String input) {
        final CommandContextBuilder<S> child = getChild();
        // TODO: this might not be needed for the derived class, come back when mixins are working
        final IMixinCommandContextBuilder<S> mixinCommandContextBuilder = (IMixinCommandContextBuilder<S>) this;
        return new SpongeCommandContext<>(
                getSource(),
                input,
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

}
