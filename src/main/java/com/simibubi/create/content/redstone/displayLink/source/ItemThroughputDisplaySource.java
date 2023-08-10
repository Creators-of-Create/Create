package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemThroughputDisplaySource extends AccumulatedItemCountDisplaySource {

	static final int POOL_SIZE = 10;

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		CompoundTag conf = context.sourceConfig();
		if (conf.contains("Inactive"))
			return ZERO.copy();

		double interval = 20 * Math.pow(60, conf.getInt("Interval"));
		double rate = conf.getFloat("Rate") * interval;

		if (rate > 0) {
			long previousTime = conf.getLong("LastReceived");
			long gameTime = context.blockEntity()
				.getLevel()
				.getGameTime();
			int diff = (int) (gameTime - previousTime);
			if (diff > 0) {
				// Too long since last item
				int lastAmount = conf.getInt("LastReceivedAmount");
				double timeBetweenStacks = lastAmount / rate;
				if (diff > timeBetweenStacks * 2)
					conf.putBoolean("Inactive", true);
			}
		}

		return CreateLang.number(rate)
			.component();
	}

	public void itemReceived(DisplayLinkBlockEntity be, int amount) {
		if (be.getBlockState()
			.getOptionalValue(DisplayLinkBlock.POWERED)
			.orElse(true))
			return;

		CompoundTag conf = be.getSourceConfig();
		long gameTime = be.getLevel()
			.getGameTime();

		if (!conf.contains("LastReceived")) {
			conf.putLong("LastReceived", gameTime);
			return;
		}

		long previousTime = conf.getLong("LastReceived");
		ListTag rates = conf.getList("PrevRates", Tag.TAG_FLOAT);

		if (rates.size() != POOL_SIZE) {
			rates = new ListTag();
			for (int i = 0; i < POOL_SIZE; i++)
				rates.add(FloatTag.valueOf(-1));
		}

		int poolIndex = conf.getInt("Index") % POOL_SIZE;
		rates.set(poolIndex, FloatTag.valueOf((float) (amount / (double) (gameTime - previousTime))));

		float rate = 0;
		int validIntervals = 0;
		for (int i = 0; i < POOL_SIZE; i++) {
			float pooledRate = rates.getFloat(i);
			if (pooledRate >= 0) {
				rate += pooledRate;
				validIntervals++;
			}
		}

		conf.remove("Rate");
		if (validIntervals > 0) {
			rate /= validIntervals;
			conf.putFloat("Rate", rate);
		}

		conf.remove("Inactive");
		conf.putInt("LastReceivedAmount", amount);
		conf.putLong("LastReceived", gameTime);
		conf.putInt("Index", poolIndex + 1);
		conf.put("PrevRates", rates);
		be.updateGatheredData();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder,
		boolean isFirstLine) {
		super.initConfigurationWidgets(context, builder, isFirstLine);
		if (isFirstLine)
			return;

		builder.addSelectionScrollInput(0, 80, (si, l) -> {
			si.forOptions(CreateLang.translatedOptions("display_source.item_throughput.interval", "second", "minute", "hour"))
				.titled(CreateLang.translateDirect("display_source.item_throughput.interval"));
		}, "Interval");
	}

	@Override
	protected String getTranslationKey() {
		return "item_throughput";
	}

}
