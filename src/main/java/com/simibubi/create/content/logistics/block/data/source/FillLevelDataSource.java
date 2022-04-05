package com.simibubi.create.content.logistics.block.data.source;

import static com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection.WIDE_MONOSPACE;

import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.content.logistics.block.data.DataGathererScreen.LineBuilder;
import com.simibubi.create.content.logistics.block.data.target.DataTargetStats;
import com.simibubi.create.content.logistics.block.redstone.StockpileSwitchTileEntity;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayTileEntity;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FillLevelDataSource extends NumericSingleLineDataSource {

	@Override
	protected MutableComponent provideLine(DataGathererContext context, DataTargetStats stats) {
		BlockEntity te = context.getSourceTE();
		if (!(te instanceof StockpileSwitchTileEntity sste))
			return EMPTY_LINE;

		float currentLevel = sste.currentLevel;
		if (usePercent(context))
			return new TextComponent(Mth.clamp((int) (currentLevel * 100), 0, 100) + "%");

		String label = context.sourceConfig()
			.getString("Label");

		int labelSize = label.isEmpty() ? 0 : label.length() + 1;
		int length = Math.min(stats.maxColumns() - labelSize, 32);

		if (context.getTargetTE() instanceof SignBlockEntity)
			length = (int) (length * 6f / 9f);
		if (context.getTargetTE() instanceof FlapDisplayTileEntity)
			length = sizeForWideChars(length);

		int filledLength = (int) (currentLevel * length);

		if (length < 1)
			return EMPTY_LINE;

		StringBuilder s = new StringBuilder();
		int emptySpaces = length - filledLength;
		for (int i = 0; i < filledLength; i++)
			s.append("\u2588");
		for (int i = 0; i < emptySpaces; i++)
			s.append("\u2592");

		return new TextComponent(s.toString());
	}

	private boolean usePercent(DataGathererContext context) {
		return context.sourceConfig()
			.getInt("Mode") == 0;
	}

	@Override
	protected String getTranslationKey() {
		return "fill_level";
	}

	@Override
	protected String getFlapDisplayLayoutName(DataGathererContext context) {
		return usePercent(context) ? super.getFlapDisplayLayoutName(context) : "Progress";
	}

	@Override
	protected FlapDisplaySection createSectionForValue(DataGathererContext context, int size) {
		return usePercent(context) ? super.createSectionForValue(context, size)
			: new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "pixel", false, false).wideFlaps();
	}

	private int sizeForWideChars(int size) {
		return (int) (size * FlapDisplaySection.MONOSPACE / WIDE_MONOSPACE);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DataGathererContext context, LineBuilder builder, boolean isFirstLine) {
		super.initConfigurationWidgets(context, builder, isFirstLine);
		if (isFirstLine)
			return;
		builder.addSelectionScrollInput(0, 120,
			(si, l) -> si.forOptions(Lang.translatedOptions("data_source.fill_level", "percent", "progress_bar"))
				.titled(Lang.translate("data_source.fill_level.display")),
			"Mode");
	}

	@Override
	protected boolean allowsLabeling(DataGathererContext context) {
		return true;
	}

}
