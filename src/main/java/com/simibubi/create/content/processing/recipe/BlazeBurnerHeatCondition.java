package com.simibubi.create.content.processing.recipe;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.recipe.HeatCondition;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import net.createmod.catnip.lang.Lang;

import java.util.List;

public enum BlazeBurnerHeatCondition implements HeatCondition {
	HEATED(0xE88300, HeatLevel.KINDLED, AllBlocks.BLAZE_BURNER),
	SUPERHEATED(0x5C93E8, HeatLevel.SEETHING, AllBlocks.BLAZE_BURNER, AllItems.BLAZE_CAKE);

	private final int color;
	private final HeatLevel heatLevel;
	private final List<ItemLike> itemsForItemHints;
	private List<ItemStack> itemHints;

	BlazeBurnerHeatCondition(int color, HeatLevel heatLevel, ItemLike... hints) {
		this.color = color;
		this.heatLevel = heatLevel;
		this.itemsForItemHints = List.of(hints);
	}

	@Override
	public boolean test(Level level, BlockPos testPos) {
		if (level.getBlockEntity(testPos) instanceof BasinBlockEntity basin)
			return basin.getHeatLevel().isAtLeast(heatLevel); // use the Basin's caching to improve performance here

		BlockState stateBelow = level.getBlockState(testPos.below());
		if (!stateBelow.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) return false;

		HeatLevel basinHeat = stateBelow.getValue(BlazeBurnerBlock.HEAT_LEVEL);
		return basinHeat.isAtLeast(heatLevel);
	}

	@Override
	@NotNull
	public List<ItemStack> getItemHints() {
		if (this.itemHints == null)
			this.itemHints = this.itemsForItemHints.stream().map(ItemStack::new).toList();
		return itemHints;
	}

	@Override
	public int getColor() {
		return color;
	}

	public String serialize() {
		return Lang.asId(name());
	}
}
