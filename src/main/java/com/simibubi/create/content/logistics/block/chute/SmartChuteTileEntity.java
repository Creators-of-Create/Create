package com.simibubi.create.content.logistics.block.chute;

import java.util.List;

import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SmartChuteTileEntity extends ChuteTileEntity {

	FilteringBehaviour filtering;

	public SmartChuteTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected boolean canAcceptItem(ItemStack stack) {
		return super.canAcceptItem(stack) && canCollectItemsFromBelow() && filtering.test(stack);
	}

	@Override
	protected int getExtractionAmount() {
		return filtering.isCountVisible() && !filtering.anyAmount() ? filtering.getAmount() : 64;
	}

	@Override
	protected ExtractionCountMode getExtractionMode() {
		return filtering.isCountVisible() && !filtering.anyAmount() ? ExtractionCountMode.EXACTLY
			: ExtractionCountMode.UPTO;
	}

	@Override
	protected boolean canCollectItemsFromBelow() {
		BlockState blockState = getBlockState();
		return blockState.hasProperty(SmartChuteBlock.POWERED) && !blockState.getValue(SmartChuteBlock.POWERED);
	}
	
	@Override
	protected boolean canOutputItems() {
		BlockState blockState = getBlockState();
		return blockState.hasProperty(SmartChuteBlock.POWERED) && !blockState.getValue(SmartChuteBlock.POWERED);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(filtering =
			new FilteringBehaviour(this, new SmartChuteFilterSlotPositioning()).showCountWhen(this::isExtracting));
		super.addBehaviours(behaviours);
	}

	private boolean isExtracting() {
		boolean up = getItemMotion() < 0;
		BlockPos chutePos = worldPosition.relative(up ? Direction.UP : Direction.DOWN);
		BlockState blockState = level.getBlockState(chutePos);
		return !AbstractChuteBlock.isChute(blockState) && !blockState.getMaterial()
			.isReplaceable();
	}

}
