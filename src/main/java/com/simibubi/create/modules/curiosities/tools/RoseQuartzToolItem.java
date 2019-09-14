package com.simibubi.create.modules.curiosities.tools;

import com.simibubi.create.foundation.item.AbstractToolItem;
import com.simibubi.create.foundation.item.AllToolTypes;

public class RoseQuartzToolItem extends AbstractToolItem {

	public RoseQuartzToolItem(float attackDamageIn, float attackSpeedIn, Properties builder, AllToolTypes... types) {
		super(attackDamageIn, attackSpeedIn, AllToolTiers.ROSE_QUARTZ, builder, types);
	}

}
