package com.simibubi.create.content.logistics.packagerLink;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.google.common.cache.Cache;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.TickBasedCache;

import net.createmod.catnip.data.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class LogisticallyLinkedBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<LogisticallyLinkedBehaviour> TYPE = new BehaviourType<>();

	public static final AtomicInteger LINK_ID_GENERATOR = new AtomicInteger();
	public int linkId; // Runtime context, not saved to disk

	public int redstonePower;
	public UUID freqId;

	private boolean addedGlobally = false;
	private boolean loadedGlobally = false;
	private boolean global = false;

	public static enum RequestType {
		RESTOCK, REDSTONE, PLAYER
	}

	private static final Cache<UUID, Cache<Integer, WeakReference<LogisticallyLinkedBehaviour>>> LINKS =
		new TickBasedCache<>(20, true);

	private static final Cache<UUID, Cache<Integer, WeakReference<LogisticallyLinkedBehaviour>>> CLIENT_LINKS =
		new TickBasedCache<>(20, true, true);

	public LogisticallyLinkedBehaviour(SmartBlockEntity be, boolean global) {
		super(be);
		this.global = global;
		linkId = LINK_ID_GENERATOR.getAndIncrement();
		freqId = UUID.randomUUID();
	}

	public static Collection<LogisticallyLinkedBehaviour> getAllPresent(UUID freq, boolean sortByPriority) {
		return getAllPresent(freq, sortByPriority, false);
	}

	public static Collection<LogisticallyLinkedBehaviour> getAllPresent(UUID freq, boolean sortByPriority,
																		boolean clientSide) {
		Cache<Integer, WeakReference<LogisticallyLinkedBehaviour>> cache =
			(clientSide ? CLIENT_LINKS : LINKS).getIfPresent(freq);
		if (cache == null)
			return Collections.emptyList();
		Stream<LogisticallyLinkedBehaviour> stream = new LinkedList<>(cache.asMap()
			.values()).stream()
			.map(WeakReference::get)
			.filter(LogisticallyLinkedBehaviour::isValidLink);

		if (sortByPriority)
			stream = stream.sorted((e1, e2) -> Integer.compare(e1.redstonePower, e2.redstonePower));

		return stream.toList();
	}

	public static void keepAlive(LogisticallyLinkedBehaviour behaviour) {
		boolean onClient = behaviour.blockEntity.getLevel().isClientSide;
		if (behaviour.redstonePower == 15)
			return;
		try {
			Cache<Integer, WeakReference<LogisticallyLinkedBehaviour>> cache =
				(onClient ? CLIENT_LINKS : LINKS).get(behaviour.freqId, () -> new TickBasedCache<>(400, false));

			if (cache == null)
				return;

			WeakReference<LogisticallyLinkedBehaviour> reference =
				cache.get(behaviour.linkId, () -> new WeakReference<>(behaviour));
			cache.put(behaviour.linkId, reference.get() != behaviour ? new WeakReference<>(behaviour) : reference);

		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public static void remove(LogisticallyLinkedBehaviour behaviour) {
		Cache<Integer, WeakReference<LogisticallyLinkedBehaviour>> cache = LINKS.getIfPresent(behaviour.freqId);
		if (cache != null)
			cache.invalidate(behaviour.linkId);
	}

	//

	@Override
	public void unload() {
		if (loadedGlobally && global && getWorld() != null)
			Create.LOGISTICS.linkInvalidated(freqId, getGlobalPos());
		super.unload();
		remove(this);
	}

	@Override
	public void lazyTick() {
		keepAlive(this);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (getWorld().isClientSide)
			return;

		if (!loadedGlobally && global) {
			loadedGlobally = true;
			Create.LOGISTICS.linkLoaded(freqId, getGlobalPos());
		}

		if (!addedGlobally && global) {
			addedGlobally = true;
			blockEntity.setChanged();
			if (blockEntity instanceof PackagerLinkBlockEntity plbe)
				Create.LOGISTICS.linkAdded(freqId, getGlobalPos(), plbe.placedBy);
		}

	}

	private GlobalPos getGlobalPos() {
		return GlobalPos.of(getWorld().dimension(), getPos());
	}

	@Override
	public void destroy() {
		super.destroy();
		if (addedGlobally && global && getWorld() != null)
			Create.LOGISTICS.linkRemoved(freqId, getGlobalPos());
	}

	public void redstonePowerChanged(int power) {
		if (power == redstonePower)
			return;
		redstonePower = power;
		blockEntity.setChanged();

		if (power == 15)
			remove(this);
		else
			keepAlive(this);
	}

	public Pair<PackagerBlockEntity, PackagingRequest> processRequest(ItemStack stack, int amount, String address,
		int linkIndex, MutableBoolean finalLink, int orderId, @Nullable PackageOrderWithCrafts context,
		@Nullable IdentifiedInventory ignoredHandler) {

		if (blockEntity instanceof PackagerLinkBlockEntity plbe)
			return plbe.processRequest(stack, amount, address, linkIndex, finalLink, orderId, context, ignoredHandler);

		return null;
	}

	public InventorySummary getSummary(@Nullable IdentifiedInventory ignoredHandler) {
		if (blockEntity instanceof PackagerLinkBlockEntity plbe)
			return plbe.fetchSummaryFromPackager(ignoredHandler);
		return InventorySummary.EMPTY;
	}

	public void deductFromAccurateSummary(ItemStackHandler packageContents) {
		InventorySummary summary = LogisticsManager.ACCURATE_SUMMARIES.getIfPresent(freqId);
		if (summary == null)
			return;
		for (int i = 0; i < packageContents.getSlots(); i++) {
			ItemStack orderedStack = packageContents.getStackInSlot(i);
			if (orderedStack.isEmpty())
				continue;
			summary.add(orderedStack, -Math.min(summary.getCountOf(orderedStack), orderedStack.getCount()));
		}
	}

	//

	public boolean mayInteract(Player player) {
		return Create.LOGISTICS.mayInteract(freqId, player);
	}

	public boolean mayInteractMessage(Player player) {
		boolean mayInteract = Create.LOGISTICS.mayInteract(freqId, player);
		if (!mayInteract)
			player.displayClientMessage(CreateLang.translate("logistically_linked.protected")
				.style(ChatFormatting.RED)
				.component(), true);
		return mayInteract;
	}

	public boolean mayAdministrate(Player player) {
		return Create.LOGISTICS.mayAdministrate(freqId, player);
	}

	public static boolean isValidLink(LogisticallyLinkedBehaviour link) {
		return link != null && !link.blockEntity.isRemoved() && !link.blockEntity.isChunkUnloaded();
	}

	@Override
	public boolean isSafeNBT() {
		return true;
	}

	@Override
	public void writeSafe(CompoundTag tag, HolderLookup.Provider registries) {
		tag.putUUID("Freq", freqId);
	}

	@Override
	public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.putUUID("Freq", freqId);
		tag.putInt("Power", redstonePower);
		tag.putBoolean("Added", addedGlobally);
	}

	@Override
	public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		if (tag.hasUUID("Freq"))
			freqId = tag.getUUID("Freq");
		redstonePower = tag.getInt("Power");
		addedGlobally = tag.getBoolean("Added");
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

}
