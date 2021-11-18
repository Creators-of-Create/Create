package com.simibubi.create.lib.utility;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.lib.extensions.AbstractMinecartExtensions;
import com.simibubi.create.lib.helper.AbstractMinecartEntityHelper;
import com.simibubi.create.lib.helper.AbstractRailBlockHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
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

	public static RailShape getDirectionOfRail(BlockState state, BlockGetter world, BlockPos pos, @Nullable AbstractMinecart cart) {
		return AbstractRailBlockHelper.getDirectionOfRail(state, world, pos, cart);
	}

	public static RailShape getDirectionOfRail(BlockState state, @Nullable AbstractMinecart cart) {
		return AbstractRailBlockHelper.getDirectionOfRail(state, cart);
	}

	// carts

	public static void moveMinecartOnRail(AbstractMinecart cart, BlockPos pos) {
		AbstractMinecartEntityHelper.moveMinecartOnRail(cart, pos);
	}

	public static ItemStack getItemFromCart(AbstractMinecart cart) {
		return AbstractMinecartEntityHelper.getCartItem(cart);
	}

	public static double getMaximumSpeed(AbstractMinecart cart) {
		return AbstractMinecartEntityHelper.getMaximumSpeed(cart);
	}

	public static boolean canCartUseRail(AbstractMinecart cart) {
		return AbstractMinecartEntityHelper.canCartUseRail(cart);
	}

	public static BlockPos getExpectedRailPos(AbstractMinecart cart) {
		return AbstractMinecartEntityHelper.getCurrentRailPos(cart);
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
