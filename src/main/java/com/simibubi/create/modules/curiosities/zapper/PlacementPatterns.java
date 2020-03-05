package com.simibubi.create.modules.curiosities.zapper;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public enum PlacementPatterns {

	Solid(ScreenResources.I_PATTERN_SOLID),
	Checkered(ScreenResources.I_PATTERN_CHECKERED),
	InverseCheckered(ScreenResources.I_PATTERN_CHECKERED_INVERSED),
	Chance25(ScreenResources.I_PATTERN_CHANCE_25),
	Chance50(ScreenResources.I_PATTERN_CHANCE_50),
	Chance75(ScreenResources.I_PATTERN_CHANCE_75);

	public String translationKey;
	public ScreenResources icon;

	private PlacementPatterns(ScreenResources icon) {
		this.translationKey = Lang.asId(name());
		this.icon = icon;
	}

	public static void applyPattern(List<BlockPos> blocksIn, ItemStack stack) {
		CompoundNBT tag = stack.getTag();
		PlacementPatterns pattern =
			!tag.contains("Pattern") ? Solid : valueOf(tag.getString("Pattern"));
		Random r = new Random();
		Predicate<BlockPos> filter = Predicates.alwaysFalse();
	
		switch (pattern) {
		case Chance25:
			filter = pos -> r.nextBoolean() || r.nextBoolean();
			break;
		case Chance50:
			filter = pos -> r.nextBoolean();
			break;
		case Chance75:
			filter = pos -> r.nextBoolean() && r.nextBoolean();
			break;
		case Checkered:
			filter = pos -> (pos.getX() + pos.getY() + pos.getZ()) % 2 == 0;
			break;
		case InverseCheckered:
			filter = pos -> (pos.getX() + pos.getY() + pos.getZ()) % 2 != 0;
			break;
		case Solid:
		default:
			break;
		}
	
		blocksIn.removeIf(filter);
	}

}
