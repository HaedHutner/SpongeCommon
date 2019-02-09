package org.spongepowered.common.mixin.core.brigadier;

import com.mojang.brigadier.StringReader;
import org.spongepowered.api.command.parameter.token.ArgumentReader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StringReader.class)
public abstract class MixinStringReader implements ArgumentReader {


}
