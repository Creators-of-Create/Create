package com.simibubi.create.content.logistics.block.display.source;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.content.logistics.block.display.target.NixieTubeDisplayTarget;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeTileEntity;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NixieTubeDisplaySource extends SingleLineDisplaySource {

	@Override
	protected String getTranslationKey() {
		return "nixie_tube";
	}

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
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
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return !(context.te().activeTarget instanceof NixieTubeDisplayTarget);
	}

	@Override
	protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
		if (isNumeric(context))
			return "Number";
		return super.getFlapDisplayLayoutName(context);
	}

	@Override
	protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
		if (isNumeric(context))
			return new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "numeric", false, false);
		return super.createSectionForValue(context, size);
	}

	protected boolean isNumeric(DisplayLinkContext context) {
		return context.flapDisplayContext == Boolean.TRUE;
	}

}
