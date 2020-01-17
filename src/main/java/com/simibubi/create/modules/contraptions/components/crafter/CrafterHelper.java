package com.simibubi.create.modules.contraptions.components.crafter;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;

public class CrafterHelper {

	public static MechanicalCrafterTileEntity getCrafter(IEnviromentBlockReader reader, BlockPos pos) {
		TileEntity te = reader.getTileEntity(pos);
		if (!(te instanceof MechanicalCrafterTileEntity))
			return null;
		return (MechanicalCrafterTileEntity) te;
	}

	public static ConnectedInputHandler.ConnectedInput getInput(IEnviromentBlockReader reader, BlockPos pos) {
		MechanicalCrafterTileEntity crafter = getCrafter(reader, pos);
		return crafter == null ? null : crafter.input;
	}

}
