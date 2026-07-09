package com.simibubi.create.content.trains.station;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.logistics.depot.SharedDepotBlockMethods;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;

import net.createmod.catnip.api.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StationBlock extends Block implements IBE<StationBlockEntity>, IWrenchable, ProperWaterloggedBlock {

	public static final BooleanProperty ASSEMBLING = BooleanProperty.create("assembling");

	public StationBlock(Properties p_54120_) {
		super(p_54120_);
		registerDefaultState(defaultBlockState().setValue(ASSEMBLING, false)
			.setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(ASSEMBLING, WATERLOGGED));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return withWater(super.getStateForPlacement(pContext), pContext);
	}

	@Override
	public BlockState updateShape(BlockState pState, LevelReader pLevel, ScheduledTickAccess ticks, BlockPos pCurrentPos, Direction pDirection, BlockPos pNeighborPos, BlockState pNeighborState, RandomSource random) {
		updateWater(ticks, pLevel, pState, pCurrentPos);
		return pState;
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState pState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos, Direction direction) {
		return getBlockEntityOptional(pLevel, pPos).map(ste -> ste.trainPresent ? 15 : 0)
			.orElse(0);
	}

	@Override
protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel worldIn, BlockPos pos, boolean isMoving) {
		IBE.onRemove(state, worldIn, pos, Blocks.AIR.defaultBlockState());
	}

	@Override
	public void fallOn(Level worldIn, BlockState state, BlockPos pos, Entity entityIn, double fallDistance) {
		super.fallOn(worldIn, state, pos, entityIn, fallDistance);
		SharedDepotBlockMethods.onLanded(worldIn, entityIn);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (player == null || player.isShiftKeyDown())
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		if (AllItems.WRENCH.isIn(stack))
			return InteractionResult.TRY_WITH_EMPTY_HAND;

		if (stack.getItem() == Items.FILLED_MAP) {
			return onBlockEntityUseItemOn(level, pos, station -> {
				if (level.isClientSide())
					return InteractionResult.SUCCESS;

				if (station.getStation() == null || station.getStation().getId() == null)
					return InteractionResult.FAIL;

				MapItemSavedData savedData = MapItem.getSavedData(stack, level);
				if (!(savedData instanceof StationMapData stationMapData))
					return InteractionResult.FAIL;

				if (!stationMapData.toggleStation(level, pos, station))
					return InteractionResult.FAIL;

				return InteractionResult.SUCCESS;
			});
		}

		InteractionResult result = onBlockEntityUse(level, pos, station -> {
			ItemStack autoSchedule = station.getAutoSchedule();
			if (autoSchedule.isEmpty())
				return InteractionResult.PASS;
			if (level.isClientSide())
				return InteractionResult.SUCCESS;
			player.getInventory()
				.placeItemBackInInventory(autoSchedule.copy());
			station.depotBehaviour.removeHeldItem();
			station.notifyUpdate();
			AllSoundEvents.playItemPickup(player);
			return InteractionResult.SUCCESS;
		});

		if (result == InteractionResult.PASS)
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> withBlockEntityDo(level, pos, be -> StationClient.displayScreen(this, be, player)));
		return InteractionResult.SUCCESS;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.STATION;
	}

	@Override
	public Class<StationBlockEntity> getBlockEntityClass() {
		return StationBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends StationBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.TRACK_STATION.get();
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

}
