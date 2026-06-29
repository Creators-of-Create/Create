package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.AutoRequestData;
import com.simibubi.create.content.logistics.redstoneRequester.AutoRequestData.Mutable;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class TableClothShopPeripheral extends SyncedPeripheral<TableClothBlockEntity> {

	public TableClothShopPeripheral(TableClothBlockEntity blockEntity) {
		super(blockEntity);
	}

	private void assertShop() throws LuaException {
		if (!blockEntity.isShop())
			throw new LuaException("TableCloth is not a shop!");
	}

	@LuaFunction(mainThread = true)
	public final boolean isShop() {
		return blockEntity.isShop();
	}

	@LuaFunction(mainThread = true)
	public final String getAddress() throws LuaException {
		assertShop();
		return blockEntity.requestData.encodedTargetAddress();
	}

	@LuaFunction(mainThread = true)
	public final void setAddress(String address) throws LuaException {
		assertShop();
		AutoRequestData.Mutable mutable = new Mutable(blockEntity.requestData);
		mutable.encodedTargetAddress = address;
		blockEntity.requestData = mutable.toImmutable();
	}

	@LuaFunction(mainThread = true)
	public final Map<String, ?> getPriceTagItem() throws LuaException {
		assertShop();
		return VanillaDetailRegistries.ITEM_STACK.getDetails(blockEntity.priceTag.getFilter());
	}

	@LuaFunction(mainThread = true)
	public final void setPriceTagItem(Optional<String> itemName) throws LuaException {
		assertShop();
		ResourceLocation resourceLocation = ResourceLocation.tryParse("minecraft:air");
		if (itemName.isPresent())
			resourceLocation = ResourceLocation.tryParse(itemName.get());
		ItemLike item = BuiltInRegistries.ITEM.get(resourceLocation);
		blockEntity.priceTag.setFilter(new ItemStack(item));
	}

	@LuaFunction(mainThread = true)
	public final int getPriceTagCount() throws LuaException {
		assertShop();
		return blockEntity.priceTag.count;
	}

	@LuaFunction(mainThread = true)
	public final void setPriceTagCount(Optional<Double> argument) throws LuaException {
		assertShop();
		if (argument.isPresent())
			blockEntity.priceTag.count = (Math.max(1, Math.min(100, argument.get().intValue())));
		else
			blockEntity.priceTag.count = 1;
		this.blockEntity.notifyUpdate();
	}

	@LuaFunction(mainThread = true)
	public final Map<Integer, Map<String, ?>> getWares() throws LuaException {
		assertShop();
		List<BigItemStack> wares = blockEntity.requestData.encodedRequest().stacks();
		Map<Integer, Map<String, ?>> result = new HashMap<>();
		for (int i = 0; i < wares.size(); i++) {
			ItemStack stack = wares.get(i).stack;
			Map<String, Object> details = new HashMap<>(
				VanillaDetailRegistries.ITEM_STACK.getDetails(stack));
			details.put("count", wares.get(i).count);
			result.put(i + 1, details); // +1 because lua
		}
		return result;
	}

	/*
	 * this functionally works, but none of us have been able to figure out how to
	 * visually update the store's wares without reloading the chunk. The render
	 * pipeline is difficult :(
	 *
	 * update: I got it working by sending a RemoveBlockEntityPacket.
	 * Do not ask why it works, this is black magic.
	 */
	@LuaFunction(mainThread = true)
	public final void setWares(IArguments arguments) throws LuaException {
		if (!blockEntity.manuallyAddedItems.isEmpty())
			throw new LuaException("Tablecloth isn't empty.");
		ArrayList<BigItemStack> list = new ArrayList<>();
		for (int i = 0; i <= 8; i++) {
			if (arguments.get(i) != null) {
				Map<?, ?> itemData = arguments.getTable(i);

				if (!(itemData instanceof Map)) {
					throw new LuaException("Table or nil expected for each item entry");
				}
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
				ItemStack itemStack = new ItemStack(item);
				if (itemStack.isEmpty())
					throw new LuaException("Invalid item at index: " + (i + 1));
				list.add(new BigItemStack(itemStack, count));
			}
		}
		AutoRequestData.Mutable mutable = new Mutable(blockEntity.requestData);
		mutable.encodedRequest = PackageOrderWithCrafts.simple(list);
		blockEntity.requestData = mutable.toImmutable();
		blockEntity.notifyUpdate();
		blockEntity.notifyShopUpdate();
	}

	@Override
	public String getType() {
		return "Create_TableClothShop";
	}

}
