package org.spongepowered.common.mixin.core.brigadier.builder;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArgumentBuilder.class)
public interface MixinArgumentBuilder<S> {

    @Accessor
    RootCommandNode<S> getArguments();

    @Accessor
    void setArguments(RootCommandNode<S> arguments);

}
