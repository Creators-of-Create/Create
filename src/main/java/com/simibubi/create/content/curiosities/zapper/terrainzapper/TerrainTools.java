package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public enum TerrainTools {

	Fill(AllIcons.I_FILL),
	Place(AllIcons.I_PLACE),
	Replace(AllIcons.I_REPLACE),
	Clear(AllIcons.I_CLEAR),
	Overlay(AllIcons.I_OVERLAY),
	Flatten(AllIcons.I_FLATTEN),

	;
	
	public String translationKey;
	public AllIcons icon;

	private TerrainTools(AllIcons icon) {
		this.translationKey = Lang.asId(name());
		this.icon = icon;
	}

	public boolean requiresSelectedBlock() {
		return this != Clear && this != Flatten;
	}

	public void run(Level world, List<BlockPos> targetPositions, Direction facing, @Nullable BlockState paintedState, @Nullable CompoundTag data, Player player) {
		switch (this) {
		case Clear:
			targetPositions.forEach(p -> world.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState()));
			break;
		case Fill:
			targetPositions.forEach(p -> {
				BlockState toReplace = world.getBlockState(p);
				if (!isReplaceable(toReplace))
					return;
				world.setBlockAndUpdate(p, paintedState);
				ZapperItem.setBlockEntityData(world, p, paintedState, data, player);
			});
			break;
		case Flatten:
			FlattenTool.apply(world, targetPositions, facing);
			break;
		case Overlay:
			targetPositions.forEach(p -> {
				BlockState toOverlay = world.getBlockState(p);
				if (isReplaceable(toOverlay))
					return;
				if (toOverlay == paintedState)
					return;

				p = p.above();

				BlockState toReplace = world.getBlockState(p);
				if (!isReplaceable(toReplace))
					return;
				world.setBlockAndUpdate(p, paintedState);
				ZapperItem.setBlockEntityData(world, p, paintedState, data, player);
			});
			break;
		case Place:
			targetPositions.forEach(p -> {
				world.setBlockAndUpdate(p, paintedState);
				ZapperItem.setBlockEntityData(world, p, paintedState, data, player);
			});
			break;
		case Replace:
			targetPositions.forEach(p -> {
				BlockState toReplace = world.getBlockState(p);
				if (isReplaceable(toReplace))
					return;
				world.setBlockAndUpdate(p, paintedState);
				ZapperItem.setBlockEntityData(world, p, paintedState, data, player);
			});
			break;
		}
	}

	public static boolean isReplaceable(BlockState toReplace) {
		return toReplace.getMaterial().isReplaceable();
	}

}
