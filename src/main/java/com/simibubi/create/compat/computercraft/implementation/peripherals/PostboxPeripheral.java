package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.Map;

import com.simibubi.create.compat.computercraft.implementation.luaObjects.PackageLuaObject;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.compat.computercraft.implementation.ComputerUtil;
import com.simibubi.create.content.logistics.packagePort.postbox.PostboxBlockEntity;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

import com.simibubi.create.compat.computercraft.events.ComputerEvent;
import com.simibubi.create.compat.computercraft.events.PackageEvent;

public class PostboxPeripheral extends SyncedPeripheral<PostboxBlockEntity> {

	public PostboxPeripheral(PostboxBlockEntity blockEntity) {
		super(blockEntity);
	}

	@LuaFunction(mainThread = true)
	public final void setAddress(String address) throws LuaException {
		blockEntity.addressFilter = address;
		blockEntity.filterChanged();
		blockEntity.notifyUpdate();
	}

	@LuaFunction(mainThread = true)
	public final String getAddress() throws LuaException {
		return blockEntity.addressFilter;
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
	public final String getConfiguration() throws LuaException {
		if (blockEntity.target == null)
			return null;
		if (blockEntity.acceptsPackages)
			return "send_recieve";
		else
			return "send";
	}

	@LuaFunction(mainThread = true)
	public final boolean setConfiguration(String config) throws LuaException {
		if (blockEntity.target == null)
			return false;
		if (config.equals("send_recieve")) {
			blockEntity.acceptsPackages = true;
			blockEntity.filterChanged();
			blockEntity.notifyUpdate();
			return true;
		}
		if (config.equals("send")) {
			blockEntity.acceptsPackages = false;
			blockEntity.filterChanged();
			blockEntity.notifyUpdate();
			return true;
		}
		throw new LuaException("Unknown configuration: \"" + config
			+ "\" Possible configurations are: \"send_recieve\" and \"send\".");
	}

	@Override
	public void prepareComputerEvent(@NotNull ComputerEvent event) {
		if (event instanceof PackageEvent pe) {
			queueEvent(pe.status, new PackageLuaObject(null, pe.box));
		}
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_Postbox";
	}

}
