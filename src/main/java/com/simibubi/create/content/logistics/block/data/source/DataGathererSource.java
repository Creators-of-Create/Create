package com.simibubi.create.content.logistics.block.data.source;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.logistics.block.data.DataGathererBehaviour;
import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.content.logistics.block.data.DataGathererScreen.LineBuilder;
import com.simibubi.create.content.logistics.block.data.target.DataGathererTarget;
import com.simibubi.create.content.logistics.block.data.target.DataTargetStats;
import com.simibubi.create.content.logistics.block.data.target.FlapDisplayDataTarget;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayLayout;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayTileEntity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class DataGathererSource extends DataGathererBehaviour {

	public static final List<MutableComponent> EMPTY = ImmutableList.of(new TextComponent(""));
	public static final MutableComponent EMPTY_LINE = new TextComponent("");
	public static final MutableComponent WHITESPACE = new TextComponent(" ");

	public abstract List<MutableComponent> provideText(DataGathererContext context, DataTargetStats stats);

	public void transferData(DataGathererContext context, DataGathererTarget activeTarget, int line) {
		DataTargetStats stats = activeTarget.provideStats(context);

		if (activeTarget instanceof FlapDisplayDataTarget fddt) {
			List<List<MutableComponent>> flapDisplayText = provideFlapDisplayText(context, stats);
			fddt.acceptFlapText(line, flapDisplayText, context);
			return;
		}

		List<MutableComponent> text = provideText(context, stats);
		activeTarget.acceptText(line, text, context);

	}

	public void onSignalReset(DataGathererContext context) {};
	
	public void populateData(DataGathererContext context) {};
	
	public int getPassiveRefreshTicks() {
		return 100;
	};

	protected String getTranslationKey() {
		return id.getPath();
	}

	public Component getName() {
		return new TranslatableComponent(id.getNamespace() + ".data_source." + getTranslationKey());
	}

	public void loadFlapDisplayLayout(DataGathererContext context, FlapDisplayTileEntity flapDisplay,
		FlapDisplayLayout layout) {
		if (!layout.isLayout("Default"))
			layout.loadDefault(flapDisplay.getMaxCharCount());
	}

	public List<List<MutableComponent>> provideFlapDisplayText(DataGathererContext context, DataTargetStats stats) {
		return provideText(context, stats).stream()
			.map(Arrays::asList)
			.toList();
	}

	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DataGathererContext context, LineBuilder builder, boolean isFirstLine) {}

}
