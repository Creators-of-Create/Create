package com.simibubi.create.content.contraptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllTags.AllMountedItemStorageTypeTags;
import com.simibubi.create.Create;
import com.simibubi.create.api.contraption.storage.SyncedMountedStorage;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageWrapper;
import com.simibubi.create.content.equipment.toolbox.ToolboxMountedStorage;
import com.simibubi.create.content.fluids.tank.storage.FluidTankMountedStorage;
import com.simibubi.create.content.fluids.tank.storage.creative.CreativeFluidTankMountedStorage;
import com.simibubi.create.content.logistics.crate.CreativeCrateMountedStorage;
import com.simibubi.create.content.logistics.depot.storage.DepotMountedStorage;
import com.simibubi.create.content.logistics.vault.ItemVaultMountedStorage;
import com.simibubi.create.impl.contraption.storage.FallbackMountedStorage;

import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

public class MountedStorageManager {
	// builders used during assembly, null afterward
	// ImmutableMap.Builder is not used because it will throw with duplicate keys, not override them
	private Map<BlockPos, MountedItemStorage> itemsBuilder;
	private Map<BlockPos, MountedFluidStorage> fluidsBuilder;
	private Map<BlockPos, SyncedMountedStorage> syncedItemsBuilder;
	private Map<BlockPos, SyncedMountedStorage> syncedFluidsBuilder;

	// built data structures after assembly, null before
	private ImmutableMap<BlockPos, MountedItemStorage> allItemStorages;
	// different from allItemStorages, does not contain internal ones
	protected MountedItemStorageWrapper items;
	@Nullable
	protected MountedItemStorageWrapper fuelItems;
	protected MountedFluidStorageWrapper fluids;

	private ImmutableMap<BlockPos, SyncedMountedStorage> syncedItems;
	private ImmutableMap<BlockPos, SyncedMountedStorage> syncedFluids;

	private List<IItemHandlerModifiable> externalHandlers;
	protected CombinedInvWrapper allItems;

	// ticks until storage can sync again
	private int syncCooldown;

	// client-side: not all storages are synced, this determines which interactions are valid
	private Set<BlockPos> interactablePositions;

	public MountedStorageManager() {
		this.reset();
	}

	public void initialize() {
		if (this.isInitialized()) {
			// originally this threw an exception to try to catch mistakes.
			// however, in the case where a Contraption is deserialized before its Entity, that would also throw,
			// since both the deserialization and the onEntityCreated callback initialize the storage.
			// this case occurs when placing a picked up minecart contraption.
			// the reverse case is fine since deserialization also resets the manager first.
			return;
		}

		this.allItemStorages = ImmutableMap.copyOf(this.itemsBuilder);

		this.items = new MountedItemStorageWrapper(subMap(this.allItemStorages, this::isExposed));

		this.allItems = this.items;
		this.itemsBuilder = null;

		ImmutableMap<BlockPos, MountedItemStorage> fuelMap = subMap(this.allItemStorages, this::canUseForFuel);
		this.fuelItems = fuelMap.isEmpty() ? null : new MountedItemStorageWrapper(fuelMap);

		ImmutableMap<BlockPos, MountedFluidStorage> fluids = ImmutableMap.copyOf(this.fluidsBuilder);
		this.fluids = new MountedFluidStorageWrapper(fluids);
		this.fluidsBuilder = null;

		this.syncedItems = ImmutableMap.copyOf(this.syncedItemsBuilder);
		this.syncedItemsBuilder = null;
		this.syncedFluids = ImmutableMap.copyOf(this.syncedFluidsBuilder);
		this.syncedFluidsBuilder = null;
	}

	private boolean isExposed(MountedItemStorage storage) {
		return !AllMountedItemStorageTypeTags.INTERNAL.matches(storage);
	}

	private boolean canUseForFuel(MountedItemStorage storage) {
		return this.isExposed(storage) && !AllMountedItemStorageTypeTags.FUEL_BLACKLIST.matches(storage);
	}

	private boolean isInitialized() {
		return this.itemsBuilder == null;
	}

	private void assertInitialized() {
		if (!this.isInitialized()) {
			throw new IllegalStateException("MountedStorageManager is uninitialized");
		}
	}

	protected void reset() {
		this.allItemStorages = null;
		this.items = null;
		this.fuelItems = null;
		this.fluids = null;
		this.externalHandlers = new ArrayList<>();
		this.allItems = null;
		this.itemsBuilder = new HashMap<>();
		this.fluidsBuilder = new HashMap<>();
		this.syncedItemsBuilder = new HashMap<>();
		this.syncedFluidsBuilder = new HashMap<>();
		// interactablePositions intentionally not reset
	}

