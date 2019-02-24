package org.spongepowered.common.command.brigadier;

import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;

public class SpongeStringReader extends StringReader implements ArgumentReader.Mutable {

    private static final char SYNTAX_QUOTE = '"';

    public SpongeStringReader(String string) {
        super(string);
    }

    public SpongeStringReader(StringReader other) {
        super(other);
    }

    @Override
    public String getInput() {
        return getString();
    }

    @Override
    public char parseChar() {
        return read();
    }

    @Override
    public int parseInt() throws ArgumentParseException {
        try {
            return readInt();
        } catch (CommandSyntaxException e) {
            throw new ArgumentParseException(t("Could not parse an integer"), e, getString(), getCursor());
        }
    }

    @Override
    public double parseDouble() throws ArgumentParseException {
        try {
            return readDouble();
        } catch (CommandSyntaxException e) {
            throw new ArgumentParseException(t("Could not parse a double"), e, getString(), getCursor());
        }
    }

    @Override
    public float parseFloat() throws ArgumentParseException {
        try {
            return readFloat();
        } catch (CommandSyntaxException e) {
            throw new ArgumentParseException(t("Could not parse a float"), e, getString(), getCursor());
        }
    }

    @Override
    public String parseUnquotedString() throws ArgumentParseException {
        final int start = getCursor();
        while (canRead() && !Character.isWhitespace(peek())) {
            skip();
        }
        return getString().substring(start, getCursor());
    }

    @Override
    public String parseString() throws ArgumentParseException {
        try {
            if (canRead() && peek() == SYNTAX_QUOTE) {
                return readQuotedString();
            } else {
                return readUnquotedString();
            }
        } catch (CommandSyntaxException e) {
            throw new ArgumentParseException(t("Could not parse string"), e, getString(), getCursor());
        }
    }

    @Override
    public boolean parseBoolean() throws ArgumentParseException {
        try {
            return readBoolean();
        } catch (CommandSyntaxException e) {
            throw new ArgumentParseException(t("Could not parse a boolean"), e, getString(), getCursor());
        }
    }

    @Override
    public SpongeImmutableArgumentReader getImmutable() {
        return new SpongeImmutableArgumentReader(getString(), getCursor());
    }

    @Override
    public void setState(ArgumentReader state) throws IllegalArgumentException {
        if (state.getInput().equals(getString())) {
            setCursor(state.getCursor());
        } else {
            throw new IllegalArgumentException("The provided ArgumentReader does not match this ArgumentReader");
        }
    }

}
