package com.simibubi.create.compat.computercraft.implementation.peripherals;

import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

import org.jetbrains.annotations.NotNull;

public class PackagerPeripheral extends SyncedPeripheral<PackagerBlockEntity> {

	public PackagerPeripheral(PackagerBlockEntity blockEntity) {
		super(blockEntity);
	}

	@LuaFunction(mainThread = true)
	public final void createPackage(IArguments arguments) throws LuaException {

		this.blockEntity.computerPacking(arguments.getString(0));
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_Packager";
	}

}
