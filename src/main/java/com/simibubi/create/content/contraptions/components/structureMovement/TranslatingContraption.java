package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public abstract class TranslatingContraption extends Contraption {

	protected Set<BlockPos> cachedColliders;
	protected Direction cachedColliderDirection;

	public Set<BlockPos> getColliders(World world, Direction movementDirection) {
		if (getBlocks() == null)
			return Collections.EMPTY_SET;
		if (cachedColliders == null || cachedColliderDirection != movementDirection) {
			cachedColliders = new HashSet<>();
			cachedColliderDirection = movementDirection;

			for (BlockInfo info : getBlocks().values()) {
				BlockPos offsetPos = info.pos.offset(movementDirection);
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
	public void removeBlocksFromWorld(World world, BlockPos offset) {
		int count = blocks.size();
		super.removeBlocksFromWorld(world, offset);
		if (count != blocks.size()) {
			cachedColliders = null;
		}
	}

	@Override
	protected boolean canAxisBeStabilized(Axis axis) {
		return false;
	}
	
}
