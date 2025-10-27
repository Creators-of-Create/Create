package com.simibubi.create.content.logistics.factoryBoard;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedClientHandler;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.packagerLink.RequestPromise;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.createmod.catnip.codecs.CatnipCodecs;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.Tags.Items;

public class FactoryPanelBehaviour extends FilteringBehaviour implements MenuProvider {

	public static final BehaviourType<FactoryPanelBehaviour> TOP_LEFT = new BehaviourType<>();
	public static final BehaviourType<FactoryPanelBehaviour> TOP_RIGHT = new BehaviourType<>();
	public static final BehaviourType<FactoryPanelBehaviour> BOTTOM_LEFT = new BehaviourType<>();
	public static final BehaviourType<FactoryPanelBehaviour> BOTTOM_RIGHT = new BehaviourType<>();

	public Map<FactoryPanelPosition, FactoryPanelConnection> targetedBy;
	public Map<BlockPos, FactoryPanelConnection> targetedByLinks;
	public Set<FactoryPanelPosition> targeting;
	public List<ItemStack> activeCraftingArrangement;

	public boolean satisfied;
	public boolean promisedSatisfied;
	public boolean waitingForNetwork;
	public String recipeAddress;
	public int recipeOutput;
	public LerpedFloat bulb;
	public PanelSlot slot;
	public int promiseClearingInterval;
	public boolean forceClearPromises;
	public UUID network;
	public boolean active;

	public boolean redstonePowered;

	public RequestPromiseQueue restockerPromises;
	private boolean promisePrimedForMarkDirty;

	private int lastReportedUnloadedLinks;
	private int lastReportedLevelInStorage;
	private int lastReportedPromises;
	private int timer;

	public FactoryPanelBehaviour(FactoryPanelBlockEntity be, PanelSlot slot) {
		super(be, new FactoryPanelSlotPositioning(slot));
		this.slot = slot;
		this.targetedBy = new HashMap<>();
		this.targetedByLinks = new HashMap<>();
		this.targeting = new HashSet<>();
		this.count = 0;
		this.satisfied = false;
		this.promisedSatisfied = false;
		this.waitingForNetwork = false;
		this.activeCraftingArrangement = List.of();
		this.recipeAddress = "";
		this.recipeOutput = 1;
		this.active = false;
		this.forceClearPromises = false;
		this.redstonePowered = false;
		this.promiseClearingInterval = -1;
		this.bulb = LerpedFloat.linear()
			.startWithValue(0)
			.chase(0, 0.175, Chaser.EXP);
		this.restockerPromises = new RequestPromiseQueue(be::setChanged);
		this.promisePrimedForMarkDirty = true;
		this.network = UUID.randomUUID();
		setLazyTickRate(40);
	}

	public void setNetwork(UUID network) {
		this.network = network;
	}

	@Nullable
	public static FactoryPanelBehaviour at(BlockAndTintGetter world, FactoryPanelConnection connection) {
		Object cached = connection.cachedSource.get();
		if (cached instanceof FactoryPanelBehaviour fbe && !fbe.blockEntity.isRemoved())
			return fbe;
		FactoryPanelBehaviour result = at(world, connection.from);
		connection.cachedSource = new WeakReference<>(result);
		return result;
	}

	@Nullable
	public static FactoryPanelBehaviour at(BlockAndTintGetter world, FactoryPanelPosition pos) {
		if (world instanceof Level l && !l.isLoaded(pos.pos()))
			return null;
		if (!(world.getBlockEntity(pos.pos()) instanceof FactoryPanelBlockEntity fpbe))
			return null;
		FactoryPanelBehaviour behaviour = fpbe.panels.get(pos.slot());
		if (!behaviour.active)
			return null;
		return behaviour;
	}

	@Nullable
	public static FactoryPanelSupportBehaviour linkAt(BlockAndTintGetter world, FactoryPanelConnection connection) {
		Object cached = connection.cachedSource.get();
		if (cached instanceof FactoryPanelSupportBehaviour fpsb && !fpsb.blockEntity.isRemoved())
			return fpsb;
		FactoryPanelSupportBehaviour result = linkAt(world, connection.from);
		connection.cachedSource = new WeakReference<>(result);
		return result;
	}

	@Nullable
	public static FactoryPanelSupportBehaviour linkAt(BlockAndTintGetter world, FactoryPanelPosition pos) {
		if (world instanceof Level l && !l.isLoaded(pos.pos()))
			return null;
		return BlockEntityBehaviour.get(world, pos.pos(), FactoryPanelSupportBehaviour.TYPE);
	}

