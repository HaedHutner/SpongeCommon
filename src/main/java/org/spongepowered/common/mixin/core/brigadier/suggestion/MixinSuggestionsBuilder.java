package org.spongepowered.common.mixin.core.brigadier.suggestion;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
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
    @Shadow public abstract SuggestionsBuilder suggest(final String text, final Message message);
    @Shadow public abstract SuggestionsBuilder suggest(final int integer);
    @Shadow public abstract SuggestionsBuilder suggest(final int integer, final Message message);

    @Override
    public Completions.Builder suggestion(String completion) {
        suggest(completion);
        return this;
    }

    @Override
    public Completions.Builder suggestion(String completion, String tooltip) {
        suggest(completion, new LiteralMessage(tooltip)); // TODO: can this be translatable text? If so, add the overloads
        return this;
    }

    @Override
    public Completions.Builder suggestion(int completion) {
        suggest(completion);
        return this;
    }

    @Override
    public Completions.Builder suggestion(int completion, String tooltip) {
        suggest(completion, new LiteralMessage(tooltip)); // TODO: can this be translatable text? If so, add the overloads
        return this;
    }

    @Override
    public Completions.Builder reset() {
        this.result.clear();
        return this;
    }

}
