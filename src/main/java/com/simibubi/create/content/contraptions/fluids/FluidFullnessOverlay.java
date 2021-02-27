package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;


//Currently only used for Tank/Drain/Spout/Basin goggle tooltips, as fullness-based replacement for the stress-based tooltips in IRotate
public enum FluidFullnessOverlay {
        LOW,
        MEDIUM,
        HIGH,
        FULL;

        public TextFormatting getAbsoluteColor() {
            return this == LOW ? TextFormatting.YELLOW : this == MEDIUM ? TextFormatting.GOLD : TextFormatting.RED;
        }

        public TextFormatting getRelativeColor() {
            return this == LOW ? TextFormatting.GREEN : this == MEDIUM ? TextFormatting.YELLOW : this == HIGH ? TextFormatting.GOLD : TextFormatting.RED;
        }

        public static FluidFullnessOverlay of(double fullnessPercent){
            if (fullnessPercent >= 1) return FluidFullnessOverlay.FULL;
            else if (fullnessPercent > .75d) return FluidFullnessOverlay.HIGH;
            else if (fullnessPercent > .5d) return FluidFullnessOverlay.MEDIUM;
            else return FluidFullnessOverlay.LOW;
        }

        public static String getFormattedFullnessText(double fullnessPercent){
            FluidFullnessOverlay fullnessLevel = of(fullnessPercent);
            TextFormatting color = fullnessLevel.getRelativeColor();
            if (fullnessPercent == 0)
                return TextFormatting.DARK_GRAY + ItemDescription.makeProgressBar(3, -1)
                        + Lang.translate("gui.stores_fluid.empty");

            String level = color + ItemDescription.makeProgressBar(3, Math.min(fullnessLevel.ordinal(), 2));
            level += Lang.translate("tooltip.fluidFullness."+Lang.asId(fullnessLevel.name()));

            level += String.format(" (%s%%) ", (int) (fullnessPercent * 100));

            return level;
        }

        public static String getFormattedCapacityText(int amount, int capacity){
            FluidFullnessOverlay fullnessLevel = of((double) amount / capacity);
            TextFormatting color = fullnessLevel.getRelativeColor();

            String mb = Lang.translate("generic.unit.millibuckets");
            String capacityString = color + "%s" + mb + TextFormatting.GRAY + " / " + TextFormatting.DARK_GRAY + "%s" + mb;

            if (amount == 0)
                return TextFormatting.DARK_GRAY + IHaveGoggleInformation.format(capacity) + mb;

            return String.format(capacityString, IHaveGoggleInformation.format(amount), IHaveGoggleInformation.format(capacity));
        }

        public static String getFormattedFluidTypeText(FluidStack fluid, double fullnessPercent){
            FluidFullnessOverlay fullnessLevel = of(fullnessPercent);
            TextFormatting color = fullnessLevel.getRelativeColor();

            if (AllFluids.POTION.get().getFluid().isEquivalentTo(fluid.getFluid())) {
                return color + PotionFluidHandler.getPotionName(fluid).getFormattedText();
            } else {
                return color + fluid.getDisplayName().getFormattedText();
            }
        }
    }

