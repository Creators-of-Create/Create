package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AdjustablePulleyTileEntity extends KineticTileEntity {

	int signal;
	boolean signalChanged;

	public AdjustablePulleyTileEntity(BlockPos pos, BlockState state, BlockEntityType<? extends AdjustablePulleyTileEntity> type) {
		super(type, pos, state);
		signal = 0;
		setLazyTickRate(40);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("Signal", signal);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		signal = compound.getInt("Signal");
		super.fromTag(state, compound, clientPacket);
	}

	public float getModifier() {
		return getModifierForSignal(signal);
	}

	public void neighborChanged() {
		if (!hasLevel())
			return;
		int power = level.getBestNeighborSignal(worldPosition);
		if (power != signal)
			signalChanged = true;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		neighborChanged();
	}

	@Override
	public void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		super.tick(level, pos, state, blockEntity);
		if (level.isClientSide)
			return;
		if (signalChanged) {
			signalChanged = false;
			analogSignalChanged(level.getBestNeighborSignal(worldPosition));
		}
	}

	protected void analogSignalChanged(int newSignal) {
		detachKinetics();
		removeSource();
		signal = newSignal;
		attachKinetics();
	}

	protected float getModifierForSignal(int newPower) {
		if (newPower == 0)
			return 1;
		return 1 + ((newPower + 1) / 16f);
	}

}
