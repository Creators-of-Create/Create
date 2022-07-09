package com.simibubi.create.content.logistics.block.display;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.logistics.block.display.source.DisplaySource;
import com.simibubi.create.content.logistics.block.display.source.RedstonePowerDisplaySource;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.player.LocalPlayer;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class DisplayLinkBlock extends WrenchableDirectionalBlock implements ITE<DisplayLinkTileEntity> {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public DisplayLinkBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		registerDefaultState(defaultBlockState().setValue(POWERED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState placed = super.getStateForPlacement(context);
		placed = placed.setValue(FACING, context.getClickedFace());
		return placed.setValue(POWERED, shouldBePowered(placed, context.getLevel(), context.getClickedPos()));
	}
	
	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
	}

	public static void notifyGatherers(LevelAccessor level, BlockPos pos) {
		forEachAttachedGatherer(level, pos, DisplayLinkTileEntity::updateGatheredData);
	}

	@SuppressWarnings("unchecked")
	public static <T extends DisplaySource> void sendToGatherers(LevelAccessor level, BlockPos pos,
		BiConsumer<DisplayLinkTileEntity, T> callback, Class<T> type) {
		forEachAttachedGatherer(level, pos, dgte -> {
			if (type.isInstance(dgte.activeSource))
				callback.accept(dgte, (T) dgte.activeSource);
		});
	}

	private static void forEachAttachedGatherer(LevelAccessor level, BlockPos pos,
		Consumer<DisplayLinkTileEntity> callback) {
		for (Direction d : Iterate.directions) {
			BlockPos offsetPos = pos.relative(d);
			BlockState blockState = level.getBlockState(offsetPos);
			if (!AllBlocks.DISPLAY_LINK.has(blockState))
				continue;

			BlockEntity blockEntity = level.getBlockEntity(offsetPos);
			if (!(blockEntity instanceof DisplayLinkTileEntity dgte))
				continue;
			if (dgte.activeSource == null)
				continue;
			if (dgte.getDirection() != d.getOpposite())
				continue;

			callback.accept(dgte);
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;

		if (fromPos.equals(pos.relative(state.getValue(FACING)
			.getOpposite())))
			sendToGatherers(worldIn, fromPos, (dlte, p) -> dlte.tickSource(), RedstonePowerDisplaySource.class);

		boolean powered = shouldBePowered(state, worldIn, pos);
		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered != powered) {
			worldIn.setBlock(pos, state.cycle(POWERED), 2);
			if (!powered)
				withTileEntityDo(worldIn, pos, DisplayLinkTileEntity::onNoLongerPowered);
		}
	}

	private boolean shouldBePowered(BlockState state, Level worldIn, BlockPos pos) {
		boolean powered = false;
		for (Direction d : Iterate.directions) {
			if (d.getOpposite() == state.getValue(FACING))
				continue;
			if (worldIn.getSignal(pos.relative(d), d) == 0)
				continue;
			powered = true;
			break;
		}
		return powered;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED));
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
		BlockHitResult pHit) {
		if (pPlayer == null)
			return InteractionResult.PASS;
		if (pPlayer.isSteppingCarefully())
			return InteractionResult.PASS;
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
			() -> () -> withTileEntityDo(pLevel, pPos, te -> this.displayScreen(te, pPlayer)));
		return InteractionResult.SUCCESS;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void displayScreen(DisplayLinkTileEntity te, Player player) {
		if (!(player instanceof LocalPlayer))
			return;
		if (te.targetOffset.equals(BlockPos.ZERO)) {
			player.displayClientMessage(Lang.translateDirect("display_link.invalid"), true);
			return;
		}
		ScreenOpener.open(new DisplayLinkScreen(te));
	}

	@Override
	public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.DATA_GATHERER.get(pState.getValue(FACING));
	}

	@Override
	public Class<DisplayLinkTileEntity> getTileEntityClass() {
		return DisplayLinkTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends DisplayLinkTileEntity> getTileEntityType() {
		return AllTileEntities.DISPLAY_LINK.get();
	}

}
