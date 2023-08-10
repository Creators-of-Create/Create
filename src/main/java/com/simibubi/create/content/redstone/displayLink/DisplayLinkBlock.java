package com.simibubi.create.content.redstone.displayLink;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.redstone.displayLink.source.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.RedstonePowerDisplaySource;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.utility.Iterate;
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

public class DisplayLinkBlock extends WrenchableDirectionalBlock implements IBE<DisplayLinkBlockEntity> {

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
		forEachAttachedGatherer(level, pos, DisplayLinkBlockEntity::updateGatheredData);
	}

	@SuppressWarnings("unchecked")
	public static <T extends DisplaySource> void sendToGatherers(LevelAccessor level, BlockPos pos,
		BiConsumer<DisplayLinkBlockEntity, T> callback, Class<T> type) {
		forEachAttachedGatherer(level, pos, dgte -> {
			if (type.isInstance(dgte.activeSource))
				callback.accept(dgte, (T) dgte.activeSource);
		});
	}

	private static void forEachAttachedGatherer(LevelAccessor level, BlockPos pos,
		Consumer<DisplayLinkBlockEntity> callback) {
		for (Direction d : Iterate.directions) {
			BlockPos offsetPos = pos.relative(d);
			BlockState blockState = level.getBlockState(offsetPos);
			if (!AllBlocks.DISPLAY_LINK.has(blockState))
				continue;

			BlockEntity blockEntity = level.getBlockEntity(offsetPos);
			if (!(blockEntity instanceof DisplayLinkBlockEntity dlbe))
				continue;
			if (dlbe.activeSource == null)
				continue;
			if (dlbe.getDirection() != d.getOpposite())
				continue;

			callback.accept(dlbe);
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
				withBlockEntityDo(worldIn, pos, DisplayLinkBlockEntity::onNoLongerPowered);
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
			() -> () -> withBlockEntityDo(pLevel, pPos, be -> this.displayScreen(be, pPlayer)));
		return InteractionResult.SUCCESS;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void displayScreen(DisplayLinkBlockEntity be, Player player) {
		if (!(player instanceof LocalPlayer))
			return;
		if (be.targetOffset.equals(BlockPos.ZERO)) {
			player.displayClientMessage(CreateLang.translateDirect("display_link.invalid"), true);
			return;
		}
		ScreenOpener.open(new DisplayLinkScreen(be));
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
	public Class<DisplayLinkBlockEntity> getBlockEntityClass() {
		return DisplayLinkBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends DisplayLinkBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.DISPLAY_LINK.get();
	}

}
