package com.simibubi.create.modules.schematics.item;

import java.util.List;

import com.simibubi.create.AllKeys;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class SchematicAndQuillItem extends Item {

	public SchematicAndQuillItem(Properties properties) {
		super(properties);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (AllKeys.shiftDown()) {
			TextFormatting gray = TextFormatting.GRAY;
			TextFormatting blue = TextFormatting.BLUE;

			tooltip.add(new StringTextComponent(gray + "Saves selected blocks in a Schematic File."));
			tooltip.add(new StringTextComponent(""));
			tooltip.add(new StringTextComponent(blue + "Step 1: Select two corner points"));
			tooltip.add(new StringTextComponent(gray + "Hold [CTRL] to select at a fixed distance."));
			tooltip.add(new StringTextComponent(gray + "[CTRL]-Scroll to modify the distance."));
			tooltip.add(new StringTextComponent("Right-Click to put a point."));
			tooltip.add(new StringTextComponent(""));
			tooltip.add(new StringTextComponent(blue + "Step 2: Adjust the bounding box"));
			tooltip.add(new StringTextComponent(gray + "Point at the Selection and use"));
			tooltip.add(new StringTextComponent(gray + "[CTRL]-Scroll to move the face in-/outward."));
			tooltip.add(new StringTextComponent("Right-Click to save."));
			tooltip.add(new StringTextComponent(""));
			tooltip.add(new StringTextComponent(gray + "Use Sneak-Right-Click to reset."));
		} else
			tooltip.add(new StringTextComponent(TextFormatting.DARK_GRAY + "< Hold Shift >"));
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

}
