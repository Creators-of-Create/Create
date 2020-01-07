package com.simibubi.create.modules.logistics.block.transposer;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.inventory.InsertingBehaviour;
import com.simibubi.create.foundation.behaviour.inventory.InventoryManagementBehaviour.Attachments;
import com.simibubi.create.modules.logistics.block.belts.AttachedLogisticalBlock;
import com.simibubi.create.modules.logistics.block.extractor.ExtractorTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.items.ItemHandlerHelper;

public class TransposerTileEntity extends ExtractorTileEntity {

	private InsertingBehaviour inserting;

	public TransposerTileEntity() {
		this(AllTileEntities.TRANSPOSER.type);
	}

	protected TransposerTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		inserting = new InsertingBehaviour(this,
				Attachments.toward(() -> AttachedLogisticalBlock.getBlockFacing(getBlockState()).getOpposite()));
		behaviours.add(inserting);
		extracting.withSpecialFilter(this::shouldExtract);
	}

	public void filterChanged(ItemStack stack) {
	}

	protected boolean shouldExtract(ItemStack stack) {
		if (filtering.getFilter().isEmpty())
			return true;
		return inserting.insert(stack, true).isEmpty();
	}
	
	@Override
	protected boolean canExtract() {
		return inserting.getInventory() != null;
	}

	@Override
	protected void onExtract(ItemStack stack) {
		ItemStack remainder = inserting.insert(stack, false);
		remainder = ItemHandlerHelper.insertItemStacked(extracting.getInventory(), remainder, false);
		if (!remainder.isEmpty())
			super.onExtract(remainder);
	}

}
