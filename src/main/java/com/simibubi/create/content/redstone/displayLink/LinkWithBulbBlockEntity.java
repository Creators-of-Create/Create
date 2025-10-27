package com.simibubi.create.content.redstone.displayLink;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class LinkWithBulbBlockEntity extends SmartBlockEntity {

	private LerpedFloat glow;
	private boolean sendPulse;

	public LinkWithBulbBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		glow = LerpedFloat.linear()
			.startWithValue(0);
		glow.chase(0, 0.5f, Chaser.EXP);
	}

	@Override
	public void tick() {
		super.tick();
		if (isVirtual() || level.isClientSide())
			glow.tickChaser();
	}

	public float getGlow(float partialTicks) {
		return glow.getValue(partialTicks);
	}

	public void sendPulseNextSync() {
		sendPulse = true;
	}

	public void pulse() {
		glow.setValue(2);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		if (clientPacket && sendPulse) {
			sendPulse = false;
			NBTHelper.putMarker(tag, "Pulse");
		}
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		if (clientPacket && tag.contains("Pulse"))
			pulse();
	}

	public Vec3 getBulbOffset(BlockState state) {
		return Vec3.ZERO;
	}

	public Direction getBulbFacing(BlockState state) {
		return state.getValue(DisplayLinkBlock.FACING);
	}

}
