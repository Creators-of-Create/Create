package com.simibubi.create.content.logistics.block.data.target;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.block.data.DataGathererBehaviour;
import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class DataGathererTarget extends DataGathererBehaviour {

	public abstract void acceptText(int line, List<MutableComponent> text, DataGathererContext context);

	public abstract DataTargetStats provideStats(DataGathererContext context);

	public AABB getMultiblockBounds(LevelAccessor level, BlockPos pos) {
		VoxelShape shape = level.getBlockState(pos)
			.getShape(level, pos);
		if (shape.isEmpty())
			return new AABB(pos);
		return shape.bounds()
			.move(pos);
	}

	public Component getLineOptionText(int line) {
		return Lang.translate("data_target.line", line + 1);
	}

	public static void reserve(int line, BlockEntity target, DataGathererContext context) {
		if (line == 0)
			return;

		CompoundTag tag = target.getTileData();
		CompoundTag compound = tag.getCompound("DataGatherer");
		compound.putLong("Line" + line, context.te()
			.getBlockPos()
			.asLong());
		tag.put("DataGatherer", compound);
	}

	public boolean isReserved(int line, BlockEntity target, DataGathererContext context) {
		CompoundTag tag = target.getTileData();
		CompoundTag compound = tag.getCompound("DataGatherer");

		if (!compound.contains("Line" + line))
			return false;

		long l = compound.getLong("Line" + line);
		BlockPos reserved = BlockPos.of(l);

		if (!reserved.equals(context.te()
			.getBlockPos()) && AllBlocks.DATA_GATHERER.has(target.getLevel()
				.getBlockState(reserved)))
			return true;

		compound.remove("Line" + line);
		if (compound.isEmpty())
			tag.remove("DataGatherer");
		return false;
	}

}
