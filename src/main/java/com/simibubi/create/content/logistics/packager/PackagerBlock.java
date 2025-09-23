package com.simibubi.create.content.logistics.packager;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;

public class PackagerBlock extends WrenchableDirectionalBlock implements IBE<PackagerBlockEntity>, IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty LINKED = BooleanProperty.create("linked");

	public PackagerBlock(Properties properties) {
		super(properties);
		BlockState defaultBlockState = defaultBlockState();
		if (defaultBlockState.hasProperty(LINKED))
			defaultBlockState = defaultBlockState.setValue(LINKED, false);
		registerDefaultState(defaultBlockState.setValue(POWERED, false));
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Capability<IItemHandler> itemCap = ForgeCapabilities.ITEM_HANDLER;
		Direction preferredFacing = null;
		for (Direction face : context.getNearestLookingDirections()) {
			BlockEntity be = context.getLevel()
				.getBlockEntity(context.getClickedPos()
					.relative(face));
			if (be instanceof PackagerBlockEntity)
				continue;
			if (be != null && (be.getCapability(itemCap)
				.isPresent())) {
				preferredFacing = face.getOpposite();
				break;
			}
		}

		Player player = context.getPlayer();
		if (preferredFacing == null) {
			Direction facing = context.getNearestLookingDirection();
			preferredFacing = player != null && player
				.isShiftKeyDown() ? facing : facing.getOpposite();
		}

		if (player != null && !(player instanceof FakePlayer)) {
			if (AllBlocks.PORTABLE_STORAGE_INTERFACE.has(context.getLevel()
				.getBlockState(context.getClickedPos()
					.relative(preferredFacing.getOpposite())))) {
				CreateLang.translate("packager.no_portable_storage")
					.sendStatus(player);
				return null;
			}
		}

		return super.getStateForPlacement(context).setValue(POWERED, context.getLevel()
				.hasNeighborSignal(context.getClickedPos()))
			.setValue(FACING, preferredFacing);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
								 BlockHitResult hit) {
		if (player == null)
			return InteractionResult.PASS;

		ItemStack itemInHand = player.getItemInHand(handIn);
		if (AllItems.WRENCH.isIn(itemInHand))
			return InteractionResult.PASS;
		if (AllBlocks.FACTORY_GAUGE.isIn(itemInHand))
			return InteractionResult.PASS;
		if (AllBlocks.STOCK_LINK.isIn(itemInHand) && !(state.hasProperty(LINKED) && state.getValue(LINKED)))
			return InteractionResult.PASS;
		if (AllBlocks.PACKAGE_FROGPORT.isIn(itemInHand))
			return InteractionResult.PASS;

		if (onBlockEntityUse(worldIn, pos, be -> {
			if (be.heldBox.isEmpty()) {
				if (be.animationTicks > 0)
					return InteractionResult.SUCCESS;
				if (PackageItem.isPackage(itemInHand)) {
					if (worldIn.isClientSide())
						return InteractionResult.SUCCESS;
					if (!be.unwrapBox(itemInHand.copy(), true))
						return InteractionResult.SUCCESS;
					be.unwrapBox(itemInHand.copy(), false);
					be.triggerStockCheck();
					itemInHand.shrink(1);
					AllSoundEvents.DEPOT_PLOP.playOnServer(worldIn, pos);
					if (itemInHand.isEmpty())
						player.setItemInHand(handIn, ItemStack.EMPTY);
					return InteractionResult.SUCCESS;
				}
				return InteractionResult.SUCCESS;
			}
			if (be.animationTicks > 0)
				return InteractionResult.SUCCESS;
			if (!worldIn.isClientSide()) {
				player.getInventory()
					.placeItemBackInInventory(be.heldBox.copy());
				AllSoundEvents.playItemPickup(player);
				be.heldBox = ItemStack.EMPTY;
				be.notifyUpdate();
			}
			return InteractionResult.SUCCESS;
		}).consumesAction())
			return InteractionResult.SUCCESS;

		return InteractionResult.SUCCESS;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED, LINKED));
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(state, level, pos, neighbor);
		if (neighbor.relative(state.getOptionalValue(FACING)
				.orElse(Direction.UP))
			.equals(pos))
			withBlockEntityDo(level, pos, PackagerBlockEntity::triggerStockCheck);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
								boolean isMoving) {
		if (worldIn.isClientSide)
			return;

		InvManipulationBehaviour behaviour = BlockEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
		if (behaviour != null)
			behaviour.onNeighborChanged(fromPos);

		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered == worldIn.hasNeighborSignal(pos))
			return;
		worldIn.setBlock(pos, state.cycle(POWERED), 2);
		if (!previouslyPowered)
			withBlockEntityDo(worldIn, pos, PackagerBlockEntity::activate);
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		IBE.onRemove(pState, pLevel, pPos, pNewState);
	}

	@Override
	public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public Class<PackagerBlockEntity> getBlockEntityClass() {
		return PackagerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends PackagerBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.PACKAGER.get();
	}

	@Override
	public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
		return false;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState pState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
		return getBlockEntityOptional(pLevel, pPos).map(pbe -> {
				boolean empty = pbe.inventory.getStackInSlot(0)
					.isEmpty();
				if (pbe.animationTicks != 0)
					empty = false;
				return empty ? 0 : 15;
			})
			.orElse(0);
	}

}
