package org.spongepowered.common.command.brigadier.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.token.ArgumentReader;

import java.util.List;
import java.util.Optional;

public class SpongeArgumentType<T>  implements ArgumentType<T>, ValueParameter<T> {

    @Override
    public <S> T parse(StringReader reader) throws CommandSyntaxException {
        return null;
    }

    @Override
    public Optional<T> getValue(ArgumentReader args, CommandContext context) throws ArgumentParseException {
        return Optional.empty();
    }

    @Override
    public List<String> complete(ArgumentReader args, CommandContext context) throws ArgumentParseException {
        return null;
    }


}
