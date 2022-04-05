package com.simibubi.create.content.logistics.block.data.source;

import com.simibubi.create.content.logistics.block.data.DataGathererBlock;
import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.content.logistics.block.data.DataGathererTileEntity;
import com.simibubi.create.content.logistics.block.data.target.DataTargetStats;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class AccumulatedItemCountDataSource extends NumericSingleLineDataSource {

	@Override
	protected MutableComponent provideLine(DataGathererContext context, DataTargetStats stats) {
		return new TextComponent(String.valueOf(context.sourceConfig()
			.getInt("Collected")));
	}

	public void itemReceived(DataGathererTileEntity te, int amount) {
		if (te.getBlockState()
			.getOptionalValue(DataGathererBlock.POWERED)
			.orElse(true))
			return;
		
		int collected = te.getSourceConfig()
			.getInt("Collected");
		te.getSourceConfig()
			.putInt("Collected", collected + amount);
		te.updateGatheredData();
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
	public void onSignalReset(DataGathererContext context) {
		context.sourceConfig()
			.remove("Collected");
	}

	@Override
	protected boolean allowsLabeling(DataGathererContext context) {
		return true;
	}

}
