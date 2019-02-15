package org.spongepowered.common.command.manager;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public class SpongeCommandMapping implements CommandMapping {

    private final String alias;
    private final Set<String> allAliases;
    private final PluginContainer container;
    @Nullable private final Command command;
    private final boolean isSpongeCommand;

    public SpongeCommandMapping(String alias, Set<String> allAliases, PluginContainer container, @Nullable Command command, boolean isSpongeCommand) {
        this.alias = alias;
        this.allAliases = ImmutableSet.copyOf(allAliases);
        this.container = container;
        this.command = command;
        this.isSpongeCommand = isSpongeCommand;
    }

    @Override
    public String getPrimaryAlias() {
        return this.alias;
    }

    @Override
    public Set<String> getAllAliases() {
        return this.allAliases;
    }

    @Override
    public PluginContainer getOwningPlugin() {
        return this.container;
    }

    @Override
    public Optional<Command> getCommand() {
        return Optional.ofNullable(this.command);
    }

    public boolean isRegisteredAsSpongeCommand() {
        return this.isSpongeCommand;
    }

}
