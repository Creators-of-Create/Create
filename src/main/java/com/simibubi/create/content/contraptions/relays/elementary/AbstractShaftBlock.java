package com.simibubi.create.content.contraptions.relays.elementary;

import java.util.Optional;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.contraptions.wrench.IWrenchableWithBracket;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.PushReaction;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public abstract class AbstractShaftBlock extends RotatedPillarKineticBlock implements IWaterLoggable, IWrenchableWithBracket {

	public AbstractShaftBlock(Properties properties) {
		super(properties);
		setDefaultState(super.getDefaultState().with(BlockStateProperties.WATERLOGGED, false));
	}
	
	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		return IWrenchableWithBracket.super.onWrenched(state, context);
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.SIMPLE_KINETIC.create();
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state != newState && !isMoving)
			removeBracket(world, pos, true).ifPresent(stack -> Block.spawnAsEntity(world, pos, stack));
		super.onReplaced(state, world, pos, newState, isMoving);
	}
	
	// IRotate:

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(AXIS);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(AXIS);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
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
		FluidState ifluidstate = context.getWorld()
			.getFluidState(context.getPos());
		return super.getStateForPlacement(context).with(BlockStateProperties.WATERLOGGED,
			Boolean.valueOf(ifluidstate.getFluid() == Fluids.WATER));
	}

	@Override
	public Optional<ItemStack> removeBracket(IBlockReader world, BlockPos pos, boolean inOnReplacedContext) {
		BracketedTileEntityBehaviour behaviour = TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);
		if (behaviour == null)
			return Optional.empty();
		BlockState bracket = behaviour.getBracket();
		behaviour.removeBracket(inOnReplacedContext);
		if (bracket == Blocks.AIR.getDefaultState())
			return Optional.empty();
		return Optional.of(new ItemStack(bracket.getBlock()));
	}
}
