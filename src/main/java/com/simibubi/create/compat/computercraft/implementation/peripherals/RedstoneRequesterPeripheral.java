package com.simibubi.create.compat.computercraft.implementation.peripherals;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;

import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterEffectPacket;

import com.simibubi.create.content.logistics.stockTicker.StockCheckingBlockEntity;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class RedstoneRequesterPeripheral  extends SyncedPeripheral<RedstoneRequesterBlockEntity> {

	public RedstoneRequesterPeripheral(RedstoneRequesterBlockEntity blockEntity) {
		super(blockEntity);
	}

	@LuaFunction(mainThread = true)
	public final void orderPackage(IArguments arguments) throws LuaException {
		List<BigItemStack> items = new java.util.ArrayList<>();

		Object obj = arguments.get(1);
		if (obj instanceof String itemName) {
			Object sizeOrNull = arguments.get(2);
			int size = 1;
			if (sizeOrNull instanceof Number theSize) size = theSize.intValue();
			items.add(new BigItemStack(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName))), size));
		} else {
			Map<String, Double> itemsMap = (Map<String, Double>) obj;
			for (var itemEntry : itemsMap.entrySet()) {
				items.add(new BigItemStack(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemEntry.getKey()))), itemEntry.getValue().intValue()));
			}
		}

		InventorySummary summary = this.blockEntity.getAccurateSummary();
		for (BigItemStack entry : items) {
			if (summary.getCountOf(entry.stack) >= entry.count) {
				continue;
			}
			if (!this.blockEntity.allowPartialRequests && this.blockEntity.getLevel() instanceof ServerLevel) {
				throw new LuaException("Request Failed (Are you out of stock?)");
			}
		}

		this.blockEntity.computerRequest(items, arguments.getString(0));
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_RedstoneRequester";
	}

}