	public void addBlock(Level level, BlockState state, BlockPos globalPos, BlockPos localPos, @Nullable BlockEntity be) {
		MountedItemStorageType<?> itemType = MountedItemStorageType.REGISTRY.get(state.getBlock());
		if (itemType != null) {
			MountedItemStorage storage = itemType.mount(level, state, globalPos, be);
			if (storage != null) {
				this.addStorage(storage, localPos);
			}
		}

		MountedFluidStorageType<?> fluidType = MountedFluidStorageType.REGISTRY.get(state.getBlock());
		if (fluidType != null) {
			MountedFluidStorage storage = fluidType.mount(level, state, globalPos, be);
			if (storage != null) {
				this.addStorage(storage, localPos);
			}
		}
	}

	public void unmount(Level level, StructureBlockInfo info, BlockPos globalPos, @Nullable BlockEntity be) {
		BlockPos localPos = info.pos();
		BlockState state = info.state();

		MountedItemStorage itemStorage = this.getAllItemStorages().get(localPos);
		if (itemStorage != null) {
			MountedItemStorageType<?> expectedType = MountedItemStorageType.REGISTRY.get(state.getBlock());
			if (itemStorage.type == expectedType) {
				itemStorage.unmount(level, state, globalPos, be);
			}
		}

		MountedFluidStorage fluidStorage = this.getFluids().storages.get(localPos);
		if (fluidStorage != null) {
			MountedFluidStorageType<?> expectedType = MountedFluidStorageType.REGISTRY.get(state.getBlock());
			if (fluidStorage.type == expectedType) {
				fluidStorage.unmount(level, state, globalPos, be);
			}
		}
	}

	public void tick(AbstractContraptionEntity entity) {
		if (this.syncCooldown > 0) {
			this.syncCooldown--;
			return;
		}

		Map<BlockPos, MountedItemStorage> items = new HashMap<>();
		Map<BlockPos, MountedFluidStorage> fluids = new HashMap<>();
		this.syncedItems.forEach((pos, storage) -> {
			if (storage.isDirty()) {
				items.put(pos, (MountedItemStorage) storage);
				storage.markClean();
			}
		});
		this.syncedFluids.forEach((pos, storage) -> {
			if (storage.isDirty()) {
				fluids.put(pos, (MountedFluidStorage) storage);
				storage.markClean();
			}
		});

		if (!items.isEmpty() || !fluids.isEmpty()) {
			MountedStorageSyncPacket packet = new MountedStorageSyncPacket(entity.getId(), items, fluids);
			CatnipServices.NETWORK.sendToClientsTrackingEntity(entity, packet);
			this.syncCooldown = 8;
		}
	}

	public void handleSync(MountedStorageSyncPacket packet, AbstractContraptionEntity entity) {
		// packet only contains changed storages, grab existing ones before resetting
		ImmutableMap<BlockPos, MountedItemStorage> items = this.getAllItemStorages();
		MountedFluidStorageWrapper fluids = this.getFluids();
		this.reset();

		// track freshly synced storages
		Map<SyncedMountedStorage, BlockPos> syncedStorages = new IdentityHashMap<>();

		try {
			// re-add existing ones
			this.itemsBuilder.putAll(items);
			this.fluidsBuilder.putAll(fluids.storages);
			// add newly synced ones, overriding existing ones if present
			packet.items().forEach((pos, storage) -> {
				this.itemsBuilder.put(pos, storage);
				syncedStorages.put((SyncedMountedStorage) storage, pos);
			});
			packet.fluids().forEach((pos, storage) -> {
				this.fluidsBuilder.put(pos, storage);
				syncedStorages.put((SyncedMountedStorage) storage, pos);
			});
		} catch (Throwable t) {
			// an exception will leave the manager in an invalid state
			Create.LOGGER.error("An error occurred while syncing a MountedStorageManager", t);
		}

		this.initialize();

		// call all afterSync methods
		Contraption contraption = entity.getContraption();
		syncedStorages.forEach((storage, pos) -> storage.afterSync(contraption, pos));
	}

