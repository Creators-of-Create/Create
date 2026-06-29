package com.simibubi.create.content.logistics.itemHatch;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.common.Tags.Items;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class ItemHatchBlock extends HorizontalDirectionalBlock
	implements IBE<ItemHatchBlockEntity>, IWrenchable, ProperWaterloggedBlock {
	public static final MapCodec<ItemHatchBlock> CODEC = simpleCodec(ItemHatchBlock::new);

	public static final BooleanProperty OPEN = BooleanProperty.create("open");

	public ItemHatchBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(OPEN, false)
			.setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(OPEN, FACING, WATERLOGGED));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockState state = super.getStateForPlacement(pContext);
		if (state == null)
			return state;
		if (pContext.getClickedFace()
			.getAxis()
			.isVertical())
			return null;

		return withWater(state.setValue(FACING, pContext.getClickedFace()
			.getOpposite())
			.setValue(OPEN, false), pContext);
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
		updateWater(pLevel, pState, pPos);
		return pState;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (level.isClientSide())
			return ItemInteractionResult.SUCCESS;
		if (player instanceof FakePlayer)
			return ItemInteractionResult.SUCCESS;

		BlockEntity blockEntity = level.getBlockEntity(pos.relative(state.getValue(FACING)));
		if (blockEntity == null)
			return ItemInteractionResult.FAIL;
		IItemHandler targetInv = level.getCapability(ItemHandler.BLOCK, blockEntity.getBlockPos(), null);
		if (targetInv == null)
			return ItemInteractionResult.FAIL;

		FilteringBehaviour filter = BlockEntityBehaviour.get(level, pos, FilteringBehaviour.TYPE);
		if (filter == null)
			return ItemInteractionResult.FAIL;

		Inventory inventory = player.getInventory();
		List<ItemStack> failedInsertions = new ArrayList<>();
		boolean anyInserted = false;
		boolean depositItemInHand = !player.isShiftKeyDown();

		if (!depositItemInHand && stack.is(Items.TOOLS_WRENCH))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		for (int i = 0; i < inventory.items.size(); i++) {
			if (Inventory.isHotbarSlot(i) != depositItemInHand)
				continue;
			if (depositItemInHand && i != inventory.selected)
				continue;
			ItemStack item = inventory.getItem(i);
			if (item.isEmpty())
				continue;
			if (!item.getItem()
				.canFitInsideContainerItems() && !PackageItem.isPackage(item))
				continue;
			if (!filter.getFilter()
				.isEmpty() && !filter.test(item))
				continue;

			ItemStack remainder = ItemHandlerHelper.insertItemStacked(targetInv, item, true);
			if (remainder.getCount() == item.getCount())
				continue;

			ItemStack extracted = inventory.removeItem(i, item.getCount() - remainder.getCount());
			remainder = ItemHandlerHelper.insertItemStacked(targetInv, extracted, false);
			anyInserted = true;

			// remainder might not be empty in itemhandler edge cases
			if (!remainder.isEmpty())
				failedInsertions.add(remainder);
		}

		failedInsertions.forEach(inventory::placeItemBackInInventory);

		if (!anyInserted)
			return ItemInteractionResult.SUCCESS;

		AllSoundEvents.ITEM_HATCH.playOnServer(level, pos);
		level.setBlockAndUpdate(pos, state.setValue(OPEN, true));
		level.scheduleTick(pos, this, 10);

		CreateLang.translate(depositItemInHand ? "item_hatch.deposit_item" : "item_hatch.deposit_inventory")
			.sendStatus(player);
		return ItemInteractionResult.SUCCESS;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.ITEM_HATCH.get(pState.getValue(FACING)
			.getOpposite());
	}

	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		if (pState.getValue(OPEN))
			pLevel.setBlockAndUpdate(pPos, pState.setValue(OPEN, false));
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		IBE.onRemove(state, level, pos, newState);
	}

	@Override
	public Class<ItemHatchBlockEntity> getBlockEntityClass() {
		return ItemHatchBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ItemHatchBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.ITEM_HATCH.get();
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}
}
