package com.simibubi.create.content.contraptions.fluids;

import java.util.Map;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Iterate;

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
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class PumpBlock extends DirectionalKineticBlock implements IWaterLoggable {

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
		if (pump.networks == null)
			return state;

		FluidNetwork apn1 = pump.networks.get(true);
		FluidNetwork apn2 = pump.networks.get(false);

		// Collect pipes that can be skipped
		apn1.clearFlows(world, true);
		apn2.clearFlows(world, true);

		// Swap skipsets as the networks change sides
		Map<BlockFace, FluidStack> skippedConnections = apn1.previousFlow;
		apn1.previousFlow = apn2.previousFlow;
		apn2.previousFlow = skippedConnections;

		// Init networks next tick
		pump.networksToUpdate.forEach(MutableBoolean::setTrue);
		pump.networks.swap();
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
	public boolean hasIntegratedCogwheel(IWorldReader world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block otherBlock, BlockPos neighborPos,
		boolean isMoving) {
		DebugPacketSender.func_218806_a(world, pos);
		if (world.isRemote)
			return;
		if (otherBlock instanceof FluidPipeBlock)
			return;
		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof PumpTileEntity))
			return;
		PumpTileEntity pump = (PumpTileEntity) tileEntity;
		Direction facing = state.get(FACING);
		for (boolean front : Iterate.trueAndFalse) {
			Direction side = front ? facing : facing.getOpposite();
			if (!pos.offset(side)
				.equals(neighborPos))
				continue;
			pump.updatePipesOnSide(side);
		}
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

}