	public void moveTo(FactoryPanelPosition newPos, ServerPlayer player) {
		Level level = getWorld();
		BlockState existingState = level.getBlockState(newPos.pos());

		// Check if target pos is valid
		if (FactoryPanelBehaviour.at(level, newPos) != null)
			return;
		boolean isAddedToOtherGauge = AllBlocks.FACTORY_GAUGE.has(existingState);
		if (!existingState.isAir() && !isAddedToOtherGauge)
			return;
		if (isAddedToOtherGauge && existingState != blockEntity.getBlockState())
			return;
		if (!isAddedToOtherGauge)
			level.setBlock(newPos.pos(), blockEntity.getBlockState(), Block.UPDATE_ALL);

		for (BlockPos blockPos : targetedByLinks.keySet())
			if (!blockPos.closerThan(newPos.pos(), 24))
				return;
		for (FactoryPanelPosition blockPos : targetedBy.keySet())
			if (!blockPos.pos().closerThan(newPos.pos(), 24))
				return;
		for (FactoryPanelPosition blockPos : targeting)
			if (!blockPos.pos().closerThan(newPos.pos(), 24))
				return;

		// Disconnect links
		for (BlockPos pos : targetedByLinks.keySet()) {
			FactoryPanelSupportBehaviour at = linkAt(level, new FactoryPanelPosition(pos, slot));
			if (at != null)
				at.disconnect(this);
		}

		SmartBlockEntity oldBE = blockEntity;
		FactoryPanelPosition oldPos = getPanelPosition();
		moveToSlot(newPos.slot());

		// Add to new BE
		if (level.getBlockEntity(newPos.pos()) instanceof FactoryPanelBlockEntity fpbe) {
			fpbe.attachBehaviourLate(this);
			fpbe.panels.put(slot, this);
			fpbe.redraw = true;
			fpbe.lastShape = null;
			fpbe.notifyUpdate();
		}

		// Remove from old BE
		if (oldBE instanceof FactoryPanelBlockEntity fpbe) {
			FactoryPanelBehaviour newBehaviour = new FactoryPanelBehaviour(fpbe, oldPos.slot());
			fpbe.attachBehaviourLate(newBehaviour);
			fpbe.panels.put(oldPos.slot(), newBehaviour);
			fpbe.redraw = true;
			fpbe.lastShape = null;
			fpbe.notifyUpdate();
		}

		// Rewire connections
		for (FactoryPanelPosition position : targeting) {
			FactoryPanelBehaviour at = at(level, position);
			if (at != null) {
				FactoryPanelConnection connection = at.targetedBy.remove(oldPos);
				connection.from = newPos;
				at.targetedBy.put(newPos, connection);
				at.blockEntity.sendData();
			}
		}

		for (FactoryPanelPosition position : targetedBy.keySet()) {
			FactoryPanelBehaviour at = at(level, position);
			if (at != null) {
				at.targeting.remove(oldPos);
				at.targeting.add(newPos);
			}
		}

		// Reconnect links
		for (BlockPos pos : targetedByLinks.keySet()) {
			FactoryPanelSupportBehaviour at = linkAt(level, new FactoryPanelPosition(pos, slot));
			if (at != null)
				at.connect(this);
		}

		// Tell player
		player.displayClientMessage(CreateLang.translate("factory_panel.relocated")
			.style(ChatFormatting.GREEN)
			.component(), true);
		player.level()
			.playSound(null, newPos.pos(), SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
	}

	private void moveToSlot(PanelSlot slot) {
		this.slot = slot;
		if (this.getSlotPositioning() instanceof FactoryPanelSlotPositioning fpsp)
			fpsp.slot = slot;
	}

	@Override
	public void initialize() {
		super.initialize();
		notifyRedstoneOutputs();
	}

	@Override
	public void tick() {
		super.tick();
		if (getWorld().isClientSide()) {
			if (blockEntity.isVirtual())
				tickStorageMonitor();
			bulb.updateChaseTarget(redstonePowered || satisfied ? 1 : 0);
			bulb.tickChaser();
			if (active)
				tickOutline();
			return;
		}

		if (!promisePrimedForMarkDirty) {
			restockerPromises.setOnChanged(blockEntity::setChanged);
			promisePrimedForMarkDirty = true;
		}

		tickStorageMonitor();
		tickRequests();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (getWorld().isClientSide())
			return;
		checkForRedstoneInput();
	}

	public void checkForRedstoneInput() {
		if (!active)
			return;

		boolean shouldPower = false;
		for (FactoryPanelConnection connection : targetedByLinks.values()) {
			if (!getWorld().isLoaded(connection.from.pos()))
				return;
			FactoryPanelSupportBehaviour linkAt = linkAt(getWorld(), connection);
			if (linkAt == null)
				return;
			shouldPower |= linkAt.shouldPanelBePowered();
		}

		if (shouldPower == redstonePowered)
			return;

		redstonePowered = shouldPower;
		blockEntity.notifyUpdate();
		timer = 1;
	}

	private void notifyRedstoneOutputs() {
		for (FactoryPanelConnection connection : targetedByLinks.values()) {
			if (!getWorld().isLoaded(connection.from.pos()))
				return;
			FactoryPanelSupportBehaviour linkAt = linkAt(getWorld(), connection);
			if (linkAt == null || linkAt.isOutput())
				return;
			linkAt.notifyLink();
		}
	}

	private void tickStorageMonitor() {
		ItemStack filter = getFilter();
		int inStorage = getLevelInStorage();
		int promised = getPromised();
		int demand = getAmount() * (upTo ? 1 : filter.getMaxStackSize());
		int unloadedLinkCount = getUnloadedLinks();
		boolean shouldSatisfy = filter.isEmpty() || inStorage >= demand;
		boolean shouldPromiseSatisfy = filter.isEmpty() || inStorage + promised >= demand;
		boolean shouldWait = unloadedLinkCount > 0;

		if (lastReportedLevelInStorage == inStorage && lastReportedPromises == promised
			&& lastReportedUnloadedLinks == unloadedLinkCount && satisfied == shouldSatisfy
			&& promisedSatisfied == shouldPromiseSatisfy && waitingForNetwork == shouldWait)
			return;

		if (!satisfied && shouldSatisfy && demand > 0) {
			AllSoundEvents.CONFIRM.playOnServer(getWorld(), getPos(), 0.075f, 1f);
			AllSoundEvents.CONFIRM_2.playOnServer(getWorld(), getPos(), 0.125f, 0.575f);
		}

		boolean notifyOutputs = satisfied != shouldSatisfy;
		lastReportedLevelInStorage = inStorage;
		satisfied = shouldSatisfy;
		lastReportedPromises = promised;
		promisedSatisfied = shouldPromiseSatisfy;
		lastReportedUnloadedLinks = unloadedLinkCount;
		waitingForNetwork = shouldWait;
		if (!getWorld().isClientSide)
			blockEntity.sendData();
		if (notifyOutputs)
			notifyRedstoneOutputs();
	}

	public static class ItemStackConnections extends ArrayList<FactoryPanelConnection> {
		public ItemStack item;
		public int totalAmount;

		public ItemStackConnections(ItemStack item) {
			this.item = item;
		}
	}

	private void tickRequests() {
		FactoryPanelBlockEntity panelBE = panelBE();
		if (targetedBy.isEmpty() && !panelBE.restocker)
			return;
		if (panelBE.restocker)
			restockerPromises.tick();
		if (satisfied || promisedSatisfied || waitingForNetwork || redstonePowered)
			return;
		if (timer > 0) {
			timer = Math.min(timer, getConfigRequestIntervalInTicks());
			timer--;
			return;
		}

		resetTimer();

		if (recipeAddress.isBlank())
			return;

		if (panelBE.restocker) {
			tryRestock();
			return;
		}

		boolean failed = false;

		Map<UUID, Map<ItemStack, ItemStackConnections>> consolidated = new HashMap<>();

		for (FactoryPanelConnection connection : targetedBy.values()) {
			FactoryPanelBehaviour source = at(getWorld(), connection);
			if (source == null)
				return;

			ItemStack item = source.getFilter();


			Map<ItemStack, ItemStackConnections> networkItemCounts = consolidated.computeIfAbsent(source.network, $ -> new Object2ObjectOpenCustomHashMap<>(ItemStackLinkedSet.TYPE_AND_TAG));
			networkItemCounts.computeIfAbsent(item, $ -> new ItemStackConnections(item));
			ItemStackConnections existingConnections = networkItemCounts.get(item);
			existingConnections.add(connection);
			existingConnections.totalAmount += connection.amount;
		}

		Multimap<UUID, BigItemStack> toRequest = HashMultimap.create();

		for (Entry<UUID, Map<ItemStack, ItemStackConnections>> entry : consolidated.entrySet()) {
			UUID network = entry.getKey();
			InventorySummary summary = LogisticsManager.getSummaryOfNetwork(network, true);

			for (ItemStackConnections connections : entry.getValue().values()) {
				if (connections.totalAmount == 0 || connections.item.isEmpty() || summary.getCountOf(connections.item) < connections.totalAmount) {
					for (FactoryPanelConnection connection : connections)
						sendEffect(connection.from, false);
					failed = true;
					continue;
				}

				BigItemStack stack = new BigItemStack(connections.item, connections.totalAmount);
				toRequest.put(network, stack);
				for (FactoryPanelConnection connection : connections)
					sendEffect(connection.from, true);
			}
		}

		if (failed)
			return;

		// Input items may come from differing networks
		Map<UUID, Collection<BigItemStack>> asMap = toRequest.asMap();
		PackageOrderWithCrafts craftingContext = PackageOrderWithCrafts.empty();
		List<Multimap<PackagerBlockEntity, PackagingRequest>> requests = new ArrayList<>();

		// Panel may enforce item arrangement
		if (!activeCraftingArrangement.isEmpty())
			craftingContext = PackageOrderWithCrafts.singleRecipe(activeCraftingArrangement.stream()
				.map(stack -> new BigItemStack(stack.copyWithCount(1)))
				.toList());

		// Collect request distributions
		for (Entry<UUID, Collection<BigItemStack>> entry : asMap.entrySet()) {
			PackageOrderWithCrafts order =
				new PackageOrderWithCrafts(new PackageOrder(new ArrayList<>(entry.getValue())), craftingContext.orderedCrafts());
			Multimap<PackagerBlockEntity, PackagingRequest> request =
				LogisticsManager.findPackagersForRequest(entry.getKey(), order, null, recipeAddress);
			requests.add(request);
		}

		// Check if any packager is busy - cancel all
		for (Multimap<PackagerBlockEntity, PackagingRequest> entry : requests)
			for (PackagerBlockEntity packager : entry.keySet())
				if (packager.isTooBusyFor(RequestType.RESTOCK))
					return;

		// Send it
		for (Multimap<PackagerBlockEntity, PackagingRequest> entry : requests)
			LogisticsManager.performPackageRequests(entry);

		// Keep the output promise
		RequestPromiseQueue promises = Create.LOGISTICS.getQueuedPromises(network);
		if (promises != null)
			promises.add(new RequestPromise(new BigItemStack(getFilter(), recipeOutput)));

		panelBE.advancements.awardPlayer(AllAdvancements.FACTORY_GAUGE);
	}

	private void tryRestock() {
		ItemStack item = getFilter();
		if (item.isEmpty())
			return;

		FactoryPanelBlockEntity panelBE = panelBE();
		PackagerBlockEntity packager = panelBE.getRestockedPackager();
		if (packager == null || !packager.targetInventory.hasInventory())
			return;

		int availableOnNetwork = LogisticsManager.getStockOf(network, item, packager.targetInventory.getIdentifiedInventory());
		if (availableOnNetwork == 0) {
			sendEffect(getPanelPosition(), false);
			return;
		}

		int inStorage = getLevelInStorage();
		int promised = getPromised();
		int maxStackSize = item.getMaxStackSize();
		int demand = getAmount() * (upTo ? 1 : maxStackSize);
		int amountToOrder = Math.clamp(demand - promised - inStorage, 0, maxStackSize * 9);

		BigItemStack orderedItem = new BigItemStack(item, Math.min(amountToOrder, availableOnNetwork));
		PackageOrderWithCrafts order = PackageOrderWithCrafts.simple(List.of(orderedItem));

		sendEffect(getPanelPosition(), true);

		if (!LogisticsManager.broadcastPackageRequest(network, RequestType.RESTOCK, order,
			packager.targetInventory.getIdentifiedInventory(), recipeAddress))
			return;

		restockerPromises.add(new RequestPromise(orderedItem));
	}

	private void sendEffect(FactoryPanelPosition fromPos, boolean success) {
		if (getWorld() instanceof ServerLevel serverLevel)
			CatnipServices.NETWORK.sendToClientsAround(serverLevel, getPos(), 64, new FactoryPanelEffectPacket(fromPos, getPanelPosition(), success));
	}

	public void addConnection(FactoryPanelPosition fromPos) {
		FactoryPanelSupportBehaviour link = linkAt(getWorld(), fromPos);
		if (link != null) {
			targetedByLinks.put(fromPos.pos(), new FactoryPanelConnection(fromPos, 1));
			link.connect(this);
			blockEntity.notifyUpdate();
			return;
		}

		if (panelBE().restocker)
			return;
		if (targetedBy.size() >= 9)
			return;

		FactoryPanelBehaviour source = at(getWorld(), fromPos);
		if (source == null)
			return;

		source.targeting.add(getPanelPosition());
		targetedBy.put(fromPos, new FactoryPanelConnection(fromPos, 1));
		blockEntity.notifyUpdate();
	}

	public FactoryPanelPosition getPanelPosition() {
		return new FactoryPanelPosition(getPos(), slot);
	}

	public FactoryPanelBlockEntity panelBE() {
		return (FactoryPanelBlockEntity) blockEntity;
	}

	@Override
	public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
		// Network is protected
		if (!Create.LOGISTICS.mayInteract(network, player)) {
			player.displayClientMessage(CreateLang.translate("logistically_linked.protected")
				.style(ChatFormatting.RED)
				.component(), true);
			return;
		}

		boolean isClientSide = player.level().isClientSide;

		// Wrench cycles through arrow bending
		if (targeting.size() + targetedByLinks.size() > 0 && player.getItemInHand(hand).is(Items.TOOLS_WRENCH)) {
			int sharedMode = -1;
			boolean notifySelf = false;

			for (FactoryPanelPosition target : targeting) {
				FactoryPanelBehaviour at = at(getWorld(), target);
				if (at == null)
					continue;
				FactoryPanelConnection connection = at.targetedBy.get(getPanelPosition());
				if (connection == null)
					continue;
				if (sharedMode == -1)
					sharedMode = (connection.arrowBendMode + 1) % 4;
				connection.arrowBendMode = sharedMode;
				if (!isClientSide)
					at.blockEntity.notifyUpdate();
			}

			for (FactoryPanelConnection connection : targetedByLinks.values()) {
				if (sharedMode == -1)
					sharedMode = (connection.arrowBendMode + 1) % 4;
				connection.arrowBendMode = sharedMode;
				if (!isClientSide)
					notifySelf = true;
			}

			if (sharedMode == -1)
				return;

			char[] boxes = "\u25a1\u25a1\u25a1\u25a1".toCharArray();
			boxes[sharedMode] = '\u25a0';
			player.displayClientMessage(CreateLang.translate("factory_panel.cycled_arrow_path", new String(boxes))
				.component(), true);
			if (notifySelf)
				blockEntity.notifyUpdate();

			return;
		}

		// Client might be in the process of connecting a panel
		if (isClientSide)
			if (FactoryPanelConnectionHandler.panelClicked(getWorld(), player, this))
				return;

		ItemStack heldItem = player.getItemInHand(hand);
		if (getFilter().isEmpty()) {
			// Open screen for setting an item through JEI
			if (heldItem.isEmpty()) {
				if (!isClientSide && player instanceof ServerPlayer sp)
					sp.openMenu(this, buf -> FactoryPanelPosition.STREAM_CODEC.encode(buf, getPanelPosition()));
				return;
			}

			// Use regular filter interaction for setting the item
			super.onShortInteract(player, hand, side, hitResult);
			return;
		}

		// Bind logistics items to this panels' frequency
		if (heldItem.getItem() instanceof LogisticallyLinkedBlockItem) {
			if (!isClientSide)
				LogisticallyLinkedBlockItem.assignFrequency(heldItem, player, network);
			return;
		}

		// Open configuration screen
		if (isClientSide)
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> displayScreen(player));
	}

	public void enable() {
		active = true;
		blockEntity.notifyUpdate();
	}

	public void disable() {
		destroy();
		active = false;
		targetedBy = new HashMap<>();
		targeting = new HashSet<>();
		count = 0;
		satisfied = false;
		promisedSatisfied = false;
		recipeAddress = "";
		recipeOutput = 1;
		setFilter(ItemStack.EMPTY);
		blockEntity.notifyUpdate();
	}

	@Override
	public boolean isActive() {
		return active;
	}

	public boolean isMissingAddress() {
		return (!targetedBy.isEmpty() || panelBE().restocker) && count != 0 && recipeAddress.isBlank();
	}

	@Override
	public void destroy() {
		disconnectAll();
		super.destroy();
	}

	public void disconnectAll() {
		FactoryPanelPosition panelPosition = getPanelPosition();
		disconnectAllLinks();
		for (FactoryPanelConnection connection : targetedBy.values()) {
			FactoryPanelBehaviour source = at(getWorld(), connection);
			if (source != null) {
				source.targeting.remove(panelPosition);
				source.blockEntity.sendData();
			}
		}
		for (FactoryPanelPosition position : targeting) {
			FactoryPanelBehaviour target = at(getWorld(), position);
			if (target != null) {
				target.targetedBy.remove(panelPosition);
				target.blockEntity.sendData();
			}
		}
		targetedBy.clear();
		targeting.clear();
	}

	public void disconnectAllLinks() {
		for (FactoryPanelConnection connection : targetedByLinks.values()) {
			FactoryPanelSupportBehaviour source = linkAt(getWorld(), connection);
			if (source != null)
				source.disconnect(this);
		}
		targetedByLinks.clear();
	}

	public int getUnloadedLinks() {
		if (getWorld().isClientSide())
			return lastReportedUnloadedLinks;
		if (panelBE().restocker)
			return panelBE().getRestockedPackager() == null ? 1 : 0;
		return Create.LOGISTICS.getUnloadedLinkCount(network);
	}

	public int getLevelInStorage() {
		if (blockEntity.isVirtual())
			return 1;
		if (getWorld().isClientSide())
			return lastReportedLevelInStorage;
		if (getFilter().isEmpty())
			return 0;

		InventorySummary summary = getRelevantSummary();
		return summary.getCountOf(getFilter());
	}

	private InventorySummary getRelevantSummary() {
		FactoryPanelBlockEntity panelBE = panelBE();
		if (!panelBE.restocker)
			return LogisticsManager.getSummaryOfNetwork(network, false);
		PackagerBlockEntity packager = panelBE.getRestockedPackager();
		if (packager == null)
			return InventorySummary.EMPTY;
		return packager.getAvailableItems();
	}

	public int getPromised() {
		if (getWorld().isClientSide())
			return lastReportedPromises;
		ItemStack item = getFilter();
		if (item.isEmpty())
			return 0;

		if (panelBE().restocker) {
			if (forceClearPromises) {
				restockerPromises.forceClear(item);
				resetTimerSlightly();
			}
			forceClearPromises = false;
			return restockerPromises.getTotalPromisedAndRemoveExpired(item, getPromiseExpiryTimeInTicks());
		}

		RequestPromiseQueue promises = Create.LOGISTICS.getQueuedPromises(network);
		if (promises == null)
			return 0;

		if (forceClearPromises) {
			promises.forceClear(item);
			resetTimerSlightly();
		}
		forceClearPromises = false;

		return promises.getTotalPromisedAndRemoveExpired(item, getPromiseExpiryTimeInTicks());
	}

	public void resetTimer() {
		timer = getConfigRequestIntervalInTicks();
	}

	public void resetTimerSlightly() {
		timer = getConfigRequestIntervalInTicks() / 2;
	}

	private int getConfigRequestIntervalInTicks() {
		return AllConfigs.server().logistics.factoryGaugeTimer.get();
	}

	private int getPromiseExpiryTimeInTicks() {
		if (promiseClearingInterval == -1)
			return -1;
		if (promiseClearingInterval == 0)
			return 20 * 30;

		return promiseClearingInterval * 20 * 60;
	}

	@Override
	public void writeSafe(CompoundTag nbt, HolderLookup.Provider registries) {
		if (!active)
			return;

		CompoundTag panelTag = new CompoundTag();
		panelTag.put("Filter", getFilter().saveOptional(registries));
		panelTag.putBoolean("UpTo", upTo);
		panelTag.putInt("FilterAmount", count);
		panelTag.putUUID("Freq", network);
		panelTag.putString("RecipeAddress", recipeAddress);
		panelTag.putInt("PromiseClearingInterval", -1);
		panelTag.putInt("RecipeOutput", 1);

		if (panelBE().restocker)
			panelTag.put("Promises", restockerPromises.write(registries));

		nbt.put(CreateLang.asId(slot.name()), panelTag);
	}

	@Override
	public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
		if (!active)
			return;

		CompoundTag panelTag = new CompoundTag();
		super.write(panelTag, registries, clientPacket);

		panelTag.putInt("Timer", timer);
		panelTag.putInt("LastLevel", lastReportedLevelInStorage);
		panelTag.putInt("LastPromised", lastReportedPromises);
		panelTag.putInt("LastUnloadedLinks", lastReportedUnloadedLinks);
		panelTag.putBoolean("Satisfied", satisfied);
		panelTag.putBoolean("PromisedSatisfied", promisedSatisfied);
		panelTag.putBoolean("Waiting", waitingForNetwork);
		panelTag.putBoolean("RedstonePowered", redstonePowered);
		panelTag.put("Targeting", CatnipCodecUtils.encode(CatnipCodecs.set(FactoryPanelPosition.CODEC), registries, targeting).orElseThrow());
		panelTag.put("TargetedBy", CatnipCodecUtils.encode(Codec.list(FactoryPanelConnection.CODEC), registries, new ArrayList<>(targetedBy.values())).orElseThrow());
		panelTag.put("TargetedByLinks", CatnipCodecUtils.encode(Codec.list(FactoryPanelConnection.CODEC), registries, new ArrayList<>(targetedByLinks.values())).orElseThrow());
		panelTag.putString("RecipeAddress", recipeAddress);
		panelTag.putInt("RecipeOutput", recipeOutput);
		panelTag.putInt("PromiseClearingInterval", promiseClearingInterval);
		panelTag.putUUID("Freq", network);
		panelTag.put("Craft", NBTHelper.writeItemList(activeCraftingArrangement, registries));

		if (panelBE().restocker && !clientPacket)
			panelTag.put("Promises", restockerPromises.write(registries));

		nbt.put(CreateLang.asId(slot.name()), panelTag);
	}

	@Override
	public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
		CompoundTag panelTag = nbt.getCompound(CreateLang.asId(slot.name()));
		if (panelTag.isEmpty()) {
			active = false;
			return;
		}

		active = true;
		filter = FilterItemStack.of(registries, panelTag.getCompound("Filter"));
		count = panelTag.getInt("FilterAmount");
		upTo = panelTag.getBoolean("UpTo");
		timer = panelTag.getInt("Timer");
		lastReportedLevelInStorage = panelTag.getInt("LastLevel");
		lastReportedPromises = panelTag.getInt("LastPromised");
		lastReportedUnloadedLinks = panelTag.getInt("LastUnloadedLinks");
		satisfied = panelTag.getBoolean("Satisfied");
		promisedSatisfied = panelTag.getBoolean("PromisedSatisfied");
		waitingForNetwork = panelTag.getBoolean("Waiting");
		redstonePowered = panelTag.getBoolean("RedstonePowered");
		promiseClearingInterval = panelTag.getInt("PromiseClearingInterval");
		if (panelTag.hasUUID("Freq"))
			network = panelTag.getUUID("Freq");

		targeting.clear();
		targeting.addAll(CatnipCodecUtils.decode(CatnipCodecs.set(FactoryPanelPosition.CODEC), registries, panelTag.get("Targeting")).orElse(Set.of()));

		targetedBy.clear();
		CatnipCodecUtils.decode(Codec.list(FactoryPanelConnection.CODEC), registries, panelTag.get("TargetedBy")).orElse(List.of())
			.forEach(c -> targetedBy.put(c.from, c));

		targetedByLinks.clear();
		CatnipCodecUtils.decode(Codec.list(FactoryPanelConnection.CODEC), registries, panelTag.get("TargetedByLinks")).orElse(List.of())
			.forEach(c -> targetedByLinks.put(c.from.pos(), c));

		activeCraftingArrangement = NBTHelper.readItemList(panelTag.getList("Craft", Tag.TAG_COMPOUND), registries);
		recipeAddress = panelTag.getString("RecipeAddress");
		recipeOutput = panelTag.getInt("RecipeOutput");

		if (nbt.getBoolean("Restocker") && !clientPacket) {
			restockerPromises = RequestPromiseQueue.read(panelTag.getCompound("Promises"), registries, () -> {
			});
			promisePrimedForMarkDirty = false;
		}
	}

	@Override
	public float getRenderDistance() {
		return 64;
	}

	@Override
	public MutableComponent formatValue(ValueSettings value) {
		if (value.value() == 0) {
			return CreateLang.translateDirect("gui.factory_panel.inactive");
		} else {
			return Component.literal(Math.max(0, value.value()) + ((value.row() == 0) ? "" : "\u25A4"));
		}
	}

	@Override
	public boolean setFilter(ItemStack stack) {
		ItemStack filter = stack.copy();
		if (stack.getItem() instanceof FilterItem)
			return false;
		this.filter = FilterItemStack.of(filter);
		blockEntity.setChanged();
		blockEntity.sendData();
		return true;
	}

	@Override
	public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
		if (getValueSettings().equals(settings))
			return;
		count = Math.max(0, settings.value());
		upTo = settings.row() == 0;
		panelBE().redraw = true;
		blockEntity.setChanged();
		blockEntity.sendData();
		playFeedbackSound(this);
		resetTimerSlightly();
		if (!getWorld().isClientSide)
			notifyRedstoneOutputs();
	}

	@Override
	public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
		int maxAmount = 100;
		return new ValueSettingsBoard(CreateLang.translate("factory_panel.target_amount")
			.component(), maxAmount, 10,
			List.of(CreateLang.translate("schedule.condition.threshold.items")
					.component(),
				CreateLang.translate("schedule.condition.threshold.stacks")
					.component()),
			new ValueSettingsFormatter(this::formatValue));
	}

	@Override
	public MutableComponent getLabel() {
		String key = "";

		if (!targetedBy.isEmpty() && count == 0)
			return CreateLang.translate("gui.factory_panel.no_target_amount_set")
				.style(ChatFormatting.RED)
				.component();

		if (isMissingAddress())
			return CreateLang.translate("gui.factory_panel.address_missing")
				.style(ChatFormatting.RED)
				.component();

		if (getFilter().isEmpty())
			key = "factory_panel.new_factory_task";
		else if (waitingForNetwork)
			key = "factory_panel.some_links_unloaded";
		else if (getAmount() == 0 || targetedBy.isEmpty())
			return getFilter().getHoverName()
				.plainCopy();
		else {
			key = getFilter().getHoverName()
				.getString();
			if (redstonePowered)
				key += " " + CreateLang.translate("factory_panel.redstone_paused")
					.string();
			else if (!satisfied)
				key += " " + CreateLang.translate("factory_panel.in_progress")
					.string();
			return CreateLang.text(key)
				.component();
		}

		return CreateLang.translate(key)
			.component();
	}

	@Override
	public ValueSettings getValueSettings() {
		return new ValueSettings(upTo ? 0 : 1, count);
	}

	@Override
	public MutableComponent getTip() {
		return CreateLang
			.translateDirect(filter.isEmpty() ? "logistics.filter.click_to_set" : "factory_panel.click_to_configure");
	}

	public MutableComponent getAmountTip() {
		return CreateLang.translateDirect("factory_panel.hold_to_set_amount");
	}

	@Override
	public MutableComponent getCountLabelForValueBox() {
		if (filter.isEmpty())
			return Component.empty();
		if (waitingForNetwork) {
			return Component.literal("?");
		}

		int levelInStorage = getLevelInStorage();
		boolean inf = levelInStorage >= BigItemStack.INF;
		int inStorage = levelInStorage / (upTo ? 1 : getFilter().getMaxStackSize());
		int promised = getPromised();
		String stacks = upTo ? "" : "\u25A4";

		if (count == 0) {
			return CreateLang.text(inf ? "  \u221e" : inStorage + stacks)
				.color(0xF1EFE8)
				.component();
		}

		return CreateLang.text(inf ? "  \u221e" : "   " + inStorage + stacks)
			.color(satisfied ? 0xD7FFA8 : promisedSatisfied ? 0xffcd75 : 0xFFBFA8)
			.add(CreateLang.text(promised == 0 ? "" : "\u23F6"))
			.add(CreateLang.text("/")
				.style(ChatFormatting.WHITE))
			.add(CreateLang.text(count + stacks + "  ")
				.color(0xF1EFE8))
			.component();
	}

	@Override
	public int netId() {
		return 2 + slot.ordinal();
	}

	@Override
	public boolean isCountVisible() {
		return !getFilter().isEmpty();
	}

	@Override
	public BehaviourType<?> getType() {
		return getTypeForSlot(slot);
	}

	public static BehaviourType<?> getTypeForSlot(PanelSlot slot) {
		return switch (slot) {
			case BOTTOM_LEFT -> BOTTOM_LEFT;
			case TOP_LEFT -> TOP_LEFT;
			case TOP_RIGHT -> TOP_RIGHT;
			case BOTTOM_RIGHT -> BOTTOM_RIGHT;
		};
	}

	@OnlyIn(value = Dist.CLIENT)
	public void displayScreen(Player player) {
		if (player instanceof LocalPlayer)
			ScreenOpener.open(new FactoryPanelScreen(this));
	}

	public int getIngredientStatusColor() {
		return count == 0 || isMissingAddress() || redstonePowered ? 0x888898
			: waitingForNetwork ? 0x5B3B3B : satisfied ? 0x9EFF7F : promisedSatisfied ? 0x22AFAF : 0x3D6EBD;
	}

	@Override
	public ItemRequirement getRequiredItems() {
		return isActive() ? new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, AllBlocks.FACTORY_GAUGE.asItem())
			: ItemRequirement.NONE;
	}

	@Override
	public boolean canShortInteract(ItemStack toApply) {
		return true;
	}

	@Override
	public boolean readFromClipboard(@NotNull HolderLookup.Provider registries, CompoundTag tag, Player player, Direction side, boolean simulate) {
		return false;
	}

	@Override
	public boolean writeToClipboard(@NotNull HolderLookup.Provider registries, CompoundTag tag, Direction side) {
		return false;
	}

	private void tickOutline() {
		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> LogisticallyLinkedClientHandler.tickPanel(this));
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return FactoryPanelSetItemMenu.create(containerId, playerInventory, this);
	}

	@Override
	public Component getDisplayName() {
		return blockEntity.getBlockState()
			.getBlock()
			.getName();
	}

	public String getFrogAddress() {
		PackagerBlockEntity packager = panelBE().getRestockedPackager();
		if (packager == null)
			return null;
		if (packager.getLevel().getBlockEntity(packager.getBlockPos().above()) instanceof FrogportBlockEntity fpbe)
			if (fpbe.addressFilter != null && !fpbe.addressFilter.isBlank())
				return fpbe.addressFilter + "";
		return null;
	}

}
