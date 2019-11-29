package com.simibubi.create.modules.contraptions.relays.belt;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BeltTunnelBlock extends Block {

	public static final IProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
	public static final IProperty<Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;

	public BeltTunnelBlock() {
		super(Properties.from(Blocks.GOLD_BLOCK));
		setDefaultState(getDefaultState().with(SHAPE, Shape.STRAIGHT));
	}

	public enum Shape implements IStringSerializable {
		STRAIGHT, WINDOW, T_LEFT, T_RIGHT, CROSS;

		@Override
		public String getName() {
			return Lang.asId(name());
		}
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new BeltTunnelTileEntity();
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockState blockState = worldIn.getBlockState(pos.down());
		if (!AllBlocks.BELT.typeOf(blockState))
			return false;
		if (blockState.get(BeltBlock.SLOPE) != Slope.HORIZONTAL)
			return false;
		if (!blockState.get(BeltBlock.CASING))
			return false;
		return true;
	}

	@Override
	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
		if (state.get(SHAPE) == Shape.WINDOW)
			return layer == BlockRenderLayer.CUTOUT_MIPPED;
		return super.canRenderInLayer(state, layer);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getTunnelState(context.getWorld(), context.getPos());
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		return getTunnelState(worldIn, currentPos);
	}

	public static void updateTunnel(World world, BlockPos pos) {
		BlockState tunnel = world.getBlockState(pos);
		BlockState newTunnel = getTunnelState(world, pos);
		if (tunnel != newTunnel)
			world.setBlockState(pos, newTunnel, 3);
	}

	private static BlockState getTunnelState(IBlockReader reader, BlockPos pos) {
		BlockState state = AllBlocks.BELT_TUNNEL.get().getDefaultState();

		BlockState belt = reader.getBlockState(pos.down());
		if (AllBlocks.BELT.typeOf(belt))
			state = state.with(HORIZONTAL_AXIS, belt.get(BeltBlock.HORIZONTAL_FACING).getAxis());
		Axis axis = state.get(HORIZONTAL_AXIS);

		// T and Cross
		Direction left = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis).rotateY();
		BlockState leftState = reader.getBlockState(pos.offset(left).down());
		boolean onLeft = AllBlocks.BELT.typeOf(leftState)
				&& leftState.get(BeltBlock.HORIZONTAL_FACING).getAxis() != axis;
		BlockState rightState = reader.getBlockState(pos.offset(left.getOpposite()).down());
		boolean onRight = AllBlocks.BELT.typeOf(rightState)
				&& rightState.get(BeltBlock.HORIZONTAL_FACING).getAxis() != axis;

		if (onLeft && onRight)
			state = state.with(SHAPE, Shape.CROSS);
		else if (onLeft)
			state = state.with(SHAPE, Shape.T_LEFT);
		else if (onRight)
			state = state.with(SHAPE, Shape.T_RIGHT);

		if (state.get(SHAPE) == Shape.STRAIGHT) {
			Direction fw = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
			if (AllBlocks.BELT_TUNNEL.typeOf(reader.getBlockState(pos.offset(fw)))
					&& AllBlocks.BELT_TUNNEL.typeOf(reader.getBlockState(pos.offset(fw.getOpposite()))))
				state = state.with(SHAPE, Shape.WINDOW);
		}

		return state;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		if (fromPos.equals(pos.down())) {
			if (!isValidPosition(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_AXIS, SHAPE);
		super.fillStateContainer(builder);
	}

}
