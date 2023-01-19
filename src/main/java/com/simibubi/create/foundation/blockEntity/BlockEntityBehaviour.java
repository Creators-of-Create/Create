package com.simibubi.create.foundation.blockEntity;

import java.util.ConcurrentModificationException;

import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockEntityBehaviour {

	public SmartBlockEntity blockEntity;
	private int lazyTickRate;
	private int lazyTickCounter;

	public BlockEntityBehaviour(SmartBlockEntity be) {
		blockEntity = be;
		setLazyTickRate(10);
	}

	public abstract BehaviourType<?> getType();

	public void initialize() {

	}

	public void tick() {
		if (lazyTickCounter-- <= 0) {
			lazyTickCounter = lazyTickRate;
			lazyTick();
		}

	}

	public void read(CompoundTag nbt, boolean clientPacket) {

	}

	public void write(CompoundTag nbt, boolean clientPacket) {

	}

	public boolean isSafeNBT() {
		return false;
	}

	public ItemRequirement getRequiredItems() {
		return ItemRequirement.NONE;
	}

	public void onBlockChanged(BlockState oldState) {

	}

	public void onNeighborChanged(BlockPos neighborPos) {

	}

	/**
	 * Block destroyed or Chunk unloaded. Usually invalidates capabilities
	 */
	public void unload() {}

	/**
	 * Block destroyed or removed. Requires block to call ITE::onRemove
	 */
	public void destroy() {}

	public void setLazyTickRate(int slowTickRate) {
		this.lazyTickRate = slowTickRate;
		this.lazyTickCounter = slowTickRate;
	}

	public void lazyTick() {

	}

	public BlockPos getPos() {
		return blockEntity.getBlockPos();
	}

	public Level getWorld() {
		return blockEntity.getLevel();
	}

	public static <T extends BlockEntityBehaviour> T get(BlockGetter reader, BlockPos pos, BehaviourType<T> type) {
		BlockEntity be;
		try {
			be = reader.getBlockEntity(pos);
		} catch (ConcurrentModificationException e) {
			be = null;
		}
		return get(be, type);
	}

	public static <T extends BlockEntityBehaviour> T get(BlockEntity be, BehaviourType<T> type) {
		if (be == null)
			return null;
		if (!(be instanceof SmartBlockEntity))
			return null;
		SmartBlockEntity ste = (SmartBlockEntity) be;
		return ste.getBehaviour(type);
	}
}
