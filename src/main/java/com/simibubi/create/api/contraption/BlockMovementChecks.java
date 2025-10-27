package com.simibubi.create.api.contraption;

import com.simibubi.create.impl.contraption.BlockMovementChecksImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Provides several interfaces that can define the behavior of blocks when mounting onto contraptions:
 * <ul>
 *     <li>{@link MovementNecessaryCheck}</li>
 *     <li>{@link MovementAllowedCheck}</li>
 *     <li>{@link BrittleCheck}</li>
 *     <li>{@link AttachedCheck}</li>
 *     <li>{@link NotSupportiveCheck}</li>
 * </ul>
 * See each one for details.
 * <p>
 * For each interface, checks can be registered and queried.
 * Registration is thread-safe and can be done in parallel mod init.
 * Each query will iterate all registered checks of that type in reverse-registration order. If a check returns
 * a non-{@link CheckResult#PASS PASS} result, that is the result of the query. If no check catches a query, then
 * a best-effort fallback is used.
 */
public class BlockMovementChecks {
	public static void registerMovementNecessaryCheck(MovementNecessaryCheck check) {
		BlockMovementChecksImpl.registerMovementNecessaryCheck(check);
	}

	public static void registerMovementAllowedCheck(MovementAllowedCheck check) {
		BlockMovementChecksImpl.registerMovementAllowedCheck(check);
	}

	public static void registerBrittleCheck(BrittleCheck check) {
		BlockMovementChecksImpl.registerBrittleCheck(check);
	}

	public static void registerAttachedCheck(AttachedCheck check) {
		BlockMovementChecksImpl.registerAttachedCheck(check);
	}

	public static void registerNotSupportiveCheck(NotSupportiveCheck check) {
		BlockMovementChecksImpl.registerNotSupportiveCheck(check);
	}

	// queries

	public static boolean isMovementNecessary(BlockState state, Level world, BlockPos pos) {
		return BlockMovementChecksImpl.isMovementNecessary(state, world, pos);
	}

	public static boolean isMovementAllowed(BlockState state, Level world, BlockPos pos) {
		return BlockMovementChecksImpl.isMovementAllowed(state, world, pos);
	}

	public static boolean isBrittle(BlockState state) {
		return BlockMovementChecksImpl.isBrittle(state);
	}

	public static boolean isBlockAttachedTowards(BlockState state, Level world, BlockPos pos, Direction direction) {
		return BlockMovementChecksImpl.isBlockAttachedTowards(state, world, pos, direction);
	}

	public static boolean isNotSupportive(BlockState state, Direction facing) {
		return BlockMovementChecksImpl.isNotSupportive(state, facing);
	}

	@FunctionalInterface
	public interface MovementNecessaryCheck {
		/**
		 * Determine if it's necessary to move the given block. Contraptions
		 * will generally ignore blocks that are unnecessary to move.
		 */
		CheckResult isMovementNecessary(BlockState state, Level world, BlockPos pos);
	}

	@FunctionalInterface
	public interface MovementAllowedCheck {
		/**
		 * Determine if the given block is movable. Immobile blocks will generally prevent a contraption from assembling.
		 * @see ContraptionMovementSetting
		 */
		CheckResult isMovementAllowed(BlockState state, Level world, BlockPos pos);
	}

	@FunctionalInterface
	public interface BrittleCheck {
		/**
		 * Brittle blocks are blocks that require another block for support, like torches or ladders.
		 * They're collected first to avoid them breaking when their support block is removed.
		 */
		CheckResult isBrittle(BlockState state);
	}

	@FunctionalInterface
	public interface AttachedCheck {
		/**
		 * Determine if the given block is attached to the block in the given direction.
		 * Attached blocks will be moved together. Examples:
		 * <ul>
		 *     <li>Ladders are attached to their support block</li>
		 *     <li>Pressure plates are attached to the floor</li>
		 *     <li>Fluid tanks are attached to others in their multiblock</li>
		 *     <li>Bed halves are attached to each other</li>
		 * </ul>
		 */
		CheckResult isBlockAttachedTowards(BlockState state, Level world, BlockPos pos, Direction direction);
	}

	@FunctionalInterface
	public interface NotSupportiveCheck {
		/**
		 * Check if the given block is non-supportive in the given direction.
		 * Non-supportive blocks stop block collection propagation.
		 * Examples:
		 * <ul>
		 *     <li>Drills are not supportive for the block in front of them</li>
		 *     <li>Carpets are not supportive for the block above them</li>
		 *     <li>Non-extended stickers are not supportive of the block in front of them</li>
		 * </ul>
		 */
		CheckResult isNotSupportive(BlockState state, Direction direction);
	}

	public enum CheckResult {
		SUCCESS, FAIL, PASS;

		public boolean toBoolean() {
			if (this == PASS) {
				throw new IllegalStateException("PASS does not have a boolean value");
			}

			return this == SUCCESS;
		}

		public static CheckResult of(boolean b) {
			return b ? SUCCESS : FAIL;
		}

		public static CheckResult of(Boolean b) {
			return b == null ? PASS : (b ? SUCCESS : FAIL);
		}
	}

	private BlockMovementChecks() {
		throw new AssertionError("This class should not be instantiated");
	}
}
