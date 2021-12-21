package com.simibubi.create.lib.util;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.lib.extensions.AbstractMinecartExtensions;
import com.simibubi.create.lib.extensions.BaseRailBlockExtensions;
import com.simibubi.create.lib.mixin.accessor.AbstractMinecartAccessor;

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

	public static RailShape getDirectionOfRail(BlockState state, BlockGetter world, BlockPos pos, @Nullable BaseRailBlock cart) {
		return ((BaseRailBlockExtensions) state.getBlock()).create$getRailDirection(state, world, pos, cart);
	}

	// carts

	public static void moveMinecartOnRail(AbstractMinecart cart, BlockPos pos) {
		((AbstractMinecartExtensions) MixinHelper.cast(cart)).create$moveMinecartOnRail(pos);
	}

	public static double getMaximumSpeed(AbstractMinecart cart) {
		return ((AbstractMinecartAccessor) MixinHelper.cast(cart)).create$getMaxSpeed();
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

	public static ItemStack getCartItem(AbstractMinecart.Type type) {
		return switch (type) {
			case FURNACE -> new ItemStack(Items.FURNACE_MINECART);
			case CHEST -> new ItemStack(Items.CHEST_MINECART);
			case TNT -> new ItemStack(Items.TNT_MINECART);
			case HOPPER -> new ItemStack(Items.HOPPER_MINECART);
			case COMMAND_BLOCK -> new ItemStack(Items.COMMAND_BLOCK_MINECART);
			default -> new ItemStack(Items.MINECART);
		};
	}
}
