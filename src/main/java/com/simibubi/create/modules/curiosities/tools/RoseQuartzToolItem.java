package com.simibubi.create.modules.curiosities.tools;

import com.simibubi.create.foundation.item.AbstractToolItem;
import com.simibubi.create.foundation.item.AllToolTypes;
import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;

public class RoseQuartzToolItem extends AbstractToolItem {

	public RoseQuartzToolItem(float attackDamageIn, float attackSpeedIn, Properties builder, AllToolTypes... types) {
		super(attackDamageIn, attackSpeedIn, AllToolTiers.ROSE_QUARTZ, builder, types);
	}

	@Override
	public ItemDescription getDescription() {
		Palette color = Palette.Purple;
		return new ItemDescription(color).withSummary(
				"A Tool of finest craftmansship with sturdy materials and detailed decoration. The extended handle allows, at a slight cost of speed, for a "
						+ h("greater reach distance", color) + ".");
	}

}
