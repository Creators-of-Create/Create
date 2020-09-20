package com.simibubi.create.content.logistics.block.inventories;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class CreativeCrateTileEntity extends CrateTileEntity {

	public CreativeCrateTileEntity(TileEntityType<? extends CreativeCrateTileEntity> type) {
		super(type);
		inv = new BottomlessItemHandler(filtering::getFilter);
		itemHandler = LazyOptional.of(() -> inv);
	}

	FilteringBehaviour filtering;
	LazyOptional<IItemHandler> itemHandler;
	private BottomlessItemHandler inv;

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(filtering = createFilter().onlyActiveWhen(this::filterVisible)
			.withCallback(this::filterChanged));
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
		if (ItemStack.areItemsEqual(filter, otherCrate.filtering.getFilter()))
			return;
		otherCrate.filtering.setFilter(filter);
	}

	@Override
	public void remove() {
		super.remove();
		if (itemHandler != null)
			itemHandler.invalidate();
	}

	private CreativeCrateTileEntity getOtherCrate() {
		if (!AllBlocks.CREATIVE_CRATE.has(getBlockState()))
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

		filtering.withCallback($ -> {
		});
		filtering.setFilter(otherCrate.filtering.getFilter());
		filtering.withCallback(this::filterChanged);
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
				MatrixStacker.of(ms)
					.rotateX(90);
			}

			@Override
			protected Vector3d getLocalOffset(BlockState state) {
				return new Vector3d(0.5, 13 / 16d, 0.5);
			}

			protected float getScale() {
				return super.getScale() * 1.5f;
			};

		});
	}

}