	// contraption is provided on the client for initial afterSync storage callbacks
	public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket, @Nullable Contraption contraption) {
		this.reset();

		try {
			NBTHelper.iterateCompoundList(nbt.getList("items", Tag.TAG_COMPOUND), tag -> {
				BlockPos pos = NBTHelper.readBlockPos(tag, "pos");
				CompoundTag data = tag.getCompound("storage");
				// TODO - Use CatnipCodecUtils
				MountedItemStorage.CODEC.decode(NbtOps.INSTANCE, data)
					.resultOrPartial(err -> Create.LOGGER.error("Failed to deserialize mounted item storage: {}", err))
					.map(Pair::getFirst)
					.ifPresent(storage -> this.addStorage(storage, pos));
			});

			NBTHelper.iterateCompoundList(nbt.getList("fluids", Tag.TAG_COMPOUND), tag -> {
				BlockPos pos = NBTHelper.readBlockPos(tag, "pos");
				CompoundTag data = tag.getCompound("storage");
				// TODO - Use CatnipCodecUtils
				MountedFluidStorage.CODEC.decode(NbtOps.INSTANCE, data)
					.resultOrPartial(err -> Create.LOGGER.error("Failed to deserialize mounted fluid storage: {}", err))
					.map(Pair::getFirst)
					.ifPresent(storage -> this.addStorage(storage, pos));
			});

			this.readLegacy(registries, nbt);

			if (nbt.contains("interactable_positions")) {
				this.interactablePositions = new HashSet<>();
				NBTHelper.iterateCompoundList(nbt.getList("interactable_positions", Tag.TAG_COMPOUND), tag -> {
					BlockPos pos = new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
					this.interactablePositions.add(pos);
				});
			}
		} catch (Throwable t) {
			Create.LOGGER.error("Error deserializing mounted storage", t);
			// an exception will leave the manager in an invalid state, initialize must be called
		}

		this.initialize();

		// for client sync, run initial afterSync callbacks
		if (!clientPacket || contraption == null)
			return;

		this.getAllItemStorages().forEach((pos, storage) -> {
			if (storage instanceof SyncedMountedStorage synced) {
				synced.afterSync(contraption, pos);
			}
		});
		this.getFluids().storages.forEach((pos, storage) -> {
			if (storage instanceof SyncedMountedStorage synced) {
				synced.afterSync(contraption, pos);
			}
		});
	}

	public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
		ListTag items = new ListTag();
		this.getAllItemStorages().forEach((pos, storage) -> {
				if (!clientPacket || storage instanceof SyncedMountedStorage) {
					// TODO - Use CatnipCodecUtils
					MountedItemStorage.CODEC.encodeStart(NbtOps.INSTANCE, storage)
						.resultOrPartial(err -> Create.LOGGER.error("Failed to serialize mounted item storage: {}", err))
						.ifPresent(encoded -> {
							CompoundTag tag = new CompoundTag();
							tag.put("pos", NbtUtils.writeBlockPos(pos));
							tag.put("storage", encoded);
							items.add(tag);
						});
				}
			}
		);
		if (!items.isEmpty()) {
			nbt.put("items", items);
		}

		ListTag fluids = new ListTag();
		this.getFluids().storages.forEach((pos, storage) -> {
				if (!clientPacket || storage instanceof SyncedMountedStorage) {
					// TODO - Use CatnipCodecUtils
					MountedFluidStorage.CODEC.encodeStart(NbtOps.INSTANCE, storage)
						.resultOrPartial(err -> Create.LOGGER.error("Failed to serialize mounted fluid storage: {}", err))
						.ifPresent(encoded -> {
							CompoundTag tag = new CompoundTag();
							tag.put("pos", NbtUtils.writeBlockPos(pos));
							tag.put("storage", encoded);
							fluids.add(tag);
						});
				}
			}
		);
		if (!fluids.isEmpty()) {
			nbt.put("fluids", fluids);
		}

		if (clientPacket) {
			// let the client know of all non-synced ones too
			SetView<BlockPos> positions = Sets.union(this.getAllItemStorages().keySet(), this.getFluids().storages.keySet());
			ListTag list = new ListTag();
			for (BlockPos pos : positions) {
				CompoundTag tag = new CompoundTag();
				tag.putInt("X", pos.getX());
				tag.putInt("Y", pos.getY());
				tag.putInt("Z", pos.getZ());

				list.add(tag);
			}
			nbt.put("interactable_positions", list);
		}
	}

	public void attachExternal(IItemHandlerModifiable externalStorage) {
		this.externalHandlers.add(externalStorage);
		IItemHandlerModifiable[] all = new IItemHandlerModifiable[this.externalHandlers.size() + 1];
		all[0] = this.items;
		for (int i = 0; i < this.externalHandlers.size(); i++) {
			all[i + 1] = this.externalHandlers.get(i);
		}

		this.allItems = new CombinedInvWrapper(all);
	}

	/**
	 * The primary way to access a contraption's inventory. Includes all
	 * non-internal mounted storages as well as all external storage.
	 */
	public CombinedInvWrapper getAllItems() {
		this.assertInitialized();
		return this.allItems;
	}

	/**
	 * Gets a map of all MountedItemStorages in the contraption, irrelevant of them being internal or providing fuel.
	 */
	public ImmutableMap<BlockPos, MountedItemStorage> getAllItemStorages() {
		this.assertInitialized();
		return this.allItemStorages;
	}

	/**
	 * Gets an item handler wrapping all non-internal mounted storages. This is not
	 * the whole contraption inventory as it does not include external storages.
	 * Most often, you want {@link #getAllItems()}, which does.
	 */
	public MountedItemStorageWrapper getMountedItems() {
		this.assertInitialized();
		return this.items;
	}

	/**
	 * Gets an item handler wrapping all non-internal mounted storages that provide fuel.
	 * May be null if none are present.
	 */
	@Nullable
	public MountedItemStorageWrapper getFuelItems() {
		this.assertInitialized();
		return this.fuelItems;
	}

	/**
	 * Gets a fluid handler wrapping all mounted fluid storages.
	 */
	public MountedFluidStorageWrapper getFluids() {
		this.assertInitialized();
		return this.fluids;
	}

	public boolean handlePlayerStorageInteraction(Contraption contraption, Player player, BlockPos localPos) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return this.interactablePositions != null && this.interactablePositions.contains(localPos);
		}

		StructureBlockInfo info = contraption.getBlocks().get(localPos);
		if (info == null)
			return false;

		MountedStorageManager storageManager = contraption.getStorage();
		MountedItemStorage storage = storageManager.getAllItemStorages().get(localPos);

		if (storage != null) {
			return storage.handleInteraction(serverPlayer, contraption, info);
		} else {
			return false;
		}
	}

	private void readLegacy(HolderLookup.Provider registries, CompoundTag nbt) {
		NBTHelper.iterateCompoundList(nbt.getList("Storage", Tag.TAG_COMPOUND), tag -> {
			BlockPos pos = NBTHelper.readBlockPos(tag, "Pos");
			CompoundTag data = tag.getCompound("Data");

			if (data.contains("Toolbox")) {
				this.addStorage(ToolboxMountedStorage.fromLegacy(registries, data), pos);
			} else if (data.contains("NoFuel")) {
				this.addStorage(ItemVaultMountedStorage.fromLegacy(registries, data), pos);
			} else if (data.contains("Bottomless")) {
				ItemStack supplied = ItemStack.parseOptional(registries, data.getCompound("ProvidedStack"));
				this.addStorage(new CreativeCrateMountedStorage(supplied), pos);
			} else if (data.contains("Synced")) {
				this.addStorage(DepotMountedStorage.fromLegacy(registries, data), pos);
			} else {
				// we can create a fallback storage safely, it will be validated before unmounting
				ItemStackHandler handler = new ItemStackHandler();
				handler.deserializeNBT(registries, data);
				this.addStorage(new FallbackMountedStorage(handler), pos);
			}
		});

		NBTHelper.iterateCompoundList(nbt.getList("FluidStorage", Tag.TAG_COMPOUND), tag -> {
			BlockPos pos = NBTHelper.readBlockPos(tag, "Pos");
			CompoundTag data = tag.getCompound("Data");

			if (data.contains("Bottomless")) {
				this.addStorage(CreativeFluidTankMountedStorage.fromLegacy(registries, data), pos);
			} else {
				this.addStorage(FluidTankMountedStorage.fromLegacy(registries, data), pos);
			}
		});
	}

	private void addStorage(MountedItemStorage storage, BlockPos pos) {
		this.itemsBuilder.put(pos, storage);
		if (storage instanceof SyncedMountedStorage synced)
			this.syncedItemsBuilder.put(pos, synced);
	}

	private void addStorage(MountedFluidStorage storage, BlockPos pos) {
		this.fluidsBuilder.put(pos, storage);
		if (storage instanceof SyncedMountedStorage synced)
			this.syncedFluidsBuilder.put(pos, synced);
	}

	private static <K, V> ImmutableMap<K, V> subMap(Map<K, V> map, Predicate<V> predicate) {
		ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
		map.forEach((key, value) -> {
			if (predicate.test(value)) {
				builder.put(key, value);
			}
		});
		return builder.build();
	}
}
