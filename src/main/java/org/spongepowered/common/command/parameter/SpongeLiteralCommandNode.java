package org.spongepowered.common.command.parameter;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.spongepowered.api.event.cause.Cause;

import java.util.function.Predicate;

public class SpongeLiteralCommandNode extends LiteralCommandNode<Cause> {

    public SpongeLiteralCommandNode(String literal, Command<Cause> command,
            Predicate<Cause> requirement, CommandNode<Cause> redirect,
            RedirectModifier<Cause> modifier, boolean forks) {
        super(literal, command, requirement, redirect, modifier, forks);
    }


}
