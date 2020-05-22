package com.simibubi.create.modules.logistics.block.inventories;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocksNew;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class CreativeCrateTileEntity extends CrateTileEntity {

	public CreativeCrateTileEntity() {
		super(AllTileEntities.CREATIVE_CRATE.type);
		inv = new CreativeCrateInventory(this);
		itemHandler = LazyOptional.of(() -> inv);
	}

	FilteringBehaviour filter;
	LazyOptional<IItemHandler> itemHandler;
	private CreativeCrateInventory inv;

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		filter = createFilter();
		filter.onlyActiveWhen(this::filterVisible);
		filter.withCallback(this::filterChanged);
		behaviours.add(filter);
	}

	private boolean filterVisible() {
		if (!hasWorld() || isDoubleCrate() && !isSecondaryCrate())
			return false;
		return true;
	}

	private void filterChanged(ItemStack filter) {
		if (!filterVisible())
			return;
		CreativeCrateTileEntity otherCrate = getOtherCrate();
		if (otherCrate == null)
			return;
		if (ItemStack.areItemsEqual(filter, otherCrate.filter.getFilter()))
			return;
		otherCrate.filter.setFilter(filter);
	}

	private CreativeCrateTileEntity getOtherCrate() {
		if (!AllBlocksNew.CREATIVE_CRATE.has(getBlockState()))
			return null;
		TileEntity tileEntity = world.getTileEntity(pos.offset(getFacing()));
		if (tileEntity instanceof CreativeCrateTileEntity)
			return (CreativeCrateTileEntity) tileEntity;
		return null;
	}

	public void onPlaced() {
		if (!isDoubleCrate())
			return;
		CreativeCrateTileEntity otherCrate = getOtherCrate();
		if (otherCrate == null)
			return;
		
		filter.withCallback($ -> {});
		filter.setFilter(otherCrate.filter.getFilter());
		filter.withCallback(this::filterChanged);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return itemHandler.cast();
		return super.getCapability(cap, side);
	}

	public FilteringBehaviour createFilter() {
		return new FilteringBehaviour(this, new ValueBoxTransform() {

			@Override
			protected void rotate(BlockState state, MatrixStack ms) {
				ms.multiply(VecHelper.rotateX(90));
			}

			@Override
			protected Vec3d getLocation(BlockState state) {
				return new Vec3d(0.5, 13 / 16d, 0.5);
			}

			protected float getScale() {
				return super.getScale() * 1.5f;
			};

		});
	}

}
