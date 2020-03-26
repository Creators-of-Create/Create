package com.simibubi.create.modules.contraptions;

import static com.simibubi.create.AllBlocks.BELT;
import static com.simibubi.create.AllBlocks.LARGE_COGWHEEL;
import static net.minecraft.state.properties.BlockStateProperties.AXIS;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.relays.advanced.SpeedControllerTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.contraptions.relays.encased.DirectionalShaftHalvesTileEntity;
import com.simibubi.create.modules.contraptions.relays.encased.EncasedBeltBlock;
import com.simibubi.create.modules.contraptions.relays.encased.SplitShaftTileEntity;
import com.simibubi.create.modules.contraptions.relays.gearbox.GearboxTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
	private static float getRotationSpeedModifier(KineticTileEntity from, KineticTileEntity to) {
		final BlockState stateFrom = from.getBlockState();
		final BlockState stateTo = to.getBlockState();
		final IRotate definitionFrom = (IRotate) stateFrom.getBlock();
		final IRotate definitionTo = (IRotate) stateTo.getBlock();
		final BlockPos diff = to.getPos().subtract(from.getPos());
		final Direction direction = Direction.getFacingFromVector(diff.getX(), diff.getY(), diff.getZ());
		final World world = from.getWorld();

		boolean alignedAxes = true;
		for (Axis axis : Axis.values())
			if (axis != direction.getAxis())
				if (axis.getCoordinate(diff.getX(), diff.getY(), diff.getZ()) != 0)
					alignedAxes = false;

		boolean connectedByAxis =
			alignedAxes && definitionFrom.hasShaftTowards(world, from.getPos(), stateFrom, direction)
					&& definitionTo.hasShaftTowards(world, to.getPos(), stateTo, direction.getOpposite());

		boolean connectedByGears = definitionFrom.hasIntegratedCogwheel(world, from.getPos(), stateFrom)
				&& definitionTo.hasIntegratedCogwheel(world, to.getPos(), stateTo);

		// Belt <-> Belt
		if (from instanceof BeltTileEntity && to instanceof BeltTileEntity && !connectedByAxis) {
			return ((BeltTileEntity) from).getController().equals(((BeltTileEntity) to).getController()) ? 1 : 0;
		}

		// Axis <-> Axis
		if (connectedByAxis) {
			float axisModifier = getAxisModifier(to, direction.getOpposite());
			if (axisModifier != 0)
				axisModifier = 1 / axisModifier;
			return getAxisModifier(from, direction) * axisModifier;
		}

		// Attached Encased Belts
		if (stateFrom.getBlock() instanceof EncasedBeltBlock && stateTo.getBlock() instanceof EncasedBeltBlock) {
			boolean connected = EncasedBeltBlock.areBlocksConnected(stateFrom, stateTo, direction);
			return connected ? EncasedBeltBlock.getRotationSpeedModifier(from, to) : 0;
		}

		// Large Gear <-> Large Gear
		if (isLargeToLargeGear(stateFrom, stateTo, diff)) {
			Axis sourceAxis = stateFrom.get(AXIS);
			Axis targetAxis = stateTo.get(AXIS);
			int sourceAxisDiff = sourceAxis.getCoordinate(diff.getX(), diff.getY(), diff.getZ());
			int targetAxisDiff = targetAxis.getCoordinate(diff.getX(), diff.getY(), diff.getZ());

			return sourceAxisDiff > 0 ^ targetAxisDiff > 0 ? -1 : 1;
		}

		// Gear <-> Large Gear
		if (LARGE_COGWHEEL.typeOf(stateFrom) && definitionTo.hasIntegratedCogwheel(world, to.getPos(), stateTo))
			if (isLargeToSmallGear(stateFrom, stateTo, definitionTo, diff))
				return -2f;
		if (LARGE_COGWHEEL.typeOf(stateTo) && definitionFrom.hasIntegratedCogwheel(world, from.getPos(), stateFrom))
			if (isLargeToSmallGear(stateTo, stateFrom, definitionFrom, diff))
				return -.5f;

		// Gear <-> Gear
		if (connectedByGears) {
			if (diff.manhattanDistance(BlockPos.ZERO) != 1)
				return 0;
			if (LARGE_COGWHEEL.typeOf(stateTo))
				return 0;
			if (direction.getAxis() == definitionFrom.getRotationAxis(stateFrom))
				return 0;
			if (definitionFrom.getRotationAxis(stateFrom) == definitionTo.getRotationAxis(stateTo))
				return -1;
		}

		return 0;
	}

	private static float getConveyedSpeed(KineticTileEntity from, KineticTileEntity to) {
		final BlockState stateFrom = from.getBlockState();
		final BlockState stateTo = to.getBlockState();
		final BlockPos diff = to.getPos().subtract(from.getPos());

		// Rotation Speed Controller <-> Large Gear
		if (isLargeGearToSpeedController(stateFrom, stateTo, diff))
			return SpeedControllerTileEntity.getConveyedSpeed(from, to, true);
		if (isLargeGearToSpeedController(stateTo, stateFrom, diff))
			return SpeedControllerTileEntity.getConveyedSpeed(to, from, false);

		return from.getTheoreticalSpeed() * getRotationSpeedModifier(from, to);
	}

	private static boolean isLargeToLargeGear(BlockState from, BlockState to, BlockPos diff) {
		if (!LARGE_COGWHEEL.typeOf(from) || !LARGE_COGWHEEL.typeOf(to))
			return false;
		Axis fromAxis = from.get(AXIS);
		Axis toAxis = to.get(AXIS);
		if (fromAxis == toAxis)
			return false;
		for (Axis axis : Axis.values()) {
			int axisDiff = axis.getCoordinate(diff.getX(), diff.getY(), diff.getZ());
			if (axis == fromAxis || axis == toAxis) {
				if (axisDiff == 0)
					return false;

			} else if (axisDiff != 0)
				return false;
		}
		return true;
	}

	private static float getAxisModifier(KineticTileEntity te, Direction direction) {
		if (!te.hasSource() || !(te instanceof DirectionalShaftHalvesTileEntity))
			return 1;
		Direction source = ((DirectionalShaftHalvesTileEntity) te).getSourceFacing();

		if (te instanceof GearboxTileEntity)
			return direction.getAxis() == source.getAxis() ? direction == source ? 1 : -1
					: direction.getAxisDirection() == source.getAxisDirection() ? -1 : 1;

		if (te instanceof SplitShaftTileEntity)
			return ((SplitShaftTileEntity) te).getRotationSpeedModifier(direction);

		return 1;
	}

	private static boolean isLargeToSmallGear(BlockState from, BlockState to, IRotate defTo, BlockPos diff) {
		Axis axisFrom = from.get(AXIS);
		if (axisFrom != defTo.getRotationAxis(to))
			return false;
		if (axisFrom.getCoordinate(diff.getX(), diff.getY(), diff.getZ()) != 0)
			return false;
		for (Axis axis : Axis.values()) {
			if (axis == axisFrom)
				continue;
			if (Math.abs(axis.getCoordinate(diff.getX(), diff.getY(), diff.getZ())) != 1)
				return false;
		}
		return true;
	}

	private static boolean isLargeGearToSpeedController(BlockState from, BlockState to, BlockPos diff) {
		if (!LARGE_COGWHEEL.typeOf(from) || !AllBlocks.ROTATION_SPEED_CONTROLLER.typeOf(to))
			return false;
		if (!diff.equals(BlockPos.ZERO.up()) && !diff.equals(BlockPos.ZERO.down()))
			return false;
		return true;
	}

	/**
	 * Insert the added position to the kinetic network.
	 * 
	 * @param worldIn
	 * @param pos
	 */
	public static void handleAdded(World worldIn, BlockPos pos, KineticTileEntity addedTE) {
		if (worldIn.isRemote || isFrozen())
			return;
		if (!worldIn.isBlockPresent(pos))
			return;
		propagateNewSource(addedTE);
	}

	/**
	 * Search for sourceless networks attached to the given entity and update them.
	 * 
	 * @param currentTE
	 */
	private static void propagateNewSource(KineticTileEntity currentTE) {
		BlockPos pos = currentTE.getPos();
		World world = currentTE.getWorld();

		for (KineticTileEntity neighbourTE : getConnectedNeighbours(currentTE)) {
			float speedOfCurrent = currentTE.getTheoreticalSpeed();
			float speedOfNeighbour = neighbourTE.getTheoreticalSpeed();
			float newSpeed = getConveyedSpeed(currentTE, neighbourTE);
			float oppositeSpeed = getConveyedSpeed(neighbourTE, currentTE);

			boolean incompatible =
				Math.signum(newSpeed) != Math.signum(speedOfNeighbour) && (newSpeed != 0 && speedOfNeighbour != 0);

			boolean tooFast = Math.abs(newSpeed) > AllConfigs.SERVER.kinetics.maxRotationSpeed.get();
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
					currentTE.setSpeed(oppositeSpeed);
					currentTE.setSource(neighbourTE.getPos());
					currentTE.onSpeedChanged(prevSpeed);
					currentTE.sendData();

					propagateNewSource(currentTE);
					return;
				}

				// Current faster, overpower the neighbours' tree
				if (Math.abs(newSpeed) >= Math.abs(speedOfNeighbour)) {

					// Do not overpower you own network -> cycle
					if (!currentTE.hasNetwork() || currentTE.network.equals(neighbourTE.network)) {
						if (Math.abs(newSpeed) > Math.abs(speedOfNeighbour))
							world.destroyBlock(pos, true);
						continue;
					}

					if (currentTE.hasSource() && currentTE.source.equals(neighbourTE.getPos()))
						currentTE.removeSource();

					float prevSpeed = neighbourTE.getSpeed();
					neighbourTE.setSpeed(newSpeed);
					neighbourTE.setSource(currentTE.getPos());
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
			neighbourTE.setSource(currentTE.getPos());
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
	 * @param removedTE
	 */
	public static void handleRemoved(World worldIn, BlockPos pos, KineticTileEntity removedTE) {
		if (worldIn.isRemote || isFrozen())
			return;
		if (removedTE == null)
			return;
		if (removedTE.getTheoreticalSpeed() == 0)
			return;

		for (BlockPos neighbourPos : getPotentialNeighbourLocations(removedTE)) {
			BlockState neighbourState = worldIn.getBlockState(neighbourPos);
			if (!(neighbourState.getBlock() instanceof IRotate))
				continue;

			final KineticTileEntity neighbourTE = (KineticTileEntity) worldIn.getTileEntity(neighbourPos);
			if (!neighbourTE.hasSource() || !neighbourTE.source.equals(pos))
				continue;

			propagateMissingSource(neighbourTE);
		}

	}

	/**
	 * Clear the entire subnetwork depending on the given entity and find a new
	 * source
	 * 
	 * @param updateTE
	 */
	private static void propagateMissingSource(KineticTileEntity updateTE) {
		final World world = updateTE.getWorld();

		List<KineticTileEntity> potentialNewSources = new LinkedList<>();
		List<BlockPos> frontier = new LinkedList<>();
		frontier.add(updateTE.getPos());
		BlockPos missingSource = updateTE.hasSource() ? updateTE.source : null;

		while (!frontier.isEmpty()) {
			final BlockPos pos = frontier.remove(0);
			final KineticTileEntity currentTE = (KineticTileEntity) world.getTileEntity(pos);

			currentTE.removeSource();
			currentTE.sendData();

			for (KineticTileEntity neighbourTE : getConnectedNeighbours(currentTE)) {
				if (neighbourTE.getPos().equals(missingSource))
					continue;
				if (!neighbourTE.hasSource())
					continue;

				if (!neighbourTE.source.equals(pos)) {
					potentialNewSources.add(neighbourTE);
					continue;
				}

				if (neighbourTE.isSource())
					potentialNewSources.add(neighbourTE);

				frontier.add(neighbourTE.getPos());
			}
		}

		for (KineticTileEntity newSource : potentialNewSources) {
			if (newSource.hasSource() || newSource.isSource()) {
				propagateNewSource(newSource);
				return;
			}
		}
	}

	private static KineticTileEntity findConnectedNeighbour(KineticTileEntity currentTE, BlockPos neighbourPos) {
		BlockState neighbourState = currentTE.getWorld().getBlockState(neighbourPos);
		if (!(neighbourState.getBlock() instanceof IRotate))
			return null;
		if (!neighbourState.hasTileEntity())
			return null;
		TileEntity neighbourTE = currentTE.getWorld().getTileEntity(neighbourPos);
		if (!(neighbourTE instanceof KineticTileEntity))
			return null;
		KineticTileEntity neighbourKTE = (KineticTileEntity) neighbourTE;
		if (!(neighbourKTE.getBlockState().getBlock() instanceof IRotate))
			return null;
		if (!isConnected(currentTE, neighbourKTE))
			return null;
		return neighbourKTE;
	}

	public static boolean isConnected(KineticTileEntity from, KineticTileEntity to) {
		final BlockState stateFrom = from.getBlockState();
		final BlockState stateTo = to.getBlockState();
		final BlockPos diff = to.getPos().subtract(from.getPos());

		if (isLargeGearToSpeedController(stateFrom, stateTo, diff))
			return true;
		if (isLargeGearToSpeedController(stateTo, stateFrom, diff))
			return true;
		return getRotationSpeedModifier(from, to) != 0;
	}

	private static List<KineticTileEntity> getConnectedNeighbours(KineticTileEntity te) {
		List<KineticTileEntity> neighbours = new LinkedList<>();
		for (BlockPos neighbourPos : getPotentialNeighbourLocations(te)) {
			final KineticTileEntity neighbourTE = findConnectedNeighbour(te, neighbourPos);
			if (neighbourTE == null)
				continue;

			neighbours.add(neighbourTE);
		}
		return neighbours;
	}

	private static List<BlockPos> getPotentialNeighbourLocations(KineticTileEntity te) {
		List<BlockPos> neighbours = new LinkedList<>();

		if (!te.getWorld().isAreaLoaded(te.getPos(), 1))
			return neighbours;

		for (Direction facing : Direction.values())
			neighbours.add(te.getPos().offset(facing));

		// Some Blocks can interface diagonally
		BlockState blockState = te.getBlockState();
		boolean isLargeWheel = LARGE_COGWHEEL.typeOf(blockState);

		if (!(blockState.getBlock() instanceof IRotate))
			return neighbours;
		IRotate block = (IRotate) blockState.getBlock();

		if (block.hasIntegratedCogwheel(te.getWorld(), te.getPos(), blockState) || isLargeWheel
				|| BELT.typeOf(blockState)) {
			Axis axis = block.getRotationAxis(blockState);

			BlockPos.getAllInBox(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1)).forEach(offset -> {
				if (!isLargeWheel && axis.getCoordinate(offset.getX(), offset.getY(), offset.getZ()) != 0)
					return;
				if (offset.distanceSq(0, 0, 0, false) != BlockPos.ZERO.distanceSq(1, 1, 0, false))
					return;
				neighbours.add(te.getPos().add(offset));
			});
		}

		return neighbours;
	}

	public static boolean isFrozen() {
		return AllConfigs.SERVER.control.freezeRotationPropagator.get();
	}

}
