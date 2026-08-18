package com.simibubi.create.api.behaviour.transport;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.Create;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;

/**
 * A target-side handler for items delivered directly by Create logistics.
 * <p>
 * Receivers can be exposed on any block through {@link #CAPABILITY}, including
 * blocks whose block entities do not extend Create's SmartBlockEntity.
 */
@FunctionalInterface
public interface DirectItemReceiver {

	BlockCapability<DirectItemReceiver, @Nullable Direction> CAPABILITY =
		BlockCapability.createSided(Create.asResource("direct_item_receiver"), DirectItemReceiver.class);

	/**
	 * Attempt to receive an item stack.
	 *
	 * @param stack a copy of the stack being delivered
	 * @param context information about the delivery source and target
	 * @param simulate when true, no state may be changed
	 * @return a non-null result describing whether the delivery was handled
	 */
	Result insert(ItemStack stack, Context context, boolean simulate);

	/**
	 * @param level the level containing the target
	 * @param targetPos the target block position
	 * @param location the world-space delivery location
	 * @param side the side from which the item is delivered
	 * @param source the block entity delivering the item, if any
	 */
	record Context(Level level, BlockPos targetPos, Vec3 location, Direction side, @Nullable BlockEntity source) {

		public Context {
			Objects.requireNonNull(level, "level");
			Objects.requireNonNull(targetPos, "targetPos");
			Objects.requireNonNull(location, "location");
			Objects.requireNonNull(side, "side");
		}
	}

	/**
	 * The remainder is applied by the caller only when the status is
	 * {@link Status#ACCEPT}.
	 */
	record Result(Status status, ItemStack remainder) {

		public Result {
			Objects.requireNonNull(status, "status");
			Objects.requireNonNull(remainder, "remainder");
		}

		public static Result pass(ItemStack remainder) {
			return new Result(Status.PASS, remainder);
		}

		public static Result accept(ItemStack remainder) {
			return new Result(Status.ACCEPT, remainder);
		}

		public static Result reject(ItemStack remainder) {
			return new Result(Status.REJECT, remainder);
		}
	}

	enum Status {
		/**
		 * This receiver does not apply. The caller should use its default behavior.
		 */
		PASS,
		/**
		 * The delivery is accepted. The remainder may be identical to the input
		 * when receiving it performs an action without consuming the item.
		 */
		ACCEPT,
		/**
		 * This receiver applies, but cannot currently accept the delivery.
		 */
		REJECT
	}
}
