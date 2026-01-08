package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class FactoryGaugePeripheral extends SyncedPeripheral<FactoryPanelBlockEntity> {

	public FactoryGaugePeripheral(FactoryPanelBlockEntity blockEntity) {
		super(blockEntity);
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_FactoryGauge";
	}

	// === PANEL SELECTION ===

	@Nullable
	private FactoryPanelBehaviour getPanelBehaviour(Optional<String> slotName) throws LuaException {
		if (slotName.isPresent()) {
			PanelSlot slot;
			try {
				slot = PanelSlot.valueOf(slotName.get().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new LuaException("Invalid slot name. Use: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT");
			}
			FactoryPanelBehaviour panel = blockEntity.panels.get(slot);
			if (panel == null || !panel.isActive()) {
				return null;
			}
			return panel;
		}

		// Find first active panel
		for (FactoryPanelBehaviour panel : blockEntity.panels.values()) {
			if (panel.isActive()) {
				return panel;
			}
		}
		return null;
	}

	// === READ METHODS ===

	@LuaFunction(mainThread = true)
	public final List<String> getActivePanels() {
		List<String> result = new ArrayList<>();
		for (Map.Entry<PanelSlot, FactoryPanelBehaviour> entry : blockEntity.panels.entrySet()) {
			if (entry.getValue().isActive()) {
				result.add(entry.getKey().name());
			}
		}
		return result;
	}

	@LuaFunction(mainThread = true)
	public final int getActivePanelCount() {
		return blockEntity.activePanels();
	}

	@LuaFunction(mainThread = true)
	public final @Nullable Map<String, Object> getFilter(Optional<String> slot) throws LuaException {
		FactoryPanelBehaviour panel = getPanelBehaviour(slot);
		if (panel == null) return null;

		ItemStack filter = panel.getFilter();
		if (filter.isEmpty()) return null;

		Map<String, Object> result = new HashMap<>(VanillaDetailRegistries.ITEM_STACK.getBasicDetails(filter));
		return result;
	}

	@LuaFunction(mainThread = true)
	public final @Nullable Map<String, Object> getThreshold(Optional<String> slot) throws LuaException {
		FactoryPanelBehaviour panel = getPanelBehaviour(slot);
		if (panel == null) return null;

		Map<String, Object> result = new HashMap<>();
		result.put("count", panel.getAmount());
		result.put("mode", panel.upTo ? "items" : "stacks");
		return result;
	}

	@LuaFunction(mainThread = true)
	public final boolean isThresholdMet(Optional<String> slot) throws LuaException {
		FactoryPanelBehaviour panel = getPanelBehaviour(slot);
		if (panel == null) return false;
		return panel.satisfied;
	}

	@LuaFunction(mainThread = true)
	public final boolean isPromisedMet(Optional<String> slot) throws LuaException {
		FactoryPanelBehaviour panel = getPanelBehaviour(slot);
		if (panel == null) return false;
		return panel.promisedSatisfied;
	}

	@LuaFunction(mainThread = true)
	public final @Nullable Map<String, Object> getStatus(Optional<String> slot) throws LuaException {
		FactoryPanelBehaviour panel = getPanelBehaviour(slot);
		if (panel == null) return null;

		Map<String, Object> result = new HashMap<>();
		result.put("active", panel.isActive());
		result.put("satisfied", panel.satisfied);
		result.put("promisedSatisfied", panel.promisedSatisfied);
		result.put("waitingForNetwork", panel.waitingForNetwork);
		result.put("redstonePowered", panel.redstonePowered);
		result.put("levelInStorage", panel.getLevelInStorage());
		result.put("promised", panel.getPromised());
		result.put("address", panel.recipeAddress);
		result.put("recipeOutput", panel.recipeOutput);
		result.put("isRestocker", blockEntity.restocker);
		return result;
	}

	@LuaFunction(mainThread = true)
	public final int getLevelInStorage(Optional<String> slot) throws LuaException {
		FactoryPanelBehaviour panel = getPanelBehaviour(slot);
		if (panel == null) return 0;
		return panel.getLevelInStorage();
	}

	@LuaFunction(mainThread = true)
	public final int getPromised(Optional<String> slot) throws LuaException {
		FactoryPanelBehaviour panel = getPanelBehaviour(slot);
		if (panel == null) return 0;
		return panel.getPromised();
	}

	@LuaFunction(mainThread = true)
	public final @Nullable String getAddress(Optional<String> slot) throws LuaException {
		FactoryPanelBehaviour panel = getPanelBehaviour(slot);
		if (panel == null) return null;
		return panel.recipeAddress;
	}

	@LuaFunction(mainThread = true)
	public final @Nullable String getNetwork(Optional<String> slot) throws LuaException {
		FactoryPanelBehaviour panel = getPanelBehaviour(slot);
		if (panel == null) return null;
		return panel.network != null ? panel.network.toString() : null;
	}

	// === WRITE METHODS ===

	@LuaFunction(mainThread = true)
	public final boolean setFilter(IArguments args) throws LuaException {
		String slotName = args.optString(1, null);
		Optional<String> slot = Optional.ofNullable(slotName);
		FactoryPanelBehaviour panel = getPanelBehaviour(slot);
		if (panel == null) {
			throw new LuaException("No active panel found");
		}

		Object arg = args.get(0);
		ItemStack stack;

		if (arg == null) {
			stack = ItemStack.EMPTY;
		} else if (arg instanceof String itemId) {
			stack = parseItemString(itemId);
		} else if (arg instanceof Map<?, ?> itemTable) {
			stack = parseItemTable(itemTable);
		} else {
			throw new LuaException("Expected item string, table, or nil");
		}

		return panel.setFilter(stack);
	}

	@LuaFunction(mainThread = true)
	public final void setThreshold(IArguments args) throws LuaException {
		int count = args.getInt(0);
		String mode = args.optString(1, "items");
		String slotName = args.optString(2, null);

		Optional<String> slot = Optional.ofNullable(slotName);
		FactoryPanelBehaviour panel = getPanelBehaviour(slot);
		if (panel == null) {
			throw new LuaException("No active panel found");
		}

		if (count < 0 || count > 100) {
			throw new LuaException("Threshold count must be between 0 and 100");
		}

		if (!mode.equalsIgnoreCase("items") && !mode.equalsIgnoreCase("stacks")) {
			throw new LuaException("Mode must be 'items' or 'stacks'");
		}

		boolean upTo = mode.equalsIgnoreCase("items");
		ValueSettings settings = new ValueSettings(upTo ? 0 : 1, count);
		panel.setValueSettings(null, settings, false);
	}

	@LuaFunction(mainThread = true)
	public final void setAddress(IArguments args) throws LuaException {
		String address = args.getString(0);
		String slotName = args.optString(1, null);

		Optional<String> slot = Optional.ofNullable(slotName);
		FactoryPanelBehaviour panel = getPanelBehaviour(slot);
		if (panel == null) {
			throw new LuaException("No active panel found");
		}

		panel.recipeAddress = address;
		panel.blockEntity.notifyUpdate();
	}

	@LuaFunction(mainThread = true)
	public final void clearPromises(Optional<String> slot) throws LuaException {
		FactoryPanelBehaviour panel = getPanelBehaviour(slot);
		if (panel == null) {
			throw new LuaException("No active panel found");
		}

		panel.forceClearPromises = true;
	}

	// === HELPER METHODS ===

	private ItemStack parseItemString(String itemId) throws LuaException {
		String fullId = itemId.contains(":") ? itemId : "minecraft:" + itemId;
		ResourceLocation id = ResourceLocation.tryParse(fullId);
		if (id == null) {
			throw new LuaException("Invalid item ID format: " + itemId);
		}

		Item item = BuiltInRegistries.ITEM.get(id);
		if (item == Items.AIR) {
			throw new LuaException("Unknown item: " + itemId);
		}

		return new ItemStack(item);
	}

	private ItemStack parseItemTable(Map<?, ?> table) throws LuaException {
		Object nameObj = table.get("name");
		if (!(nameObj instanceof String name)) {
			throw new LuaException("Item table must have a 'name' field");
		}

		ItemStack stack = parseItemString(name);

		Object countObj = table.get("count");
		if (countObj instanceof Number count) {
			stack.setCount(count.intValue());
		}

		return stack;
	}
}
