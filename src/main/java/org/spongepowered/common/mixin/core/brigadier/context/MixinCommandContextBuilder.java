package org.spongepowered.common.mixin.core.brigadier.context;

import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.tree.CommandNode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.command.brigadier.context.IMixinCommandContextBuilder;

import java.util.Map;

import javax.annotation.Nullable;

@Mixin(CommandContextBuilder.class)
public abstract class MixinCommandContextBuilder<S> implements IMixinCommandContextBuilder<S> {

    @Shadow @Final private Map<CommandNode<S>, StringRange> nodes;
    @Shadow private StringRange range;
    @Shadow @Nullable private RedirectModifier<S> modifier = null;
    @Shadow private boolean forks;

    public RedirectModifier<S> getRedirectModifier() {
        return this.modifier;
    }

    public boolean isForks() {
        return this.forks;
    }

    @Override
    public void putNodes(Map<CommandNode<S>, StringRange> nodes) {
        this.nodes.putAll(nodes);
    }

    @Override
    public void setRedirectModifier(@Nullable RedirectModifier<S> redirectModifier) {
        this.modifier = redirectModifier;
    }

    @Override
    public void setFork(boolean fork) {
        this.forks = fork;
    }

    @Override public boolean isFork() {
        return this.forks;
    }
}
