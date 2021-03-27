package com.simibubi.create.content.contraptions.fluids;

import java.util.Random;

import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class PumpBlock extends DirectionalKineticBlock implements IWaterLoggable, ICogWheel {

	public PumpBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		setDefaultState(super.getDefaultState().with(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.MECHANICAL_PUMP.create();
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		return originalState.with(FACING, originalState.get(FACING)
			.getOpposite());
	}

	@Override
	public BlockState updateAfterWrenched(BlockState newState, ItemUseContext context) {
		BlockState state = super.updateAfterWrenched(newState, context);
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		if (world.isRemote)
			return state;
		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof PumpTileEntity))
			return state;
		PumpTileEntity pump = (PumpTileEntity) tileEntity;
		pump.sidesToUpdate.forEach(MutableBoolean::setTrue);
		pump.reversed = !pump.reversed;
		return state;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING)
			.getAxis();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.PUMP.get(state.get(FACING));
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block otherBlock, BlockPos neighborPos,
		boolean isMoving) {
		DebugPacketSender.func_218806_a(world, pos);
		Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
		if (d == null)
			return;
		if (!isOpenAt(state, d))
			return;
		world.getPendingBlockTicks()
			.scheduleTick(pos, this, 1, TickPriority.HIGH);
//		if (world.isRemote)
//			return;
//		if (otherBlock instanceof FluidPipeBlock)
//			return;
//		TileEntity tileEntity = world.getTileEntity(pos);
//		if (!(tileEntity instanceof PumpTileEntity))
//			return;
//		PumpTileEntity pump = (PumpTileEntity) tileEntity;
//		Direction facing = state.get(FACING);
//		for (boolean front : Iterate.trueAndFalse) {
//			Direction side = front ? facing : facing.getOpposite();
//			if (!pos.offset(side)
//				.equals(neighborPos))
//				continue;
//			pump.updatePipesOnSide(side);
//		}
	}

	@Override
	public IFluidState getFluidState(BlockState state) {
		return state.get(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false)
			: Fluids.EMPTY.getDefaultState();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.WATERLOGGED);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState neighbourState,
		IWorld world, BlockPos pos, BlockPos neighbourPos) {
		if (state.get(BlockStateProperties.WATERLOGGED)) {
			world.getPendingFluidTicks()
				.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		IFluidState ifluidstate = context.getWorld()
			.getFluidState(context.getPos());
		return super.getStateForPlacement(context).with(BlockStateProperties.WATERLOGGED,
			Boolean.valueOf(ifluidstate.getFluid() == Fluids.WATER));
	}

	public static boolean isPump(BlockState state) {
		return state.getBlock() instanceof PumpBlock;
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (world.isRemote)
			return;
		if (state != oldState)
			world.getPendingBlockTicks()
				.scheduleTick(pos, this, 1, TickPriority.HIGH);
	}

	public static boolean isOpenAt(BlockState state, Direction d) {
		return d.getAxis() == state.get(FACING)
			.getAxis();
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random r) {
		FluidPropagator.propagateChangedPipe(world, pos, state);
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		boolean blockTypeChanged = state.getBlock() != newState.getBlock();
		if (blockTypeChanged && !world.isRemote)
			FluidPropagator.propagateChangedPipe(world, pos, state);
		if (state.hasTileEntity() && (blockTypeChanged || !newState.hasTileEntity()))
			world.removeTileEntity(pos);
	}
	
}
