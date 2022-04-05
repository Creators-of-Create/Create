package com.simibubi.create.content.logistics.block.data.source;

import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.content.logistics.block.data.target.DataTargetStats;
import com.simibubi.create.content.logistics.block.data.target.NixieTubeDataTarget;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeTileEntity;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NixieTubeDataSource extends SingleLineDataSource {

	@Override
	protected String getTranslationKey() {
		return "nixie_tube";
	}

	@Override
	protected MutableComponent provideLine(DataGathererContext context, DataTargetStats stats) {
		BlockEntity sourceTE = context.getSourceTE();
		if (!(sourceTE instanceof NixieTubeTileEntity nte))
			return EMPTY_LINE;
		
		MutableComponent text = nte.getFullText();

		try {
			String line = text.getString();
			Integer.valueOf(line);
			context.flapDisplayContext = Boolean.TRUE;
		} catch (NumberFormatException e) {
		}

		return text;
	}

	@Override
	protected boolean allowsLabeling(DataGathererContext context) {
		return !(context.te().activeTarget instanceof NixieTubeDataTarget);
	}

	@Override
	protected String getFlapDisplayLayoutName(DataGathererContext context) {
		if (isNumeric(context))
			return "Number";
		return super.getFlapDisplayLayoutName(context);
	}

	@Override
	protected FlapDisplaySection createSectionForValue(DataGathererContext context, int size) {
		if (isNumeric(context))
			return new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "numeric", false, false);
		return super.createSectionForValue(context, size);
	}

	protected boolean isNumeric(DataGathererContext context) {
		return context.flapDisplayContext == Boolean.TRUE;
	}

}
