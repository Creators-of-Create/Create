package com.simibubi.create.api.contraption.dispenser;

import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.impl.contraption.dispenser.DispenserBehaviorConverter;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

/**
 * A parallel to {@link DispenseItemBehavior}, for use by mounted dispensers.
 * Create will attempt to wrap existing {@link DispenseItemBehavior}s, but this interface can be used to provide better or fixed behavior.
 * @see DefaultMountedDispenseBehavior
 * @see MountedProjectileDispenseBehavior
 * @see OptionalMountedDispenseBehavior
 */
@FunctionalInterface
public interface MountedDispenseBehavior {
	SimpleRegistry<Item, MountedDispenseBehavior> REGISTRY = Util.make(() -> {
		SimpleRegistry<Item, MountedDispenseBehavior> registry = SimpleRegistry.create();
		registry.registerProvider(DispenserBehaviorConverter.INSTANCE);
		return registry;
	});

	/**
	 * Dispense the given stack into the world.
	 * @param stack the stack to dispense. Safe to modify, behaviors are given a copy
	 * @param context the MovementContext of the dispenser
	 * @param pos the BlockPos being visited by the dispenser
	 * @return the remaining stack after dispensing one item
	 */
	ItemStack dispense(ItemStack stack, MovementContext context, BlockPos pos);

	// utilities for implementations

	static Vec3 getDispenserNormal(MovementContext ctx) {
		Direction facing = ctx.state.getValue(DispenserBlock.FACING);
		Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
		return ctx.rotation.apply(normal).normalize();
	}

	static Direction getClosestFacingDirection(Vec3 facing) {
		return Direction.getNearest(facing.x, facing.y, facing.z);
	}

	/**
	 * Attempt to place an item back into the inventory. This is used in the case of item overflow, such as a stack
	 * of buckets becoming two separate stacks when one is filled with water.
	 * <p>
	 * First tries to insert directly into the dispenser inventory. If that fails, it then tries the contraption's
	 * whole inventory. If that still fails, the stack is dispensed into the world with the default behavior.
	 * @param stack the stack to store in the inventory
	 * @param context the MovementContext given to the behavior
	 * @param pos the position given to the behavior
	 */
	static void placeItemInInventory(ItemStack stack, MovementContext context, BlockPos pos) {
		ItemStack toInsert = stack.copy();
		// try inserting into own inventory first
		ItemStack remainder = ItemHandlerHelper.insertItem(context.getItemStorage(), toInsert, false);
		if (!remainder.isEmpty()) {
			// next, try the whole contraption inventory
			CombinedInvWrapper contraption = context.contraption.getStorage().getAllItems();
			ItemStack newRemainder = ItemHandlerHelper.insertItem(contraption, remainder, false);
			if (!newRemainder.isEmpty()) {
				// if there's *still* something left, dispense into world
				DefaultMountedDispenseBehavior.INSTANCE.dispense(remainder, context, pos);
			}
		}
	}
}
