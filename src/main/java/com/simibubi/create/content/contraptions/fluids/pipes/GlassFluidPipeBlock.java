package com.simibubi.create.content.contraptions.fluids.pipes;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GlassFluidPipeBlock extends AxisPipeBlock implements SimpleWaterloggedBlock, ISpecialBlockItemRequirement {

	public static final BooleanProperty ALT = BooleanProperty.create("alt");

	public GlassFluidPipeBlock(Properties p_i48339_1_) {
		super(p_i48339_1_);
		registerDefaultState(defaultBlockState().setValue(ALT, false).setValue(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> p_206840_1_) {
		super.createBlockStateDefinition(p_206840_1_.add(ALT, BlockStateProperties.WATERLOGGED));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return AllTileEntities.GLASS_FLUID_PIPE.create();
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		if (tryRemoveBracket(context))
			return InteractionResult.SUCCESS;
		BlockState newState;
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		FluidTransportBehaviour.cacheFlows(world, pos);
		newState = toRegularPipe(world, pos, state).setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
		world.setBlock(pos, newState, 3);
		FluidTransportBehaviour.loadFlows(world, pos);
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState ifluidstate = context.getLevel()
			.getFluidState(context.getClickedPos());
		BlockState state = super.getStateForPlacement(context);
		return state == null ? null : state.setValue(BlockStateProperties.WATERLOGGED,
			ifluidstate.getType() == Fluids.WATER);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false)
			: Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity te) {
		return ItemRequirement.of(AllBlocks.FLUID_PIPE.getDefaultState(), te);
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
