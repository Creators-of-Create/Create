package com.simibubi.create.api.recipe;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HeatCondition {
	boolean test(BlockGetter getter, BlockPos basinPos);

	String getTranslationKey();

	@Nullable
	default IDrawable visualize() {
		return null;
	}

	@NotNull
	default List<ItemStack> getItemHints() {
		return List.of();
	}

	default int getColor() {
		return 0xffffff;
	}

	default int ordinal() {
		return CreateBuiltInRegistries.HEAT_CONDITION.getId(this);
	}
}
