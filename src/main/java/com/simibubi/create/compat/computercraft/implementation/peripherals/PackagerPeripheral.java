package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.compat.computercraft.implementation.CreateLuaTable;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.detail.VanillaDetailRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class PackagerPeripheral extends SyncedPeripheral<PackagerBlockEntity> {

  public PackagerPeripheral(PackagerBlockEntity blockEntity) {
    super(blockEntity);
  }

  protected final ItemStack getHeldStack() throws LuaException {
    ItemStack stack = this.blockEntity.inventory.getStackInSlot(1);
    if (stack.isEmpty())
      throw new LuaException("No package in packager");
    return stack;
  }

  @LuaFunction
  public final String getHeldAddress() throws LuaException {
    ItemStack stack = getHeldStack();
    
    return PackageItem.getAddress(stack);
  }

  @LuaFunction(mainThread = true)
  public final void setHeldAddress(String newAddress) throws LuaException {
    ItemStack stack = getHeldStack();
    
    PackageItem.addAddress(stack, newAddress);
  }

  @LuaFunction
  public final int getHeldOrderID() throws LuaException {
    ItemStack stack = getHeldStack();
    
    return PackageItem.getOrderId(stack);
  }

  @LuaFunction
  public final CreateLuaTable getHeldContext() throws LuaException {
    return getHeldContextTable(getHeldStack());
  }

  @LuaFunction
  public final CreateLuaTable getHeldItems() throws LuaException {
    return getHeldItemsTable(getHeldStack());
  }

  protected static CreateLuaTable getHeldContextTable(ItemStack stack) {
    PackageOrderWithCrafts context = PackageItem.getOrderContext(stack);
    if (context == null)
      return new CreateLuaTable();

    try {
      return fromCompoundTag(context.write());
    } catch (LuaException e) {
      return new CreateLuaTable();
    }
  }
  
  protected static CreateLuaTable getHeldItemsTable(ItemStack stack) {
    try {
      return fromCompoundTag(PackageItem.getContents(stack).serializeNBT()).getTable("items");
    } catch (LuaException e) {
      return new CreateLuaTable();
    }
  }

  // return Address, OrderID, Items, Context as a list
  public static List<Object> getPackageItemDetails(ItemStack stack) {
    return List.of(PackageItem.getAddress(stack), PackageItem.getOrderId(stack), getHeldItemsTable(stack), getHeldContextTable(stack));
  }
  
  public static void registerItemDetailProviders() {
    VanillaDetailRegistries.ITEM_STACK.addProvider((out, stack) -> {
      if (!PackageItem.isPackage(stack))
        return;
      
      List<Object> details = getPackageItemDetails(stack);
      out.put("package_address", details.get(0));
      out.put("package_orderID", details.get(1));
      out.put("package_items", details.get(2));
      out.put("package_orderContext", details.get(3));
    });
  }

	@NotNull
	@Override
	public String getType() {
		return "Create_Packager";
	}
}
