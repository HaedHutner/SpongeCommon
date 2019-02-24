package org.spongepowered.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.managed.CommandExecutor;
import org.spongepowered.api.event.cause.Cause;

public class SpongeCommandExecutorWrapper implements Command<Cause>, CommandExecutor {

    private final CommandExecutor executor;

    public SpongeCommandExecutorWrapper(CommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public int run(CommandContext<Cause> context) throws CommandSyntaxException {
        try {
            return execute(CommandHelper.fromBrig(context)).getResult();
        } catch (CommandException e) {
            // TODO: Appropriate exception
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create(e.getMessage());
        }
    }

    @Override
    public CommandResult execute(org.spongepowered.api.command.parameter.CommandContext context) throws CommandException {
        return this.executor.execute(context);
    }
}
