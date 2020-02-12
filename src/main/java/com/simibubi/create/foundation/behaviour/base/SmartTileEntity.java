package com.simibubi.create.foundation.behaviour.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.simibubi.create.foundation.block.SyncedTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;

public abstract class SmartTileEntity extends SyncedTileEntity implements ITickableTileEntity {

	private Map<IBehaviourType<?>, TileEntityBehaviour> behaviours;
	private boolean initialized;
	private boolean firstNbtRead;
	private int lazyTickRate;
	private int lazyTickCounter;

	public SmartTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		behaviours = new HashMap<>();
		initialized = false;
		firstNbtRead = true;
		setLazyTickRate(10);

		ArrayList<TileEntityBehaviour> list = new ArrayList<>();
		addBehaviours(list);
		list.forEach(b -> behaviours.put(b.getType(), b));
	}

	public abstract void addBehaviours(List<TileEntityBehaviour> behaviours);

	/**
	 * Gets called just before reading tile data for behaviours. Register anything
	 * here that depends on your custom te data.
	 */
	public void addBehavioursDeferred(List<TileEntityBehaviour> behaviours) {
	}

	@Override
	public void tick() {
		if (!initialized && hasWorld()) {
			initialize();
			initialized = true;
		}

		if (lazyTickCounter-- <= 0) {
			lazyTickCounter = lazyTickRate;
			lazyTick();
		}

		behaviours.values().forEach(TileEntityBehaviour::tick);
	}

	public void initialize() {
		behaviours.values().forEach(TileEntityBehaviour::initialize);
		lazyTick();
	}

	public void updateClient(CompoundNBT compound) {
		behaviours.values().forEach(tb -> tb.updateClient(compound));
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		behaviours.values().forEach(tb -> tb.writeNBT(compound));
		return super.write(compound);
	}
	
	@Override
	public CompoundNBT writeToClient(CompoundNBT compound) {
		behaviours.values().forEach(tb -> tb.writeToClient(compound));
		return super.writeToClient(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		if (firstNbtRead) {
			firstNbtRead = false;
			ArrayList<TileEntityBehaviour> list = new ArrayList<>();
			addBehavioursDeferred(list);
			list.forEach(b -> behaviours.put(b.getType(), b));
		}

		forEachBehaviour(tb -> tb.readNBT(compound));
		super.read(compound);
		if (world != null && world.isRemote)
			updateClient(compound);
	}

	@Override
	public void remove() {
		forEachBehaviour(TileEntityBehaviour::remove);
		super.remove();
	}

	public void setLazyTickRate(int slowTickRate) {
		this.lazyTickRate = slowTickRate;
		this.lazyTickCounter = slowTickRate;
	}

	public void lazyTick() {

	}

	protected void forEachBehaviour(Consumer<TileEntityBehaviour> action) {
		behaviours.values().forEach(tb -> {
			if (!tb.isPaused())
				action.accept(tb);
		});
	}

	protected void putBehaviour(TileEntityBehaviour behaviour) {
		behaviours.put(behaviour.getType(), behaviour);
		behaviour.initialize();
	}

	protected void removeBehaviour(IBehaviourType<?> type) {
		TileEntityBehaviour remove = behaviours.remove(type);
		if (remove != null)
			remove.remove();
	}

	@SuppressWarnings("unchecked")
	protected <T extends TileEntityBehaviour> T getBehaviour(IBehaviourType<T> type) {
		if (behaviours.containsKey(type))
			return (T) behaviours.get(type);
		return null;
	}

}
