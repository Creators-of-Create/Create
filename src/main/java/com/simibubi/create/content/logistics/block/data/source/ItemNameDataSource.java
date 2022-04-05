package com.simibubi.create.content.logistics.block.data.source;

import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.content.logistics.block.data.DataGathererTileEntity;
import com.simibubi.create.content.logistics.block.data.target.DataTargetStats;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;

import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class ItemNameDataSource extends SingleLineDataSource {

	@Override
	protected MutableComponent provideLine(DataGathererContext context, DataTargetStats stats) {
		DataGathererTileEntity gatherer = context.te();
		Direction direction = gatherer.getDirection();
		MutableBlockPos pos = gatherer.getSourcePosition()
			.mutable();

		MutableComponent combined = EMPTY_LINE.copy();

		for (int i = 0; i < 32; i++) {
			TransportedItemStackHandlerBehaviour behaviour =
				TileEntityBehaviour.get(context.level(), pos, TransportedItemStackHandlerBehaviour.TYPE);
			pos.move(direction);

			if (behaviour == null)
				break;

			MutableObject<ItemStack> stackHolder = new MutableObject<>();
			behaviour.handleCenteredProcessingOnAllItems(.25f, tis -> {
				stackHolder.setValue(tis.stack);
				return TransportedResult.doNothing();
			});

			ItemStack stack = stackHolder.getValue();
			if (stack != null && !stack.isEmpty())
				combined = combined.append(stack.getHoverName());
		}

		return combined;
	}

	@Override
	protected String getTranslationKey() {
		return "combine_item_names";
	}

	@Override
	protected boolean allowsLabeling(DataGathererContext context) {
		return true;
	}

	@Override
	protected String getFlapDisplayLayoutName(DataGathererContext context) {
		return "Number";
	}

}
