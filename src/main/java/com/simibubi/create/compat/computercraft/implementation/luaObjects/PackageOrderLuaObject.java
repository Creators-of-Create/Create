package com.simibubi.create.compat.computercraft.implementation.luaObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.compat.computercraft.implementation.CreateLuaTable;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts.CraftingEntry;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.world.item.ItemStack;

public class PackageOrderLuaObject implements LuaComparable {
	private final PackageLuaObject parent;
	private final PackageOrderWithCrafts context;

	public PackageOrderLuaObject(PackageLuaObject packageLuaObject) {
		this.parent = packageLuaObject;
		this.context = PackageItem.getOrderContext(parent.box);
	}

	@LuaFunction(mainThread = true)
	public final int getOrderID() throws LuaException {
		return PackageItem.getOrderId(parent.box);
	}

	@LuaFunction(mainThread = true)
	public final int getIndex() throws LuaException {
		return PackageItem.getIndex(parent.box) + 1;
	}

	@LuaFunction(mainThread = true)
	public final boolean isFinal() throws LuaException {
		return PackageItem.isFinal(parent.box);
	}

	@LuaFunction(mainThread = true)
	public final int getLinkIndex() throws LuaException {
		return PackageItem.getLinkIndex(parent.box) + 1;
	}

	@LuaFunction(mainThread = true)
	public final boolean isFinalLink() throws LuaException {
		return PackageItem.isFinalLink(parent.box);
	}

	// the list  and getItemDetail functions here are hard coded because it's for BigItemStacks. Every other implementation should use ComputerUtils functions.

	@LuaFunction(mainThread = true)
	public final CreateLuaTable list() throws LuaException {
		if (context == null) {
			return null;
		}

		CreateLuaTable stacks = new CreateLuaTable();

		int i = 0;
		for (BigItemStack bis : context.stacks()) {
			i++;
			Map<String, Object> details = new HashMap<>(
				VanillaDetailRegistries.ITEM_STACK.getBasicDetails(bis.stack));
			details.put("count", bis.count); // Use bis count
			stacks.put(i, details);
		}

		return stacks;
	}

	@LuaFunction(mainThread = true)
	public final CreateLuaTable getItemDetail(int slot) throws LuaException {
		if (context == null) {
			return null;
		}

		if (slot < 1) { // All positive can technically be valid
			throw new LuaException("Slot out of range (1 or greater)");
		}

		List<BigItemStack> stacks = context.stacks();
		if (slot > stacks.size()) {
			return null;
		}

		BigItemStack bis = stacks.get(slot - 1);
		Map<String, Object> details = new HashMap<>(
			VanillaDetailRegistries.ITEM_STACK.getDetails(bis.stack));
		details.put("count", bis.count); // Use bis count

		return new CreateLuaTable(details);
	}

	@LuaFunction(mainThread = true)
	public final CreateLuaTable getCrafts() throws LuaException {
		if (context == null) {
			return null;
		}

		CreateLuaTable crafts = new CreateLuaTable();

		int i = 0;
		for (CraftingEntry entry : context.orderedCrafts()) {
			CreateLuaTable craft = new CreateLuaTable();
			craft.put("count", entry.count());

			CreateLuaTable recipe = new CreateLuaTable();
			int j = 0;
			for (BigItemStack bis : entry.pattern().stacks()) {
				j++;
				// Not sure if this is the best way to get the in game ID for the item, if there is please let me know
				String name = VanillaDetailRegistries.ITEM_STACK.getBasicDetails(bis.stack).get("name").toString();
				recipe.put(j, name.equals("minecraft:air") ? null : name);
			}
			i++;
			craft.put("recipe", recipe);
			crafts.put(i, craft);
		}

		return crafts;
	}

	public final List<LuaBigItemStack> getLuaItemStacks() {
		List<LuaBigItemStack> result = new ArrayList<>();

		for (BigItemStack bis : context.stacks()) {
			ItemStack stack = bis.stack;
			if (!stack.isEmpty()) {
				result.add(new LuaBigItemStack(bis));
			}
		}
		return result;
	}

	@Override
	public Map<?, ?> getTableRepresentation() {
		try {
			Map<String, Object> result = new HashMap<>();
			result.put("orderID", getOrderID());
			result.put("index", getIndex());
			result.put("isFinal", isFinal());
			result.put("linkIndex", getLinkIndex());
			result.put("isFinalLink", isFinalLink());
			if (context != null) {
				result.put("stacks", getLuaItemStacks());
				result.put("crafts", getCrafts());
			}
			return result;
		} catch (LuaException e) {
			return null; // Should never happen
		}
	}
}
