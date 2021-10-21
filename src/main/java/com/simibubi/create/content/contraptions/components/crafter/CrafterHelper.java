package com.simibubi.create.content.contraptions.components.crafter;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

public class CrafterHelper {

	public static MechanicalCrafterTileEntity getCrafter(IBlockDisplayReader reader, BlockPos pos) {
		TileEntity te = reader.getBlockEntity(pos);
		if (!(te instanceof MechanicalCrafterTileEntity))
			return null;
		return (MechanicalCrafterTileEntity) te;
	}

	public static ConnectedInputHandler.ConnectedInput getInput(IBlockDisplayReader reader, BlockPos pos) {
		MechanicalCrafterTileEntity crafter = getCrafter(reader, pos);
		return crafter == null ? null : crafter.input;
	}

}
