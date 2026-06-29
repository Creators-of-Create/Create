package com.simibubi.create.compat.computercraft.implementation.luaObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.compat.computercraft.implementation.ComputerUtil;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.ItemStackHandler;

public class PackageLuaObject implements LuaComparable {
	public final PackagerBlockEntity blockEntity;
	public final ItemStack box;
	public String address;
	// address is the only mutable data of the package.
	// we update this along with .setAddress().
	// if the package changes address for any other reason, we don't know that.

	public PackageLuaObject(PackagerBlockEntity blockEntity, ItemStack box) {
		this.blockEntity = blockEntity;
		this.box = box;
		this.address = PackageItem.getAddress(box);
	}

	@LuaFunction(mainThread = true)
	public final boolean isEditable() {
		return (blockEntity != null && !blockEntity.heldBox.isEmpty() && blockEntity.heldBox == box);
	}

	@LuaFunction(mainThread = true)
	public final String getAddress() throws LuaException {
		if (isEditable())
			this.address = PackageItem.getAddress(box);
		return this.address;
	}

	@LuaFunction(mainThread = true)
	public final void setAddress(String argument) throws LuaException {
		if (!isEditable())
			throw new LuaException("Package is not editable");
		PackageItem.addAddress(box, argument);
		this.address = argument;
	}

	@LuaFunction(mainThread = true)
	public Map<Integer, Map<String, ?>> list() {
		return ComputerUtil.list(PackageItem.getContents(box));
	}

	@LuaFunction(mainThread = true)
	public Map<String, ?> getItemDetail(int slot) throws LuaException {
		return ComputerUtil.getItemDetail(PackageItem.getContents(box), slot);
	}

	public boolean hasOrderData() {
		return PackageItem.hasOrderData(box);
	}

	@LuaFunction(mainThread = true)
	public final PackageOrderLuaObject getOrderData() throws LuaException {

		if (!hasOrderData())
			return null;

		return new PackageOrderLuaObject(this);
	}

	public final List<LuaItemStack> getLuaItemStacks() {
		ItemStackHandler results = PackageItem.getContents(box);
		List<LuaItemStack> result = new ArrayList<>();

		for (int i = 0; i < results.getSlots(); i++) {
			ItemStack stack = results.getStackInSlot(i);
			if (!stack.isEmpty()) {
				result.add(new LuaItemStack(stack));
			}
		}

		return result;
	}

	@Override
	public Map<?, ?> getTableRepresentation() {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("address", getAddress());
			// Lazy getter so we don't need to get the contents if we don't need to
			map.put("contents", getLuaItemStacks());

			if (hasOrderData())
				map.put("orderData", getOrderData());
			return map;

		} catch (LuaException e) {
			return null; // Should never happen
		}
	}
}
