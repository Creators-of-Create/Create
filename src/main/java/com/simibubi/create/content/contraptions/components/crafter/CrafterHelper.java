package com.simibubi.create.content.contraptions.components.crafter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CrafterHelper {

	public static MechanicalCrafterBlockEntity getCrafter(BlockAndTintGetter reader, BlockPos pos) {
		BlockEntity blockEntity = reader.getBlockEntity(pos);
		if (!(blockEntity instanceof MechanicalCrafterBlockEntity))
			return null;
		return (MechanicalCrafterBlockEntity) blockEntity;
	}

	public static ConnectedInputHandler.ConnectedInput getInput(BlockAndTintGetter reader, BlockPos pos) {
		MechanicalCrafterBlockEntity crafter = getCrafter(reader, pos);
		return crafter == null ? null : crafter.input;
	}

}
