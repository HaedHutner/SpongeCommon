package org.spongepowered.common.command.parameter;

import com.google.common.base.Preconditions;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.ValueUsage;
import org.spongepowered.api.command.source.CommandSource;
import org.spongepowered.api.event.cause.Cause;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class SpongeParameterValueBuilder<T> implements Parameter.Value.Builder<T> {

    @Nullable private Class<T> valueClass;
    @Nullable private String key;
    private final List<ValueParser<? extends T>> parsers = new ArrayList<>();
    @Nullable private ValueCompleter completer;
    @Nullable private ValueUsage usage;
    @Nullable private BiPredicate<Cause, CommandSource> executionRequirements;
    @Nullable private BiFunction<Cause, CommandSource, T> defaultValueFunction;
    private boolean consumesAll;
    private boolean isOptional;


    @Override
    public Parameter.Value.Builder<T> setKey(String key) {
        this.key = Objects.requireNonNull(key, "The key cannot be null");
        return this;
    }

    @Override
    public Parameter.Value.Builder<T> setValueClass(Class<T> valueClass) throws IllegalArgumentException {

        return null;
    }

    @Override
    public Parameter.Value.Builder<T> parser(ValueParser<? extends T> parser) {
        this.parsers.add(Objects.requireNonNull(parser, "The ValueParser may not be null"));
        return this;
    }

    @Override
    public Parameter.Value.Builder<T> setSuggestions(@Nullable ValueCompleter completer) {
        this.completer = completer;
        return this;
    }

    @Override
    public Parameter.Value.Builder<T> setUsage(@Nullable ValueUsage usage) {
        this.usage = usage;
        return this;
    }

    @Override
    public Parameter.Value.Builder<T> setRequirements(@Nullable BiPredicate<Cause, CommandSource> executionRequirements) {
        this.executionRequirements = executionRequirements;
        return this;
    }

    @Override
    public Parameter.Value.Builder<T> consumeAllRemaining() {
        this.consumesAll = true;
        return this;
    }

    @Override
    public Parameter.Value.Builder<T> optional() {
        this.isOptional = true;
        return this;
    }

    @Override
    public Parameter.Value.Builder<T> orDefault(Supplier<T> defaultValueSupplier) {
        return orDefault((cause, commandSource) -> defaultValueSupplier.get());
    }

    @Override
    public Parameter.Value.Builder<T> orDefault(BiFunction<Cause, CommandSource, T> defaultValueFunction) {
        this.defaultValueFunction = defaultValueFunction;
        return this;
    }

    @Override
    public Parameter.Value<T> build() throws IllegalStateException {
        Preconditions.checkState(this.key != null, "The command key may not be null");
        Preconditions.checkState(!this.parsers.isEmpty(), "There must be parses");
        return null;
    }

    @Override
    public Parameter.Value.Builder<T> reset() {
        this.key = null;
        this.parsers.clear();
        this.completer = null;
        this.usage = null;
        this.defaultValueFunction = null;
        this.isOptional = false;
        this.consumesAll = false;
        return this;
    }

}
