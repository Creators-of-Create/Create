package com.simibubi.create.content.curiosities.zapper;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class ZapperLog {

	private Level activeWorld;
	private List<List<StructureBlockInfo>> log = new LinkedList<>();
//	private int redoIndex;

	/*
	 * Undo and redo operations applied by tools what information is necessary?
	 * 
	 * For survival mode: does undo have the required blocks
	 * 
	 * For creative mode: what data did removed TEs have
	 * 
	 * When undo: remove added blocks (added -> air) replace replaced blocks (added
	 * -> before) add removed blocks (air -> before)
	 * 
	 */

	public void record(Level world, List<BlockPos> positions) {
//		if (maxLogLength() == 0)
//			return;
		if (world != activeWorld)
			log.clear();
		activeWorld = world;

		List<StructureBlockInfo> blocks = positions.stream().map(pos -> {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			return new StructureBlockInfo(pos, world.getBlockState(pos), blockEntity == null ? null : blockEntity.saveWithFullMetadata());
		}).collect(Collectors.toList());

		log.add(0, blocks);
//		redoIndex = 0;

//		if (maxLogLength() < log.size())
//			log.remove(log.size() - 1);
	}

//	protected Integer maxLogLength() {
//		return AllConfigs.SERVER.curiosities.zapperUndoLogLength.get();
//	}

	public void undo() {

	}

	public void redo() {

	}

}
