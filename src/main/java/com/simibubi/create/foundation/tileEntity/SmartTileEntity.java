package com.simibubi.create.foundation.tileEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.simibubi.create.api.event.TileEntityBehaviourEvent;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.foundation.gui.IInteractionChecker;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.IPartialSafeNBT;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public abstract class SmartTileEntity extends SyncedTileEntity implements TickableBlockEntity, IPartialSafeNBT, IInteractionChecker {

	private final Map<BehaviourType<?>, TileEntityBehaviour> behaviours;
	// Internally maintained to be identical to behaviorMap.values() in order to improve iteration performance.
	private final List<TileEntityBehaviour> behaviourList;
	private boolean initialized;
	private boolean firstNbtRead;
	private int lazyTickRate;
	private int lazyTickCounter;

	// Used for simulating this TE in a client-only setting
	private boolean virtualMode;

	public SmartTileEntity(BlockEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		behaviours = new HashMap<>();
		initialized = false;
		firstNbtRead = true;
		setLazyTickRate(10);

		ArrayList<TileEntityBehaviour> list = new ArrayList<>();
		addBehaviours(list);
		list.forEach(b -> behaviours.put(b.getType(), b));

		behaviourList = new ArrayList<>(list.size());
		updateBehaviorList();
	}

	public abstract void addBehaviours(List<TileEntityBehaviour> behaviours);

	/**
	 * Gets called just before reading tile data for behaviours. Register anything
	 * here that depends on your custom te data.
	 */
	public void addBehavioursDeferred(List<TileEntityBehaviour> behaviours) {}

	@Override
	public void tick() {
		if (!initialized && hasLevel()) {
			initialize();
			initialized = true;
		}

		if (lazyTickCounter-- <= 0) {
			lazyTickCounter = lazyTickRate;
			lazyTick();
		}

		behaviourList.forEach(TileEntityBehaviour::tick);
	}

	public void initialize() {
		if (firstNbtRead) {
			firstNbtRead = false;
			MinecraftForge.EVENT_BUS.post(new TileEntityBehaviourEvent<>(getBlockState(), this, behaviours));
			updateBehaviorList();
		}

		behaviourList.forEach(TileEntityBehaviour::initialize);
		lazyTick();
	}

	@Override
	public final CompoundTag save(CompoundTag compound) {
		write(compound, false);
		return compound;
	}

	@Override
	public final CompoundTag writeToClient(CompoundTag compound) {
		write(compound, true);
		return compound;
	}

	@Override
	public final void readClientUpdate(BlockState state, CompoundTag tag) {
		fromTag(state, tag, true);
	}

	@Override
	public final void load(BlockState state, CompoundTag tag) {
		fromTag(state, tag, false);
	}

	/**
	 * Hook only these in future subclasses of STE
	 */
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		if (firstNbtRead) {
			firstNbtRead = false;
			ArrayList<TileEntityBehaviour> list = new ArrayList<>();
			addBehavioursDeferred(list);
			list.forEach(b -> behaviours.put(b.getType(), b));
			MinecraftForge.EVENT_BUS.post(new TileEntityBehaviourEvent<>(state, this, behaviours));
			updateBehaviorList();
		}
		super.load(state, compound);
		behaviourList.forEach(tb -> tb.read(compound, clientPacket));
	}

	/**
	 * Hook only these in future subclasses of STE
	 */
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.save(compound);
		behaviourList.forEach(tb -> tb.write(compound, clientPacket));
	}

	@Override
	public void writeSafe(CompoundTag compound, boolean clientPacket) {
		super.save(compound);
		behaviourList.forEach(tb -> {
			if (tb.isSafeNBT())
				tb.write(compound, clientPacket);
		});
	}

	public ItemRequirement getRequiredItems() {
		return behaviourList.stream().reduce(
				ItemRequirement.NONE,
				(a, b) -> a.with(b.getRequiredItems()),
				(a, b) -> a.with(b)
		);
	}

	@Override
	public void setRemoved() {
		forEachBehaviour(TileEntityBehaviour::remove);
		super.setRemoved();
	}

	public void setLazyTickRate(int slowTickRate) {
		this.lazyTickRate = slowTickRate;
		this.lazyTickCounter = slowTickRate;
	}

	public void lazyTick() {

	}

	protected void forEachBehaviour(Consumer<TileEntityBehaviour> action) {
		behaviourList.forEach(action);
	}

	protected void attachBehaviourLate(TileEntityBehaviour behaviour) {
		behaviours.put(behaviour.getType(), behaviour);
		behaviour.initialize();

		updateBehaviorList();
	}

	protected void removeBehaviour(BehaviourType<?> type) {
		TileEntityBehaviour remove = behaviours.remove(type);
		if (remove != null) {
			remove.remove();
			updateBehaviorList();
		}
	}

	// We don't trust the input to the API will be sane, so we
	// update all the contents whenever something changes. It's
	// simpler than trying to manipulate the list one element at
	// a time.
	private void updateBehaviorList() {
		behaviourList.clear();
		behaviourList.addAll(behaviours.values());
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

	@Override
	public boolean canPlayerUse(Player player) {
		if (level == null || level.getBlockEntity(worldPosition) != this)
			return false;
		return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
			worldPosition.getZ() + 0.5D) <= 64.0D;
	}
	
	public void sendToContainer(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(getBlockPos());
		buffer.writeNbt(getUpdateTag());
	}

	public Level getWorld() {
		return getLevel();
	}
}
