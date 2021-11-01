package com.simibubi.create.content.contraptions.components.crafter;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

public class CrafterHelper {

	public static MechanicalCrafterTileEntity getCrafter(BlockAndTintGetter reader, BlockPos pos) {
		BlockEntity te = reader.getBlockEntity(pos);
		if (!(te instanceof MechanicalCrafterTileEntity))
			return null;
		return (MechanicalCrafterTileEntity) te;
	}

	public static ConnectedInputHandler.ConnectedInput getInput(BlockAndTintGetter reader, BlockPos pos) {
		MechanicalCrafterTileEntity crafter = getCrafter(reader, pos);
		return crafter == null ? null : crafter.input;
	}

}
