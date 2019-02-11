/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.brigadier;

import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.command.brigadier.SpongeImmutableArgumentReader;

@Mixin(value = StringReader.class, remap = false)
public abstract class MixinStringReader implements ArgumentReader.Mutable {

    @Shadow @Final private static char SYNTAX_QUOTE;

    @Shadow @Final private String string;
    @Shadow private int cursor;

    @Shadow public abstract char read();
    @Shadow public abstract int readInt() throws CommandSyntaxException;
    @Shadow public abstract double readDouble() throws CommandSyntaxException;
    @Shadow public abstract float readFloat() throws CommandSyntaxException;
    @Shadow public abstract String readUnquotedString() throws CommandSyntaxException;
    @Shadow public abstract char peek();
    @Shadow public abstract void skip();
    @Shadow public abstract boolean readBoolean() throws CommandSyntaxException;

    @Override
    public String getInput() {
        return this.string;
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
            throw new ArgumentParseException(t("Could not parse an integer"), e, this.string, this.cursor);
        }
    }

    @Override
    public double parseDouble() throws ArgumentParseException {
        try {
            return readDouble();
        } catch (CommandSyntaxException e) {
            throw new ArgumentParseException(t("Could not parse a double"), e, this.string, this.cursor);
        }
    }

    @Override
    public float parseFloat() throws ArgumentParseException {
        try {
            return readFloat();
        } catch (CommandSyntaxException e) {
            throw new ArgumentParseException(t("Could not parse a float"), e, this.string, this.cursor);
        }
    }

    @Override
    public String parseUnquotedString() throws ArgumentParseException {
        final int start = this.cursor;
        while (canRead() && !Character.isWhitespace(peek())) {
            skip();
        }
        return string.substring(start, this.cursor);
    }

    @Override
    public String parseString() throws ArgumentParseException {
        try {
            if (canRead() && peek() == SYNTAX_QUOTE) {
                return parseString();
            } else {
                return readUnquotedString();
            }
        } catch (CommandSyntaxException e) {
            throw new ArgumentParseException(t("Could not parse string"), e, this.string, this.cursor);
        }
    }

    @Override
    public boolean parseBoolean() throws ArgumentParseException {
        try {
            return readBoolean();
        } catch (CommandSyntaxException e) {
            throw new ArgumentParseException(t("Could not parse a boolean"), e, this.string, this.cursor);
        }
    }

    @Override
    public SpongeImmutableArgumentReader getImmutable() {
        return new SpongeImmutableArgumentReader(this.string, this.cursor);
    }

    @Override
    public void setState(ArgumentReader state) throws IllegalArgumentException {
        if (state.getInput().equals(this.string)) {
            this.cursor = state.getCursor();
        } else {
            throw new IllegalArgumentException("The provided ArgumentReader does not match this ArgumentReader");
        }
    }

}
