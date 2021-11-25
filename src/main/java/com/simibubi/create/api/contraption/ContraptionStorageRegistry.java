package com.simibubi.create.api.contraption;

import com.simibubi.create.Create;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class ContraptionStorageRegistry extends ForgeRegistryEntry<ContraptionStorageRegistry> {
	public static final ItemStackHandler dummyHandler = new ItemStackHandler();
	public static final DeferredRegister<ContraptionStorageRegistry> STORAGES = DeferredRegister.create(ContraptionStorageRegistry.class, Create.ID);
	public static final Supplier<IForgeRegistry<ContraptionStorageRegistry>> REGISTRY = STORAGES.makeRegistry("mountable_storage", RegistryBuilder::new);
	public static final String REGISTRY_NAME = "StorageRegistryId";
	private static Map<TileEntityType<?>, ContraptionStorageRegistry> tileEntityMappingsCache = null;

	public static void initCache() {
		if (tileEntityMappingsCache != null) return;
		tileEntityMappingsCache = new HashMap<>();
		ContraptionStorageRegistry other;
		for (ContraptionStorageRegistry registry : REGISTRY.get()) {
			for (TileEntityType<?> tileEntityType : registry.affectedStorages()) {
				if ((other = tileEntityMappingsCache.get(tileEntityType)) != null) {
					if (other.getPriority() == registry.getPriority() && other.getPriority() != Priority.DUMMY) {
						throw new RegistryConflictException(tileEntityType, other.getClass(), registry.getClass());
					} else if (!registry.getPriority().isOverwrite(other.getPriority()))
						continue;
				}

				tileEntityMappingsCache.put(tileEntityType, registry);
			}
		}
	}

	/**
	 * Returns registry entry that handles provided entity type, or null if no matching entry found
	 *
	 * @param type Type of tile entity
	 * @return matching registry entry, or null if nothing is found
	 */
	@Nullable
	public static ContraptionStorageRegistry forTileEntity(TileEntityType<?> type) {
		return tileEntityMappingsCache.get(type);
	}

	/**
	 * Helper method to conditionally register handlers. Registers value from {@code supplier} parameter if {@code condition} returns true, otherwise generates new {@link DummyHandler} and registers it
	 *
	 * @param registry     registry for entry registering
	 * @param condition    Loading condition supplier
	 * @param registryName Name to register the entry under
	 * @param supplier     Supplier to get the entry
	 */
	public static void registerConditionally(IForgeRegistry<ContraptionStorageRegistry> registry, Supplier<Boolean> condition, String registryName, Supplier<ContraptionStorageRegistry> supplier) {
		ContraptionStorageRegistry entry;
		if (condition.get()) {
			entry = supplier.get().setRegistryName(registryName);
		} else {
			entry = new DummyHandler().setRegistryName(registryName);
		}
		registry.register(entry);
	}

	/**
	 * Helper method to conditionally register handlers based on if specified mod is loaded. Registers value from {@code supplier} parameter if specified mod is loaded, otherwise generates new {@link DummyHandler} and registers it
	 *
	 * @param registry     registry for entry registering
	 * @param modid        Required mod ID
	 * @param registryName Name to register the entry under
	 * @param supplier     Supplier to get the entry
	 */
	public static void registerIfModLoaded(IForgeRegistry<ContraptionStorageRegistry> registry, String modid, String registryName, Supplier<ContraptionStorageRegistry> supplier) {
		registerConditionally(registry, () -> ModList.get().isLoaded(modid), registryName, supplier);
	}

	/**
	 * Helper method to unconditionally register handlers
	 *
	 * @param registry     registry for entry registering
	 * @param registryName Name to register the entry under
	 * @param supplier     Supplier to get the entry
	 */
	public static void register(IForgeRegistry<ContraptionStorageRegistry> registry, String registryName, Supplier<ContraptionStorageRegistry> supplier) {
		registerConditionally(registry, () -> true, registryName, supplier);
	}

	/**
	 * Helper method to get default item handler capability from tile entity
	 *
	 * @param te TileEntity to get handler from
	 * @return IItemHandler from {@link CapabilityItemHandler#ITEM_HANDLER_CAPABILITY} capability
	 */
	protected static IItemHandler getHandlerFromDefaultCapability(TileEntity te) {
		return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(dummyHandler);
	}

	/**
	 * Helper method for getting registry instance
	 *
	 * @param id registry name
	 * @return Lazy with given name
	 */
	public static Lazy<ContraptionStorageRegistry> getInstance(String id) {
		return Lazy.of(() -> REGISTRY.get().getValue(new ResourceLocation(id)));
	}

	/**
	 * Method for getting registry priority. For additional info see {@link Priority}
	 *
	 * @return registry priority
	 */
	public abstract Priority getPriority();

	/**
	 * @return array of Tile Entity types handled by this registry
	 */
	public abstract TileEntityType<?>[] affectedStorages();

	/**
	 * @param te Tile Entity
	 * @return true if given tile entity can be used as mounted storage
	 */
	public boolean canUseAsStorage(TileEntity te) {
		return true;
	}

	/**
	 * @param te original Tile Entity
	 * @return Item handler to be used in contraption or null if default logic should be used
	 */
	public ContraptionItemStackHandler createHandler(TileEntity te) {
		return null;
	}

	/**
	 * Returns {@link  ContraptionItemStackHandler} deserialized from NBT
	 *
	 * @param nbt serialized NBT
	 * @return deserialized handler
	 */
	public ContraptionItemStackHandler deserializeHandler(CompoundNBT nbt) {
		throw new NotImplementedException();
	}

	/**
	 * Helper method for deserializing handler from NBT
	 *
	 * @param handler handler to deserialize
	 * @param nbt     serialized NBT
	 * @return Deserialized handler
	 */
	protected final <T extends ContraptionItemStackHandler> T deserializeHandler(T handler, CompoundNBT nbt) {
		handler.deserializeNBT(nbt);
		return handler;
	}

	/**
	 * Provides world to the contraption upon deserialization
	 * @param world contraption world
	 */

	/**
	 * Registry priority enum
	 */
	public enum Priority {
		/**
		 * Dummy priority, use this in case if your registry is a dummy and should be overwritten by any better option
		 */
		DUMMY {
			@Override
			public boolean isOverwrite(Priority other) {
				return false;
			}
		},
		/**
		 * Add-on priority, use this if your registry is coming from an add-on to the external mod and should be overwritten if official support is added
		 */
		ADDON {
			@Override
			public boolean isOverwrite(Priority other) {
				return other == DUMMY;
			}
		},
		/**
		 * Native mod priority, use this if your registry is a part of the mod it's adding support to
		 */
		NATIVE {
			@Override
			public boolean isOverwrite(Priority other) {
				return other != NATIVE;
			}
		};

		public abstract boolean isOverwrite(Priority other);
	}

	public static class DummyHandler extends ContraptionStorageRegistry {
		@Override
		public boolean canUseAsStorage(TileEntity te) {
			return false;
		}

		@Override
		public Priority getPriority() {
			return Priority.DUMMY;
		}

		@Override
		public TileEntityType<?>[] affectedStorages() {
			return new TileEntityType[0];
		}
	}

	public static class RegistryConflictException extends RuntimeException {
		public RegistryConflictException(TileEntityType<?> teType, Class<? extends ContraptionStorageRegistry> a, Class<? extends ContraptionStorageRegistry> b) {
			super("Registry conflict: registries " + a.getName() + " and " + b.getName() + " tried to register the same tile entity " + teType.getRegistryName());
		}
	}
}
