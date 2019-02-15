package org.spongepowered.common.command.manager;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.common.command.CommandCauseHelper;
import org.spongepowered.common.mixin.core.brigadier.builder.MixinArgumentBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class SpongeCommandManager extends CommandDispatcher<ICommandSource> implements CommandManager {

    // Our Sponge commands will end up on a different node so that we can take advantage of the Cause instead.
    private final CommandDispatcher<Cause> spongeCommandDispatcher = new CommandDispatcher<>();

    private final Map<String, SpongeCommandMapping> commandMappings = new HashMap<>();
    private final Multimap<PluginContainer, SpongeCommandMapping> pluginToCommandMap = HashMultimap.create();

    // For mods and others that use this. We get the plugin container from the CauseStack
    @Override
    public LiteralCommandNode<ICommandSource> register(LiteralArgumentBuilder<ICommandSource> command) {
        // Get the plugin container
        PluginContainer container = Sponge.getCauseStackManager().getCurrentCause().first(PluginContainer.class)
                .orElseThrow(() -> new IllegalStateException("Cannot register command without knowing it's origin."));

        // Get the builder and the first literal.
        String requestedAlias = command.getLiteral();

        String aliasToRegister = container.getId() + ":" + requestedAlias;

        // Check the mappings we have.
        CommandMapping mapping = this.commandMappings.get(aliasToRegister);
        if (mapping != null && mapping.getCommand().isPresent()) {
            // Already registered as a Sponge command, abort, we're not merging Sponge commands.
            throw new IllegalArgumentException("The command " + aliasToRegister + " is already registered.");
        } else if (mapping != null) {
            // This is a mod. Let the trees merge as expected
            return super.register(adjustForNamespace(aliasToRegister, command));
        }

        LiteralArgumentBuilder<ICommandSource> toBuild = adjustForNamespace(aliasToRegister, command);

        // permissions
        final String permission = container.getId() + ".command." + requestedAlias;

        // TODO: can we use IPhaseContexts to our advantage and store the subject?
        //  May just do what mods expect too.
        Predicate<ICommandSource> requirement = toBuild.getRequirement().and(source ->
                CommandCauseHelper.getSubject(
                        Sponge.getCauseStackManager().getCurrentCause()).orElseGet(() -> Sponge.getServer().getConsole()).hasPermission(permission));
        toBuild.requires(requirement);

        boolean registerRootCommand = this.commandMappings.containsKey(requestedAlias);
        Set<String> aliases = new HashSet<>();
        aliases.add(aliasToRegister);
        if (registerRootCommand) {
            aliases.add(requestedAlias);
        }

        // Create the mapping
        SpongeCommandMapping newMapping = new SpongeCommandMapping(aliasToRegister, aliases, container, null, false);
        aliases.forEach(x -> this.commandMappings.put(x, newMapping));
        this.pluginToCommandMap.put(container, newMapping);

        LiteralCommandNode<ICommandSource> node = super.register(toBuild);

        if (registerRootCommand) {
            // Create a redirect
            super.register(LiteralArgumentBuilder.<ICommandSource>literal(requestedAlias).requires(requirement).redirect(node));
        }

        return node;
    }

    // Sponge command
    @Override
    public Optional<CommandMapping> register(PluginContainer container, Command command, String primaryAlias, String... secondaryAliases) {
        return Optional.empty(); // TODO
    }

    @Override
    public Optional<CommandMapping> unregister(CommandMapping mapping) {
        return Optional.empty(); // TODO
    }

    @Override
    public Collection<CommandMapping> unregisterAll(PluginContainer container) {
        Collection<SpongeCommandMapping> mappingsToRemove = this.pluginToCommandMap.get(container);
        ImmutableList.Builder<CommandMapping> commandMappingBuilder = ImmutableList.builder();
        for (CommandMapping toRemove : mappingsToRemove) {
            unregister(toRemove).ifPresent(commandMappingBuilder::add);
        }

        return commandMappingBuilder.build();
    }

    @Override
    public Collection<PluginContainer> getPlugins() {
        return ImmutableSet.copyOf(this.pluginToCommandMap.keySet());
    }


    @Override
    public CommandResult process(String arguments) throws CommandException {
        return process(Sponge.getCauseStackManager().getCurrentCause(), arguments);
    }

    @Override
    public <T extends Subject & MessageReceiver> CommandResult process(T subjectReceiver, String arguments) throws CommandException {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SUBJECT, subjectReceiver);
            frame.addContext(EventContextKeys.MESSAGE_CHANNEL, MessageChannel.fixed(subjectReceiver));
            return process(frame.getCurrentCause(), arguments);
        }
    }

    @Override
    public CommandResult process(Subject subject, MessageChannel receiver, String arguments) throws CommandException {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SUBJECT, subject);
            frame.addContext(EventContextKeys.MESSAGE_CHANNEL, receiver);
            return process(frame.getCurrentCause(), arguments);
        }
    }

    @Override
    public List<String> suggest(String arguments) {
        return suggest(Sponge.getCauseStackManager().getCurrentCause(), arguments);
    }

    @Override
    public <T extends Subject & MessageReceiver> List<String> suggest(T subjectReceiver, String arguments) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SUBJECT, subjectReceiver);
            frame.addContext(EventContextKeys.MESSAGE_CHANNEL, MessageChannel.fixed(subjectReceiver));
            return suggest(frame.getCurrentCause(), arguments);
        }
    }

    @Override
    public List<String> suggest(Subject subject, MessageChannel receiver, String arguments) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SUBJECT, subject);
            frame.addContext(EventContextKeys.MESSAGE_CHANNEL, receiver);
            return suggest(frame.getCurrentCause(), arguments);
        }
    }

    private LiteralArgumentBuilder<ICommandSource> adjustForNamespace(String commandName, LiteralArgumentBuilder<ICommandSource> command) {
        LiteralArgumentBuilder<ICommandSource> toReturn = LiteralArgumentBuilder.<ICommandSource>literal(commandName)
                .executes(command.getCommand())
                .requires(command.getRequirement());
        if (command.getRedirectModifier() != null) {
            toReturn.forward(command.getRedirect(), command.getRedirectModifier(), command.isFork());
        }
        ((MixinArgumentBuilder<CommandSource>) command).setArguments(((MixinArgumentBuilder<CommandSource>) command).getArguments());
        return command;
    }

    // TODO: Temporary logic,?
    private ICommandSource getSource(Cause cause) {
        return CommandCauseHelper.getSubject(cause)
                .filter(x -> x instanceof ICommandSource)
                .map(x -> (ICommandSource) x)
                .orElseGet(() -> (ICommandSource) Sponge.getServer().getConsole());
    }

    private CommandResult process(Cause cause, String arguments) throws CommandException {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            // Get the first part
            String command = arguments.split(" ", 2)[0].toLowerCase();

            // Is the command registered?
            CommandMapping mapping = this.commandMappings.get(command);
            if (mapping == null) {
                // nope.
                throw new CommandException(Text.of("The command " + command + " does not exist."));
            }

            // TODO
            return null;
        }
    }

    private List<String> suggest(Cause cause, String arguments) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            return ImmutableList.of(); // TODO: obviously not this
        }
    }

}
