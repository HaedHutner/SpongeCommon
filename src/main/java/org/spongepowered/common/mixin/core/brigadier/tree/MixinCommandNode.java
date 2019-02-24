package org.spongepowered.common.mixin.core.brigadier.tree;

import com.mojang.brigadier.AmbiguityConsumer;
import com.mojang.brigadier.tree.CommandNode;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.command.brigadier.tree.IMixinCommandNode;

import javax.annotation.Nullable;

@Mixin(value = CommandNode.class, remap = false)
public abstract class MixinCommandNode<S> implements IMixinCommandNode {

    private Cause storedCause = null;

    @Shadow protected abstract boolean isValidInput(String input);

    @Shadow public abstract void findAmbiguities(AmbiguityConsumer<S> consumer);

    public boolean isValidInput(String input, Cause cause) {
        return isValidInput(input);
    }

    public void findAmbiguities(AmbiguityConsumer<S> consumer, @Nullable Cause cause) {
        this.storedCause = cause;
        findAmbiguities(consumer);
        this.storedCause = null;
    }

}
