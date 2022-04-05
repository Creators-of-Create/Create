package com.simibubi.create.content.logistics.block.data.target;

import java.util.List;

import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class SingleLineDataTarget extends DataGathererTarget {

	@Override
	public final void acceptText(int line, List<MutableComponent> text, DataGathererContext context) {
		acceptLine(text.get(0), context);
	}
	
	protected abstract void acceptLine(MutableComponent text, DataGathererContext context);

	@Override
	public final DataTargetStats provideStats(DataGathererContext context) {
		return new DataTargetStats(1, getWidth(context), this);
	}
	
	@Override
	public Component getLineOptionText(int line) {
		return Lang.translate("data_target.single_line");
	}
	
	protected abstract int getWidth(DataGathererContext context);

}
