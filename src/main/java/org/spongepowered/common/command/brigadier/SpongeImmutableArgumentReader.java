package org.spongepowered.common.command.brigadier;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import org.spongepowered.api.command.parameter.ArgumentReader;

public class SpongeImmutableArgumentReader implements ArgumentReader.Immutable, ImmutableStringReader {

    private final String input;
    private final int cursor;
    private final int length;
    private final int remaining;

    public SpongeImmutableArgumentReader(String input, int cursor) {
        this.input = input;
        this.cursor = cursor;
        this.length = this.input.length();
        this.remaining = this.length - cursor;
    }

    @Override
    public String getString() {
        return this.input;
    }

    @Override
    public String getInput() {
        return this.input;
    }

    @Override
    public int getRemainingLength() {
        return this.remaining;
    }

    @Override
    public int getTotalLength() {
        return this.length;
    }

    @Override
    public int getCursor() {
        return this.cursor;
    }

    @Override
    public String getRead() {
        return this.input.substring(0, this.cursor);
    }

    @Override
    public String getRemaining() {
        return this.input.substring(this.cursor);
    }

    @Override
    public boolean canRead(int length) {
        return this.cursor + length <= this.length;
    }

    @Override
    public boolean canRead() {
        return canRead(1);
    }

    @Override
    public char peek() {
        return peek(0);
    }

    @Override
    public char peek(int offset) {
        return this.input.charAt(this.cursor + offset);
    }

    @Override
    public ArgumentReader.Mutable getMutable() {
        StringReader reader = new StringReader(this.input);
        reader.setCursor(this.cursor);
        return (ArgumentReader.Mutable) reader;
    }
}
