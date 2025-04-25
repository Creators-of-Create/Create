package com.simibubi.create.compat.computercraft.implementation.peripherals;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class PackagerPeripheral extends SyncedPeripheral<PackagerBlockEntity> {

  public PackagerPeripheral(PackagerBlockEntity blockEntity) {
    super(blockEntity);
  }

  @LuaFunction(mainThread = true)
  public final void setAddress(String newAddress) throws LuaException {
    ItemStack stack = this.blockEntity.inventory.getStackInSlot(1);
    CompoundTag tags = stack.getTag();
    tags.putString("Address", newAddress);
  }

  @LuaFunction
  public final String getAddress() throws LuaException {
    ItemStack stack = this.blockEntity.inventory.getStackInSlot(1);
    if (stack.isEmpty())
      throw new LuaException("no package in packager");
    CompoundTag tags = stack.getTag();
    // Get the address Tag
    return tags.getString("Address");
  }

  
	@NotNull
	@Override
	public String getType() {
		return "Create_Packager";
	}
}
