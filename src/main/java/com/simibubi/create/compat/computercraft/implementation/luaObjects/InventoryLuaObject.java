package com.simibubi.create.compat.computercraft.implementation.luaObjects;

import java.util.Collections;
import java.util.Map;

import com.simibubi.create.compat.computercraft.implementation.ComputerUtil;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * A read-only view of the inventory attached to the packager (the work inventory behind it).
 * Returned by {@code PackagerPeripheral#getInventory()}.
 */
public class InventoryLuaObject {
	private final PackagerBlockEntity blockEntity;

	public InventoryLuaObject(PackagerBlockEntity blockEntity) {
		this.blockEntity = blockEntity;
	}

	@LuaFunction(mainThread = true)
	public final Map<Integer, Map<String, ?>> list() {
		IItemHandler inventory = blockEntity.targetInventory.getInventory();
		if (inventory == null)
			return Collections.emptyMap();
		return ComputerUtil.list(inventory);
	}

	@LuaFunction(mainThread = true)
	public final Map<String, ?> getItemDetail(int slot) throws LuaException {
		IItemHandler inventory = blockEntity.targetInventory.getInventory();
		if (inventory == null)
			throw new LuaException("No inventory attached to the packager");
		return ComputerUtil.getItemDetail(inventory, slot);
	}
}
