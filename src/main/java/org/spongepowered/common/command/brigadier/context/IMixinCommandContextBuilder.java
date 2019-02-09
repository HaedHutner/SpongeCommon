package org.spongepowered.common.command.brigadier.context;

import com.google.common.collect.Maps;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.tree.CommandNode;

import java.util.Map;

import javax.annotation.Nullable;

public interface IMixinCommandContextBuilder<S> {

    void putNodes(Map<CommandNode<S>, StringRange> nodes);

    void setRedirectModifier(@Nullable RedirectModifier<S> redirectModifier);

    void setFork(boolean fork);

    @Nullable RedirectModifier<S> getRedirectModifier();

    boolean isFork();

}
