package com.simibubi.create.content.redstone.displayLink.target;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.redstone.displayLink.DisplayBehaviour;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class DisplayTarget extends DisplayBehaviour {

	public abstract void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context);

	public abstract DisplayTargetStats provideStats(DisplayLinkContext context);

	public AABB getMultiblockBounds(LevelAccessor level, BlockPos pos) {
		VoxelShape shape = level.getBlockState(pos)
			.getShape(level, pos);
		if (shape.isEmpty())
			return new AABB(pos);
		return shape.bounds()
			.move(pos);
	}

	public Component getLineOptionText(int line) {
		return Lang.translateDirect("display_target.line", line + 1);
	}

	public static void reserve(int line, BlockEntity target, DisplayLinkContext context) {
		if (line == 0)
			return;

		CompoundTag tag = target.getTileData();
		CompoundTag compound = tag.getCompound("DisplayLink");
		compound.putLong("Line" + line, context.blockEntity()
			.getBlockPos()
			.asLong());
		tag.put("DisplayLink", compound);
	}

	public boolean isReserved(int line, BlockEntity target, DisplayLinkContext context) {
		CompoundTag tag = target.getTileData();
		CompoundTag compound = tag.getCompound("DisplayLink");

		if (!compound.contains("Line" + line))
			return false;

		long l = compound.getLong("Line" + line);
		BlockPos reserved = BlockPos.of(l);

		if (!reserved.equals(context.blockEntity()
			.getBlockPos()) && AllBlocks.DISPLAY_LINK.has(target.getLevel()
				.getBlockState(reserved)))
			return true;

		compound.remove("Line" + line);
		if (compound.isEmpty())
			tag.remove("DisplayLink");
		return false;
	}

}
