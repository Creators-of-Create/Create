package com.simibubi.create.content.trains.signal;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.api.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;

public class SignalBlock extends Block implements IBE<SignalBlockEntity>, IWrenchable {

	public static final EnumProperty<SignalType> TYPE = EnumProperty.create("type", SignalType.class);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public enum SignalType implements StringRepresentable {
		ENTRY_SIGNAL, CROSS_SIGNAL;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

	public SignalBlock(Properties p_53182_) {
		super(p_53182_);
		registerDefaultState(defaultBlockState().setValue(TYPE, SignalType.ENTRY_SIGNAL)
			.setValue(POWERED, false));
	}

	@Override
	public Class<SignalBlockEntity> getBlockEntityClass() {
		return SignalBlockEntity.class;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(TYPE, POWERED));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return this.defaultBlockState()
			.setValue(POWERED, Boolean.valueOf(pContext.getLevel()
				.hasNeighborSignal(pContext.getClickedPos())));
	}

	@Override
	protected void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, Orientation orientation,
		boolean pIsMoving) {
		if (pLevel.isClientSide())
			return;
		boolean powered = pState.getValue(POWERED);
		Optional<SignalBlockEntity> ste = getBlockEntityOptional(pLevel, pPos);
		boolean neighborPowered = false;
		if (ste.isEmpty() || !ste.get().computerBehaviour.hasAttachedComputer()) {
			neighborPowered = pLevel.hasNeighborSignal(pPos);
		}
		if (powered == neighborPowered)
			return;
		if (powered) {
			pLevel.scheduleTick(pPos, this, 4);
		} else {
			pLevel.setBlock(pPos, pState.cycle(POWERED), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRand) {
		Optional<SignalBlockEntity> ste = getBlockEntityOptional(pLevel, pPos);
		if ((ste.isEmpty() || !ste.get().computerBehaviour.hasAttachedComputer()) && pState.getValue(POWERED) && !pLevel.hasNeighborSignal(pPos))
			pLevel.setBlock(pPos, pState.cycle(POWERED), Block.UPDATE_CLIENTS);
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel worldIn, BlockPos pos, boolean isMoving) {
		IBE.onRemove(state, worldIn, pos, Blocks.AIR.defaultBlockState());
		super.affectNeighborsAfterRemoval(state, worldIn, pos, isMoving);
	}

	@Override
	public BlockEntityType<? extends SignalBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.TRACK_SIGNAL.get();
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (level.isClientSide())
			return InteractionResult.SUCCESS;
		withBlockEntityDo(level, pos, ste -> {
			Player player = context.getPlayer();
			if (ste.computerBehaviour.hasAttachedComputer()) {
				if (player != null)
					player.sendOverlayMessage(CreateLang.translateDirect("track_signal.mode_controlled_by_computer"));
				return;
			}
			SignalBoundary signal = ste.getSignal();
			if (signal != null) {
				signal.cycleSignalType(pos);
				if (player != null)
					player.sendOverlayMessage(CreateLang.translateDirect("track_signal.mode_change." + signal.getTypeFor(pos)
						.getSerializedName()));
			} else if (player != null)
				player.sendOverlayMessage(CreateLang.translateDirect("track_signal.cannot_change_mode"));
		});
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState pState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState pState, Level blockAccess, BlockPos pPos, Direction direction) {
		return getBlockEntityOptional(blockAccess, pPos).filter(SignalBlockEntity::isPowered)
			.map($ -> 15)
			.orElse(0);
	}

}
