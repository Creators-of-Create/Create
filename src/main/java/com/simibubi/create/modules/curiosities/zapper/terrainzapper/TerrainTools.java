package com.simibubi.create.modules.curiosities.zapper.terrainzapper;

import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public enum TerrainTools {

	Fill(ScreenResources.I_FILL),
	Place(ScreenResources.I_PLACE),
	Replace(ScreenResources.I_REPLACE),
	Clear(ScreenResources.I_CLEAR),
	Overlay(ScreenResources.I_OVERLAY),
	Flatten(ScreenResources.I_FLATTEN);

	public String translationKey;
	public ScreenResources icon;

	private TerrainTools(ScreenResources icon) {
		this.translationKey = Lang.asId(name());
		this.icon = icon;
	}

	public boolean requiresSelectedBlock() {
		return this != Clear && this != Flatten;
	}

	public void run(World world, List<BlockPos> targetPositions, Direction facing, @Nullable BlockState paintedState) {
		switch (this) {
		case Clear:
			targetPositions.forEach(p -> world.setBlockState(p, Blocks.AIR.getDefaultState()));
			break;
		case Fill:
			targetPositions.forEach(p -> {
				BlockState toReplace = world.getBlockState(p);
				if (!isReplaceable(toReplace))
					return;
				world.setBlockState(p, paintedState);
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

				p = p.up();

				BlockState toReplace = world.getBlockState(p);
				if (!isReplaceable(toReplace))
					return;
				world.setBlockState(p, paintedState);
			});
			break;
		case Place:
			targetPositions.forEach(p -> {
				world.setBlockState(p, paintedState);
			});
			break;
		case Replace:
			targetPositions.forEach(p -> {
				BlockState toReplace = world.getBlockState(p);
				if (isReplaceable(toReplace))
					return;
				world.setBlockState(p, paintedState);
			});
			break;
		}
	}

	public static boolean isReplaceable(BlockState toReplace) {
		return toReplace.getMaterial().isReplaceable();
	}

}
