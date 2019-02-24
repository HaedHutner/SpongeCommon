package org.spongepowered.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.common.command.brigadier.SpongeStringReader;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContextBuilder;
import org.spongepowered.common.command.parameter.SpongeCommandNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpongeCommandDispatcher extends CommandDispatcher<Cause> {

    @Override
    public ParseResults<Cause> parse(String command, Cause source) {
        return parse(new SpongeStringReader(command), source);
    }

    @Override
    public ParseResults<Cause> parse(StringReader command, Cause source) {
        return parse(new SpongeStringReader(command), source);
    }

    public ParseResults<Cause> parse(SpongeStringReader command, Cause source) {
        final SpongeCommandContextBuilder context = new SpongeCommandContextBuilder(this, source, 0);
        return parse(getRoot(), command, context);
    }

    /*
     * This method is mostly the same as CommandDispatcher#parseNodes, but due to the fact we want
     * to replace the StringReader and CommandContextBuilder, we override the method.
     *
     * We don't use mixins and we don't want to affect the base class, particularly for mod
     * compatibility.
     *
     * TODO: see if we can get away with not doing this like this.
     */
    private ParseResults<Cause> parse(
            final CommandNode<Cause> node,
            final SpongeStringReader originalReader,
            final SpongeCommandContextBuilder contextSoFar) {
        final Cause source = contextSoFar.getSource();
        Map<CommandNode<Cause>, CommandSyntaxException> errors = null;
        List<SpongePartialParse> potentials = null;
        final int cursor = originalReader.getCursor();

        for (final CommandNode<Cause> child : node.getRelevantNodes(originalReader)) {
            if (!child.canUse(source)) {
                continue;
            }
            final SpongeCommandContextBuilder context = contextSoFar.copy();
            final SpongeStringReader reader = new SpongeStringReader(originalReader);
            try {
                try {
                    if (child instanceof SpongeCommandNode) {
                        ((SpongeCommandNode) child).parseArg(reader, context);
                    } else {
                        child.parse(reader, context);
                    }
                } catch (final RuntimeException ex) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, ex.getMessage());
                }
                if (reader.canRead()) {
                    if (reader.peek() != ARGUMENT_SEPARATOR_CHAR) {
                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().createWithContext(reader);
                    }
                }
            } catch (final CommandSyntaxException ex) {
                if (errors == null) {
                    errors = new LinkedHashMap<>();
                }
                errors.put(child, ex);
                reader.setCursor(cursor);
                continue;
            }

            context.withCommand(child.getCommand());
            if (reader.canRead(child.getRedirect() == null ? 2 : 1)) {
                reader.skip();
                if (child.getRedirect() != null) {
                    final SpongeCommandContextBuilder childContext = new SpongeCommandContextBuilder(this, source, reader.getCursor());
                    childContext.withNode(child.getRedirect(), StringRange.between(cursor, reader.getCursor() - 1));
                    final ParseResults<Cause> parse = parse(child.getRedirect(), reader, childContext);
                    context.withChild(parse.getContext());
                    return new ParseResults<>(context, originalReader.getCursor(), parse.getReader(), parse.getExceptions());
                } else {
                    final ParseResults<Cause> parse = parse(child, reader, context);
                    if (potentials == null) {
                        potentials = new ArrayList<>(1);
                    }
                    potentials.add(new SpongePartialParse(context, parse));
                }
            } else {
                if (potentials == null) {
                    potentials = new ArrayList<>(1);
                }
                potentials.add(new SpongePartialParse(context, new ParseResults<>(context, originalReader.getCursor(), reader, Collections.emptyMap())));
            }
        }

        if (potentials != null) {
            if (potentials.size() > 1) {
                potentials.sort((a, b) -> {
                    if (!a.parse.getReader().canRead() && b.parse.getReader().canRead()) {
                        return -1;
                    }
                    if (a.parse.getReader().canRead() && !b.parse.getReader().canRead()) {
                        return 1;
                    }
                    if (a.parse.getExceptions().isEmpty() && !b.parse.getExceptions().isEmpty()) {
                        return -1;
                    }
                    if (!a.parse.getExceptions().isEmpty() && b.parse.getExceptions().isEmpty()) {
                        return 1;
                    }
                    return 0;
                });
            }
            return potentials.get(0).parse;
        }

        return new ParseResults<>(contextSoFar, originalReader.getCursor(), originalReader, errors == null ? Collections.emptyMap() : errors);
    }

    private static class SpongePartialParse {
        public final SpongeCommandContextBuilder context;
        public final ParseResults<Cause> parse;

        private SpongePartialParse(final SpongeCommandContextBuilder context, final ParseResults<Cause> parse) {
            this.context = context;
            this.parse = parse;
        }

    }

}
