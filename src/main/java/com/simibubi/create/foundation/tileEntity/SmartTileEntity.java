package com.simibubi.create.foundation.tileEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.simibubi.create.api.event.TileEntityBehaviourEvent;
import com.simibubi.create.content.schematics.ISpecialBlockEntityItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.IInteractionChecker;
import com.simibubi.create.foundation.utility.IPartialSafeNBT;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public abstract class SmartTileEntity extends CachedRenderBBTileEntity implements IPartialSafeNBT, IInteractionChecker, ISpecialBlockEntityItemRequirement {

	private final Map<BehaviourType<?>, TileEntityBehaviour> behaviours = new HashMap<>();
	private boolean initialized = false;
	private boolean firstNbtRead = true;
	protected int lazyTickRate;
	protected int lazyTickCounter;

	// Used for simulating this TE in a client-only setting
	private boolean virtualMode;

	public SmartTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);

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

	public void initialize() {
		if (firstNbtRead) {
			firstNbtRead = false;
			MinecraftForge.EVENT_BUS.post(new TileEntityBehaviourEvent<>(this, behaviours));
		}

		forEachBehaviour(TileEntityBehaviour::initialize);
		lazyTick();
	}

	public void tick() {
		if (!initialized && hasLevel()) {
			initialize();
			initialized = true;
		}

		if (lazyTickCounter-- <= 0) {
			lazyTickCounter = lazyTickRate;
			lazyTick();
		}

		forEachBehaviour(TileEntityBehaviour::tick);
	}

	public void lazyTick() {}

	/**
	 * Hook only these in future subclasses of STE
	 */
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.saveAdditional(tag);
		forEachBehaviour(tb -> tb.write(tag, clientPacket));
	}

	@Override
	public void writeSafe(CompoundTag tag) {
		super.saveAdditional(tag);
		forEachBehaviour(tb -> {
			if (tb.isSafeNBT())
				tb.write(tag, false);
		});
	}

	/**
	 * Hook only these in future subclasses of STE
	 */
	protected void read(CompoundTag tag, boolean clientPacket) {
		if (firstNbtRead) {
			firstNbtRead = false;
			ArrayList<TileEntityBehaviour> list = new ArrayList<>();
			addBehavioursDeferred(list);
			list.forEach(b -> behaviours.put(b.getType(), b));
			MinecraftForge.EVENT_BUS.post(new TileEntityBehaviourEvent<>(this, behaviours));
		}
		super.load(tag);
		forEachBehaviour(tb -> tb.read(tag, clientPacket));
	}

	@Override
	public final void load(CompoundTag tag) {
		read(tag, false);
	}

	/*
	 * TODO: Remove this hack once this issue is resolved:
	 * https://github.com/MinecraftForge/MinecraftForge/issues/8302 Once the PR
	 * linked in the issue is accepted, we should use the new method for determining
	 * whether setRemoved was called due to a chunk unload or not, and remove this
	 * volatile workaround
	 */
	private boolean unloaded;

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		unloaded = true;
	}

	protected void setRemovedNotDueToChunkUnload() {
		forEachBehaviour(TileEntityBehaviour::remove);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();

		if (!unloaded) {
			setRemovedNotDueToChunkUnload();
		}
	}

	@Override
	public final void saveAdditional(CompoundTag tag) {
		write(tag, false);
	}

	@Override
	public final void readClient(CompoundTag tag) {
		read(tag, true);
	}

	@Override
	public final CompoundTag writeClient(CompoundTag tag) {
		write(tag, true);
		return tag;
	}

	@SuppressWarnings("unchecked")
	public <T extends TileEntityBehaviour> T getBehaviour(BehaviourType<T> type) {
		return (T) behaviours.get(type);
	}

	protected void forEachBehaviour(Consumer<TileEntityBehaviour> action) {
		behaviours.values()
			.forEach(action);
	}

	protected void attachBehaviourLate(TileEntityBehaviour behaviour) {
		behaviours.put(behaviour.getType(), behaviour);
		behaviour.initialize();
	}

	public ItemRequirement getRequiredItems(BlockState state) {
		return behaviours.values()
			.stream()
			.reduce(ItemRequirement.NONE, (r, b) -> r.union(b.getRequiredItems()), (r, r1) -> r.union(r1));
	}

	protected void removeBehaviour(BehaviourType<?> type) {
		TileEntityBehaviour remove = behaviours.remove(type);
		if (remove != null) {
			remove.remove();
		}
	}

	public void setLazyTickRate(int slowTickRate) {
		this.lazyTickRate = slowTickRate;
		this.lazyTickCounter = slowTickRate;
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

	@SuppressWarnings("deprecation")
	public void refreshBlockState() {
		setBlockState(getLevel().getBlockState(getBlockPos()));
	}

	protected boolean isItemHandlerCap(Capability<?> cap) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	protected boolean isFluidHandlerCap(Capability<?> cap) {
		return cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	public void registerAwardables(List<TileEntityBehaviour> behaviours, CreateAdvancement... advancements) {
		for (TileEntityBehaviour behaviour : behaviours) {
			if (behaviour instanceof AdvancementBehaviour ab) {
				ab.add(advancements);
				return;
			}
		}
		behaviours.add(new AdvancementBehaviour(this, advancements));
	}

	public void award(CreateAdvancement advancement) {
		AdvancementBehaviour behaviour = getBehaviour(AdvancementBehaviour.TYPE);
		if (behaviour != null)
			behaviour.awardPlayer(advancement);
	}

	public void awardIfNear(CreateAdvancement advancement, int range) {
		AdvancementBehaviour behaviour = getBehaviour(AdvancementBehaviour.TYPE);
		if (behaviour != null)
			behaviour.awardPlayerIfNear(advancement, range);
	}

}
