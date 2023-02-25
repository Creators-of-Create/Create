package com.simibubi.create.content.logistics.block.display.target;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeBlock;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NixieTubeDisplayTarget extends SingleLineDisplayTarget {

	@Override
	protected void acceptLine(MutableComponent text, DisplayLinkContext context) {
		String tagElement = Component.Serializer.toJson(text);
		NixieTubeBlock.walkNixies(context.level(), context.getTargetPos(), (currentPos, rowPosition) -> {
			BlockEntity blockEntity = context.level()
				.getBlockEntity(currentPos);
			if (blockEntity instanceof NixieTubeBlockEntity nixie)
				nixie.displayCustomText(tagElement, rowPosition);
		});
	}

	@Override
	protected int getWidth(DisplayLinkContext context) {
		MutableInt count = new MutableInt(0);
		NixieTubeBlock.walkNixies(context.level(), context.getTargetPos(), (currentPos, rowPosition) -> count.add(2));
		return count.intValue();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AABB getMultiblockBounds(LevelAccessor level, BlockPos pos) {
		MutableObject<BlockPos> start = new MutableObject<>(null);
		MutableObject<BlockPos> end = new MutableObject<>(null);
		NixieTubeBlock.walkNixies(level, pos, (currentPos, rowPosition) -> {
			end.setValue(currentPos);
			if (start.getValue() == null)
				start.setValue(currentPos);
		});

		BlockPos diffToCurrent = start.getValue()
			.subtract(pos);
		BlockPos diff = end.getValue()
			.subtract(start.getValue());

		return super.getMultiblockBounds(level, pos).move(diffToCurrent)
			.expandTowards(Vec3.atLowerCornerOf(diff));
	}

}
