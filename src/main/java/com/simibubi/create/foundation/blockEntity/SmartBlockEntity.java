package com.simibubi.create.foundation.blockEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.api.event.BlockEntityBehaviourEvent;
import com.simibubi.create.api.schematic.nbt.PartialSafeNBT;
import com.simibubi.create.api.schematic.requirement.SpecialBlockEntityItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.IInteractionChecker;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.createmod.ponder.api.VirtualBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.common.NeoForge;

public abstract class SmartBlockEntity extends CachedRenderBBBlockEntity
	implements PartialSafeNBT, IInteractionChecker, SpecialBlockEntityItemRequirement, VirtualBlockEntity {

	private final Map<BehaviourType<?>, BlockEntityBehaviour> behaviours = new Reference2ObjectArrayMap<>();
	private boolean initialized = false;
	private boolean firstNbtRead = true;
	protected int lazyTickRate;
	protected int lazyTickCounter;
	private boolean chunkUnloaded;

	// Used for simulating this BE in a client-only setting
	private boolean virtualMode;

	public SmartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);

		setLazyTickRate(10);

		ArrayList<BlockEntityBehaviour> list = new ArrayList<>();
		addBehaviours(list);
		list.forEach(b -> behaviours.put(b.getType(), b));
	}

	public abstract void addBehaviours(List<BlockEntityBehaviour> behaviours);

	/**
	 * Gets called just before reading block entity data for behaviours. Register
	 * anything here that depends on your custom BE data.
	 */
	public void addBehavioursDeferred(List<BlockEntityBehaviour> behaviours) {}

	public void initialize() {
		if (firstNbtRead) {
			firstNbtRead = false;
			NeoForge.EVENT_BUS.post(new BlockEntityBehaviourEvent(this, behaviours));
		}

		forEachBehaviour(BlockEntityBehaviour::initialize);
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

		forEachBehaviour(BlockEntityBehaviour::tick);
	}

	public void lazyTick() {}

	/**
	 * Hook only these in future subclasses of STE
	 */
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.saveAdditional(tag, registries);
		forEachBehaviour(tb -> tb.write(tag, registries, clientPacket));
	}

	@Override
	public void writeSafe(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		forEachBehaviour(tb -> {
			if (tb.isSafeNBT())
				tb.writeSafe(tag, registries);
		});
	}

	/**
	 * Hook only these in future subclasses of STE
	 */
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		if (firstNbtRead) {
			firstNbtRead = false;
			ArrayList<BlockEntityBehaviour> list = new ArrayList<>();
			addBehavioursDeferred(list);
			list.forEach(b -> behaviours.put(b.getType(), b));
			NeoForge.EVENT_BUS.post(new BlockEntityBehaviourEvent(this, behaviours));
		}
		super.loadAdditional(tag, registries);
		forEachBehaviour(tb -> tb.read(tag, registries, clientPacket));
	}

	@Override
	protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
		read(tag, registries, false);
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		chunkUnloaded = true;
	}

	@Override
	public final void setRemoved() {
		super.setRemoved();
		if (!chunkUnloaded)
			remove();
		invalidate();
	}

	/**
	 * Block destroyed or Chunk unloaded. Usually invalidates capabilities
	 */
	public void invalidate() {
		forEachBehaviour(BlockEntityBehaviour::unload);
	}

	/**
	 * Block destroyed or picked up by a contraption. Usually detaches kinetics
	 */
	public void remove() {}

	/**
	 * Block destroyed or replaced. Requires Block to call IBE::onRemove
	 */
	public void destroy() {
		forEachBehaviour(BlockEntityBehaviour::destroy);
	}

	@Override
	public final void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		write(tag, registries, false);
	}

	@Override
	public final void readClient(CompoundTag tag, HolderLookup.Provider registries) {
		read(tag, registries, true);
	}

	@Override
	public final CompoundTag writeClient(CompoundTag tag, HolderLookup.Provider registries) {
		write(tag, registries, true);
		return tag;
	}

	@SuppressWarnings("unchecked")
	public <T extends BlockEntityBehaviour> T getBehaviour(BehaviourType<T> type) {
		return (T) behaviours.get(type);
	}

	public void forEachBehaviour(Consumer<BlockEntityBehaviour> action) {
		getAllBehaviours().forEach(action);
	}

	public Collection<BlockEntityBehaviour> getAllBehaviours() {
		return behaviours.values();
	}

	public void attachBehaviourLate(BlockEntityBehaviour behaviour) {
		behaviours.put(behaviour.getType(), behaviour);
		behaviour.blockEntity = this;
		behaviour.initialize();
	}

	public ItemRequirement getRequiredItems(BlockState state) {
		return getAllBehaviours().stream()
			.reduce(ItemRequirement.NONE, (r, b) -> r.union(b.getRequiredItems()), ItemRequirement::union);
	}

	public void removeBehaviour(BehaviourType<?> type) {
		BlockEntityBehaviour remove = behaviours.remove(type);
		if (remove != null) {
			remove.unload();
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

	public boolean isChunkUnloaded() {
		return chunkUnloaded;
	}

	@Override
	public boolean canPlayerUse(Player player) {
		if (level == null || level.getBlockEntity(worldPosition) != this)
			return false;
		return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
			worldPosition.getZ() + 0.5D) <= 64.0D;
	}

	public void sendToMenu(RegistryFriendlyByteBuf buffer) {
		buffer.writeBlockPos(getBlockPos());
		buffer.writeNbt(getUpdateTag(buffer.registryAccess()));
	}

	@SuppressWarnings("deprecation")
	public void refreshBlockState() {
		setBlockState(getLevel().getBlockState(getBlockPos()));
	}

	public void registerAwardables(List<BlockEntityBehaviour> behaviours, CreateAdvancement... advancements) {
		for (BlockEntityBehaviour behaviour : behaviours) {
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
