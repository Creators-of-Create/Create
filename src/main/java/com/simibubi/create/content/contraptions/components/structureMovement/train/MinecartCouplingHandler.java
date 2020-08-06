package com.simibubi.create.content.contraptions.components.structureMovement.train;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.train.MinecartCouplingSerializer.CouplingData;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.WorldAttached;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

/*
 * 
 * Couplings are a directional connection of two Minecart entities
 * - ID and Key is the UUID of the main cart
 * - They are immediately written to both minecarts' nbt tags upon creation.
 * {Main: true, Id: {L: ", M: "}, Length: 5}
 * 
 * Trains are an ordered list of Couplings
 * - ID and Key is the UUID of the main coupling
 * - Every coupling is part of exactly one train, lonely couplings are still treated as such
 * - When two trains are merged, the couplings have to be re-oriented to always point towards the main coupling
 * 
 * Loaded carts are queued to be dealt with on world tick, 
 * so that the world functions are not accessed during the chunk deserialization
 *
 * Challenges:
 * - Minecarts can only be corrected by changing their motion or their position
 * - A Minecarts' motion vector does not represent its actual movement next tick
 * - There is no accessible simulation step (can be copied / at'd)
 * - It is not always known which cart is ahead/behind 
 * - If both ends keep a contant momentum, the link stress is not necessarily satisfied
 * - Carts cannot be "dragged" directly towards resolving link stress; 
 * 	 It is not entirely predictable how motions outside of the rail vector get projected
 * 
 * 
 *
 * Day III, couplings still too unstable. Why is that? What causes the instability, is it the general approach or a specific issue
 * Explored strategies:
 * 
 * Acellerate against violation diff -> perpetual motion, Jittering, bouncyness
 * Brake and correct towards violation diff -> quick loss of momentum
 * Move against diff -> de-rails carts on tricky paths
 *
 * Not yet explored: running an actual simulation step for the minecarts' movement.
 * 
 *	- satisfied link
 *	-- stretched link
 *  . shortened link
 *	? not visible in ctx
 * 	= cart
 *  => moving cart
 * 
 *  Create algorithm to find a tick order which maximizes resolved stress
 *  
 *	=> ? <= ? = - =>	(@t)
 *			  ^ tick here first
 *
 *	cart[], motion[], position[]
 *	Predict through simulation + motion, that without any intervention, this happens:
 *	
 *	=> ? <= ? = -- => 	(@t+1)
 * 	
 * 	Decision: 	Accelerate trailing? (free motion)
 * 				Brake leading? (loss of momentum)
 * 			->	Both? 
 * 
 * Soft collisions can always be resolved. Just have to adjust motions accordingly.
 * Hard collisions should never be resolved by the soft/motion resolver, as it would generate or void momentum!
 * 
 * Approach: Hard pass then soft pass. two iterations of the coupling list
 * 
 * find starting point of hard resolve: the center of balance
 * i from left, j from right
 * compare and approach the exact center of the resolved chain.
 * 
 * 	-3	  -2-10v  0
 * 0-----0-0-0-0
 * 0--0--0--0--0
 *    2                        1     
 *  0---0-0---0---0--0--0-0-0-0-0
 * 0--0--0--0--0--0--0--0--0--0--0
 * 
 *     v
 *  0-0-0
 * 0--0--0
 * 
 *       v
 * 0---0---0---0
 *    0-0-0-0
 * 
 *  -1 0          -1   0
 * 0-0---0--0---0-0
 * 0--0--0--0--0--0
 * 
 * 
 * 
 * iterate both ways from the center and resolve hard collisions.
 * 
 * <HARD Resolve>
 * if coupling is NOT ok @t { 
 * 		Something unpredictable happened.
 * 		Try to reach soft state asap. every lost cycle in hard will act strangely and render inconsistently
 * 		Using motion to hard resolve is probably a bad idea
 * 		
 * 		if cart on rail -> force move along rail away from collision
 * 		else straight-up push it
 * 		use simulation to test if movements are possible
 * 
 * 		hard resolves are usually quite tiny. If they go beyond 1m then something really messed up
 * }
 * 
 * if coupling could not be fixed {
 *      clear all motion of the two carts (?)
 *      A failed hard collision implies that the Minecarts cannot be forced together/apart, might aswell not make things worse
 * }
 * </HARD Resolve>
 * 
 * Soft collisions only mess with motion values. It is still good to find a good order of iteration- 
 * that way predictions of earlier couplings in the loop are still accurate
 * 
 * =>>> - = - <= - =>>
 *
 * left to right
 * =>> - = - => - =>
 * right to left
 * =>> - => - = - =>
 * 
 * 
 * 
 * 
 * if now coupling is ok @t { 
 * 		<Soft Resolve>
 * 		Run Prediction I
 * 		if (coupling is ok @t+1)
 *          my job here is done; return
 * 		
 * 		get required force to resolve (front or back)
 * 		distribute equally over both carts
 * 		
 * 		Run Prediction II
 * 		if (coupling is ok @t+1*)
 * 			looks good; return
 * 	
 * 		re-distribute force to other cart
 * 		all collisions should be fixed at this point. return;
 * 		(in case of sudden changes/ bad predictions, the next cycle can handle the hard resolve)
 * 		</Soft Resolve>
 * }
 * 
 * 
 *
 * NEXT STEPS
 * 1. normalize diagonal rail vectors and debug all possible rail motion transfers. The required tools have to work properly
 * 2. implement a prediction step
 * 3. find a suitable hard collision resolver
 * 4. find a suitable soft collision order
 *
 */

