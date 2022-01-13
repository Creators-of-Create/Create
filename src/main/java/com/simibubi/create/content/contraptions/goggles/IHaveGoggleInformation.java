package com.simibubi.create.content.contraptions.goggles;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.lib.transfer.fluid.IFluidHandler;
import com.simibubi.create.lib.util.FluidHandlerData;
import com.simibubi.create.lib.util.FluidHandlerData.FluidTankData;
import com.simibubi.create.lib.util.FluidTextUtil;
import com.simibubi.create.lib.util.FluidUnit;
import com.simibubi.create.lib.util.FluidUtil;
import com.simibubi.create.lib.util.LazyOptional;
import com.simibubi.create.lib.util.MinecraftClientUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/*
* Implement this Interface in the TileEntity class that wants to add info to the screen
* */
public interface IHaveGoggleInformation {

	Format numberFormat = new Format();
	String spacing = "    ";
	Component componentSpacing = new TextComponent(spacing);

	/**
	 * this method will be called when looking at a TileEntity that implemented this
	 * interface
	 *
	 * @return {@code true} if the tooltip creation was successful and should be displayed,
	 * or {@code false} if the overlay should not be displayed
	* */
	default boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking){
		return false;
	}

	static String format(double d) {
		return numberFormat.get()
			.format(d).replace("\u00A0", " ");
	}

	default boolean containedFluidTooltip(List<Component> tooltip, boolean isPlayerSneaking, LazyOptional<IFluidHandler> handler) {
		tooltip.add(componentSpacing.plainCopy().append(Lang.translate("gui.goggles.fluid_container")));
		FluidUnit unit = AllConfigs.CLIENT.fluidUnitType.get();
		TranslatableComponent mb = Lang.translate(unit.getTranslationKey());
		FluidHandlerData tank = FluidHandlerData.CURRENT;
		if (tank == null || tank.getTanks() == 0)
			return false;

		Component indent = new TextComponent(spacing + " ");

		boolean isEmpty = true;
		for (int i = 0; i < tank.getTanks(); i++) {
			FluidTankData data = tank.data[i];
			String translationKey = FluidUtil.getTranslationKey(data.fluid());
			long amount = data.amount();

			if (translationKey.isEmpty() || amount == 0)
				continue;

			long tankCapacity = data.capacity();
			Component fluidName = new TranslatableComponent(translationKey).withStyle(ChatFormatting.GRAY);
			Component contained = new TextComponent(FluidTextUtil.getUnicodeMillibuckets(amount)).append(mb).withStyle(ChatFormatting.GOLD);
			Component slash = new TextComponent(" / ").withStyle(ChatFormatting.GRAY);
			Component capacity = new TextComponent(FluidTextUtil.getUnicodeMillibuckets(tankCapacity)).append(mb).withStyle(ChatFormatting.DARK_GRAY);

			tooltip.add(indent.plainCopy()
					.append(fluidName));
			tooltip.add(indent.plainCopy()
				.append(contained)
				.append(slash)
				.append(capacity));

			isEmpty = false;
		}

		if (tank.getTanks() > 1) {
			if (isEmpty)
				tooltip.remove(tooltip.size() - 1);
			return true;
		}

		if (!isEmpty)
			return true;

		Component capacity = Lang.translate("gui.goggles.fluid_container.capacity").withStyle(ChatFormatting.GRAY);
		Component amount = new TextComponent(FluidTextUtil.getUnicodeMillibuckets(tank.data[0].capacity())).append(mb).withStyle(ChatFormatting.GOLD);

		tooltip.add(indent.plainCopy()
				.append(capacity)
				.append(amount));
		return true;
	}

	class Format {

		private NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);

		private Format() {}

		public NumberFormat get() {
			return format;
		}

		public void update() {
			format = NumberFormat.getInstance(MinecraftClientUtil.getLocale());
			format.setMaximumFractionDigits(2);
			format.setMinimumFractionDigits(0);
			format.setGroupingUsed(true);
		}

	}

}
