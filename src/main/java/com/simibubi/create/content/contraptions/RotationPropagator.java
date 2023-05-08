package com.simibubi.create.content.contraptions;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerBlock;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerBlockEntity;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.content.contraptions.relays.encased.DirectionalShaftHalvesBlockEntity;
import com.simibubi.create.content.contraptions.relays.encased.EncasedBeltBlock;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftBlockEntity;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxBlockEntity;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


public class RotationPropagator {

	private static final int MAX_FLICKER_SCORE = 128;

	/**
	 * Determines the change in rotation between two attached kinetic entities. For
	 * instance, an axis connection returns 1 while a 1-to-1 gear connection
	 * reverses the rotation and therefore returns -1.
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	private static float getRotationSpeedModifier(KineticBlockEntity from, KineticBlockEntity to) {
		final BlockState stateFrom = from.getBlockState();
		final BlockState stateTo = to.getBlockState();

		Block fromBlock = stateFrom.getBlock();
		Block toBlock = stateTo.getBlock();
		if (!(fromBlock instanceof IRotate && toBlock instanceof IRotate))
			return 0;

		final IRotate definitionFrom = (IRotate) fromBlock;
		final IRotate definitionTo = (IRotate) toBlock;
		final BlockPos diff = to.getBlockPos()
			.subtract(from.getBlockPos());
		final Direction direction = Direction.getNearest(diff.getX(), diff.getY(), diff.getZ());
		final Level world = from.getLevel();

		boolean alignedAxes = true;
		for (Axis axis : Axis.values())
			if (axis != direction.getAxis())
				if (axis.choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
					alignedAxes = false;

		boolean connectedByAxis =
			alignedAxes && definitionFrom.hasShaftTowards(world, from.getBlockPos(), stateFrom, direction)
				&& definitionTo.hasShaftTowards(world, to.getBlockPos(), stateTo, direction.getOpposite());

		boolean connectedByGears = ICogWheel.isSmallCog(stateFrom)
			&& ICogWheel.isSmallCog(stateTo);

		float custom = from.propagateRotationTo(to, stateFrom, stateTo, diff, connectedByAxis, connectedByGears);
		if (custom != 0)
			return custom;

		// Axis <-> Axis
		if (connectedByAxis) {
			float axisModifier = getAxisModifier(to, direction.getOpposite());
			if (axisModifier != 0)
				axisModifier = 1 / axisModifier;
			return getAxisModifier(from, direction) * axisModifier;
		}

		// Attached Encased Belts
		if (fromBlock instanceof EncasedBeltBlock && toBlock instanceof EncasedBeltBlock) {
			boolean connected = EncasedBeltBlock.areBlocksConnected(stateFrom, stateTo, direction);
			return connected ? EncasedBeltBlock.getRotationSpeedModifier(from, to) : 0;
		}

		// Large Gear <-> Large Gear
		if (isLargeToLargeGear(stateFrom, stateTo, diff)) {
			Axis sourceAxis = stateFrom.getValue(AXIS);
			Axis targetAxis = stateTo.getValue(AXIS);
			int sourceAxisDiff = sourceAxis.choose(diff.getX(), diff.getY(), diff.getZ());
			int targetAxisDiff = targetAxis.choose(diff.getX(), diff.getY(), diff.getZ());

			return sourceAxisDiff > 0 ^ targetAxisDiff > 0 ? -1 : 1;
		}

		// Gear <-> Large Gear
		if (ICogWheel.isLargeCog(stateFrom) && ICogWheel.isSmallCog(stateTo))
			if (isLargeToSmallCog(stateFrom, stateTo, definitionTo, diff))
				return -2f;
		if (ICogWheel.isLargeCog(stateTo) && ICogWheel.isSmallCog(stateFrom))
			if (isLargeToSmallCog(stateTo, stateFrom, definitionFrom, diff))
				return -.5f;

		// Gear <-> Gear
		if (connectedByGears) {
			if (diff.distManhattan(BlockPos.ZERO) != 1)
				return 0;
			if (ICogWheel.isLargeCog(stateTo))
				return 0;
			if (direction.getAxis() == definitionFrom.getRotationAxis(stateFrom))
				return 0;
			if (definitionFrom.getRotationAxis(stateFrom) == definitionTo.getRotationAxis(stateTo))
				return -1;
		}

		return 0;
	}

	private static float getConveyedSpeed(KineticBlockEntity from, KineticBlockEntity to) {
		final BlockState stateFrom = from.getBlockState();
		final BlockState stateTo = to.getBlockState();

		// Rotation Speed Controller <-> Large Gear
		if (isLargeCogToSpeedController(stateFrom, stateTo, to.getBlockPos()
			.subtract(from.getBlockPos())))
			return SpeedControllerBlockEntity.getConveyedSpeed(from, to, true);
		if (isLargeCogToSpeedController(stateTo, stateFrom, from.getBlockPos()
			.subtract(to.getBlockPos())))
			return SpeedControllerBlockEntity.getConveyedSpeed(to, from, false);

		float rotationSpeedModifier = getRotationSpeedModifier(from, to);
		return from.getTheoreticalSpeed() * rotationSpeedModifier;
	}

	private static boolean isLargeToLargeGear(BlockState from, BlockState to, BlockPos diff) {
		if (!ICogWheel.isLargeCog(from) || !ICogWheel.isLargeCog(to))
			return false;
		Axis fromAxis = from.getValue(AXIS);
		Axis toAxis = to.getValue(AXIS);
		if (fromAxis == toAxis)
			return false;
		for (Axis axis : Axis.values()) {
			int axisDiff = axis.choose(diff.getX(), diff.getY(), diff.getZ());
			if (axis == fromAxis || axis == toAxis) {
				if (axisDiff == 0)
					return false;

			} else if (axisDiff != 0)
				return false;
		}
		return true;
	}

	private static float getAxisModifier(KineticBlockEntity be, Direction direction) {
		if (!(be.hasSource()||be.isSource()) || !(be instanceof DirectionalShaftHalvesBlockEntity))
			return 1;
		Direction source = ((DirectionalShaftHalvesBlockEntity) be).getSourceFacing();

		if (be instanceof GearboxBlockEntity)
			return direction.getAxis() == source.getAxis() ? direction == source ? 1 : -1
				: direction.getAxisDirection() == source.getAxisDirection() ? -1 : 1;

		if (be instanceof SplitShaftBlockEntity)
			return ((SplitShaftBlockEntity) be).getRotationSpeedModifier(direction);

		return 1;
	}

	private static boolean isLargeToSmallCog(BlockState from, BlockState to, IRotate defTo, BlockPos diff) {
		Axis axisFrom = from.getValue(AXIS);
		if (axisFrom != defTo.getRotationAxis(to))
			return false;
		if (axisFrom.choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
			return false;
		for (Axis axis : Axis.values()) {
			if (axis == axisFrom)
				continue;
			if (Math.abs(axis.choose(diff.getX(), diff.getY(), diff.getZ())) != 1)
				return false;
		}
		return true;
	}

	private static boolean isLargeCogToSpeedController(BlockState from, BlockState to, BlockPos diff) {
		if (!ICogWheel.isLargeCog(from) || !AllBlocks.ROTATION_SPEED_CONTROLLER.has(to))
			return false;
		if (!diff.equals(BlockPos.ZERO.below()))
			return false;
		Axis axis = from.getValue(CogWheelBlock.AXIS);
		if (axis.isVertical())
			return false;
		if (to.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) == axis)
			return false;
		return true;
	}

	/**
	 * Insert the added position to the kinetic network.
	 *
	 * @param worldIn
	 * @param pos
	 */
	public static void handleAdded(Level worldIn, BlockPos pos, KineticBlockEntity addedTE) {
		if (worldIn.isClientSide)
			return;
		if (!worldIn.isLoaded(pos))
			return;
		propagateNewSource(addedTE);
	}

