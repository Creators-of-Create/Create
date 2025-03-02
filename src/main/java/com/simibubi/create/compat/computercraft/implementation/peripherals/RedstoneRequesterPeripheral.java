package com.simibubi.create.compat.computercraft.implementation.peripherals;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RedstoneRequesterPeripheral  extends SyncedPeripheral<RedstoneRequesterBlockEntity> {

	public RedstoneRequesterPeripheral(RedstoneRequesterBlockEntity blockEntity) {
		super(blockEntity);
	}

	@LuaFunction(mainThread = true)
	public final void orderPackage(IArguments arguments) throws LuaException {
		List<BigItemStack> items = new java.util.ArrayList<>(List.of());

		items.add(new BigItemStack(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(arguments.getString(1)))), arguments.getInt(2)));
		this.blockEntity.computerRequest(items, arguments.getString(0));
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_RedstoneRequester";
	}

}
