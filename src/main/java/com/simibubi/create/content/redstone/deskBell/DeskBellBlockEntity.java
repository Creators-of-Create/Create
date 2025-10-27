package com.simibubi.create.content.redstone.deskBell;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DeskBellBlockEntity extends SmartBlockEntity {

	public LerpedFloat animation = LerpedFloat.linear()
		.startWithValue(0);

	public boolean ding;

	int blockStateTimer;
	float animationOffset;

	public DeskBellBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		blockStateTimer = 0;
	}

	@Override
	public void tick() {
		super.tick();
		animation.tickChaser();

		if (level.isClientSide)
			return;
		if (blockStateTimer == 0)
			return;

		blockStateTimer--;

		if (blockStateTimer > 0)
			return;
		BlockState blockState = getBlockState();
		if (blockState.getValue(DeskBellBlock.POWERED))
			AllBlocks.DESK_BELL.get()
				.unPress(blockState, level, worldPosition);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		if (clientPacket && ding)
			NBTHelper.putMarker(tag, "Ding");
		ding = false;
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		if (clientPacket && tag.contains("Ding"))
			ding();
	}

	public void ding() {
		if (!level.isClientSide) {
			blockStateTimer = 20;
			ding = true;
			sendData();
			return;
		}

		animationOffset = level.random.nextFloat() * 2 * Mth.PI;
		animation.startWithValue(1)
			.chase(0, 0.05, Chaser.LINEAR);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

}
