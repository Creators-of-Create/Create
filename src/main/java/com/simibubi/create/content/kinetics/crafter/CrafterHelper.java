package com.simibubi.create.content.kinetics.crafter;

import com.simibubi.create.content.kinetics.crafter.ConnectedInputHandler.ConnectedInput;

import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CrafterHelper {

	public static MechanicalCrafterBlockEntity getCrafter(BlockAndTintGetter reader, BlockPos pos) {
		return getCrafter(reader.getBlockEntity(pos));
	}

	public static MechanicalCrafterBlockEntity getCrafter(Level level, BlockPos pos) {
		return getCrafter(level.getBlockEntity(pos));
	}

	private static MechanicalCrafterBlockEntity getCrafter(BlockEntity blockEntity) {
		return blockEntity instanceof MechanicalCrafterBlockEntity crafter ? crafter : null;
	}

	public static ConnectedInputHandler.ConnectedInput getInput(BlockAndTintGetter reader, BlockPos pos) {
		MechanicalCrafterBlockEntity crafter = getCrafter(reader, pos);
		return crafter == null ? null : crafter.input;
	}

	public static ConnectedInputHandler.ConnectedInput getInput(Level level, BlockPos pos) {
		MechanicalCrafterBlockEntity crafter = getCrafter(level, pos);
		return crafter == null ? null : crafter.input;
	}

	public static boolean areCraftersConnected(BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos) {
		ConnectedInput input1 = getInput(reader, pos);
		ConnectedInput input2 = getInput(reader, otherPos);
		return areCraftersConnected(input1, input2, pos, otherPos);
	}

	public static boolean areCraftersConnected(Level level, BlockPos pos, BlockPos otherPos) {
		ConnectedInput input1 = getInput(level, pos);
		ConnectedInput input2 = getInput(level, otherPos);
		return areCraftersConnected(input1, input2, pos, otherPos);
	}

	private static boolean areCraftersConnected(ConnectedInput input1, ConnectedInput input2, BlockPos pos, BlockPos otherPos) {
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
