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
package org.spongepowered.common.mixin.core.item.inventory;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.event.item.inventory.EnchantItemEvent;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.slot.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.IMixinSingleBlockCarrier;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.Random;

@Mixin(ContainerEnchantment.class)
public class MixinCarrierContainerEnchantment implements IMixinSingleBlockCarrier {

    @Shadow @Final public net.minecraft.world.World world;
    @Shadow @Final public BlockPos position;

    @Shadow @Final private Random rand;

    @Shadow public int xpSeed;

    @Shadow public IInventory tableInventory;

    private ItemStackSnapshot prevItem;
    private ItemStackSnapshot prevLapis;

    @Override
    public Location getLocation() {
        return new Location(((World) this.world), new Vector3d(this.position.getX(), this.position.getY(), this.position.getZ()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return ((CarriedInventory) this);
    }

    @Redirect(method = "onCraftMatrixChanged", at = @At(value = "INVOKE", target = "calcItemStackEnchantability"))
    private int onCalcItemStackEnchantability(EnchantmentHelper staticHelper, Random random, int option, int power, ItemStack itemStack) {
        // TODO impl: Mixin onCraftMatrixChanged in loop after EnchantmentHelper.calcItemStackEnchantability
        // TODO impl: Forge ForgeEventFactory.onEnchantmentLevelSet
        int levelRequirement = EnchantmentHelper.calcItemStackEnchantability(random, option, power, itemStack);
        levelRequirement = SpongeCommonEventFactory.callEnchantEventLevelRequirement((ContainerEnchantment)(Object) this, this.xpSeed, option, power, itemStack, levelRequirement);
        return levelRequirement;
    }

    @Inject(method = "getEnchantmentList", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "RETURN"))
    private void onBuildEnchantmentList(CallbackInfoReturnable<List<EnchantmentData>> cir, ItemStack stack, int enchantSlot, int level, List<EnchantmentData> list) {
        // TODO impl: Mixin ContainerEnchantment.getEnchantmentList and/or
        //                  EnchantmentHelper.buildEnchantmentList at getEnchantmentDatas
        List<EnchantmentData> newList = SpongeCommonEventFactory
                .callEnchantEventEnchantmentList((ContainerEnchantment) (Object) this, this.xpSeed, stack, enchantSlot, level, list);
        cir.setReturnValue(newList); // TODO only when changed?
    }


    @Inject(method = "enchantItem", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE", target = "onEnchant"))
    private void beforeEnchantItem(CallbackInfoReturnable<Boolean> cir, EntityPlayer playerIn, int option) {
        this.prevItem = ItemStackUtil.snapshotOf(this.tableInventory.getStackInSlot(0));
        this.prevLapis = ItemStackUtil.snapshotOf(this.tableInventory.getStackInSlot(2));
    }

    @Inject(method = "enchantItem", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE", target = "addStat"))
    private void afterEnchantItem(CallbackInfoReturnable<Boolean> cir, EntityPlayer playerIn, int option) {
        ItemStackSnapshot newItem = ItemStackUtil.snapshotOf(this.tableInventory.getStackInSlot(0));
        ItemStackSnapshot newLapis = ItemStackUtil.snapshotOf(this.tableInventory.getStackInSlot(1));

        org.spongepowered.api.item.inventory.Container container = ContainerUtil.fromNative((Container) (Object) this);

        Slot slotItem = container.getSlot(SlotIndex.of(0)).get();
        Slot slotLapis = container.getSlot(SlotIndex.of(1)).get();

        EnchantItemEvent.EnchantItem event =
                SpongeCommonEventFactory.callEnchantEventEnchant((ContainerEnchantment) (Object) this,
                        new SlotTransaction(slotItem, this.prevItem, newItem),
                        new SlotTransaction(slotLapis, this.prevLapis, newLapis),
                        option, this.xpSeed);

        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }



}
