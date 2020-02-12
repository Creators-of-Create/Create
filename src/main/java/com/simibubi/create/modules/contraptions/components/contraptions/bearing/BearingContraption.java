package com.simibubi.create.modules.contraptions.components.contraptions.bearing;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlockTags;
import com.simibubi.create.modules.contraptions.components.contraptions.Contraption;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class BearingContraption extends Contraption {

	protected int sailBlocks;
	protected Direction facing;

	public static BearingContraption assembleBearingAt(World world, BlockPos pos, Direction direction) {
		if (isFrozen())
			return null;
		BearingContraption construct = new BearingContraption();
		construct.facing = direction;
		if (!construct.searchMovedStructure(world, pos.offset(direction), direction))
			return null;
		construct.initActors(world);
		return construct;
	}

	@Override
	public void add(BlockPos pos, Pair<BlockInfo, TileEntity> capture) {
		if (AllBlockTags.WINDMILL_SAILS.matches(capture.getKey().state))
			sailBlocks++;
		super.add(pos, capture);
	}

	@Override
	public CompoundNBT writeNBT() {
		CompoundNBT tag = super.writeNBT();
		tag.putInt("Sails", sailBlocks);
		tag.putInt("facing", facing.getIndex());
		return tag;
	}

	@Override
	public void readNBT(World world, CompoundNBT tag) {
		sailBlocks = tag.getInt("Sails");
		facing = Direction.byIndex(tag.getInt("Facing"));
		super.readNBT(world, tag);
	}

	public int getSailBlocks() {
		return sailBlocks;
	}

}
