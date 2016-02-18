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
package org.spongepowered.common.event.tracking;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.event.tracking.phase.SpawningPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.WorldPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.CaptureType;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * A simple utility for aiding in tracking, either with resolving notifiers
 * and owners, or proxying out the logic for ticking a block, entity, etc.
 */
public class TrackingHelper {

    public static final String CURRENT_TICK_BLOCK = "CurrentTickBlock";
    public static final String RESTORING_BLOCK = "RestoringBlock";
    public static final String POPULATOR_CAPTURE_MAP = "PopulatorCaptureMap";
    public static final String CAPTURED_POPULATOR = "PopulatorType";
    public static final String CAPTURED_PACKET = "Packet";
    public static final String OPEN_CONTAINER = "OpenContainer";
    public static final String CURSOR = "Cursor";
    public static final String ITEM_USED = "ItemUsed";
    public static final String IGNORING_CREATIVE = "IgnoringCreative";
    public static final String PACKET_PLAYER = "PacketPlayer";

    public static boolean fireMinecraftBlockEvent(CauseTracker causeTracker, WorldServer worldIn, BlockEventData event,
            Map<BlockPos, User> trackedBlockEvents) {
        IBlockState currentState = worldIn.getBlockState(event.getPosition());
        final World minecraftWorld = causeTracker.getMinecraftWorld();
        final IMixinWorld mixinWorld = causeTracker.getMixinWorld();
        final BlockSnapshot currentTickBlock = mixinWorld.createSpongeBlockSnapshot(currentState, currentState.getBlock().getActualState(currentState,
                minecraftWorld, event.getPosition()), event.getPosition(), 3);
        final PhaseContext phaseContext = PhaseContext.start()
                .add(NamedCause.source(currentTickBlock));
        if (trackedBlockEvents.get(event.getPosition()) != null) {
            User user = trackedBlockEvents.get(event.getPosition());
            phaseContext.add(NamedCause.notifier(user));
        }
        phaseContext.complete();
        causeTracker.switchToPhase(TrackingPhases.GENERAL, WorldPhase.Tick.TICKING_BLOCK, phaseContext);
        boolean result = worldIn.fireBlockEvent(event);
        causeTracker.completePhase();
        trackedBlockEvents.remove(event.getPosition());
        return result;
    }

    public static void randomTickBlock(CauseTracker causeTracker, Block block, BlockPos pos, IBlockState state, Random random) {
        final IMixinWorld mixinWorld = causeTracker.getMixinWorld();
        final World minecraftWorld = causeTracker.getMinecraftWorld();
        final BlockSnapshot currentTickBlock = mixinWorld.createSpongeBlockSnapshot(state, state.getBlock().getActualState(state,
                minecraftWorld, pos), pos, 0);
        causeTracker.switchToPhase(TrackingPhases.GENERAL, WorldPhase.Tick.RANDOM_TICK_BLOCK, PhaseContext.start()
                .add(NamedCause.source(currentTickBlock))
                .complete());
        block.randomTick(minecraftWorld, pos, state, random);
        causeTracker.completePhase();
    }

    public static void updateTickBlock(CauseTracker causeTracker, Block block, BlockPos pos, IBlockState state, Random random) {
        final IMixinWorld mixinWorld = causeTracker.getMixinWorld();
        final World minecraftWorld = causeTracker.getMinecraftWorld();
        BlockSnapshot snapshot = mixinWorld.createSpongeBlockSnapshot(state, state.getBlock().getActualState(state, minecraftWorld, pos), pos, 0);
        causeTracker.switchToPhase(TrackingPhases.WORLD, WorldPhase.Tick.TICKING_BLOCK, PhaseContext.start()
                .add(NamedCause.source(snapshot))
                .complete());
        block.updateTick(minecraftWorld, pos, state, random);
        causeTracker.completePhase();
    }

    public static boolean doInvalidTransactionsExist(List<Transaction<BlockSnapshot>> invalidTransactions, Iterator<Entity> iter, Entity currentEntity) {
        if (!invalidTransactions.isEmpty()) {
            // check to see if this drop is invalid and if so, remove
            boolean invalid = false;
            for (Transaction<BlockSnapshot> blockSnapshot : invalidTransactions) {
                if (blockSnapshot.getOriginal().getLocation().get().getBlockPosition().equals(currentEntity.getLocation().getBlockPosition())) {
                    invalid = true;
                    iter.remove();
                    break;
                }
            }
            if (invalid) {
                return true;
            }
        }
        return false;
    }

