package com.simibubi.create.content.processing.recipe;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.recipe.HeatCondition;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import mezz.jei.api.gui.drawable.IDrawable;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum BlazeBurnerHeatCondition implements HeatCondition {
	HEATED(0xE88300, HeatLevel.KINDLED, AllBlocks.BLAZE_BURNER),
	SUPERHEATED(0x5C93E8, HeatLevel.SEETHING, AllBlocks.BLAZE_BURNER, AllItems.BLAZE_CAKE);

	private static final AnimatedBlazeBurner blaze = new AnimatedBlazeBurner();

	private final int color;
	private final HeatLevel heatLevel;
	private final List<ItemLike> itemHints;

	BlazeBurnerHeatCondition(int color, HeatLevel heatLevel, ItemLike... hints) {
		this.color = color;
		this.heatLevel = heatLevel;
		this.itemHints = List.of(hints);
	}

	@Override
	public boolean test(BlockGetter getter, BlockPos basinPos) {
		BlockState stateBelow = getter.getBlockState(basinPos.below());
		if(!stateBelow.is(AllBlocks.BLAZE_BURNER.get())) return false;
		HeatLevel basinHeat = stateBelow.getValue(BlazeBurnerBlock.HEAT_LEVEL);
		return basinHeat.isAtLeast(heatLevel);
	}

	public BlazeBurnerBlock.HeatLevel visualizeAsBlazeBurner() {
		if (this == SUPERHEATED)
			return HeatLevel.SEETHING;
		if (this == HEATED)
			return HeatLevel.KINDLED;
		return HeatLevel.NONE;
	}

	@Override
	public IDrawable visualize() {
		return blaze.withHeat(this.visualizeAsBlazeBurner());
	}

	@Override
	@NotNull
	public List<ItemStack> getItemHints() {
		return itemHints.stream().map(ItemStack::new).toList();
	}

	public String serialize() {
		return Lang.asId(name());
	}

	public String getTranslationKey() {
		return "create.recipe.heat_requirement." + serialize();
	}

	public int getColor() {
		return color;
	}

}
