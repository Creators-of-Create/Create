package com.simibubi.create.modules.logistics.management.controller;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.block.IWithContainer;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.type.CombinedCountedItemsList;
import com.simibubi.create.foundation.type.CountedItemsList;
import com.simibubi.create.foundation.type.CountedItemsList.ItemStackEntry;
import com.simibubi.create.modules.logistics.item.CardboardBoxItem;
import com.simibubi.create.modules.logistics.management.LogisticalNetwork;
import com.simibubi.create.modules.logistics.management.base.LogisticalCasingTileEntity;
import com.simibubi.create.modules.logistics.management.base.LogisticalControllerBlock;
import com.simibubi.create.modules.logistics.management.base.LogisticalControllerTileEntity;
import com.simibubi.create.modules.logistics.management.base.LogisticalTask;
import com.simibubi.create.modules.logistics.management.base.LogisticalTask.DepositTask;
import com.simibubi.create.modules.logistics.management.base.LogisticalTask.SupplyTask;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public abstract class LogisticalInventoryControllerTileEntity extends LogisticalControllerTileEntity
		implements IWithContainer<LogisticalInventoryControllerTileEntity, LogisticalInventoryControllerContainer> {

	protected Map<BlockPos, ConnectedInventory> observedInventories = new HashMap<>();
	protected Map<IItemHandler, ConnectedInventory> inventoryByHandler = new HashMap<>();
	protected CombinedCountedItemsList<IItemHandler> allItems = new CombinedCountedItemsList<>();
	protected boolean inventorySetDirty;

	protected LazyOptional<IItemHandler> shippingInventory;
	protected boolean tryInsertBox;

	public boolean isActive;

	public LogisticalInventoryControllerTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		this.shippingInventory = LazyOptional.of(this::createInventory);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		if (compound.contains("ShippingInventory")) {
			ShippingInventory inv = (ShippingInventory) shippingInventory.orElse(null);
			inv.deserializeNBT(compound.getCompound("ShippingInventory"));
		}
		isActive = compound.getBoolean("Active");
	}

	public void inventoryChanged(BlockPos pos) {
		removeInvalidatedInventories();
		TileEntity invTE = world.getTileEntity(pos);
		if (invTE == null)
			return;
		if (invTE instanceof LogisticalCasingTileEntity)
			return;

		LazyOptional<IItemHandler> inventory = invTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (!observedInventories.containsKey(pos)) {
			addInventory(pos, inventory);
			notifyIndexers(observedInventories.get(pos).countedItems);
		} else {
			if (inventoryByHandler.containsKey(inventory.orElse(null))) {
				List<ItemStackEntry> localChanges = observedInventories.get(pos).refresh();
				CountedItemsList changes = new CountedItemsList();
				for (ItemStackEntry entry : localChanges)
					changes.add(entry.stack, allItems.get().getItemCount(entry.stack));
				notifyIndexers(changes);
			}
		}
		checkTasks = true;
		tryInsertBox = true;
	}

	public void detachInventory(BlockPos pos) {
		observedInventories.remove(pos);
		inventorySetDirty = true;
	}

	public void addInventory(BlockPos pos, LazyOptional<IItemHandler> inventory) {
		observedInventories.put(pos, new ConnectedInventory(inventory));
		inventorySetDirty = true;
	}

	protected void notifyIndexers(CountedItemsList updates) {
		if (network == null)
			return;
		network.indexers.forEach(indexer -> indexer.handleUpdatedController(address, updates));
	}

	@Override
	public void tick() {
		super.tick();

		if (taskCooldown > 0 || world.isRemote)
			return;

		if (tryInsertBox) {
			tryInsertBox = false;
			tryInsertBox();
		}

		if (checkTasks) {
			checkTasks = false;
			if (getNetwork() == null)
				return;
			checkTasks();
		}

		taskCooldown = COOLDOWN;
	}

	private void tryInsertBox() {
		if (!canReceive())
			return;

		ShippingInventory inventory = getInventory();
		ItemStack box = inventory.getStackInSlot(ShippingInventory.RECEIVING);
		if (box.isEmpty())
			return;
		List<ItemStack> contents = CardboardBoxItem.getContents(box);
		if (InsertAll(contents, true)) {
			ItemStack copy = box.copy();
			copy.shrink(1);
			inventory.setStackInSlot(ShippingInventory.RECEIVING, copy);
			InsertAll(contents, false);
		}
	}

	public boolean InsertAll(List<ItemStack> stacks, boolean simulate) {
		removeInvalidatedInventories();
		for (ItemStack stack : stacks) {
			ItemStack toInsert = stack.copy();

			InventoryScan: for (IItemHandler inv : getObservedInventories()) {
				for (int slot = 0; slot < inv.getSlots(); slot++) {
					toInsert = inv.insertItem(slot, stack, simulate);
					if (toInsert.isEmpty())
						break InventoryScan;
				}
			}
			if (!toInsert.isEmpty())
				return false;
		}
		return true;
	}

	private void removeInvalidatedInventories() {
		observedInventories.keySet().stream().filter(key -> !observedInventories.get(key).itemHandler.isPresent())
				.collect(Collectors.toList()).forEach(this::detachInventory);
	}

	@Override
	protected void initialize() {
		super.initialize();
		BlockPos start = pos.offset(getBlockState().get(FACING).getOpposite());
		List<BlockPos> toUpdate = LogisticalControllerBlock.collectCasings(world, start);
		for (BlockPos blockPos : toUpdate) {
			world.updateComparatorOutputLevel(blockPos, world.getBlockState(blockPos).getBlock());

			for (Direction face : Direction.values()) {
				BlockPos neighbour = blockPos.offset(face);
				BlockState invState = world.getBlockState(neighbour);
				if (!invState.hasTileEntity())
					continue;
				TileEntity invTE = world.getTileEntity(neighbour);
				if (invTE instanceof LogisticalCasingTileEntity)
					continue;

				LazyOptional<IItemHandler> inventory = invTE
						.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
				if (!inventory.isPresent())
					continue;
				addInventory(neighbour, inventory);
				taskCooldown = COOLDOWN;
				checkTasks = true;
			}
		}
	}

	private void checkTasks() {
		for (Iterator<LogisticalTask> iterator = getNetwork().internalTaskQueue.iterator(); iterator.hasNext();) {
			LogisticalTask task = iterator.next();

			if (canSupply() && task instanceof SupplyTask) {
				List<Pair<Ingredient, Integer>> items = ((SupplyTask) task).items;
				if (findItems(items, true) == null)
					continue;

				List<ItemStack> collectedStacks = findItems(items, false);
				getInventory().createPackage(collectedStacks, task.targetAddress);
				iterator.remove();
				checkTasks = true;
				return;
			}

			if (canReceive() && task instanceof DepositTask) {

			}
		}
	}

	public List<ItemStack> findItems(List<Pair<Ingredient, Integer>> items, boolean simulate) {
		removeInvalidatedInventories();
		List<ItemStack> foundItems = new ArrayList<>();

		// Over Requested Ingredients
		for (Pair<Ingredient, Integer> pair : items) {
			int amountLeft = pair.getValue();

			// Over Attached inventories
			InventoryScan: for (IItemHandler inv : getObservedInventories()) {

				// Over Slots
				for (int slot = 0; slot < inv.getSlots(); slot++) {
					ItemStack stackInSlot = inv.getStackInSlot(slot);
					if (!pair.getKey().test(stackInSlot))
						continue;

					ItemStack extracted = inv.extractItem(slot, amountLeft, simulate);
					amountLeft -= extracted.getCount();
					ItemHelper.addToList(extracted, foundItems);

					if (amountLeft == 0)
						break InventoryScan;
				}
			}
			if (amountLeft > 0)
				return null;
		}
		return foundItems;
	}

	public CountedItemsList getAllItems() {
		if (inventorySetDirty)
			refreshItemHandlerSet();
		return allItems.get();
	}

	public Collection<IItemHandler> getObservedInventories() {
		if (inventorySetDirty)
			refreshItemHandlerSet();
		return inventoryByHandler.keySet();
	}

	public void refreshItemHandlerSet() {
		inventorySetDirty = false;
		inventoryByHandler.clear();
		allItems.clear();
		observedInventories.forEach((pos, connectedInventory) -> {
			if (connectedInventory.itemHandler.isPresent()) {
				IItemHandler inv = connectedInventory.itemHandler.orElse(null);
				for (IItemHandler iItemHandler : inventoryByHandler.keySet()) {
					if (ItemHelper.isSameInventory(iItemHandler, inv))
						return;
				}
				inventoryByHandler.put(inv, connectedInventory);
				allItems.add(inv, connectedInventory.countedItems);
			}
		});

	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		shippingInventory.ifPresent(inv -> compound.put("ShippingInventory", ((ShippingInventory) inv).serializeNBT()));
		compound.putBoolean("Active", isActive);
		return super.write(compound);
	}

	@Override
	public void remove() {
		shippingInventory.invalidate();
		super.remove();
	}

	public <T> LazyOptional<T> getCasingCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return shippingInventory.cast();
		return LazyOptional.empty();
	}

	protected boolean canSupply() {
		return isSupplier() && getInventory().canCreatePackage();
	}

	protected boolean canReceive() {
		return isReceiver();
	}

	public ShippingInventory getInventory() {
		return (ShippingInventory) shippingInventory.orElse(null);
	}

	public class ConnectedInventory {
		LazyOptional<IItemHandler> itemHandler;
		CountedItemsList countedItems;

		public ConnectedInventory(LazyOptional<IItemHandler> inv) {
			itemHandler = inv;
			countedItems = makeList(inv);
		}

		public CountedItemsList makeList(LazyOptional<IItemHandler> inv) {
			return inv.isPresent() ? new CountedItemsList(inv.orElse(null)) : new CountedItemsList();
		}

		public List<ItemStackEntry> refresh() {
			CountedItemsList newList = makeList(itemHandler);
			List<ItemStackEntry> stacksToUpdate = countedItems.getStacksToUpdate(newList);
			countedItems = newList;
			allItems.add(itemHandler.orElse(null), countedItems);
			return stacksToUpdate;
		}
	}

	@Override
	public IContainerFactory<LogisticalInventoryControllerTileEntity, LogisticalInventoryControllerContainer> getContainerFactory() {
		return LogisticalInventoryControllerContainer::new;
	}

	@Override
	public void sendToContainer(PacketBuffer buffer) {
		IWithContainer.super.sendToContainer(buffer);
	}

	protected abstract ShippingInventory createInventory();

	public class ShippingInventory extends ItemStackHandler {

		public static final int SHIPPING = 0;
		public static final int RECEIVING = 1;
		public static final int FILTER = 2;
		int filterAmount = 0;

		boolean ships;
		boolean receives;

		public ShippingInventory(boolean ships, boolean receives) {
			super(3);
			this.ships = ships;
			this.receives = receives;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			if (slot == FILTER)
				return true;
			if (slot == RECEIVING && receives)
				return stack.getItem() instanceof CardboardBoxItem && CardboardBoxItem.matchAddress(stack, address);
			return false;
		}

		public boolean canCreatePackage() {
			return getStackInSlot(SHIPPING).isEmpty() && ships;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (slot == RECEIVING)
				return ItemStack.EMPTY;
			return super.extractItem(slot, amount, simulate);
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			if (slot == FILTER) {
				stack = stack.copy();
				stack.setCount(1);
			}
			super.setStackInSlot(slot, stack);
		}

		public void createPackage(List<ItemStack> contents, String address) {
			ItemStack box = CardboardBoxItem.containing(contents);
			CardboardBoxItem.addAddress(box, address);
			setStackInSlot(SHIPPING, box);
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			markDirty();
			boolean empty = getStackInSlot(slot).isEmpty();

			if (slot == RECEIVING && !empty)
				tryInsertBox = true;

			if (slot == RECEIVING && empty) {
				if (getNetwork() != null) {
					getNetwork().packageTargets.forEach(target -> {
						if (target.getAddressList().stream()
								.anyMatch(e -> LogisticalNetwork.matchAddresses(e, address)))
							target.slotOpened();
					});
				}
			}

			if (slot == SHIPPING && empty)
				checkTasks = true;

			BlockPos start = pos.offset(getBlockState().get(FACING).getOpposite());
			List<BlockPos> toUpdate = LogisticalControllerBlock.collectCasings(world, start);
			for (BlockPos blockPos : toUpdate) {
				TileEntity tileEntity = world.getTileEntity(blockPos);
				if (tileEntity == null)
					continue;
				tileEntity.getWorld().updateComparatorOutputLevel(blockPos, tileEntity.getBlockState().getBlock());
			}
		}

		@Override
		public CompoundNBT serializeNBT() {
			CompoundNBT tag = super.serializeNBT();
			tag.putBoolean("Ships", ships);
			tag.putBoolean("Receives", receives);
			tag.putInt("FilterAmount", filterAmount);
			return tag;
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt) {
			ships = nbt.getBoolean("Ships");
			receives = nbt.getBoolean("Receives");
			filterAmount = nbt.getInt("FilterAmount");
			super.deserializeNBT(nbt);
		}
	}

}
