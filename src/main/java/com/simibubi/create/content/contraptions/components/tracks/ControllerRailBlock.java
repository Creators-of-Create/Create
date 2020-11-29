package com.simibubi.create.content.contraptions.components.tracks;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.VecHelper;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.state.properties.RailShape.*;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("deprecation")
public class ControllerRailBlock extends AbstractRailBlock implements IWrenchable {
	public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
	public static final IntegerProperty POWERED = BlockStateProperties.POWER_0_15;
	public static final BooleanProperty BACKWARDS = BooleanProperty.create("backwards");

	public ControllerRailBlock(Properties p_i48444_2_) {
		super(true, p_i48444_2_);
		this.setDefaultState(this.stateContainer.getBaseState().with(POWERED, 0).with(BACKWARDS, false).with(SHAPE, NORTH_SOUTH));
	}

	private static Vec3i getAccelerationVector(BlockState state) {
		Direction pointingTo = getPointingTowards(state);
		return (state.get(BACKWARDS) ? pointingTo.getOpposite() : pointingTo).getDirectionVec();
	}

	private static Direction getPointingTowards(BlockState state) {
		switch (state.get(SHAPE)) {
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

	private static void decelerateCart(BlockPos pos, AbstractMinecartEntity cart) {
		Vec3d diff = VecHelper.getCenterOf(pos).subtract(cart.getPositionVec());
		cart.setMotion(diff.x / 16f, 0, diff.z / 16f);
	}

	private static boolean isStableWith(BlockState testState, IBlockReader world, BlockPos pos) {
		return hasSolidSideOnTop(world, pos.down()) && (!testState.get(SHAPE).isAscending() || hasSolidSideOnTop(world, pos.offset(getPointingTowards(testState))));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
		Direction direction = p_196258_1_.getPlacementHorizontalFacing();
		BlockState base = super.getStateForPlacement(p_196258_1_);
		return (base == null ? getDefaultState() : base).with(BACKWARDS, direction == Direction.SOUTH || direction == Direction.EAST);
	}

	@Override
	public IProperty<RailShape> getShapeProperty() {
		return SHAPE;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> p_206840_1_) {
		p_206840_1_.add(SHAPE, POWERED, BACKWARDS);
	}

	@Override
	public void onMinecartPass(BlockState state, World world, BlockPos pos, AbstractMinecartEntity cart) {
		if (world.isRemote)
			return;
		Vec3d accelerationVec = new Vec3d(getAccelerationVector(state));
		double targetSpeed = cart.getMaxSpeedWithRail() * state.get(POWERED) / 15.;
		if ((cart.getMotion().dotProduct(accelerationVec) >= 0 || cart.getMotion().lengthSquared() < 0.0001) && targetSpeed > 0)
			cart.setMotion(accelerationVec.scale(targetSpeed));
		else
			decelerateCart(pos, cart);
	}

	@Override
	protected void updateState(BlockState state, World world, BlockPos pos, Block block) {
		int newPower = Math.max(world.getRedstonePowerFromNeighbors(pos), 0); // TODO: Add power calculation
		if (state.get(POWERED) != newPower)
			placeAndNotify(state.with(POWERED, newPower), pos, world);
	}

	@Override
	public BlockState rotate(BlockState p_185499_1_, Rotation p_185499_2_) {
		switch (p_185499_2_) {
			case CLOCKWISE_180:
				switch (p_185499_1_.get(SHAPE)) {
					case ASCENDING_EAST:
						return p_185499_1_.with(SHAPE, ASCENDING_WEST);
					case ASCENDING_WEST:
						return p_185499_1_.with(SHAPE, ASCENDING_EAST);
					case ASCENDING_NORTH:
						return p_185499_1_.with(SHAPE, ASCENDING_SOUTH);
					case ASCENDING_SOUTH:
						return p_185499_1_.with(SHAPE, ASCENDING_NORTH);
					default:
						return p_185499_1_.with(BACKWARDS, !p_185499_1_.get(BACKWARDS));
				}
			case COUNTERCLOCKWISE_90:
				switch (p_185499_1_.get(SHAPE)) {
					case ASCENDING_EAST:
						return p_185499_1_.with(SHAPE, ASCENDING_NORTH);
					case ASCENDING_WEST:
						return p_185499_1_.with(SHAPE, ASCENDING_SOUTH);
					case ASCENDING_NORTH:
						return p_185499_1_.with(SHAPE, ASCENDING_WEST);
					case ASCENDING_SOUTH:
						return p_185499_1_.with(SHAPE, ASCENDING_EAST);
					case NORTH_SOUTH:
						return p_185499_1_.with(SHAPE, EAST_WEST);
					case EAST_WEST:
						return p_185499_1_.with(SHAPE, NORTH_SOUTH).with(BACKWARDS, !p_185499_1_.get(BACKWARDS));
				}
			case CLOCKWISE_90:
				switch (p_185499_1_.get(SHAPE)) {
					case ASCENDING_EAST:
						return p_185499_1_.with(SHAPE, ASCENDING_SOUTH);
					case ASCENDING_WEST:
						return p_185499_1_.with(SHAPE, ASCENDING_NORTH);
					case ASCENDING_NORTH:
						return p_185499_1_.with(SHAPE, ASCENDING_EAST);
					case ASCENDING_SOUTH:
						return p_185499_1_.with(SHAPE, ASCENDING_WEST);
					case NORTH_SOUTH:
						return p_185499_1_.with(SHAPE, EAST_WEST).with(SHAPE, NORTH_SOUTH).with(BACKWARDS, !p_185499_1_.get(BACKWARDS));
					case EAST_WEST:
						return p_185499_1_.with(SHAPE, NORTH_SOUTH);
				}
			default:
				return p_185499_1_;
		}
	}

	@Override
	public BlockState mirror(BlockState p_185471_1_, Mirror p_185471_2_) {
		RailShape railshape = p_185471_1_.get(SHAPE);
		switch (p_185471_2_) {
			case LEFT_RIGHT:
				switch (railshape) {
					case ASCENDING_NORTH:
						return p_185471_1_.with(SHAPE, RailShape.ASCENDING_SOUTH);
					case ASCENDING_SOUTH:
						return p_185471_1_.with(SHAPE, RailShape.ASCENDING_NORTH);
					case NORTH_SOUTH:
						return p_185471_1_.with(BACKWARDS, !p_185471_1_.get(BACKWARDS));
					default:
						return super.mirror(p_185471_1_, p_185471_2_);
				}
			case FRONT_BACK:
				switch (railshape) {
					case ASCENDING_EAST:
						return p_185471_1_.with(SHAPE, RailShape.ASCENDING_WEST);
					case ASCENDING_WEST:
						return p_185471_1_.with(SHAPE, RailShape.ASCENDING_EAST);
					case EAST_WEST:
						return p_185471_1_.with(BACKWARDS, !p_185471_1_.get(BACKWARDS));
					default:
						break;
				}
		}
		return super.mirror(p_185471_1_, p_185471_2_);
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getWorld();
		if (world.isRemote)
			return ActionResultType.SUCCESS;
		BlockPos pos = context.getPos();
		for (Rotation testRotation : new Rotation[]{Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180, Rotation.COUNTERCLOCKWISE_90}) {
			BlockState testState = rotate(state, testRotation);
			if (isStableWith(testState, world, pos)) {
				placeAndNotify(testState, pos, world);
				break;
			}
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResultType onSneakWrenched(BlockState state, ItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		BlockState testState = state.with(BACKWARDS, !state.get(BACKWARDS));
		if (isStableWith(testState, world, pos))
			placeAndNotify(testState, pos, world);
		return ActionResultType.SUCCESS;
	}

	private void placeAndNotify(BlockState state, BlockPos pos, World world) {
		world.setBlockState(pos, state, 3);
		world.notifyNeighborsOfStateChange(pos.down(), this);
		if (state.get(SHAPE).isAscending())
			world.notifyNeighborsOfStateChange(pos.up(), this);
	}
}
