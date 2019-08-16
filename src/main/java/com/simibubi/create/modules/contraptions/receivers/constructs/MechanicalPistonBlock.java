package com.simibubi.create.modules.contraptions.receivers.constructs;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.base.KineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class MechanicalPistonBlock extends KineticBlock {

	public static final EnumProperty<PistonState> STATE = EnumProperty.create("state", PistonState.class);
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final BooleanProperty AXIS_ALONG_FIRST_COORDINATE = BooleanProperty.create("axis_along_first");

	protected static final VoxelShape BASE_SHAPE_UP = makeCuboidShape(0, 0, 0, 16, 12, 16),
			BASE_SHAPE_DOWN = makeCuboidShape(0, 4, 0, 16, 16, 16),
			BASE_SHAPE_EAST = makeCuboidShape(0, 0, 0, 12, 16, 16),
			BASE_SHAPE_WEST = makeCuboidShape(4, 0, 0, 16, 16, 16),
			BASE_SHAPE_SOUTH = makeCuboidShape(0, 0, 0, 16, 16, 12),
			BASE_SHAPE_NORTH = makeCuboidShape(0, 0, 4, 16, 16, 16),

			EXTENDED_SHAPE_UP = VoxelShapes.or(BASE_SHAPE_UP, MechanicalPistonHeadBlock.AXIS_SHAPE_Y),
			EXTENDED_SHAPE_DOWN = VoxelShapes.or(BASE_SHAPE_DOWN, MechanicalPistonHeadBlock.AXIS_SHAPE_Y),
			EXTENDED_SHAPE_EAST = VoxelShapes.or(BASE_SHAPE_EAST, MechanicalPistonHeadBlock.AXIS_SHAPE_X),
			EXTENDED_SHAPE_WEST = VoxelShapes.or(BASE_SHAPE_WEST, MechanicalPistonHeadBlock.AXIS_SHAPE_X),
			EXTENDED_SHAPE_SOUTH = VoxelShapes.or(BASE_SHAPE_SOUTH, MechanicalPistonHeadBlock.AXIS_SHAPE_Z),
			EXTENDED_SHAPE_NORTH = VoxelShapes.or(BASE_SHAPE_NORTH, MechanicalPistonHeadBlock.AXIS_SHAPE_Z);

	protected boolean isSticky;

	public MechanicalPistonBlock(boolean sticky) {
		super(Properties.from(Blocks.PISTON));
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(STATE, PistonState.RETRACTED));
		isSticky = sticky;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(STATE, FACING, AXIS_ALONG_FIRST_COORDINATE);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction facing = context.getNearestLookingDirection().getOpposite();
		if (context.isPlacerSneaking())
			facing = facing.getOpposite();

		Vec3d diff = context.getHitVec().subtract(new Vec3d(context.getPos()));
		double firstCoord = facing.getAxis() == Axis.X ? diff.y : diff.x;
		double secondCoord = facing.getAxis() == Axis.Z ? diff.y : diff.z;
		boolean alongFirst = firstCoord + secondCoord < 1 ^ firstCoord < secondCoord;

		return this.getDefaultState().with(FACING, facing).with(STATE, PistonState.RETRACTED)
				.with(AXIS_ALONG_FIRST_COORDINATE, alongFirst);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MechanicalPistonTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		Axis pistonAxis = state.get(FACING).getAxis();
		boolean alongFirst = state.get(AXIS_ALONG_FIRST_COORDINATE);

		if (pistonAxis == Axis.X)
			return alongFirst ? Axis.Y : Axis.Z;
		if (pistonAxis == Axis.Y)
			return alongFirst ? Axis.X : Axis.Z;
		if (pistonAxis == Axis.Z)
			return alongFirst ? Axis.X : Axis.Y;

		return super.getRotationAxis(state);
	}

	@Override
	public boolean isAxisTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == getRotationAxis(state);
	}

	public enum PistonState implements IStringSerializable {
		RETRACTED, MOVING, EXTENDED;

		@Override
		public String getName() {
			return name().toLowerCase();
		}
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		Direction direction = state.get(FACING);
		BlockPos pistonHead = null;
		BlockPos pistonBase = pos;

		for (int offset = 1; offset < Construct.MAX_EXTENSIONS; offset++) {
			BlockPos currentPos = pos.offset(direction, offset);
			BlockState block = worldIn.getBlockState(currentPos);

			if (AllBlocks.PISTON_POLE.typeOf(block)
					&& direction.getAxis() == block.get(BlockStateProperties.FACING).getAxis())
				continue;

			if (AllBlocks.MECHANICAL_PISTON_HEAD.typeOf(block) && block.get(BlockStateProperties.FACING) == direction) {
				pistonHead = currentPos;
			}

			break;
		}

		if (pistonHead != null && pistonBase != null) {
			BlockPos.getAllInBox(pistonBase, pistonHead).filter(p -> !p.equals(pos))
					.forEach(p -> worldIn.destroyBlock(p, !player.isCreative()));
		}

		for (int offset = 1; offset < Construct.MAX_EXTENSIONS; offset++) {
			BlockPos currentPos = pos.offset(direction.getOpposite(), offset);
			BlockState block = worldIn.getBlockState(currentPos);

			if (AllBlocks.PISTON_POLE.typeOf(block)
					&& direction.getAxis() == block.get(BlockStateProperties.FACING).getAxis()) {
				worldIn.destroyBlock(currentPos, !player.isCreative());
				continue;
			}

			break;
		}

		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

		if (state.get(STATE) == PistonState.EXTENDED)
			switch (state.get(FACING)) {
			case DOWN:
				return EXTENDED_SHAPE_DOWN;
			case EAST:
				return EXTENDED_SHAPE_EAST;
			case NORTH:
				return EXTENDED_SHAPE_NORTH;
			case SOUTH:
				return EXTENDED_SHAPE_SOUTH;
			case UP:
				return EXTENDED_SHAPE_UP;
			case WEST:
				return EXTENDED_SHAPE_WEST;
			}

		if (state.get(STATE) == PistonState.MOVING)
			switch (state.get(FACING)) {
			case DOWN:
				return BASE_SHAPE_DOWN;
			case EAST:
				return BASE_SHAPE_EAST;
			case NORTH:
				return BASE_SHAPE_NORTH;
			case SOUTH:
				return BASE_SHAPE_SOUTH;
			case UP:
				return BASE_SHAPE_UP;
			case WEST:
				return BASE_SHAPE_WEST;
			}

		return VoxelShapes.fullCube();
	}

}