public class MinecartCouplingHandler {

	static WorldAttached<Map<UUID, MinecartCoupling>> loadedCouplings = new WorldAttached<>(HashMap::new);
	static WorldAttached<Map<UUID, MinecartTrain>> loadedTrains = new WorldAttached<>(HashMap::new);

	static WorldAttached<List<AbstractMinecartEntity>> queuedCarts =
		new WorldAttached<>(() -> ObjectLists.synchronize(new ObjectArrayList<>()));

	public static void connectCarts(@Nullable PlayerEntity player, World world, int cartId1, int cartId2) {
		Entity entity1 = world.getEntityByID(cartId1);
		Entity entity2 = world.getEntityByID(cartId2);

		if (!(entity1 instanceof AbstractMinecartEntity))
			return;
		if (!(entity2 instanceof AbstractMinecartEntity))
			return;
		if ((int) entity1.getPositionVec()
			.distanceTo(entity2.getPositionVec()) > maxDistance())
			return;

		AbstractMinecartEntity cart1 = (AbstractMinecartEntity) entity1;
		AbstractMinecartEntity cart2 = (AbstractMinecartEntity) entity2;

		if (alreadyCoupled(world, cart1, cart2))
			return;

		addCoupling(world, MinecartCoupling.create(cart1, cart2), false);

		if (world.isRemote)
			return;
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> cart1),
			new MinecartCouplingSyncPacket(cart1, cart2));
	}

	@OnlyIn(Dist.CLIENT)
	public static void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		ClientWorld world = Minecraft.getInstance().world;
		if (world == null)
			return;
		loadedCouplings.get(world)
			.values()
			.forEach(c -> MinecartCouplingRenderer.renderCoupling(ms, buffer, c));
	}

	public static void tick(World world) {
		initQueuedCarts(world);
		removeUnloadedCouplings(world);
		loadedTrains.get(world)
			.values()
			.forEach(t -> t.tickCouplings(world));
	}

	private static void initQueuedCarts(World world) {
		List<AbstractMinecartEntity> queued = queuedCarts.get(world);
		if (queued == null)
			return;
		for (AbstractMinecartEntity minecart : queued)
			MinecartCoupling.loadAllAttached(world, minecart)
				.forEach(c -> addCoupling(world, c, true));
		queued.clear();
	}

	private static void removeUnloadedCouplings(World world) {
		List<UUID> toRemove = new ArrayList<>();
		Map<UUID, MinecartCoupling> couplings = loadedCouplings.get(world);
		if (couplings == null)
			return;
		for (Entry<UUID, MinecartCoupling> entry : couplings.entrySet())
			if (!entry.getValue()
				.areBothEndsPresent())
				toRemove.add(entry.getKey());
		couplings.keySet()
			.removeAll(toRemove);
	}

	public static void handleAddedMinecart(Entity entity, World world) {
		if (!(entity instanceof AbstractMinecartEntity))
			return;
		if (world.isRemote)
			queueLoadedMinecartClient(entity, world);
		else
			queueLoadedMinecart(entity, world);
	}

	public static void queueLoadedMinecartClient(Entity entity, World world) {
		AllPackets.channel.sendToServer(new PersistantDataPacketRequest(entity));
	}

	public static void queueLoadedMinecart(Entity entity, World world) {
		AbstractMinecartEntity minecart = (AbstractMinecartEntity) entity;
		CompoundNBT nbt = minecart.getPersistentData();
		if (!nbt.contains("Couplings"))
			return;
		queuedCarts.get(world)
			.add(minecart);
	}

	static int maxDistance() {
		return AllConfigs.SERVER.kinetics.maxCartCouplingLength.get();
	}

	public static Pair<UUID, Boolean> getTrainIfComplete(World world, AbstractMinecartEntity minecart,
		@Nullable UUID ignore) {
		AbstractMinecartEntity current = minecart;
		UUID trainId = current.getUniqueID();
		for (int i = 0; i < 100; i++) {
			List<CouplingData> couplingData = MinecartCouplingSerializer.getCouplingData(current);
			for (CouplingData data : couplingData) {
				if (data.main)
					continue;
				if (ignore != null && ignore.equals(data.id))
					continue;
				trainId = data.id;
				MinecartCoupling coupling = loadedCouplings.get(world)
					.get(trainId);

				// Not fully loaded in
				if (coupling == null)
					return Pair.of(trainId, false);

				current = coupling.mainCart.get();
			}
		}

		// Complete
		return Pair.of(trainId, true);
	}

	private static boolean alreadyCoupled(World world, AbstractMinecartEntity cart1, AbstractMinecartEntity cart2) {
		Pair<UUID, Boolean> trainOf = getTrainIfComplete(world, cart1, null);
		Pair<UUID, Boolean> trainOf2 = getTrainIfComplete(world, cart2, null);
		return trainOf.getRight() && trainOf2.getRight() && trainOf.getLeft()
			.equals(trainOf2.getLeft());
	}

	private static void addCoupling(World world, MinecartCoupling coupling, boolean loadedFromChunk) {
		MinecartTrain train = new MinecartTrain(coupling);
		Pair<UUID, Boolean> trainIdOfMain = getTrainIfComplete(world, coupling.mainCart.get(), null);
		Pair<UUID, Boolean> trainIdOfConnected =
			getTrainIfComplete(world, coupling.connectedCart.get(), loadedFromChunk ? coupling.getId() : null);

		// Something is not loaded
		if (!loadedFromChunk && !(trainIdOfMain.getValue() && trainIdOfConnected.getValue()))
			return;

		// Coupling was already loaded in
		if (loadedFromChunk && loadedCouplings.get(world)
			.containsKey(coupling.getId()))
			return;

		if (!world.isRemote) {
			Map<UUID, MinecartTrain> trains = loadedTrains.get(world);
			MinecartTrain trainOfMain = trains.get(trainIdOfMain.getKey());
			MinecartTrain trainOfConnected = trains.get(trainIdOfConnected.getKey());

			// Connected cart is part of a train, merge it onto the newly created one
			if (trainOfConnected != null)
				trains.remove(trainIdOfConnected.getKey())
					.mergeOnto(world, train);

			// Main cart is part of a train, merge the newly created one onto it
			boolean mainCartHasTrain = trainOfMain != null && trainIdOfMain.getKey()
				.equals(coupling.getId());
			if (trainOfMain != null) {
				if (mainCartHasTrain && !loadedFromChunk)
					flipTrain(world, trainOfMain);
				train.mergeOnto(world, trainOfMain);
				train = null;
			}

			// ...add the new train otherwise
			if (train != null)
				trains.put(train.getId(), train);
		}

		loadedCouplings.get(world)
			.put(coupling.getId(), coupling);
		if (!loadedFromChunk)
			coupling.writeToCarts();
	}

	public static void flipTrain(World world, MinecartTrain train) {
		Map<UUID, MinecartTrain> map = loadedTrains.get(world);
		map.remove(train.getId());
		train.flip(world);
		map.put(train.getId(), train);
	}

	public static MinecartCoupling getCoupling(World world, UUID id) {
		Map<UUID, MinecartCoupling> map = loadedCouplings.get(world);
		return map.get(id);
	}

	public static void flipCoupling(World world, MinecartCoupling coupling) {
		Map<UUID, MinecartCoupling> map = loadedCouplings.get(world);
		map.remove(coupling.getId());

		if (coupling.areBothEndsPresent()) {
			Couple<AbstractMinecartEntity> carts = coupling.asCouple();
			Couple<UUID> ids = carts.map(Entity::getUniqueID);
			carts.map(c -> c.isBeingRidden() ? c.getPassengers()
				.get(0) : null)
				.map(c -> c instanceof ContraptionEntity ? (ContraptionEntity) c : null)
				.forEachWithContext((contraption, current) -> {
					if (contraption == null || contraption.getCouplingId() == null)
						return;
					boolean switchTo = contraption.getCouplingId()
						.equals(ids.get(current)) ? !current : current;
					if (!carts.get(switchTo).getUniqueID().equals(contraption.getCoupledCart()))
						return;
					contraption.setCouplingId(ids.get(switchTo));
					contraption.setCoupledCart(ids.get(!switchTo));
				});
		}

		coupling.flip();
		map.put(coupling.getId(), coupling);
	}

}
