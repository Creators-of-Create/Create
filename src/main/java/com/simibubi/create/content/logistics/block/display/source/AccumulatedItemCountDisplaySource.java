package com.simibubi.create.content.logistics.block.display.source;

import com.simibubi.create.content.logistics.block.display.DisplayLinkBlock;
import com.simibubi.create.content.logistics.block.display.DisplayLinkBlockEntity;
import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.network.chat.MutableComponent;

public class AccumulatedItemCountDisplaySource extends NumericSingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		return Components.literal(String.valueOf(context.sourceConfig()
			.getInt("Collected")));
	}

	public void itemReceived(DisplayLinkBlockEntity be, int amount) {
		if (be.getBlockState()
			.getOptionalValue(DisplayLinkBlock.POWERED)
			.orElse(true))
			return;
		
		int collected = be.getSourceConfig()
			.getInt("Collected");
		be.getSourceConfig()
			.putInt("Collected", collected + amount);
		be.updateGatheredData();
	}

	@Override
	protected String getTranslationKey() {
		return "accumulate_items";
	}

	@Override
	public int getPassiveRefreshTicks() {
		return 200;
	}

	@Override
	public void onSignalReset(DisplayLinkContext context) {
		context.sourceConfig()
			.remove("Collected");
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}

}