	/**
	 * Search for sourceless networks attached to the given entity and update them.
	 *
	 * @param currentTE
	 */
	private static void propagateNewSource(KineticBlockEntity currentTE) {
		BlockPos pos = currentTE.getBlockPos();
		Level world = currentTE.getLevel();

		for (KineticBlockEntity neighbourTE : getConnectedNeighbours(currentTE)) {
			float speedOfCurrent = currentTE.getTheoreticalSpeed();
			float speedOfNeighbour = neighbourTE.getTheoreticalSpeed();
			float newSpeed = getConveyedSpeed(currentTE, neighbourTE);
			float oppositeSpeed = getConveyedSpeed(neighbourTE, currentTE);

			if (newSpeed == 0 && oppositeSpeed == 0)
				continue;

			boolean incompatible =
				Math.signum(newSpeed) != Math.signum(speedOfNeighbour) && (newSpeed != 0 && speedOfNeighbour != 0);

			boolean tooFast = Math.abs(newSpeed) > AllConfigs.server().kinetics.maxRotationSpeed.get()
					|| Math.abs(oppositeSpeed) > AllConfigs.server().kinetics.maxRotationSpeed.get();
			// Check for both the new speed and the opposite speed, just in case

			boolean speedChangedTooOften = currentTE.getFlickerScore() > MAX_FLICKER_SCORE;
			if (tooFast || speedChangedTooOften) {
				world.destroyBlock(pos, true);
				return;
			}

			// Opposite directions
			if (incompatible) {
				world.destroyBlock(pos, true);
				return;

				// Same direction: overpower the slower speed
			} else {

				// Neighbour faster, overpower the incoming tree
				if (Math.abs(oppositeSpeed) > Math.abs(speedOfCurrent)) {
					float prevSpeed = currentTE.getSpeed();
					currentTE.setSource(neighbourTE.getBlockPos());
					currentTE.setSpeed(getConveyedSpeed(neighbourTE, currentTE));
					currentTE.onSpeedChanged(prevSpeed);
					currentTE.sendData();

					propagateNewSource(currentTE);
					return;
				}

				// Current faster, overpower the neighbours' tree
				if (Math.abs(newSpeed) >= Math.abs(speedOfNeighbour)) {

					// Do not overpower you own network -> cycle
					if (!currentTE.hasNetwork() || currentTE.network.equals(neighbourTE.network)) {
						float epsilon = Math.abs(speedOfNeighbour) / 256f / 256f;
						if (Math.abs(newSpeed) > Math.abs(speedOfNeighbour) + epsilon)
							world.destroyBlock(pos, true);
						continue;
					}

					if (currentTE.hasSource() && currentTE.source.equals(neighbourTE.getBlockPos()))
						currentTE.removeSource();

					float prevSpeed = neighbourTE.getSpeed();
					neighbourTE.setSource(currentTE.getBlockPos());
					neighbourTE.setSpeed(getConveyedSpeed(currentTE, neighbourTE));
					neighbourTE.onSpeedChanged(prevSpeed);
					neighbourTE.sendData();
					propagateNewSource(neighbourTE);
					continue;
				}
			}

			if (neighbourTE.getTheoreticalSpeed() == newSpeed)
				continue;

			float prevSpeed = neighbourTE.getSpeed();
			neighbourTE.setSpeed(newSpeed);
			neighbourTE.setSource(currentTE.getBlockPos());
			neighbourTE.onSpeedChanged(prevSpeed);
			neighbourTE.sendData();
			propagateNewSource(neighbourTE);

		}
	}

