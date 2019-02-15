package org.spongepowered.common.command;

import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.command.brigadier.SpongeCommandDispatcher;

import java.util.Collection;
import java.util.Optional;

public abstract class SpongeCommandManager<S> extends SpongeCommandDispatcher<S> implements CommandManager {

    @Override
    public Optional<CommandMapping> register(PluginContainer container, Command command, String primaryAlias, String... secondaryAliases) {
        return Optional.empty();
    }

    @Override
    public Optional<CommandMapping> unregister(PluginContainer container, String alias) {
        return Optional.empty();
    }

    @Override
    public Collection<CommandMapping> unregisterAll(PluginContainer container) {
        return null;
    }

    @Override
    public Collection<PluginContainer> getPlugins() {
        return null;
    }
}
