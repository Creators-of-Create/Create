package com.simibubi.create.content.decoration.placard;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.schematics.requirement.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;

public class PlacardBlock extends FaceAttachedHorizontalDirectionalBlock
	implements ProperWaterloggedBlock, IBE<PlacardBlockEntity>, ISpecialBlockItemRequirement, IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public PlacardBlock(Properties p_53182_) {
		super(p_53182_);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(FACE, FACING, WATERLOGGED, POWERED));
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		return canAttachLenient(pLevel, pPos, getConnectedDirection(pState).getOpposite());
	}

	public static boolean canAttachLenient(LevelReader pReader, BlockPos pPos, Direction pDirection) {
		BlockPos blockpos = pPos.relative(pDirection);
		return !pReader.getBlockState(blockpos)
			.getCollisionShape(pReader, blockpos)
			.isEmpty();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockState stateForPlacement = super.getStateForPlacement(pContext);
		if (stateForPlacement == null)
			return null;
		if (stateForPlacement.getValue(FACE) == AttachFace.FLOOR)
			stateForPlacement = stateForPlacement.setValue(FACING, stateForPlacement.getValue(FACING)
				.getOpposite());
		return withWater(stateForPlacement, pContext);
	}

	@Override
	public boolean isSignalSource(BlockState pState) {
		return true;
	}

	@Override
	public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
		return pBlockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
		return pBlockState.getValue(POWERED) && getConnectedDirection(pBlockState) == pSide ? 15 : 0;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.PLACARD.get(getConnectedDirection(pState));
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel,
		BlockPos pCurrentPos, BlockPos pFacingPos) {
		updateWater(pLevel, pState, pCurrentPos);
		return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player player, InteractionHand pHand,
		BlockHitResult pHit) {
		if (player.isShiftKeyDown())
			return InteractionResult.PASS;
		if (pLevel.isClientSide)
			return InteractionResult.SUCCESS;

		ItemStack inHand = player.getItemInHand(pHand);
		return onBlockEntityUse(pLevel, pPos, pte -> {
			ItemStack inBlock = pte.getHeldItem();

			if (!player.mayBuild() || inHand.isEmpty() || !inBlock.isEmpty()) {
				if (inBlock.isEmpty())
					return InteractionResult.FAIL;
				if (inHand.isEmpty())
					return InteractionResult.FAIL;
				if (pState.getValue(POWERED))
					return InteractionResult.FAIL;

				boolean test = inBlock.getItem() instanceof FilterItem ? FilterItemStack.of(inBlock)
					.test(pLevel, inHand) : ItemHandlerHelper.canItemStacksStack(inHand, inBlock);
				if (!test) {
					AllSoundEvents.DENY.play(pLevel, null, pPos, 1, 1);
					return InteractionResult.SUCCESS;
				}

				AllSoundEvents.CONFIRM.play(pLevel, null, pPos, 1, 1);
				pLevel.setBlock(pPos, pState.setValue(POWERED, true), 3);
				updateNeighbours(pState, pLevel, pPos);
				pte.poweredTicks = 19;
				pte.notifyUpdate();
				return InteractionResult.SUCCESS;
			}

			pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1, 1);
			pte.setHeldItem(ItemHandlerHelper.copyStackWithSize(inHand, 1));

			if (!player.isCreative()) {
				inHand.shrink(1);
				if (inHand.isEmpty())
					player.setItemInHand(pHand, ItemStack.EMPTY);
			}

			return InteractionResult.SUCCESS;
		});
	}

	public static Direction connectedDirection(BlockState state) {
		return getConnectedDirection(state);
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		boolean blockChanged = !pState.is(pNewState.getBlock());
		if (!pIsMoving && blockChanged)
			if (pState.getValue(POWERED))
				updateNeighbours(pState, pLevel, pPos);

		if (pState.hasBlockEntity() && (blockChanged || !pNewState.hasBlockEntity())) {
			if (!pIsMoving)
				withBlockEntityDo(pLevel, pPos, be -> Block.popResource(pLevel, pPos, be.getHeldItem()));
			pLevel.removeBlockEntity(pPos);
		}
	}

	public static void updateNeighbours(BlockState pState, Level pLevel, BlockPos pPos) {
		pLevel.updateNeighborsAt(pPos, pState.getBlock());
		pLevel.updateNeighborsAt(pPos.relative(getConnectedDirection(pState).getOpposite()), pState.getBlock());
	}

	@Override
	public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
		if (pLevel.isClientSide)
			return;
		withBlockEntityDo(pLevel, pPos, pte -> {
			ItemStack heldItem = pte.getHeldItem();
			if (heldItem.isEmpty())
				return;
			pPlayer.getInventory()
				.placeItemBackInInventory(heldItem);
			pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1, 1);
			pte.setHeldItem(ItemStack.EMPTY);
		});
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
		ItemStack placardStack = AllBlocks.PLACARD.asStack();
		if (be instanceof PlacardBlockEntity pbe) {
			ItemStack heldItem = pbe.getHeldItem();
			if (!heldItem.isEmpty()) {
				return new ItemRequirement(List.of(
					new ItemRequirement.StackRequirement(placardStack, ItemUseType.CONSUME),
					new ItemRequirement.StrictNbtStackRequirement(heldItem, ItemUseType.CONSUME)
				));
			}
		}
		return new ItemRequirement(ItemUseType.CONSUME, placardStack);
	}

	@Override
	public Class<PlacardBlockEntity> getBlockEntityClass() {
		return PlacardBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends PlacardBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.PLACARD.get();
	}

}
