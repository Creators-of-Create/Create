package com.simibubi.create.content.contraptions.components.crafter;

import com.simibubi.create.content.contraptions.components.crafter.ConnectedInputHandler.ConnectedInput;

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

	public static boolean areCraftersConnected(BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos) {
		ConnectedInput input1 = getInput(reader, pos);
		ConnectedInput input2 = getInput(reader, otherPos);
	
		if (input1 == null || input2 == null)
			return false;
		if (input1.data.isEmpty() || input2.data.isEmpty())
			return false;
		try {
			if (pos.offset(input1.data.get(0))
					.equals(otherPos.offset(input2.data.get(0))))
				return true;
		} catch (IndexOutOfBoundsException e) {
			// race condition. data somehow becomes empty between the last 2 if statements
		}
		
		return false;
	}

}
