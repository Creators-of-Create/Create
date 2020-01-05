package com.simibubi.create.foundation.behaviour.base;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;

public abstract class TileEntityBehaviour {

	public SmartTileEntity tileEntity;
	private boolean paused;

	public TileEntityBehaviour(SmartTileEntity te) {
		tileEntity = te;
		paused = false;
	}

	public abstract IBehaviourType<?> getType();

	public void initialize() {

	}

	public void tick() {

	}

	public void readNBT(CompoundNBT nbt) {

	}

	public void updateClient(CompoundNBT nbt) {

	}

	public void writeNBT(CompoundNBT nbt) {

	}

	public void onBlockChanged(BlockState oldState) {

	}

	public void onNeighborChanged(Direction direction) {

	}

	public void remove() {

	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public BlockPos getPos() {
		return tileEntity.getPos();
	}
	
	public World getWorld() {
		return tileEntity.getWorld();
	}

	public static <T extends TileEntityBehaviour> T get(IEnviromentBlockReader reader, BlockPos pos,
			IBehaviourType<T> type) {
		return get(reader.getTileEntity(pos), type);
	}

	public static <T extends TileEntityBehaviour> T get(TileEntity te, IBehaviourType<T> type) {
		if (te == null)
			return null;
		if (!(te instanceof SmartTileEntity))
			return null;
		SmartTileEntity ste = (SmartTileEntity) te;
		return ste.getBehaviour(type);
	}

}