	/**
	 * Remove the given entity from the network.
	 *
	 * @param worldIn
	 * @param pos
	 * @param removedBE
	 */
	public static void handleRemoved(Level worldIn, BlockPos pos, KineticBlockEntity removedBE) {
		if (worldIn.isClientSide)
			return;
		if (removedBE == null)
			return;
		if (removedBE.getTheoreticalSpeed() == 0)
			return;

		for (BlockPos neighbourPos : getPotentialNeighbourLocations(removedBE)) {
			BlockState neighbourState = worldIn.getBlockState(neighbourPos);
			if (!(neighbourState.getBlock() instanceof IRotate))
				continue;
			BlockEntity blockEntity = worldIn.getBlockEntity(neighbourPos);
			if (!(blockEntity instanceof KineticBlockEntity))
				continue;

			final KineticBlockEntity neighbourBE = (KineticBlockEntity) blockEntity;
			if (!neighbourBE.hasSource() || !neighbourBE.source.equals(pos))
				continue;

			propagateMissingSource(neighbourBE);
		}

	}

	/**
	 * Clear the entire subnetwork depending on the given entity and find a new
	 * source
	 *
	 * @param updateTE
	 */
	private static void propagateMissingSource(KineticBlockEntity updateTE) {
		final Level world = updateTE.getLevel();

		List<KineticBlockEntity> potentialNewSources = new LinkedList<>();
		List<BlockPos> frontier = new LinkedList<>();
		frontier.add(updateTE.getBlockPos());
		BlockPos missingSource = updateTE.hasSource() ? updateTE.source : null;

		while (!frontier.isEmpty()) {
			final BlockPos pos = frontier.remove(0);
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (!(blockEntity instanceof KineticBlockEntity))
				continue;
			final KineticBlockEntity currentBE = (KineticBlockEntity) blockEntity;

			currentBE.removeSource();
			currentBE.sendData();

			for (KineticBlockEntity neighbourBE : getConnectedNeighbours(currentBE)) {
				if (neighbourBE.getBlockPos()
					.equals(missingSource))
					continue;
				if (!neighbourBE.hasSource())
					continue;

				if (!neighbourBE.source.equals(pos)) {
					potentialNewSources.add(neighbourBE);
					continue;
				}

				if (neighbourBE.isSource())
					potentialNewSources.add(neighbourBE);

				frontier.add(neighbourBE.getBlockPos());
			}
		}

		for (KineticBlockEntity newSource : potentialNewSources) {
			if (newSource.hasSource() || newSource.isSource()) {
				propagateNewSource(newSource);
				return;
			}
		}
	}

