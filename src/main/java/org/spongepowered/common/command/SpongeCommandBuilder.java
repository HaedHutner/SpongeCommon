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
