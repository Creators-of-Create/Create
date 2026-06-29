package com.simibubi.create.content.equipment.clipboard;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;

import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.FakePlayer;

public class ClipboardBlock extends FaceAttachedHorizontalDirectionalBlock
	implements IBE<ClipboardBlockEntity>, IWrenchable, ProperWaterloggedBlock {

	public static final BooleanProperty WRITTEN = BooleanProperty.create("written");

	public static final MapCodec<ClipboardBlock> CODEC = simpleCodec(ClipboardBlock::new);

	public ClipboardBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(WRITTEN, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(WRITTEN, FACE, FACING, WATERLOGGED));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockState stateForPlacement = super.getStateForPlacement(pContext);
		if (stateForPlacement == null)
			return null;
		if (stateForPlacement.getValue(FACE) != AttachFace.WALL)
			stateForPlacement = stateForPlacement.setValue(FACING, stateForPlacement.getValue(FACING)
				.getOpposite());
		return withWater(stateForPlacement, pContext).setValue(WRITTEN, !pContext.getItemInHand().isComponentsPatchEmpty());
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return (switch (pState.getValue(FACE)) {
		case FLOOR -> AllShapes.CLIPBOARD_FLOOR;
		case CEILING -> AllShapes.CLIPBOARD_CEILING;
		default -> AllShapes.CLIPBOARD_WALL;
		}).get(pState.getValue(FACING));
	}

	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		return !pLevel.getBlockState(pPos.relative(getConnectedDirection(pState).getOpposite()))
			.canBeReplaced();
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (player.isShiftKeyDown()) {
			breakAndCollect(state, level, pos, player);
			return InteractionResult.SUCCESS;
		}

		return onBlockEntityUse(level, pos, cbe -> {
			if (level.isClientSide())
				CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> openScreen(player, cbe.components(), pos));
			return InteractionResult.SUCCESS;
		});
	}

	@OnlyIn(Dist.CLIENT)
	private void openScreen(Player player, DataComponentMap components, BlockPos pos) {
		if (Minecraft.getInstance().player == player)
			ScreenOpener.open(new ClipboardScreen(player.getInventory().selected, components, pos));
	}

	@Override
	public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
		breakAndCollect(pState, pLevel, pPos, pPlayer);
	}

	private void breakAndCollect(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
		if (pPlayer instanceof FakePlayer)
			return;
		if (pLevel.isClientSide)
			return;
		ItemStack cloneItemStack = getCloneItemStack(pLevel, pPos, pState);
		pLevel.destroyBlock(pPos, false);
		if (pLevel.getBlockState(pPos) != pState) {
			Inventory inv = pPlayer.getInventory();
			ItemStack selected = inv.getSelected();
			if (selected.isEmpty()) {
				inv.setItem(inv.selected, cloneItemStack);
			} else {
				inv.placeItemBackInInventory(cloneItemStack);
			}
		}
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
		return applyComponentsToDropStack(new ItemStack(this), level.getBlockEntity(pos));
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!(level.getBlockEntity(pos) instanceof ClipboardBlockEntity cbe))
			return state;
		if (level.isClientSide || player.isCreative())
			return state;
		Block.popResource(level, pos, applyComponentsToDropStack(new ItemStack(this), cbe));

		return state;
	}

	@Override
	public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pBuilder) {
		if (!(pBuilder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ClipboardBlockEntity cbe))
			return super.getDrops(pState, pBuilder);
		ItemStack drop = applyComponentsToDropStack(new ItemStack(this), cbe);
		pBuilder.withDynamicDrop(ShulkerBoxBlock.CONTENTS, c -> c.accept(drop.copy()));
		return ImmutableList.of(drop);
	}

	@SuppressWarnings("deprecation")
	private ItemStack applyComponentsToDropStack(ItemStack stack, BlockEntity blockEntity) {
		if (blockEntity instanceof ClipboardBlockEntity cbe) {
			stack.applyComponents(cbe.components());
			return stack;
		}
		return stack;
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel,
		BlockPos pCurrentPos, BlockPos pFacingPos) {
		updateWater(pLevel, pState, pCurrentPos);
		return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	public Class<ClipboardBlockEntity> getBlockEntityClass() {
		return ClipboardBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ClipboardBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.CLIPBOARD.get();
	}

	@Override
	protected @NotNull MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
		return CODEC;
	}
}
