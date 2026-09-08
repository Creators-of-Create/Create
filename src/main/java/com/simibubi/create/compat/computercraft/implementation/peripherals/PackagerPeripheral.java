package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.compat.computercraft.implementation.ComputerUtil;
import com.simibubi.create.compat.computercraft.implementation.luaObjects.InventoryLuaObject;
import com.simibubi.create.compat.computercraft.implementation.luaObjects.PackageLuaObject;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;

import com.simibubi.create.compat.computercraft.events.ComputerEvent;
import com.simibubi.create.compat.computercraft.events.PackageEvent;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class PackagerPeripheral extends SyncedPeripheral<PackagerBlockEntity> {

	public PackagerPeripheral(PackagerBlockEntity blockEntity) {
		super(blockEntity);
	}

	@Override
	public void attach(@NotNull IComputerAccess computer) {
		super.attach(computer);
		// Ephemeral nature of address, should not be set on load until a computer
		// explicitly calls setAddress again on the BE.
		blockEntity.hasCustomComputerAddress = false;
	}

	@Override
	public void detach(@NotNull IComputerAccess computer) {
		super.detach(computer);
		// Ephemeral nature of address, should not be set on load until a computer
		// explicitly calls setAddress again on the BE.
		blockEntity.hasCustomComputerAddress = false;
	}

	@LuaFunction(mainThread = true)
	public final boolean makePackage() {
		if (!blockEntity.heldBox.isEmpty())
			return false;
		blockEntity.activate();
		if (blockEntity.heldBox.isEmpty())
			return false;
		return true;
	}

	@LuaFunction(mainThread = true)
	public Map<Integer, Map<String, ?>> list() {
		return ComputerUtil.list(blockEntity.inventory);
	}

	@LuaFunction(mainThread = true)
	public Map<String, ?> getItemDetail(int slot) throws LuaException {
		return ComputerUtil.getItemDetail(blockEntity.inventory, slot);
	}

	@LuaFunction(mainThread = true)
	public final String getAddress() {
		blockEntity.updateSignAddress();
		return blockEntity.signBasedAddress;
	}

	@LuaFunction(mainThread = true)
	public final void setAddress(Optional<String> argument) {
		if (argument.isPresent()) {
			blockEntity.customComputerAddress = argument.get();
			blockEntity.signBasedAddress = argument.get();
			blockEntity.hasCustomComputerAddress = true;
		} else {
			blockEntity.customComputerAddress = "";
			blockEntity.hasCustomComputerAddress = false;
		}
	}

	@LuaFunction(mainThread = true)
	public final PackageLuaObject getPackage() {
		ItemStack box = blockEntity.heldBox;
		if (box.isEmpty())
			return null;
		return new PackageLuaObject(blockEntity, box);
	}

	/**
	 * Get a read-only view of the inventory attached to the packager (the work inventory behind it).
	 * The returned object exposes {@code list()} and {@code getItemDetail(slot)}.
	 */
	@LuaFunction(mainThread = true)
	public final InventoryLuaObject getInventory() {
		return new InventoryLuaObject(blockEntity);
	}

	/**
	 * Move a box out of the packager into another inventory on the same wired network.
	 * Mirrors the generic inventory API that existed before the custom package API replaced it.
	 *
	 * @param computer The computer calling this method (injected by CC:Tweaked)
	 * @param toName   The name of the peripheral to push to
	 * @param fromSlot The slot in the packager (always 1) to move from
	 * @param limit    The maximum number of items to move
	 * @param toSlot   The slot in the target inventory to move to
	 */
	@LuaFunction(mainThread = true)
	public final int pushItems(IComputerAccess computer, String toName, int fromSlot, Optional<Integer> limit,
		Optional<Integer> toSlot) throws LuaException {
		IItemHandler from = blockEntity.inventory;
		IPeripheral target = computer.getAvailablePeripheral(toName);
		if (target == null)
			throw new LuaException("Target '" + toName + "' does not exist");
		IItemHandler to = extractHandler(target);
		if (to == null)
			throw new LuaException("Target '" + toName + "' is not an inventory");

		int actualLimit = limit.orElse(Integer.MAX_VALUE);
		if (fromSlot < 1 || fromSlot > from.getSlots())
			throw new LuaException("From slot out of range");
		if (toSlot.isPresent() && (toSlot.get() < 1 || toSlot.get() > to.getSlots()))
			throw new LuaException("To slot out of range");
		if (actualLimit <= 0)
			return 0;

		return moveItem(from, fromSlot - 1, to, toSlot.orElse(0) - 1, actualLimit);
	}

	/**
	 * Move a box from another inventory on the same wired network into the packager, to be unpacked.
	 * Mirrors the generic inventory API that existed before the custom package API replaced it.
	 *
	 * @param computer The computer calling this method (injected by CC:Tweaked)
	 * @param fromName The name of the peripheral to pull from
	 * @param fromSlot The slot in the source inventory to move from
	 * @param limit    The maximum number of items to move
	 * @param toSlot   The slot in the packager (always 1) to move to
	 */
	@LuaFunction(mainThread = true)
	public final int pullItems(IComputerAccess computer, String fromName, int fromSlot, Optional<Integer> limit,
		Optional<Integer> toSlot) throws LuaException {
		IPeripheral source = computer.getAvailablePeripheral(fromName);
		if (source == null)
			throw new LuaException("Source '" + fromName + "' does not exist");
		IItemHandler from = extractHandler(source);
		if (from == null)
			throw new LuaException("Source '" + fromName + "' is not an inventory");
		IItemHandler to = blockEntity.inventory;

		int actualLimit = limit.orElse(Integer.MAX_VALUE);
		if (fromSlot < 1 || fromSlot > from.getSlots())
			throw new LuaException("From slot out of range");
		if (toSlot.isPresent() && (toSlot.get() < 1 || toSlot.get() > to.getSlots()))
			throw new LuaException("To slot out of range");
		if (actualLimit <= 0)
			return 0;

		return moveItem(from, fromSlot - 1, to, toSlot.orElse(0) - 1, actualLimit);
	}

	/**
	 * Resolve the item handler exposed by another peripheral on the network, mirroring CC:Tweaked's own logic.
	 */
	@Nullable
	private static IItemHandler extractHandler(IPeripheral peripheral) {
		Object target = peripheral.getTarget();

		if (target instanceof BlockEntity blockEntity) {
			if (blockEntity.isRemoved())
				return null;
			Level level = blockEntity.getLevel();
			if (level == null)
				return null;
			IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, blockEntity.getBlockPos(),
				blockEntity.getBlockState(), blockEntity, null);
			if (handler != null)
				return handler;
		}

		if (target instanceof IItemHandler handler)
			return handler;
		if (target instanceof Container container)
			return new InvWrapper(container);
		return null;
	}

	private static int moveItem(IItemHandler from, int fromSlot, IItemHandler to, int toSlot, int limit) {
		ItemStack stack = from.extractItem(fromSlot, limit, true);
		if (stack.isEmpty())
			return 0;

		ItemStack leftover = toSlot >= 0 ? to.insertItem(toSlot, stack, false)
			: ItemHandlerHelper.insertItemStacked(to, stack, false);
		int moved = stack.getCount() - leftover.getCount();
		if (moved > 0)
			from.extractItem(fromSlot, moved, false);
		return moved;
	}

	@Override
	public void prepareComputerEvent(@NotNull ComputerEvent event) {
		if (event instanceof PackageEvent pe) {
			queueEvent(pe.status, new PackageLuaObject(blockEntity, pe.box));
		}
	}

	@NotNull
	@Override
	public Object getTarget() {
		return blockEntity;
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_Packager";
	}

}