    public static Cause identifyCauses(Cause cause, List<BlockSnapshot> capturedSnapshots, World world) {
        if (!cause.first(User.class).isPresent() && !(capturedSnapshots.size() > 0
                                                      && ((SpongeBlockSnapshot) capturedSnapshots.get(0)).captureType
                                                         == CaptureType.DECAY)) {
            if ((cause.first(BlockSnapshot.class).isPresent() || cause.first(TileEntity.class).isPresent())) {
                // Check for player at pos of first transaction
                Optional<BlockSnapshot> snapshot = cause.first(BlockSnapshot.class);
                Optional<TileEntity> te = cause.first(TileEntity.class);
                BlockPos pos;
                if (snapshot.isPresent()) {
                    pos = VecHelper.toBlockPos(snapshot.get().getPosition());
                } else {
                    pos = ((net.minecraft.tileentity.TileEntity) te.get()).getPos();
                }
                net.minecraft.world.chunk.Chunk chunk = world.getChunkFromBlockCoords(pos);
                if (chunk != null) {
                    IMixinChunk spongeChunk = (IMixinChunk) chunk;

                    Optional<User> owner = spongeChunk.getBlockOwner(pos);
                    Optional<User> notifier = spongeChunk.getBlockNotifier(pos);
                    if (notifier.isPresent() && !cause.containsNamed(NamedCause.NOTIFIER)) {
                        cause = cause.with(NamedCause.notifier(notifier.get()));
                    }
                    if (owner.isPresent() && !cause.containsNamed(NamedCause.OWNER)) {
                        cause = cause.with(NamedCause.owner(owner.get()));
                    }
                }
            } else if (cause.first(Entity.class).isPresent()) {
                Entity entity = cause.first(Entity.class).get();
                if (entity instanceof EntityTameable) {
                    EntityTameable tameable = (EntityTameable) entity;
                    if (tameable.getOwner() != null && !cause.containsNamed(NamedCause.OWNER)) {
                        cause = cause.with(NamedCause.owner(tameable.getOwner()));
                    }
                } else {
                    Optional<User> owner = ((IMixinEntity) entity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
                    if (owner.isPresent() && !cause.contains(NamedCause.OWNER)) {
                        cause = cause.with(NamedCause.owner(owner.get()));
                    }
                }
            }
        }
        return cause;
    }

    static void sendItemChangeToPlayer(EntityPlayerMP player, PhaseContext context) {
        ItemStack preProcessItem = context.firstNamed(TrackingHelper.ITEM_USED, ItemStack.class).orElse(null);
        if (preProcessItem == null) {
            return;
        }

        // handle revert
        player.isChangingQuantityOnly = true;
        player.inventory.mainInventory[player.inventory.currentItem] = preProcessItem;
        Slot slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
        player.openContainer.detectAndSendChanges();
        player.isChangingQuantityOnly = false;
        // force client itemstack update if place event was cancelled
        player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, slot.slotNumber, preProcessItem));
    }

    static void processList(CauseTracker causeTracker, ListIterator<Transaction<BlockSnapshot>> listIterator) {
        while (listIterator.hasPrevious()) {
            Transaction<BlockSnapshot> transaction = listIterator.previous();
            causeTracker.switchToPhase(TrackingPhases.BLOCK, BlockPhase.State.RESTORING_BLOCKS, PhaseContext.start()
                .add(NamedCause.of("Processing", listIterator))
                .complete());
            transaction.getOriginal().restore(true, false);
            causeTracker.completePhase();
        }
    }

    static boolean shouldChainCause(CauseTracker tracker, Cause cause) {
        final Tuple<IPhaseState, PhaseContext> currentPhase = tracker.getPhases().peek();
        if (currentPhase != null) {
            final IPhaseState state = currentPhase.getFirst();
            final PhaseContext context = currentPhase.getSecond();
            Optional<BlockSnapshot> currentTickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class);
            return state == WorldPhase.Tick.TICKING_BLOCK && currentTickingBlock.isPresent()
                   && !context.first(PluginContainer.class).isPresent() && !cause.contains(currentTickingBlock);
        }
        return false;
    }

    public static Tuple<List<EntitySnapshot>, Cause> processSnapshotsForSpawning(Cause cause, org.spongepowered.api.world.World world, List<Entity> capturedEntities, List<Transaction<BlockSnapshot>> invalidTransactions) {
        Iterator<Entity> iter = capturedEntities.iterator();
        ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
        while (iter.hasNext()) {
            Entity currentEntity = iter.next();
            if (TrackingHelper.doInvalidTransactionsExist(invalidTransactions, iter, currentEntity)) {
                continue;
            }
            if (cause.first(User.class).isPresent()) {
                // store user UUID with entity to track later
                User user = cause.first(User.class).get();
                ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, user.getUniqueId());
            } else if (cause.first(Entity.class).isPresent()) {
                IMixinEntity spongeEntity = (IMixinEntity) cause.first(Entity.class).get();
                Optional<User> owner = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
                if (owner.isPresent() && !cause.containsNamed(NamedCause.OWNER)) {
                    cause = cause.with(NamedCause.of(NamedCause.OWNER, owner.get()));
                    ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, owner.get().getUniqueId());
                }
            }
            entitySnapshotBuilder.add(currentEntity.createSnapshot());
        }

        List<EntitySnapshot> entitySnapshots = entitySnapshotBuilder.build();
        return new Tuple<>(entitySnapshots, cause);
    }

    public static void tickTileEntity(CauseTracker causeTracker, ITickable tile) {
        causeTracker.switchToPhase(TrackingPhases.GENERAL, WorldPhase.Tick.TICKING_TILE_ENTITY, PhaseContext.start()
            .add(NamedCause.source(tile))
            .complete());
        checkArgument(tile instanceof TileEntity, "ITickable %s is not a TileEntity!", tile);
        checkNotNull(tile, "Cannot capture on a null ticking tile entity!");
        tile.update();
        causeTracker.completePhase();
    }

    public static void tickEntity(CauseTracker causeTracker, net.minecraft.entity.Entity entityIn) {
        checkArgument(entityIn instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entityIn);
        checkNotNull(entityIn, "Cannot capture on a null ticking entity!");
        causeTracker.switchToPhase(TrackingPhases.GENERAL, WorldPhase.Tick.TICKING_ENTITY, PhaseContext.start()
                .add(NamedCause.source(entityIn))
                .complete());
        entityIn.onUpdate();
        SpongeCommonEventFactory.handleEntityMovement(entityIn);
        causeTracker.completePhase();
    }

}
