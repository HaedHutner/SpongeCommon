package org.spongepowered.common.command.brigadier.context;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;

import java.util.Optional;

import javax.annotation.Nullable;

class CommandContextHelper {

    public static MessageChannel getTargetMessageChannel(Cause cause) {
        MessageChannel channel = cause.getContext().get(EventContextKeys.MESSAGE_CHANNEL)
                    .orElseGet(() -> cause.first(MessageReceiver.class).map(MessageChannel::fixed).orElse(null));
        if (channel == null) {
            channel = MessageChannel.TO_CONSOLE;
        }

        return channel;
    }

    public static Optional<Subject> getSubject(Cause cause) {
        Optional<Subject> subject = cause.getContext().get(EventContextKeys.SUBJECT);
        if (!subject.isPresent()) {
            subject = cause.first(Subject.class);
        }

        return subject;
    }

    public static Optional<Location> getLocation(Cause cause) {
        return getLocation(cause, getTargetBlock(cause).orElse(null));
    }

    public static Optional<Location> getLocation(Cause cause, @Nullable BlockSnapshot snapshot) {
        Optional<Location> location = Optional.empty();
        if (snapshot != null) {
            location = snapshot.getLocation();
        }

        if (!location.isPresent()) {
            location = cause.first(Locatable.class).map(Locatable::getLocation);
        }

        return location;
    }

    public static Optional<BlockSnapshot> getTargetBlock(Cause cause) {
        Optional<BlockSnapshot> blockSnapshot = cause.getContext().get(EventContextKeys.BLOCK_HIT);
        if (!blockSnapshot.isPresent()) {
            blockSnapshot = cause.first(BlockSnapshot.class);
        }

        return blockSnapshot;

    }
}
