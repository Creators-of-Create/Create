package com.simibubi.create.foundation.tileEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public abstract class SmartTileEntity extends SyncedTileEntity implements ITickableTileEntity {

	private Map<BehaviourType<?>, TileEntityBehaviour> behaviours;
	private boolean initialized;
	private boolean firstNbtRead;
	private int lazyTickRate;
	private int lazyTickCounter;

	// Used for simulating this TE in a client-only setting
	private boolean virtualMode;

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
	public void addBehavioursDeferred(List<TileEntityBehaviour> behaviours) {}

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

		behaviours.values()
			.forEach(TileEntityBehaviour::tick);
	}

	public void initialize() {
		behaviours.values()
			.forEach(TileEntityBehaviour::initialize);
		lazyTick();
	}

	@Override
	public final CompoundNBT write(CompoundNBT compound) {
		write(compound, false);
		return compound;
	}

	@Override
	public final CompoundNBT writeToClient(CompoundNBT compound) {
		write(compound, true);
		return compound;
	}

	@Override
	public final void readClientUpdate(BlockState state, CompoundNBT tag) {
		fromTag(state, tag, true);
	}

	@Override
	public final void fromTag(BlockState state, CompoundNBT tag) {
		fromTag(state, tag, false);
	}

	/**
	 * Hook only these in future subclasses of STE
	 */
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		if (firstNbtRead) {
			firstNbtRead = false;
			ArrayList<TileEntityBehaviour> list = new ArrayList<>();
			addBehavioursDeferred(list);
			list.forEach(b -> behaviours.put(b.getType(), b));
		}
		super.fromTag(state, compound);
		behaviours.values()
			.forEach(tb -> tb.read(compound, clientPacket));
	}

	/**
	 * Hook only these in future subclasses of STE
	 */
	protected void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound);
		behaviours.values()
			.forEach(tb -> tb.write(compound, clientPacket));
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
		behaviours.values()
			.forEach(action);
	}

	protected void attachBehaviourLate(TileEntityBehaviour behaviour) {
		behaviours.put(behaviour.getType(), behaviour);
		behaviour.initialize();
	}

	protected void removeBehaviour(BehaviourType<?> type) {
		TileEntityBehaviour remove = behaviours.remove(type);
		if (remove != null)
			remove.remove();
	}

	@SuppressWarnings("unchecked")
	public <T extends TileEntityBehaviour> T getBehaviour(BehaviourType<T> type) {
		return (T) behaviours.get(type);
	}

	protected boolean isItemHandlerCap(Capability<?> cap) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	protected boolean isFluidHandlerCap(Capability<?> cap) {
		return cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}
	
	public void markVirtual() {
		virtualMode = true;
	}
	
	public boolean isVirtual() {
		return virtualMode;
	}

}
