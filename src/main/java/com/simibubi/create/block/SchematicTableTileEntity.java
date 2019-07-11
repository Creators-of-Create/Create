package com.simibubi.create.block;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.utility.TileEntitySynced;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class SchematicTableTileEntity extends TileEntitySynced implements ITickableTileEntity, INamedContainerProvider {

	public ItemStack inputStack;
	public ItemStack outputStack;
	
	public SchematicTableTileEntity() {
		this(AllTileEntities.SchematicTable.type);
	}
	
	public SchematicTableTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		inputStack = ItemStack.EMPTY;
		outputStack = ItemStack.EMPTY;
	}
	
	@Override
	public void read(CompoundNBT compound) {
		NonNullList<ItemStack> stacks = NonNullList.create();
		ItemStackHelper.loadAllItems(compound, stacks);
		
		if (!stacks.isEmpty()) {
			inputStack = stacks.get(0);
			outputStack = stacks.get(1);
		}
		
		super.read(compound);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		NonNullList<ItemStack> stacks = NonNullList.create();

		stacks.add(inputStack);
		stacks.add(outputStack);
		
		ItemStackHelper.saveAllItems(compound, stacks);
		return super.write(compound);
	}

	@Override
	public void tick() {
		
	}

	@Override
	public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
		return new SchematicTableContainer(p_createMenu_1_, p_createMenu_2_, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent(getType().getRegistryName().toString());
	}
	
}
