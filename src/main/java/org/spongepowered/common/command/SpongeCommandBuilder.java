package org.spongepowered.common.command;

import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.managed.ChildExceptionBehavior;
import org.spongepowered.api.command.managed.CommandExecutor;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.flag.Flags;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public class SpongeCommandBuilder implements Command.Builder {

    @Override
    public Command.Builder child(Command child, Iterable<String> keys) {
        return null;
    }

    @Override
    public Command.Builder parameter(Parameter parameter) {
        return null;
    }

    @Override
    public Command.Builder setChildExceptionBehavior(ChildExceptionBehavior exceptionBehavior) {
        return null;
    }

    @Override
    public Command.Builder setExecutor(CommandExecutor executor) {
        return null;
    }

    @Override
    public Command.Builder setExtendedDescription(Function<Cause, Optional<Text>> extendedDescriptionFunction) {
        return null;
    }

    @Override
    public Command.Builder setFlags(Flags flags) {
        return null;
    }

    @Override
    public Command.Builder setShortDescription(Function<Cause, Optional<Text>> descriptionFunction) {
        return null;
    }

    @Override
    public Command.Builder setPermission(@Nullable String permission) {
        return null;
    }

    @Override
    public Command.Builder setExecutionRequirements(@Nullable Predicate<Cause> executionRequirements) {
        return null;
    }

    @Override
    public Command.Builder setCheckRequirementForChildren(boolean required) {
        return null;
    }

    @Override
    public Command build() {
        return null;
    }

    @Override
    public Command.Builder reset() {
        return null;
    }
}
