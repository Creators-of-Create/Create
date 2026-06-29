package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts.CraftingEntry;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class RedstoneRequesterPeripheral extends SyncedPeripheral<RedstoneRequesterBlockEntity> {

	public RedstoneRequesterPeripheral(RedstoneRequesterBlockEntity blockEntity) {
		super(blockEntity);
	}

	@LuaFunction(mainThread = true)
	public final void request() throws LuaException {
		blockEntity.triggerRequest();
	}

	@LuaFunction(mainThread = true)
	public final void setRequest(IArguments arguments) throws LuaException {
		List<BigItemStack> orderStacks = generateOrder(arguments);
		this.blockEntity.encodedRequest = PackageOrderWithCrafts.simple(orderStacks);
		this.blockEntity.notifyUpdate();
	}

	@LuaFunction(mainThread = true)
	public final void setCraftingRequest(IArguments arguments) throws LuaException {
		int count = arguments.getInt(0);
		arguments = arguments.drop(1);

		List<BigItemStack> orderStacks = generateOrder(arguments);

		PackageOrder order = new PackageOrder(orderStacks);
		CraftingEntry orderContext = new CraftingEntry(new PackageOrder(orderStacks.stream()
			.map(stack -> new BigItemStack(stack.stack.copyWithCount(1)))
			.toList()), count);

		this.blockEntity.encodedRequest = new PackageOrderWithCrafts(order, List.of(orderContext));
		this.blockEntity.notifyUpdate();
	}

	@LuaFunction(mainThread = true)
	public final Map<Integer, Map<String, ?>> getRequest() throws LuaException {
		List<BigItemStack> stacks = blockEntity.encodedRequest.stacks();
		Map<Integer, Map<String, ?>> result = new HashMap<>();
		// Loop through the packageOrder get each bigItem stack
		for (int i = 0; i < stacks.size(); i++) {
			ItemStack stack = stacks.get(i).stack;
			Map<String, Object> details = new HashMap<>(
				VanillaDetailRegistries.ITEM_STACK.getDetails(stack));
			if (!details.get("name").equals("minecraft:air")) {
				details.put("count", stacks.get(i).count);
				result.put(i + 1, details); // +1 because lua
			}
		}
		return result;
	}

	@LuaFunction(mainThread = true)
	public final String getConfiguration() throws LuaException {
		if (blockEntity.allowPartialRequests)
			return "allow_partial";
		else
			return "strict";
	}

	@LuaFunction(mainThread = true)
	public void setConfiguration(String config) throws LuaException {
		if (config.equals("allow_partial")) {
			blockEntity.allowPartialRequests = true;
			blockEntity.notifyUpdate();
			return;
		}
		if (config.equals("strict")) {
			blockEntity.allowPartialRequests = false;
			blockEntity.notifyUpdate();
			return;
		}
		throw new LuaException("Unknown configuration: \"" + config
			+ "\" Possible configurations are: \"allow_partial\" and \"strict\".");
	}

	@LuaFunction(mainThread = true)
	public final void setAddress(String address) throws LuaException {
		blockEntity.encodedTargetAdress = address;
		this.blockEntity.notifyUpdate();
	}

	@LuaFunction(mainThread = true)
	public final String getAddress() throws LuaException {
		return blockEntity.encodedTargetAdress;
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_RedstoneRequester";
	}

	private List<BigItemStack> generateOrder(IArguments arguments) throws LuaException {
		ArrayList<BigItemStack> list = new ArrayList<>();

		for (int i = 0; i < 9; i++) {
			if (arguments.get(i) == null) {
				list.add(new BigItemStack(ItemStack.EMPTY, 1));
			} else {
				Object arg = arguments.get(i);
				if (arg instanceof String itemName) {
					ResourceLocation resourceLocation = ResourceLocation.tryParse(itemName);
					ItemLike item = BuiltInRegistries.ITEM.get(resourceLocation);
					list.add(new BigItemStack(new ItemStack(item), 1));
				} else if (arg instanceof Map<?, ?> itemData) {
					String itemName = "minecraft:air";
					if (itemData.get("name") instanceof String) {
						itemName = (String) itemData.get("name");
					}
					int count = 1;
					if (itemData.get("count") instanceof Number) {
						Object countObj = itemData.get("count");
						count = (countObj instanceof Number) ? ((Number) countObj).intValue() : 1;
						if (count > 256)
							throw new LuaException("Count for item " + itemName + " exceeds 256");
					}
					ResourceLocation resourceLocation = ResourceLocation.tryParse(itemName);
					ItemLike item = BuiltInRegistries.ITEM.get(resourceLocation);
					list.add(new BigItemStack(new ItemStack(item), count));
				}
			}
		}

		return list;
	}
}
