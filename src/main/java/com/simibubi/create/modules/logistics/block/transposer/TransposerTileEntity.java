package com.simibubi.create.modules.logistics.block.transposer;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour.SlotPositioning;
import com.simibubi.create.modules.logistics.block.extractor.ExtractorBlock;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;

public class TransposerTileEntity extends SmartTileEntity {

	private static FilteringBehaviour.SlotPositioning slots;
	private FilteringBehaviour filtering;

	public TransposerTileEntity() {
		this(AllTileEntities.TRANSPOSER.type);
	}

	protected TransposerTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		if (slots == null)
			slots = new SlotPositioning(ExtractorBlock::getFilterSlotPosition, ExtractorBlock::getFilterSlotOrientation)
					.scale(.4f);
		filtering = new FilteringBehaviour(this).withCallback(this::filterChanged).withSlotPositioning(slots)
				.showCount();
		behaviours.add(filtering);
	}

	public void filterChanged(ItemStack stack) {
	}

}
