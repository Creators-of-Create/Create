package com.simibubi.create.content.contraptions.components.crafter;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

public class CrafterHelper {

	public static MechanicalCrafterTileEntity getCrafter(ILightReader reader, BlockPos pos) {
		TileEntity te = reader.getTileEntity(pos);
		if (!(te instanceof MechanicalCrafterTileEntity))
			return null;
		return (MechanicalCrafterTileEntity) te;
	}

	public static ConnectedInputHandler.ConnectedInput getInput(ILightReader reader, BlockPos pos) {
		MechanicalCrafterTileEntity crafter = getCrafter(reader, pos);
		return crafter == null ? null : crafter.input;
	}

}
