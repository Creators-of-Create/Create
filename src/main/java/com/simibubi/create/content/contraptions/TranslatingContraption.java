package com.simibubi.create.content.contraptions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public abstract class TranslatingContraption extends Contraption {

	protected Set<BlockPos> cachedColliders;
	protected Direction cachedColliderDirection;

	public Set<BlockPos> getOrCreateColliders(Level world, Direction movementDirection) {
		if (getBlocks() == null)
			return Collections.emptySet();
		if (cachedColliders == null || cachedColliderDirection != movementDirection) {
			cachedColliderDirection = movementDirection;
			cachedColliders= createColliders(world, movementDirection);
		}
		return cachedColliders;
	}

	public Set<BlockPos> createColliders(Level world, Direction movementDirection) {
		Set<BlockPos> colliders = new HashSet<>();
		for (StructureBlockInfo info : getBlocks().values()) {
			BlockPos offsetPos = info.pos.relative(movementDirection);
			if (info.state.getCollisionShape(world, offsetPos)
				.isEmpty())
				continue;
			if (getBlocks().containsKey(offsetPos)
				&& !getBlocks().get(offsetPos).state.getCollisionShape(world, offsetPos)
					.isEmpty())
				continue;
			colliders.add(info.pos);
		}
		return colliders;
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
		return AllConfigs.server().kinetics.stabiliseStableContraptions.get();
	}
	
}
