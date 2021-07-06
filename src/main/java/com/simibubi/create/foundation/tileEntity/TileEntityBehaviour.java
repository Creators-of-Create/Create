package com.simibubi.create.foundation.tileEntity;

import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public abstract class TileEntityBehaviour {

	public SmartTileEntity tileEntity;
	private int lazyTickRate;
	private int lazyTickCounter;

	public TileEntityBehaviour(SmartTileEntity te) {
		tileEntity = te;
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

	public void read(CompoundNBT nbt, boolean clientPacket) {

	}

	public void write(CompoundNBT nbt, boolean clientPacket) {

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

	public void remove() {

	}

	public void destroy() {

	}

	public void setLazyTickRate(int slowTickRate) {
		this.lazyTickRate = slowTickRate;
		this.lazyTickCounter = slowTickRate;
	}

	public void lazyTick() {

	}

	public BlockPos getPos() {
		return tileEntity.getPos();
	}

	public World getWorld() {
		return tileEntity.getWorld();
	}

	public static <T extends TileEntityBehaviour> T get(IBlockReader reader, BlockPos pos, BehaviourType<T> type) {
		return get(reader.getTileEntity(pos), type);
	}

	public static <T extends TileEntityBehaviour> void destroy(IBlockReader reader, BlockPos pos,
		BehaviourType<T> type) {
		T behaviour = get(reader.getTileEntity(pos), type);
		if (behaviour != null)
			behaviour.destroy();
	}

	public static <T extends TileEntityBehaviour> T get(TileEntity te, BehaviourType<T> type) {
		if (te == null)
			return null;
		if (!(te instanceof SmartTileEntity))
			return null;
		SmartTileEntity ste = (SmartTileEntity) te;
		return ste.getBehaviour(type);
	}
}
