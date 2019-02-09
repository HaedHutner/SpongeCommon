package org.spongepowered.common.command.brigadier;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.ValueUsage;
import org.spongepowered.api.command.parameter.token.ArgumentReader;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeWrappedArgumentType<T> implements ArgumentType<T>, ValueParameter<T> {

    private final ValueParser<? extends T> parser;
    private final ValueCompleter completer;
    private final ValueUsage usage;

    public static <S> SpongeWrappedArgumentType<S> fromValueParameter(ValueParameter<? extends S> parameter) {
        return new SpongeWrappedArgumentType<>(parameter, parameter, parameter);
    }

    public static <S> SpongeWrappedArgumentType<S> fromParser(ValueParser<? extends S> parser, ValueCompleter completer, ValueUsage usage) {
        return new SpongeWrappedArgumentType<>(parser, completer, usage);
    }

    private SpongeWrappedArgumentType(ValueParser<? extends T> parser, ValueCompleter completer, ValueUsage usage) {
        this.parser = parser;
        this.completer = completer;
        this.usage = usage;
    }

    @Override
    public <S> T parse(StringReader reader) throws CommandSyntaxException {
        // TODO: parse
        return null;
    }

    @Override
    public List<String> complete(ArgumentReader args, CommandContext context) throws ArgumentParseException {
        return this.completer.complete(args, context);
    }

    @Override
    public Optional<T> getValue(ArgumentReader args, CommandContext context) throws ArgumentParseException {
        return (Optional<T>) this.parser.getValue(args, context);
    }

    @Override
    public Text getUsage(Cause cause, Text key) {
        return this.usage.getUsage(cause, key);
    }
}
