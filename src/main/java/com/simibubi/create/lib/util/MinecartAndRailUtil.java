package com.simibubi.create.lib.util;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.lib.extensions.AbstractMinecartExtensions;
import com.simibubi.create.lib.extensions.BaseRailBlockExtensions;
import com.simibubi.create.lib.mixin.common.accessor.AbstractMinecartAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public class MinecartAndRailUtil {

	// rails

	// doesn't support modded activator rails
	public static boolean isActivatorRail(Block rail) {
		return rail == Blocks.ACTIVATOR_RAIL;
	}

	public static RailShape getDirectionOfRail(BlockState state, BlockGetter world, BlockPos pos, @Nullable BaseRailBlock block) {
		return ((BaseRailBlockExtensions) state.getBlock()).create$getRailDirection(state, world, pos, block);
	}

	// carts

	public static void moveMinecartOnRail(AbstractMinecart cart, BlockPos pos) {
		((AbstractMinecartExtensions) cart).create$moveMinecartOnRail(pos);
	}

	public static double getMaximumSpeed(AbstractMinecart cart) {
		return ((AbstractMinecartAccessor) cart).create$getMaxSpeed();
	}

	public static boolean canCartUseRail(AbstractMinecart cart) {
		return ((AbstractMinecartExtensions) cart).create$canUseRail();
	}

	public static BlockPos getExpectedRailPos(AbstractMinecart cart) {
		return ((AbstractMinecartExtensions) cart).create$getCurrentRailPos();
	}

	public static double getSlopeAdjustment() {
		return 0.0078125D;
	}

	public static MinecartController getController(AbstractMinecart cart) {
		return ((AbstractMinecartExtensions) cart).create$getController();
	}

	public static LazyOptional<MinecartController> getControllerLazy(AbstractMinecart cart) {
		return LazyOptional.ofObject(getController(cart));
	}
}
