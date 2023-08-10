package com.simibubi.create.content.contraptions.minecart.capability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.minecart.CouplingHandler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.WorldAttached;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkEvent;

public class CapabilityMinecartController implements ICapabilitySerializable<CompoundTag> {

	/* Global map of loaded carts */

	public static WorldAttached<Map<UUID, MinecartController>> loadedMinecartsByUUID;
	public static WorldAttached<Set<UUID>> loadedMinecartsWithCoupling;
	static WorldAttached<List<AbstractMinecart>> queuedAdditions;
	static WorldAttached<List<UUID>> queuedUnloads;

	/**
	 * This callback wrapper ensures that the listeners map in the controller
	 * capability only ever contains one instance
	 */
	public static class MinecartRemovalListener implements NonNullConsumer<LazyOptional<MinecartController>> {

		private Level world;
		private AbstractMinecart cart;

		public MinecartRemovalListener(Level world, AbstractMinecart cart) {
			this.world = world;
			this.cart = cart;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof MinecartRemovalListener;
		}

		@Override
		public int hashCode() {
			return 100;
		}

		@Override
		public void accept(LazyOptional<MinecartController> t) {
			onCartRemoved(world, cart);
		}

	}

	static {
		loadedMinecartsByUUID = new WorldAttached<>($ -> new HashMap<>());
		loadedMinecartsWithCoupling = new WorldAttached<>($ -> new HashSet<>());
		queuedAdditions = new WorldAttached<>($ -> ObjectLists.synchronize(new ObjectArrayList<>()));
		queuedUnloads = new WorldAttached<>($ -> ObjectLists.synchronize(new ObjectArrayList<>()));
	}

	public static void tick(Level world) {
		List<UUID> toRemove = new ArrayList<>();
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
		List<AbstractMinecart> queued = queuedAdditions.get(world);
		List<UUID> queuedRemovals = queuedUnloads.get(world);
		Set<UUID> cartsWithCoupling = loadedMinecartsWithCoupling.get(world);
		Set<UUID> keySet = carts.keySet();

		keySet.removeAll(queuedRemovals);
		cartsWithCoupling.removeAll(queuedRemovals);

		for (AbstractMinecart cart : queued) {
			UUID uniqueID = cart.getUUID();

			if (world.isClientSide && carts.containsKey(uniqueID)) {
				MinecartController minecartController = carts.get(uniqueID);
				if (minecartController != null) {
					AbstractMinecart minecartEntity = minecartController.cart();
					if (minecartEntity != null && minecartEntity.getId() != cart.getId())
						continue; // Away with you, Fake Entities!
				}
			}

			cartsWithCoupling.remove(uniqueID);

			LazyOptional<MinecartController> capability = cart.getCapability(MINECART_CONTROLLER_CAPABILITY);
			MinecartController controller = capability.orElse(null);
			capability.addListener(new MinecartRemovalListener(world, cart));
			carts.put(uniqueID, controller);

			capability.ifPresent(mc -> {
				if (mc.isLeadingCoupling())
					cartsWithCoupling.add(uniqueID);
			});
			if (!world.isClientSide && controller != null)
				controller.sendData();
		}

		queuedRemovals.clear();
		queued.clear();

		for (Entry<UUID, MinecartController> entry : carts.entrySet()) {
			MinecartController controller = entry.getValue();
			if (controller != null) {
				if (controller.isPresent()) {
					controller.tick();
					continue;
				}
			}
			toRemove.add(entry.getKey());
		}

		cartsWithCoupling.removeAll(toRemove);
		keySet.removeAll(toRemove);
	}

	public static void onChunkUnloaded(ChunkEvent.Unload event) {
		ChunkPos chunkPos = event.getChunk()
			.getPos();
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(event.getWorld());
		for (MinecartController minecartController : carts.values()) {
			if (minecartController == null)
				continue;
			if (!minecartController.isPresent())
				continue;
			AbstractMinecart cart = minecartController.cart();
			if (cart.chunkPosition()
				.equals(chunkPos))
				queuedUnloads.get(event.getWorld())
					.add(cart.getUUID());
		}
	}

	protected static void onCartRemoved(Level world, AbstractMinecart entity) {
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
		List<UUID> unloads = queuedUnloads.get(world);
		UUID uniqueID = entity.getUUID();
		if (!carts.containsKey(uniqueID) || unloads.contains(uniqueID))
			return;
		if (world.isClientSide)
			return;
		handleKilledMinecart(world, carts.get(uniqueID), entity.position());
	}

	protected static void handleKilledMinecart(Level world, MinecartController controller, Vec3 removedPos) {
		if (controller == null)
			return;
		for (boolean forward : Iterate.trueAndFalse) {
			MinecartController next = CouplingHandler.getNextInCouplingChain(world, controller, forward);
			if (next == null || next == MinecartController.EMPTY)
				continue;

			next.removeConnection(!forward);
			if (controller.hasContraptionCoupling(forward))
				continue;
			AbstractMinecart cart = next.cart();
			if (cart == null)
				continue;

			Vec3 itemPos = cart.position()
				.add(removedPos)
				.scale(.5f);
			ItemEntity itemEntity =
				new ItemEntity(world, itemPos.x, itemPos.y, itemPos.z, AllItems.MINECART_COUPLING.asStack());
			itemEntity.setDefaultPickUpDelay();
			world.addFreshEntity(itemEntity);
		}
	}

	@Nullable
	public static MinecartController getIfPresent(Level world, UUID cartId) {
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
		if (carts == null)
			return null;
		if (!carts.containsKey(cartId))
			return null;
		return carts.get(cartId);
	}

	/* Capability management */

	public static Capability<MinecartController> MINECART_CONTROLLER_CAPABILITY =
		CapabilityManager.get(new CapabilityToken<>() {
		});

	public static void attach(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (!(entity instanceof AbstractMinecart))
			return;

		CapabilityMinecartController capability = new CapabilityMinecartController((AbstractMinecart) entity);
		ResourceLocation id = Create.asResource("minecart_controller");
		event.addCapability(id, capability);
		event.addListener(() -> {
			if (capability.cap.isPresent())
				capability.cap.invalidate();
		});
		queuedAdditions.get(entity.getCommandSenderWorld())
			.add((AbstractMinecart) entity);
	}

	public static void startTracking(PlayerEvent.StartTracking event) {
		Entity entity = event.getTarget();
		if (!(entity instanceof AbstractMinecart))
			return;
		entity.getCapability(MINECART_CONTROLLER_CAPABILITY)
			.ifPresent(MinecartController::sendData);
	}

	/* Capability provider */

	private final LazyOptional<MinecartController> cap;
	private MinecartController handler;

	public CapabilityMinecartController(AbstractMinecart minecart) {
		handler = new MinecartController(minecart);
		cap = LazyOptional.of(() -> handler);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == MINECART_CONTROLLER_CAPABILITY)
			return this.cap.cast();
		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		return handler.serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		handler.deserializeNBT(nbt);
	}

}
