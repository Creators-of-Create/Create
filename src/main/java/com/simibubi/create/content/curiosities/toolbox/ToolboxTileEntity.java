package com.simibubi.create.content.curiosities.toolbox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ToolboxTileEntity extends SmartTileEntity implements INamedContainerProvider, INameable {

	public LerpedFloat lid = LerpedFloat.linear()
		.startWithValue(0);

	public LerpedFloat drawers = LerpedFloat.linear()
		.startWithValue(0);

	ToolboxInventory inventory;
	LazyOptional<IItemHandler> inventoryProvider;
	protected int openCount;

	Map<Integer, WeakHashMap<PlayerEntity, Integer>> connectedPlayers;

	private ITextComponent customName;

	public ToolboxTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		connectedPlayers = new HashMap<>();
		inventory = new ToolboxInventory(this);
		inventoryProvider = LazyOptional.of(() -> inventory);
		setLazyTickRate(10);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public void initialize() {
		super.initialize();
		ToolboxHandler.onLoad(this);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		ToolboxHandler.onUnload(this);
	}

	@Override
	public void tick() {
		super.tick();

		if (level.isClientSide)
			tickAudio();
		if (!level.isClientSide)
			tickPlayers();

		lid.chase(openCount > 0 ? 1 : 0, 0.2f, Chaser.LINEAR);
		drawers.chase(openCount > 0 ? 1 : 0, 0.2f, Chaser.EXP);
		lid.tickChaser();
		drawers.tickChaser();
	}

	private void tickPlayers() {
		boolean update = false;

		for (Iterator<Entry<Integer, WeakHashMap<PlayerEntity, Integer>>> toolboxSlots = connectedPlayers.entrySet()
			.iterator(); toolboxSlots.hasNext();) {

			Entry<Integer, WeakHashMap<PlayerEntity, Integer>> toolboxSlotEntry = toolboxSlots.next();
			WeakHashMap<PlayerEntity, Integer> set = toolboxSlotEntry.getValue();
			int slot = toolboxSlotEntry.getKey();

			ItemStack referenceItem = inventory.filters.get(slot);
			boolean clear = referenceItem.isEmpty();

			for (Iterator<Entry<PlayerEntity, Integer>> playerEntries = set.entrySet()
				.iterator(); playerEntries.hasNext();) {
				Entry<PlayerEntity, Integer> playerEntry = playerEntries.next();

				PlayerEntity player = playerEntry.getKey();
				int hotbarSlot = playerEntry.getValue();

				if (!clear && !ToolboxHandler.withinRange(player, this))
					continue;

				ItemStack playerStack = player.inventory.getItem(hotbarSlot);

				if (clear || !playerStack.isEmpty()
					&& !ToolboxInventory.canItemsShareCompartment(playerStack, referenceItem)) {
					player.getPersistentData()
						.getCompound("CreateToolboxData")
						.remove(String.valueOf(hotbarSlot));
					playerEntries.remove();
					if (player instanceof ServerPlayerEntity)
						ToolboxHandler.syncData(player);
					continue;
				}

				int count = playerStack.getCount();
				int targetAmount = (referenceItem.getMaxStackSize() + 1) / 2;

				if (count < targetAmount) {
					int amountToReplenish = targetAmount - count;

					if (isOpenInContainer(player)) {
						ItemStack extracted = inventory.takeFromCompartment(amountToReplenish, slot, true);
						if (!extracted.isEmpty()) {
							ToolboxHandler.unequip(player, hotbarSlot, false);
							ToolboxHandler.syncData(player);
							continue;
						}
					}

					ItemStack extracted = inventory.takeFromCompartment(amountToReplenish, slot, false);
					if (!extracted.isEmpty()) {
						update = true;
						player.inventory.setItem(hotbarSlot,
							ItemHandlerHelper.copyStackWithSize(extracted, count + extracted.getCount()));
					}
				}

				if (count > targetAmount) {
					int amountToDeposit = count - targetAmount;
					ItemStack toDistribute = ItemHandlerHelper.copyStackWithSize(playerStack, amountToDeposit);

					if (isOpenInContainer(player)) {
						int deposited = amountToDeposit - inventory.distributeToCompartment(toDistribute, slot, true)
							.getCount();
						if (deposited > 0) {
							ToolboxHandler.unequip(player, hotbarSlot, true);
							ToolboxHandler.syncData(player);
							continue;
						}
					}

					int deposited = amountToDeposit - inventory.distributeToCompartment(toDistribute, slot, false)
						.getCount();
					if (deposited > 0) {
						update = true;
						player.inventory.setItem(hotbarSlot,
							ItemHandlerHelper.copyStackWithSize(playerStack, count - deposited));
					}
				}
			}

			if (clear)
				toolboxSlots.remove();
		}

		if (update)

			sendData();

	}

	private boolean isOpenInContainer(PlayerEntity player) {
		return player.containerMenu instanceof ToolboxContainer
			&& ((ToolboxContainer) player.containerMenu).contentHolder == this;
	}

	public void unequipTracked() {
		if (level.isClientSide)
			return;

		Set<ServerPlayerEntity> affected = new HashSet<>();

		for (Iterator<Entry<Integer, WeakHashMap<PlayerEntity, Integer>>> toolboxSlots = connectedPlayers.entrySet()
			.iterator(); toolboxSlots.hasNext();) {

			Entry<Integer, WeakHashMap<PlayerEntity, Integer>> toolboxSlotEntry = toolboxSlots.next();
			WeakHashMap<PlayerEntity, Integer> set = toolboxSlotEntry.getValue();

			for (Iterator<Entry<PlayerEntity, Integer>> playerEntries = set.entrySet()
				.iterator(); playerEntries.hasNext();) {
				Entry<PlayerEntity, Integer> playerEntry = playerEntries.next();

				PlayerEntity player = playerEntry.getKey();
				int hotbarSlot = playerEntry.getValue();

				ToolboxHandler.unequip(player, hotbarSlot, false);
				if (player instanceof ServerPlayerEntity)
					affected.add((ServerPlayerEntity) player);
			}
		}

		for (ServerPlayerEntity player : affected)
			ToolboxHandler.syncData(player);
		connectedPlayers.clear();
	}

	public void unequip(int slot, PlayerEntity player, int hotbarSlot, boolean keepItems) {
		if (!connectedPlayers.containsKey(slot))
			return;
		connectedPlayers.get(slot)
			.remove(player);
		if (keepItems)
			return;

		ItemStack playerStack = player.inventory.getItem(hotbarSlot);
		ItemStack remainder = inventory.distributeToCompartment(playerStack, slot, false);

		if (remainder.getCount() != playerStack.getCount())
			player.inventory.setItem(hotbarSlot, remainder);
	}

	private void tickAudio() {
		Vector3d vec = VecHelper.getCenterOf(worldPosition);
		if (lid.settled()) {
			if (openCount > 0 && lid.getChaseTarget() == 0) {
				level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.IRON_DOOR_OPEN, SoundCategory.BLOCKS, 0.25F,
					level.random.nextFloat() * 0.1F + 1.2F, true);
				level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.CHEST_OPEN, SoundCategory.BLOCKS, 0.1F,
					level.random.nextFloat() * 0.1F + 1.1F, true);
			}
			if (openCount == 0 && lid.getChaseTarget() == 1)
				level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.CHEST_CLOSE, SoundCategory.BLOCKS, 0.1F,
					level.random.nextFloat() * 0.1F + 1.1F, true);

		} else if (openCount == 0 && lid.getChaseTarget() == 0 && lid.getValue(0) > 1 / 16f
			&& lid.getValue(1) < 1 / 16f)
			level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 0.25F,
				level.random.nextFloat() * 0.1F + 1.2F, true);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (isItemHandlerCap(cap))
			return inventoryProvider.cast();
		return super.getCapability(cap, side);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		super.fromTag(state, compound, clientPacket);
		if (compound.contains("CustomName", 8))
			this.customName = ITextComponent.Serializer.fromJson(compound.getString("CustomName"));
		if (clientPacket)
			openCount = compound.getInt("OpenCount");
	}

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		compound.put("Inventory", inventory.serializeNBT());
		if (customName != null)
			compound.putString("CustomName", ITextComponent.Serializer.toJson(customName));
		super.write(compound, clientPacket);
		if (clientPacket)
			compound.putInt("OpenCount", openCount);
	}

	@Override
	public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return ToolboxContainer.create(id, inv, this);
	}

	@Override
	public void lazyTick() {
		updateOpenCount();
		// keep re-advertising active TEs
		ToolboxHandler.onLoad(this);
		super.lazyTick();
	}

	void updateOpenCount() {
		if (level.isClientSide)
			return;
		if (openCount == 0)
			return;

		int prevOpenCount = openCount;
		openCount = 0;

		for (PlayerEntity playerentity : level.getEntitiesOfClass(PlayerEntity.class,
			new AxisAlignedBB(worldPosition).inflate(8)))
			if (playerentity.containerMenu instanceof ToolboxContainer
				&& ((ToolboxContainer) playerentity.containerMenu).contentHolder == this)
				openCount++;

		if (prevOpenCount != openCount)
			sendData();
	}

	public void startOpen(PlayerEntity player) {
		if (player.isSpectator())
			return;
		if (openCount < 0)
			openCount = 0;
		openCount++;
		sendData();
	}

	public void stopOpen(PlayerEntity player) {
		if (player.isSpectator())
			return;
		openCount--;
		sendData();
	}

	public void connectPlayer(int slot, PlayerEntity player, int hotbarSlot) {
		if (level.isClientSide)
			return;
		WeakHashMap<PlayerEntity, Integer> map = connectedPlayers.computeIfAbsent(slot, WeakHashMap::new);
		Integer previous = map.get(player);
		if (previous != null) {
			if (previous == hotbarSlot)
				return;
			ToolboxHandler.unequip(player, previous, false);
		}
		map.put(player, hotbarSlot);
	}

	public void readInventory(CompoundNBT compound) {
		inventory.deserializeNBT(compound);
	}

	public void setCustomName(ITextComponent customName) {
		this.customName = customName;
	}

	@Override
	public ITextComponent getDisplayName() {
		return customName != null ? customName : new TranslationTextComponent("block.create.toolbox");
	}

	@Override
	public ITextComponent getCustomName() {
		return customName;
	}

	@Override
	public boolean hasCustomName() {
		return customName != null;
	}

	@Override
	public ITextComponent getName() {
		return customName;
	}

}
