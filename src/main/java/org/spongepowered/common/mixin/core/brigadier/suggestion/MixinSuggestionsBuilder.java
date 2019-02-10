package org.spongepowered.common.mixin.core.brigadier.suggestion;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.spongepowered.api.command.parameter.Completions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = SuggestionsBuilder.class, remap = false)
public abstract class MixinSuggestionsBuilder implements Completions.Builder {

    @Shadow @Final private List<Suggestion> result;

    @Shadow public abstract SuggestionsBuilder suggest(final String text);

    @Override
    public Completions.Builder addCompletion(String completion) {
        suggest(completion);
        return this;
    }

    @Override
    public Completions.Builder reset() {
        this.result.clear();
        return this;
    }

}
