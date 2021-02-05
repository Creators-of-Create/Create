package com.simibubi.create.content.logistics.block.chute;

import java.util.List;

import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class SmartChuteTileEntity extends ChuteTileEntity {

	FilteringBehaviour filtering;

	public SmartChuteTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	protected boolean canAcceptItem(ItemStack stack) {
		return super.canAcceptItem(stack) && canCollectItemsFromBelow();
	}

	@Override
	protected int getExtractionAmount() {
		return filtering.isCountVisible() ? filtering.getAmount() : 0;
	}

	@Override
	protected boolean canCollectItemsFromBelow() {
		BlockState blockState = getBlockState();
		return blockState.has(SmartChuteBlock.POWERED) && !blockState.get(SmartChuteBlock.POWERED);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(filtering =
			new FilteringBehaviour(this, new SmartChuteFilterSlotPositioning()).showCountWhen(this::isExtracting));
		super.addBehaviours(behaviours);
	}

	private boolean isExtracting() {
		boolean up = getItemMotion() < 0;
		BlockPos chutePos = pos.offset(up ? Direction.UP : Direction.DOWN);
		BlockState blockState = world.getBlockState(chutePos);
		return !AbstractChuteBlock.isChute(blockState) && !blockState.getMaterial()
			.isReplaceable();
	}

}
