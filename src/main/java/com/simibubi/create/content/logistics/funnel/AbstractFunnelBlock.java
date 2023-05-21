package com.simibubi.create.content.logistics.funnel;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.block.render.ReducedDestroyEffects;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

public abstract class AbstractFunnelBlock extends Block
	implements IBE<FunnelBlockEntity>, IWrenchable, ProperWaterloggedBlock {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	protected AbstractFunnelBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
		registerDefaultState(defaultBlockState().setValue(POWERED, false)
			.setValue(WATERLOGGED, false));
	}

	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
		consumer.accept(new ReducedDestroyEffects());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withWater(defaultBlockState().setValue(POWERED, context.getLevel()
			.hasNeighborSignal(context.getClickedPos())), context);
	}
	
	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
		updateWater(pLevel, pState, pCurrentPos);
		return pState;
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED, WATERLOGGED));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;
		InvManipulationBehaviour behaviour = BlockEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
		if (behaviour != null)
			behaviour.onNeighborChanged(fromPos);
		if (!worldIn.getBlockTicks()
			.willTickThisTick(pos, this))
			worldIn.scheduleTick(pos, this, 0);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource r) {
		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered != worldIn.hasNeighborSignal(pos))
			worldIn.setBlock(pos, state.cycle(POWERED), 2);
	}

	public static ItemStack tryInsert(Level worldIn, BlockPos pos, ItemStack toInsert, boolean simulate) {
		FilteringBehaviour filter = BlockEntityBehaviour.get(worldIn, pos, FilteringBehaviour.TYPE);
		InvManipulationBehaviour inserter = BlockEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
		if (inserter == null)
			return toInsert;
		if (filter != null && !filter.test(toInsert))
			return toInsert;
		if (simulate)
			inserter.simulate();
		ItemStack insert = inserter.insert(toInsert);

		if (!simulate && insert.getCount() != toInsert.getCount()) {
			BlockEntity blockEntity = worldIn.getBlockEntity(pos);
			if (blockEntity instanceof FunnelBlockEntity) {
				FunnelBlockEntity funnelBlockEntity = (FunnelBlockEntity) blockEntity;
				funnelBlockEntity.onTransfer(toInsert);
				if (funnelBlockEntity.hasFlap())
					funnelBlockEntity.flap(true);
			}
		}
		return insert;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		Block block = world.getBlockState(pos.relative(getFunnelFacing(state).getOpposite()))
			.getBlock();
		return !(block instanceof AbstractFunnelBlock);
	}

	@Nullable
	public static boolean isFunnel(BlockState state) {
		return state.getBlock() instanceof AbstractFunnelBlock;
	}

	@Nullable
	public static Direction getFunnelFacing(BlockState state) {
		if (!(state.getBlock() instanceof AbstractFunnelBlock))
			return null;
		return ((AbstractFunnelBlock) state.getBlock()).getFacing(state);
	}

	protected abstract Direction getFacing(BlockState state);

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock() && !isFunnel(newState) || !newState.hasBlockEntity())
			IBE.onRemove(state, world, pos, newState);
	}

	@Override
	public Class<FunnelBlockEntity> getBlockEntityClass() {
		return FunnelBlockEntity.class;
	}

	public BlockEntityType<? extends FunnelBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.FUNNEL.get();
	};

}
