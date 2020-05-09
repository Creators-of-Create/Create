package com.simibubi.create.modules.logistics.block.belts.tunnel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.WrappedWorld;
import com.simibubi.create.modules.contraptions.IWrenchable;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BeltTunnelBlock extends Block implements ITE<BeltTunnelTileEntity>, IWrenchable {

	public static final IProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
	public static final IProperty<Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;

	public BeltTunnelBlock() {
		super(Properties.from(Blocks.GOLD_BLOCK));
		setDefaultState(getDefaultState().with(SHAPE, Shape.STRAIGHT));
	}

	public enum Shape implements IStringSerializable {
		STRAIGHT, WINDOW, HALFSHADE, FULLSHADE, T_LEFT, T_RIGHT, CROSS;

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
	public boolean isSolid(BlockState state) {
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return BeltTunnelShapes.getShape(state);
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
		if (hasWindow(state))
			return layer == BlockRenderLayer.CUTOUT_MIPPED;
		return super.canRenderInLayer(state, layer);
	}

	public static boolean hasWindow(BlockState state) {
		Shape shape = state.get(SHAPE);
		return shape == Shape.WINDOW || shape == Shape.HALFSHADE || shape == Shape.FULLSHADE;
	}

	public static boolean isStraight(BlockState state) {
		return hasWindow(state) || state.get(SHAPE) == Shape.STRAIGHT;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getTunnelState(context.getWorld(), context.getPos());
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (!(worldIn instanceof WrappedWorld))
			withTileEntityDo(worldIn, currentPos, BeltTunnelTileEntity::initFlaps);
		BlockState tunnelState = getTunnelState(worldIn, currentPos);

		if (tunnelState.get(HORIZONTAL_AXIS) == state.get(HORIZONTAL_AXIS)) {
			if (hasWindow(tunnelState) == hasWindow(state))
				return state;
		}

		return tunnelState;
	}

	public static void updateTunnel(World world, BlockPos pos) {
		BlockState tunnel = world.getBlockState(pos);
		BlockState newTunnel = getTunnelState(world, pos);
		if (tunnel != newTunnel) {
			world.setBlockState(pos, newTunnel, 3);
			TileEntity te = world.getTileEntity(pos);
			if (te != null && (te instanceof BeltTunnelTileEntity))
				((BeltTunnelTileEntity) te).initFlaps();
		}
	}

	public static List<BeltTunnelTileEntity> getSynchronizedGroup(World world, BlockPos pos, Direction flapFacing) {
		List<BeltTunnelTileEntity> group = new ArrayList<>();
		Direction searchDirection = flapFacing.rotateY();

		for (Direction d : Arrays.asList(searchDirection, searchDirection.getOpposite())) {
			BlockPos currentPos = pos;
			while (true) {
				if (!world.isBlockPresent(currentPos))
					break;
				TileEntity te = world.getTileEntity(currentPos);
				if (te == null || !(te instanceof BeltTunnelTileEntity))
					break;
				BeltTunnelTileEntity tunnel = (BeltTunnelTileEntity) te;
				if (!tunnel.syncedFlaps.containsKey(flapFacing))
					break;
				group.add(tunnel);
				currentPos = currentPos.offset(d);
			}
		}

		return group;
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
		boolean onLeft =
			AllBlocks.BELT.typeOf(leftState) && leftState.get(BeltBlock.HORIZONTAL_FACING).getAxis() != axis;
		BlockState rightState = reader.getBlockState(pos.offset(left.getOpposite()).down());
		boolean onRight =
			AllBlocks.BELT.typeOf(rightState) && rightState.get(BeltBlock.HORIZONTAL_FACING).getAxis() != axis;

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
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {

		// Toggle sync
		if (toggleSync(context.getWorld(), context.getPos(), context.getFace()))
			return ActionResultType.SUCCESS;

		// Toggle windows
		if (!hasWindow(state))
			return IWrenchable.super.onWrenched(state, context);
		Shape next = state.get(SHAPE);
		switch (state.get(SHAPE)) {
		case FULLSHADE:
			next = Shape.WINDOW;
			break;
		case HALFSHADE:
			next = Shape.FULLSHADE;
			break;
		case WINDOW:
			next = Shape.HALFSHADE;
			break;
		default:
			break;
		}
		if (!context.getWorld().isRemote)
			context.getWorld().setBlockState(context.getPos(), state.with(SHAPE, next), 2);
		return ActionResultType.SUCCESS;
	}

	private boolean toggleSync(World world, BlockPos pos, Direction face) {
		TileEntity te = world.getTileEntity(pos);
		if (te == null || !(te instanceof BeltTunnelTileEntity))
			return false;
		BeltTunnelTileEntity tunnel = (BeltTunnelTileEntity) te;
		return tunnel.toggleSyncForFlap(face);
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

	@Override
	public Class<BeltTunnelTileEntity> getTileEntityClass() {
		return BeltTunnelTileEntity.class;
	}

}
