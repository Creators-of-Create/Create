package com.simibubi.create.foundation.behaviour.inventory;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.modules.logistics.block.inventories.FlexcrateBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.state.properties.ChestType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

public class SynchronizedExtraction {

	static boolean extractSynchronized(ILightReader reader, BlockPos inventoryPos) {
		List<SingleTargetAutoExtractingBehaviour> actors = getAllSyncedExtractors(reader, inventoryPos);
		int startIndex = actors.size() - 1;
		boolean success = false;

		for (; startIndex > 0; startIndex--)
			if (actors.get(startIndex).advantageOnNextSync)
				break;
		for (int i = 0; i < actors.size(); i++)
			success |= actors.get((startIndex + i) % actors.size()).extractFromInventory();

		if (success) {
			actors.get(startIndex).advantageOnNextSync = false;
			actors.get((startIndex + 1) % actors.size()).advantageOnNextSync = true;
		}

		return success;
	}

	private static List<SingleTargetAutoExtractingBehaviour> getAllSyncedExtractors(ILightReader reader,
			BlockPos inventoryPos) {
		List<SingleTargetAutoExtractingBehaviour> list = new ArrayList<>();
		List<BlockPos> inventoryPositions = new ArrayList<>();
		inventoryPositions.add(inventoryPos);

		// Sync across double chests
		BlockState blockState = reader.getBlockState(inventoryPos);
		if (blockState.getBlock() instanceof ChestBlock)
			if (blockState.get(ChestBlock.TYPE) != ChestType.SINGLE)
				inventoryPositions.add(inventoryPos.offset(ChestBlock.getDirectionToAttached(blockState)));

		// Sync across flexcrates
		if (AllBlocks.FLEXCRATE.typeOf(blockState))
			if (blockState.get(FlexcrateBlock.DOUBLE))
				inventoryPositions.add(inventoryPos.offset(blockState.get(FlexcrateBlock.FACING)));

		for (BlockPos pos : inventoryPositions) {
			for (Direction direction : Direction.values()) {
				SingleTargetAutoExtractingBehaviour behaviour = TileEntityBehaviour.get(reader, pos.offset(direction),
						SingleTargetAutoExtractingBehaviour.TYPE);
				if (behaviour == null)
					continue;
				if (!behaviour.synced)
					continue;
				if (behaviour.getShouldPause().get())
					continue;
				if (!behaviour.getShouldExtract().get())
					continue;
				if (!behaviour.inventories.keySet().stream()
						.anyMatch(p -> p.getKey().add(behaviour.getPos()).equals(pos)))
					continue;

				list.add(behaviour);
			}
		}
		return list;
	}

}
