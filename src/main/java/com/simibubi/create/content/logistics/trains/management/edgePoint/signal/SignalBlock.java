package com.simibubi.create.content.logistics.trains.management.edgePoint.signal;

import java.util.Random;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class SignalBlock extends Block implements ITE<SignalTileEntity>, IWrenchable {

	public static final EnumProperty<SignalType> TYPE = EnumProperty.create("type", SignalType.class);

	public enum SignalType implements StringRepresentable {
		ENTRY_SIGNAL, CROSS_SIGNAL;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

	public SignalBlock(Properties p_53182_) {
		super(p_53182_);
		registerDefaultState(defaultBlockState().setValue(TYPE, SignalType.ENTRY_SIGNAL));
	}

	@Override
	public Class<SignalTileEntity> getTileEntityClass() {
		return SignalTileEntity.class;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(TYPE));
	}

	@Override
	public BlockEntityType<? extends SignalTileEntity> getTileEntityType() {
		return AllTileEntities.TRACK_SIGNAL.get();
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (level.isClientSide)
			return InteractionResult.SUCCESS;
		withTileEntityDo(level, pos, ste -> {
			SignalBoundary signal = ste.getSignal();
			Player player = context.getPlayer();
			if (signal != null) {
				signal.cycleSignalType(pos);
				if (player != null)
					player.displayClientMessage(Lang.translate("track_signal.mode_change." + signal.getTypeFor(pos)
						.getSerializedName()), true);
			} else if (player != null)
				player.displayClientMessage(Lang.translate("track_signal.cannot_change_mode"), true);
		});
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		return side != null;
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	public void tick(BlockState blockState, ServerLevel world, BlockPos pos, Random random) {
		getTileEntityOptional(world, pos).ifPresent(SignalTileEntity::updatePowerAfterDelay);
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return getTileEntityOptional(blockAccess, pos).filter(SignalTileEntity::isPowered)
			.map($ -> 15)
			.orElse(0);
	}

}
