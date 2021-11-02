package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public abstract class TranslatingContraption extends Contraption {

	protected Set<BlockPos> cachedColliders;
	protected Direction cachedColliderDirection;

	public Set<BlockPos> getColliders(Level world, Direction movementDirection) {
		if (getBlocks() == null)
			return Collections.emptySet();
		if (cachedColliders == null || cachedColliderDirection != movementDirection) {
			cachedColliders = new HashSet<>();
			cachedColliderDirection = movementDirection;

			for (StructureBlockInfo info : getBlocks().values()) {
				BlockPos offsetPos = info.pos.relative(movementDirection);
				if (info.state.getCollisionShape(world, offsetPos)
					.isEmpty())
					continue;
				if (getBlocks().containsKey(offsetPos)
					&& !getBlocks().get(offsetPos).state.getCollisionShape(world, offsetPos)
						.isEmpty())
					continue;
				cachedColliders.add(info.pos);
			}

		}
		return cachedColliders;
	}

	@Override
	public void removeBlocksFromWorld(Level world, BlockPos offset) {
		int count = blocks.size();
		super.removeBlocksFromWorld(world, offset);
		if (count != blocks.size()) {
			cachedColliders = null;
		}
	}

	@Override
	public boolean canBeStabilized(Direction facing, BlockPos localPos) {
		return false;
	}
	
}
