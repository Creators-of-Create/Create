package com.simibubi.create.content.logistics.block.vault;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.LinkedList;
import java.util.List;

public class ItemVaultBlock extends Block implements IWrenchable, ITE<ItemVaultTileEntity> {

	public static final Property<Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;

	public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 16);
	public static final BooleanProperty LARGE = BooleanProperty.create("large");

	public ItemVaultBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
		registerDefaultState(defaultBlockState().setValue(LARGE, false)
				.setValue(COLOR, 16));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(HORIZONTAL_AXIS, LARGE);
		super.createBlockStateDefinition(pBuilder);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		if (pContext.getPlayer() == null || !pContext.getPlayer()
			.isSteppingCarefully()) {
			BlockState placedOn = pContext.getLevel()
				.getBlockState(pContext.getClickedPos()
					.relative(pContext.getClickedFace()
						.getOpposite()));
			Axis preferredAxis = getVaultBlockAxis(placedOn);
			if (preferredAxis != null)
				return this.defaultBlockState()
					.setValue(HORIZONTAL_AXIS, preferredAxis);
		}
		return this.defaultBlockState()
			.setValue(HORIZONTAL_AXIS, pContext.getHorizontalDirection()
				.getAxis());
	}

	@Override
	public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
		if (pOldState.getBlock() == pState.getBlock())
			return;
		if (pIsMoving)
			return;
		withTileEntityDo(pLevel, pPos, ItemVaultTileEntity::updateConnectivity);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		if (context.getClickedFace()
			.getAxis()
			.isVertical()) {
			BlockEntity te = context.getLevel()
				.getBlockEntity(context.getClickedPos());
			if (te instanceof ItemVaultTileEntity) {
				ItemVaultTileEntity vault = (ItemVaultTileEntity) te;
				ConnectivityHandler.splitMulti(vault);
				vault.removeController(true);
			}
			state = state.setValue(LARGE, false);
		}
		InteractionResult onWrenched = IWrenchable.super.onWrenched(state, context);
		return onWrenched;
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean pIsMoving) {
		if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
			BlockEntity te = world.getBlockEntity(pos);
			if (!(te instanceof ItemVaultTileEntity))
				return;
			ItemVaultTileEntity vaultTE = (ItemVaultTileEntity) te;
			ItemHelper.dropContents(world, pos, vaultTE.inventory);
			world.removeBlockEntity(pos);
			ConnectivityHandler.splitMulti(vaultTE);
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn,
								 BlockHitResult hit){
		ItemStack heldItem = player.getItemInHand(handIn);

		boolean hasWater = EmptyingByBasin.emptyItem(world, heldItem, true)
				.getFirst()
				.getFluid()
				.isSame(Fluids.WATER);
		boolean isDye = heldItem.is(Tags.Items.DYES);
		if (isDye || hasWater) {
			if (!world.isClientSide)
				withTileEntityDo(world, pos, te -> te.applyColor(DyeColor.getColor(heldItem)));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public static boolean isVault(BlockState state) {
		return AllBlocks.ITEM_VAULT.has(state);
	}

	@Nullable
	public static Axis getVaultBlockAxis(BlockState state) {
		if (!isVault(state))
			return null;
		return state.getValue(HORIZONTAL_AXIS);
	}

	public static DyeColor getVaultBlockColor(BlockState state) {
		if (!isVault(state) || state.getValue(COLOR) == 16)
			return null;
		return DyeColor.byId(state.getValue(COLOR));
	}

	public static boolean isLarge(BlockState state) {
		if (!isVault(state))
			return false;
		return state.getValue(LARGE);
	}

	public static List<BlockPos> getVaultBlocks(Level world, BlockPos controllerPos) {
		List<BlockPos> blocks = new LinkedList<>();

		BlockState blockState = world.getBlockState(controllerPos);
		if (!AllBlocks.ITEM_VAULT.has(blockState))
			return blocks;

		BlockPos current = controllerPos;

		Axis axis = blockState.getValue(HORIZONTAL_AXIS);
		ItemVaultTileEntity vault = (ItemVaultTileEntity) world.getBlockEntity(current);
		int length = vault.getHeight();
		int radius = vault.getWidth();

		boolean alongZ = axis == Axis.Z;
		for (int yOffset = 0; yOffset < length; yOffset++) {
			for (int xOffset = 0; xOffset < radius; xOffset++) {
				for (int zOffset = 0; zOffset < radius; zOffset++) {
					current = alongZ ? controllerPos.offset(xOffset, zOffset, yOffset) : controllerPos.offset(yOffset, xOffset, zOffset);
					if (!AllBlocks.ITEM_VAULT.has(world.getBlockState(current)))
						break;
					blocks.add(current);
				}
			}
		}
		return blocks;
	}

		@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		Axis axis = state.getValue(HORIZONTAL_AXIS);
		return state.setValue(HORIZONTAL_AXIS, rot.rotate(Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE))
			.getAxis());
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state;
	}

	// Vaults are less noisy when placed in batch
	public static final SoundType SILENCED_METAL =
		new ForgeSoundType(0.1F, 1.5F, () -> SoundEvents.NETHERITE_BLOCK_BREAK, () -> SoundEvents.NETHERITE_BLOCK_STEP,
			() -> SoundEvents.NETHERITE_BLOCK_PLACE, () -> SoundEvents.NETHERITE_BLOCK_HIT,
			() -> SoundEvents.NETHERITE_BLOCK_FALL);

	@Override
	public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
		SoundType soundType = super.getSoundType(state, world, pos, entity);
		if (entity != null && entity.getPersistentData()
			.contains("SilenceVaultSound"))
			return SILENCED_METAL;
		return soundType;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState p_149740_1_) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
		return getTileEntityOptional(pLevel, pPos)
			.map(vte -> vte.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
			.map(lo -> lo.map(ItemHelper::calcRedstoneFromInventory)
				.orElse(0))
			.orElse(0);
	}


	@Override
	public BlockEntityType<? extends ItemVaultTileEntity> getTileEntityType() {
		return AllTileEntities.ITEM_VAULT.get();
	}

	@Override
	public Class<ItemVaultTileEntity> getTileEntityClass() {
		return ItemVaultTileEntity.class;
	}
}
