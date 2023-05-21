package com.simibubi.create.content.equipment.zapper;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public enum PlacementPatterns {

	Solid(AllIcons.I_PATTERN_SOLID),
	Checkered(AllIcons.I_PATTERN_CHECKERED),
	InverseCheckered(AllIcons.I_PATTERN_CHECKERED_INVERSED),
	Chance25(AllIcons.I_PATTERN_CHANCE_25),
	Chance50(AllIcons.I_PATTERN_CHANCE_50),
	Chance75(AllIcons.I_PATTERN_CHANCE_75);

	public final String translationKey;
	public final AllIcons icon;

	private PlacementPatterns(AllIcons icon) {
		this.translationKey = Lang.asId(name());
		this.icon = icon;
	}

	public static void applyPattern(List<BlockPos> blocksIn, ItemStack stack) {
		CompoundTag tag = stack.getTag();
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
