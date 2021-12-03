package com.simibubi.create.content.logistics.block.inventories;

import java.util.List;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class CreativeCrateTileEntity extends CrateTileEntity {

	public CreativeCrateTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		inv = new BottomlessItemHandler(filtering::getFilter);
		itemHandler = LazyOptional.of(() -> inv);
	}

	FilteringBehaviour filtering;
	LazyOptional<IItemHandler> itemHandler;
	private BottomlessItemHandler inv;

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(filtering = createFilter());
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (itemHandler != null)
			itemHandler.invalidate();
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
			protected void rotate(BlockState state, PoseStack ms) {
				TransformStack.cast(ms)
					.rotateX(90);
			}

			@Override
			protected Vec3 getLocalOffset(BlockState state) {
				return new Vec3(0.5, 13 / 16d, 0.5);
			}

			protected float getScale() {
				return super.getScale() * 1.5f;
			};

		});
	}

}
