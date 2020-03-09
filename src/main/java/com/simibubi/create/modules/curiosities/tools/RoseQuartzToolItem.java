package com.simibubi.create.modules.curiosities.tools;

import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.foundation.item.AbstractToolItem;
import com.simibubi.create.foundation.item.AllToolTypes;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

public class RoseQuartzToolItem extends AbstractToolItem {

	static Multimap<String, AttributeModifier> rangeModifier;
	static final UUID attributeId = UUID.fromString("7f7dbdb2-0d0d-458a-aa40-ac7633691f66");

	public RoseQuartzToolItem(float attackDamageIn, float attackSpeedIn, Properties builder, AllToolTypes... types) {
		super(attackDamageIn, attackSpeedIn, AllToolTiers.ROSE_QUARTZ, builder, types);
		if (rangeModifier == null) {
			rangeModifier = HashMultimap.create();
			rangeModifier.put(PlayerEntity.REACH_DISTANCE.getName(),
					new AttributeModifier(attributeId, "Range modifier", 3, AttributeModifier.Operation.ADDITION));
		}
	}

}
