package com.simibubi.create.content.contraptions.goggles;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/*
* Implement this Interface in the TileEntity class that wants to add info to the screen
* */
public interface IHaveGoggleInformation {

	Format numberFormat = new Format();
	String spacing = "    ";

	/**
	 * this method will be called when looking at a TileEntity that implemented this
	 * interface
	 *
	 * @return {@code true} if the tooltip creation was successful and should be
	 *         displayed, or {@code false} if the overlay should not be displayed
	 */
	default boolean addToGoggleTooltip(List<String> tooltip, boolean isPlayerSneaking) {
		return false;
	}

	static String format(double d) {
		return numberFormat.get()
			.format(d);
	}

	default boolean containedFluidTooltip(List<String> tooltip, boolean isPlayerSneaking,
		LazyOptional<IFluidHandler> handler) {

		tooltip.add(spacing + Lang.translate("gui.goggles.fluid_container"));
		String mb = Lang.translate("generic.unit.millibuckets");
		IFluidHandler tank = handler.orElse(null);
		if (tank == null || tank.getTanks() == 0)
			return false;

		ITextComponent indent = new StringTextComponent(spacing + " ");

		boolean isEmpty = true;
		for (int i = 0; i < tank.getTanks(); i++) {
			FluidStack fluidStack = tank.getFluidInTank(i);
			if (fluidStack.isEmpty())
				continue;

			ITextComponent fluidName =
				new TranslationTextComponent(fluidStack.getTranslationKey()).applyTextStyle(TextFormatting.GRAY);
			ITextComponent contained =
				new StringTextComponent(format(fluidStack.getAmount()) + mb).applyTextStyle(TextFormatting.GOLD);
			ITextComponent slash = new StringTextComponent(" / ").applyTextStyle(TextFormatting.GRAY);
			ITextComponent capacity =
				new StringTextComponent(format(tank.getTankCapacity(i)) + mb).applyTextStyle(TextFormatting.DARK_GRAY);

			tooltip.add(indent.deepCopy()
				.appendSibling(fluidName)
				.getFormattedText());
			tooltip.add(indent.deepCopy()
				.appendSibling(contained)
				.appendSibling(slash)
				.appendSibling(capacity)
				.getFormattedText());

			isEmpty = false;
		}

		if (tank.getTanks() > 1) {
			if (isEmpty)
				tooltip.remove(tooltip.size() - 1);
			return true;
		}
		
		if (!isEmpty)
			return true;

		ITextComponent capacity = new StringTextComponent(Lang.translate("gui.goggles.fluid_container.capacity"))
			.applyTextStyle(TextFormatting.GRAY);
		ITextComponent amount =
			new StringTextComponent(format(tank.getTankCapacity(0)) + mb).applyTextStyle(TextFormatting.GOLD);

		String capacityString = indent.deepCopy()
			.appendSibling(capacity)
			.appendSibling(amount)
			.getFormattedText();
		tooltip.add(capacityString);
		return true;
	}

	class Format {

		private NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);;

		private Format() {}

		public NumberFormat get() {
			return format;
		}

		public void update() {
			format = NumberFormat.getInstance(Minecraft.getInstance()
				.getLanguageManager()
				.getCurrentLanguage()
				.getJavaLocale());
			format.setMaximumFractionDigits(2);
			format.setMinimumFractionDigits(0);
			format.setGroupingUsed(true);
		}

	}

}