	private static KineticBlockEntity findConnectedNeighbour(KineticBlockEntity currentTE, BlockPos neighbourPos) {
		BlockState neighbourState = currentTE.getLevel()
			.getBlockState(neighbourPos);
		if (!(neighbourState.getBlock() instanceof IRotate))
			return null;
		if (!neighbourState.hasBlockEntity())
			return null;
		BlockEntity neighbourBE = currentTE.getLevel()
			.getBlockEntity(neighbourPos);
		if (!(neighbourBE instanceof KineticBlockEntity))
			return null;
		KineticBlockEntity neighbourKBE = (KineticBlockEntity) neighbourBE;
		if (!(neighbourKBE.getBlockState()
			.getBlock() instanceof IRotate))
			return null;
		if (!isConnected(currentTE, neighbourKBE) && !isConnected(neighbourKBE, currentTE))
			return null;
		return neighbourKBE;
	}

	public static boolean isConnected(KineticBlockEntity from, KineticBlockEntity to) {
		final BlockState stateFrom = from.getBlockState();
		final BlockState stateTo = to.getBlockState();
		return isLargeCogToSpeedController(stateFrom, stateTo, to.getBlockPos()
			.subtract(from.getBlockPos())) || getRotationSpeedModifier(from, to) != 0
			|| from.isCustomConnection(to, stateFrom, stateTo);
	}

	private static List<KineticBlockEntity> getConnectedNeighbours(KineticBlockEntity be) {
		List<KineticBlockEntity> neighbours = new LinkedList<>();
		for (BlockPos neighbourPos : getPotentialNeighbourLocations(be)) {
			final KineticBlockEntity neighbourBE = findConnectedNeighbour(be, neighbourPos);
			if (neighbourBE == null)
				continue;

			neighbours.add(neighbourBE);
		}
		return neighbours;
	}

	private static List<BlockPos> getPotentialNeighbourLocations(KineticBlockEntity be) {
		List<BlockPos> neighbours = new LinkedList<>();
		BlockPos blockPos = be.getBlockPos();
		Level level = be.getLevel();

		if (!level.isLoaded(blockPos))
			return neighbours;

		for (Direction facing : Iterate.directions) {
			BlockPos relative = blockPos.relative(facing);
			if (level.isLoaded(relative))
				neighbours.add(relative);
		}

		BlockState blockState = be.getBlockState();
		if (!(blockState.getBlock() instanceof IRotate))
			return neighbours;
		IRotate block = (IRotate) blockState.getBlock();
		return be.addPropagationLocations(block, blockState, neighbours);
	}

}
