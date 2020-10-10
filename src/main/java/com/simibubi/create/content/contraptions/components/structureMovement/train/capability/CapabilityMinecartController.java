package com.simibubi.create.content.contraptions.components.structureMovement.train.capability;

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
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandler;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.WorldAttached;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;

public class CapabilityMinecartController implements ICapabilitySerializable<CompoundNBT> {

	/* Global map of loaded carts */

	public static WorldAttached<Map<UUID, MinecartController>> loadedMinecartsByUUID;
	public static WorldAttached<Set<UUID>> loadedMinecartsWithCoupling;
	static WorldAttached<List<AbstractMinecartEntity>> queuedAdditions;
	static WorldAttached<List<UUID>> queuedUnloads;

	static {
		loadedMinecartsByUUID = new WorldAttached<>(HashMap::new);
		loadedMinecartsWithCoupling = new WorldAttached<>(HashSet::new);
		queuedAdditions = new WorldAttached<>(() -> ObjectLists.synchronize(new ObjectArrayList<>()));
		queuedUnloads = new WorldAttached<>(() -> ObjectLists.synchronize(new ObjectArrayList<>()));
	}

	public static void tick(World world) {
		List<UUID> toRemove = new ArrayList<>();
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
		List<AbstractMinecartEntity> queued = queuedAdditions.get(world);
		List<UUID> queuedRemovals = queuedUnloads.get(world);
		Set<UUID> cartsWithCoupling = loadedMinecartsWithCoupling.get(world);
		Set<UUID> keySet = carts.keySet();

		keySet.removeAll(queuedRemovals);
		cartsWithCoupling.removeAll(queuedRemovals);

		for (AbstractMinecartEntity cart : queued) {
			UUID uniqueID = cart.getUniqueID();
			cartsWithCoupling.remove(uniqueID);
			LazyOptional<MinecartController> capability = cart.getCapability(MINECART_CONTROLLER_CAPABILITY);
			MinecartController controller = capability.orElse(null);
			capability.addListener(cap -> onCartRemoved(world, cart));
			carts.put(uniqueID, controller);
			capability.ifPresent(mc -> {
				if (mc.isLeadingCoupling())
					cartsWithCoupling.add(uniqueID);
			});
			if (!world.isRemote && controller != null)
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
			if (!minecartController.isPresent())
				continue;
			AbstractMinecartEntity cart = minecartController.cart();
			if (cart.chunkCoordX == chunkPos.x && cart.chunkCoordZ == chunkPos.z)
				queuedUnloads.get(event.getWorld())
					.add(cart.getUniqueID());
		}
	}

	protected static void onCartRemoved(World world, AbstractMinecartEntity entity) {
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
		List<UUID> unloads = queuedUnloads.get(world);
		UUID uniqueID = entity.getUniqueID();
		if (!carts.containsKey(uniqueID) || unloads.contains(uniqueID))
			return;
		if (world.isRemote)
			return;
		handleKilledMinecart(world, carts.get(uniqueID), entity.getPositionVec());
	}

	protected static void handleKilledMinecart(World world, MinecartController controller, Vec3d removedPos) {
		if (controller == null)
			return;
		for (boolean forward : Iterate.trueAndFalse) {
			MinecartController next = CouplingHandler.getNextInCouplingChain(world, controller, forward);
			if (next == null || next == MinecartController.EMPTY)
				continue;
			
			next.removeConnection(!forward);
			if (controller.hasContraptionCoupling(forward))
				continue;
			AbstractMinecartEntity cart = next.cart();
			if (cart == null)
				continue;
			
			Vec3d itemPos = cart.getPositionVec()
				.add(removedPos)
				.scale(.5f);
			ItemEntity itemEntity =
				new ItemEntity(world, itemPos.x, itemPos.y, itemPos.z, AllItems.MINECART_COUPLING.asStack());
			itemEntity.setDefaultPickupDelay();
			world.addEntity(itemEntity);
		}
	}

	@Nullable
	public static MinecartController getIfPresent(World world, UUID cartId) {
		Map<UUID, MinecartController> carts = loadedMinecartsByUUID.get(world);
		if (carts == null)
			return null;
		if (!carts.containsKey(cartId))
			return null;
		return carts.get(cartId);
	}

	/* Capability management */

	@CapabilityInject(MinecartController.class)
	public static Capability<MinecartController> MINECART_CONTROLLER_CAPABILITY = null;

	public static void attach(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (!(entity instanceof AbstractMinecartEntity))
			return;
		event.addCapability(Create.asResource("minecart_controller"),
			new CapabilityMinecartController((AbstractMinecartEntity) entity));
		queuedAdditions.get(entity.getEntityWorld())
			.add((AbstractMinecartEntity) entity);
	}

	public static void register() {
		CapabilityManager.INSTANCE.register(MinecartController.class, new Capability.IStorage<MinecartController>() {

			@Override
			public INBT writeNBT(Capability<MinecartController> capability, MinecartController instance,
				Direction side) {
				return instance.serializeNBT();
			}

			@Override
			public void readNBT(Capability<MinecartController> capability, MinecartController instance, Direction side,
				INBT base) {
				instance.deserializeNBT((CompoundNBT) base);
			}

		}, MinecartController::empty);
	}

	/* Capability provider */

	private final LazyOptional<MinecartController> cap;
	private MinecartController handler;

	public CapabilityMinecartController(AbstractMinecartEntity minecart) {
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
	public CompoundNBT serializeNBT() {
		return handler.serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		handler.deserializeNBT(nbt);
	}

}
