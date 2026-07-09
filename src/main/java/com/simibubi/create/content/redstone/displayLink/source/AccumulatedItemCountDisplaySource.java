package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class AccumulatedItemCountDisplaySource extends NumericSingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		return Component.literal(String.valueOf(context.sourceConfig()
			.getIntOr("Collected", 0)));
	}

	public void itemReceived(DisplayLinkBlockEntity be, int amount) {
		if (be.getBlockState()
			.getOptionalValue(DisplayLinkBlock.POWERED)
			.orElse(true))
			return;

		int collected = be.getSourceConfig()
			.getIntOr("Collected", 0);
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
