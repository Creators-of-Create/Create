package com.simibubi.create.content.contraptions.components.tracks;

import static net.minecraft.state.properties.RailShape.NORTH_SOUTH;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ControllerRailBlock extends AbstractRailBlock implements IWrenchable {

	public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
	public static final BooleanProperty BACKWARDS = BooleanProperty.create("backwards");
	public static final IntegerProperty POWER = BlockStateProperties.POWER;

	public ControllerRailBlock(Properties properties) {
		super(true, properties);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(POWER, 0)
			.setValue(BACKWARDS, false)
			.setValue(SHAPE, NORTH_SOUTH));
	}

	public static Vector3i getAccelerationVector(BlockState state) {
		Direction pointingTo = getPointingTowards(state);
		return (isStateBackwards(state) ? pointingTo.getOpposite() : pointingTo).getNormal();
	}

	private static Direction getPointingTowards(BlockState state) {
		switch (state.getValue(SHAPE)) {
		case ASCENDING_WEST:
		case EAST_WEST:
			return Direction.WEST;
		case ASCENDING_EAST:
			return Direction.EAST;
		case ASCENDING_SOUTH:
			return Direction.SOUTH;
		default:
			return Direction.NORTH;
		}
	}

	@Override
	protected BlockState updateDir(World world, BlockPos pos, BlockState state, boolean p_208489_4_) {
		BlockState updatedState = super.updateDir(world, pos, state, p_208489_4_);
		if (updatedState.getValue(SHAPE) == state.getValue(SHAPE))
			return updatedState;
		BlockState reversedUpdatedState = updatedState;

		// Rails snapping to others at 90 degrees should follow their direction
		if (getPointingTowards(state).getAxis() != getPointingTowards(updatedState).getAxis()) {
			for (boolean opposite : Iterate.trueAndFalse) {
				Direction offset = getPointingTowards(updatedState);
				if (opposite)
					offset = offset.getOpposite();
				for (BlockPos adjPos : Iterate.hereBelowAndAbove(pos.relative(offset))) {
					BlockState adjState = world.getBlockState(adjPos);
					if (!AllBlocks.CONTROLLER_RAIL.has(adjState))
						continue;
					if (getPointingTowards(adjState).getAxis() != offset.getAxis())
						continue;
					if (adjState.getValue(BACKWARDS) != reversedUpdatedState.getValue(BACKWARDS))
						reversedUpdatedState = reversedUpdatedState.cycle(BACKWARDS);
				}
			}
		}

		// Replace if changed
		if (reversedUpdatedState != updatedState)
			world.setBlockAndUpdate(pos, reversedUpdatedState);
		return reversedUpdatedState;
	}

	private static void decelerateCart(BlockPos pos, AbstractMinecartEntity cart) {
		Vector3d diff = VecHelper.getCenterOf(pos)
			.subtract(cart.position());
		cart.setDeltaMovement(diff.x / 16f, 0, diff.z / 16f);

		if (cart instanceof FurnaceMinecartEntity) {
			FurnaceMinecartEntity fme = (FurnaceMinecartEntity) cart;
			fme.xPush = fme.zPush = 0;
		}
	}

	private static boolean isStableWith(BlockState testState, IBlockReader world, BlockPos pos) {
		return canSupportRigidBlock(world, pos.below()) && (!testState.getValue(SHAPE)
			.isAscending() || canSupportRigidBlock(world, pos.relative(getPointingTowards(testState))));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
		Direction direction = p_196258_1_.getHorizontalDirection();
		BlockState base = super.getStateForPlacement(p_196258_1_);
		return (base == null ? defaultBlockState() : base).setValue(BACKWARDS,
			direction.getAxisDirection() == AxisDirection.POSITIVE);
	}

	@Override
	public Property<RailShape> getShapeProperty() {
		return SHAPE;
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> p_206840_1_) {
		p_206840_1_.add(SHAPE, POWER, BACKWARDS);
	}

	@Override
	public void onMinecartPass(BlockState state, World world, BlockPos pos, AbstractMinecartEntity cart) {
		if (world.isClientSide)
			return;
		Vector3d accelerationVec = Vector3d.atLowerCornerOf(getAccelerationVector(state));
		double targetSpeed = cart.getMaxSpeedWithRail() * state.getValue(POWER) / 15f;

		if (cart instanceof FurnaceMinecartEntity) {
			FurnaceMinecartEntity fme = (FurnaceMinecartEntity) cart;
			fme.xPush = accelerationVec.x;
			fme.zPush = accelerationVec.z;
		}

		Vector3d motion = cart.getDeltaMovement();
		if ((motion.dot(accelerationVec) >= 0 || motion.lengthSqr() < 0.0001) && targetSpeed > 0)
			cart.setDeltaMovement(accelerationVec.scale(targetSpeed));
		else
			decelerateCart(pos, cart);
	}

	@Override
	protected void updateState(BlockState state, World world, BlockPos pos, Block block) {
		int newPower = calculatePower(world, pos);
		if (state.getValue(POWER) != newPower)
			placeAndNotify(state.setValue(POWER, newPower), pos, world);
	}

	private int calculatePower(World world, BlockPos pos) {
		int newPower = world.getBestNeighborSignal(pos);
		if (newPower != 0)
			return newPower;

		int forwardDistance = 0;
		int backwardsDistance = 0;
		BlockPos lastForwardRail = pos;
		BlockPos lastBackwardsRail = pos;
		int forwardPower = 0;
		int backwardsPower = 0;

		for (int i = 0; i < 15; i++) {
			BlockPos testPos = findNextRail(lastForwardRail, world, false);
			if (testPos == null)
				break;
			forwardDistance++;
			lastForwardRail = testPos;
			forwardPower = world.getBestNeighborSignal(testPos);
			if (forwardPower != 0)
				break;
		}
		for (int i = 0; i < 15; i++) {
			BlockPos testPos = findNextRail(lastBackwardsRail, world, true);
			if (testPos == null)
				break;
			backwardsDistance++;
			lastBackwardsRail = testPos;
			backwardsPower = world.getBestNeighborSignal(testPos);
			if (backwardsPower != 0)
				break;
		}

		if (forwardDistance > 8 && backwardsDistance > 8)
			return 0;
		if (backwardsPower == 0 && forwardDistance <= 8)
			return forwardPower;
		if (forwardPower == 0 && backwardsDistance <= 8)
			return backwardsPower;
		if (backwardsPower != 0 && forwardPower != 0)
			return MathHelper.ceil((backwardsPower * forwardDistance + forwardPower * backwardsDistance)
				/ (double) (forwardDistance + backwardsDistance));
		return 0;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getLevel();
		if (world.isClientSide)
			return ActionResultType.SUCCESS;
		BlockPos pos = context.getClickedPos();
		for (Rotation testRotation : new Rotation[] { Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180,
			Rotation.COUNTERCLOCKWISE_90 }) {
			BlockState testState = rotate(state, testRotation);
			if (isStableWith(testState, world, pos)) {
				placeAndNotify(testState, pos, world);
				return ActionResultType.SUCCESS;
			}
		}
		BlockState testState = state.setValue(BACKWARDS, !state.getValue(BACKWARDS));
		if (isStableWith(testState, world, pos))
			placeAndNotify(testState, pos, world);
		return ActionResultType.SUCCESS;
	}

	private void placeAndNotify(BlockState state, BlockPos pos, World world) {
		world.setBlock(pos, state, 3);
		world.updateNeighborsAt(pos.below(), this);
		if (state.getValue(SHAPE)
			.isAscending())
			world.updateNeighborsAt(pos.above(), this);
	}

	@Nullable
	private BlockPos findNextRail(BlockPos from, IBlockReader world, boolean reversed) {
		BlockState current = world.getBlockState(from);
		if (!(current.getBlock() instanceof ControllerRailBlock))
			return null;
		Vector3i accelerationVec = getAccelerationVector(current);
		BlockPos baseTestPos = reversed ? from.subtract(accelerationVec) : from.offset(accelerationVec);
		for (BlockPos testPos : Iterate.hereBelowAndAbove(baseTestPos)) {
			if (testPos.getY() > from.getY() && !current.getValue(SHAPE)
				.isAscending())
				continue;
			BlockState testState = world.getBlockState(testPos);
			if (testState.getBlock() instanceof ControllerRailBlock
				&& getAccelerationVector(testState).equals(accelerationVec))
				return testPos;
		}
		return null;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos) {
		return state.getValue(POWER);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		if (rotation == Rotation.NONE)
			return state;

		RailShape railshape = Blocks.POWERED_RAIL.defaultBlockState()
			.setValue(SHAPE, state.getValue(SHAPE))
			.rotate(rotation)
			.getValue(SHAPE);
		state = state.setValue(SHAPE, railshape);

		if (rotation == Rotation.CLOCKWISE_180
			|| (getPointingTowards(state).getAxis() == Axis.Z) == (rotation == Rotation.COUNTERCLOCKWISE_90))
			return state.cycle(BACKWARDS);

		return state;
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.NONE)
			return state;

		RailShape railshape = Blocks.POWERED_RAIL.defaultBlockState()
			.setValue(SHAPE, state.getValue(SHAPE))
			.mirror(mirror)
			.getValue(SHAPE);
		state = state.setValue(SHAPE, railshape);

		if ((getPointingTowards(state).getAxis() == Axis.Z) == (mirror == Mirror.LEFT_RIGHT))
			return state.cycle(BACKWARDS);

		return state;
	}

	public static boolean isStateBackwards(BlockState state) {
		return state.getValue(BACKWARDS) ^ isReversedSlope(state);
	}

	public static boolean isReversedSlope(BlockState state) {
		return state.getValue(SHAPE) == RailShape.ASCENDING_SOUTH || state.getValue(SHAPE) == RailShape.ASCENDING_EAST;
	}
}
