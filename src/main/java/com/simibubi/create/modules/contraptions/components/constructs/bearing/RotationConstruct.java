package com.simibubi.create.modules.contraptions.components.constructs.bearing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simibubi.create.AllBlockTags;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.modules.contraptions.components.constructs.ChassisTileEntity;
import com.simibubi.create.modules.contraptions.components.constructs.RadialChassisBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class RotationConstruct {

	protected Map<BlockPos, BlockInfo> blocks;
	protected int sailBlocks;
	
	public RotationConstruct() {
		blocks = new HashMap<>();
		sailBlocks = 0;
	}

	public static RotationConstruct getAttachedForRotating(World world, BlockPos pos, Direction direction) {
		RotationConstruct construct = new RotationConstruct();

		if (!construct.collectAttached(world, pos, direction))
			return null;

		return construct;
	}
	
	public int getSailBlocks() {
		return sailBlocks;
	}

	protected boolean collectAttached(World world, BlockPos pos, Direction direction) {
		if (isFrozen())
			return false;

		// Find chassis
		List<BlockInfo> chassis = collectChassis(world, pos, direction);
		if (chassis == null)
			return false;

		// Get single block
		if (chassis.isEmpty()) {
			BlockPos blockPos = pos.offset(direction);
			BlockState state = world.getBlockState(pos.offset(direction));

			if (state.getMaterial().isReplaceable() || state.isAir(world, blockPos))
				return true;
			if (state.getCollisionShape(world, blockPos).isEmpty())
				return true;
			if (!canRotate(world, blockPos, direction))
				return false;

			blocks.put(blockPos, new BlockInfo(blockPos.subtract(pos), state, null));

			// Get attached blocks by chassis
		} else {
			List<BlockInfo> attachedBlocksByChassis = getAttachedBlocksByChassis(world, direction, chassis);
			if (attachedBlocksByChassis == null)
				return false;
			attachedBlocksByChassis.forEach(info -> {
				if (isSailBlock(info.state))
					sailBlocks++;
				blocks.put(info.pos, new BlockInfo(info.pos.subtract(pos), info.state, info.nbt));
			});
		}

		return true;
	}

	private List<BlockInfo> getAttachedBlocksByChassis(World world, Direction direction, List<BlockInfo> chassis) {
		List<BlockInfo> blocks = new ArrayList<>();
		RadialChassisBlock def = (RadialChassisBlock) AllBlocks.ROTATION_CHASSIS.block;

		for (BlockInfo chassisBlock : chassis) {
			blocks.add(chassisBlock);
			BlockState state = chassisBlock.state;
			BlockPos currentPos = chassisBlock.pos;
			TileEntity tileEntity = world.getTileEntity(currentPos);

			if (!(tileEntity instanceof ChassisTileEntity))
				return null;

			int chassisRange = ((ChassisTileEntity) tileEntity).getRange();
			Set<BlockPos> visited = new HashSet<>();

			for (Direction facing : Direction.values()) {
				if (facing.getAxis() == direction.getAxis())
					continue;
				if (!state.get(def.getGlueableSide(state, facing)))
					continue;

				BlockPos startPos = currentPos.offset(facing);
				List<BlockPos> frontier = new LinkedList<>();
				frontier.add(startPos);
				CompoundNBT nbt = new CompoundNBT();
				nbt.putInt("Range", chassisRange);

				while (!frontier.isEmpty()) {
					BlockPos searchPos = frontier.remove(0);
					BlockState searchedState = world.getBlockState(searchPos);

					if (visited.contains(searchPos))
						continue;
					if (!searchPos.withinDistance(currentPos, chassisRange + .5f))
						continue;
					if (searchedState.getMaterial().isReplaceable() || state.isAir(world, searchPos))
						continue;
					if (searchedState.getCollisionShape(world, searchPos).isEmpty())
						continue;
					if (!canRotate(world, searchPos, direction))
						return null;

					visited.add(searchPos);

					blocks.add(new BlockInfo(searchPos, searchedState,
							AllBlocks.ROTATION_CHASSIS.typeOf(searchedState) ? nbt : null));

					for (Direction offset : Direction.values()) {
						if (offset.getAxis() == direction.getAxis())
							continue;
						if (searchPos.equals(currentPos) && offset != facing)
							continue;

						frontier.add(searchPos.offset(offset));
					}
				}
			}
		}
		return blocks;
	}

	private List<BlockInfo> collectChassis(World world, BlockPos pos, Direction direction) {
		List<BlockInfo> chassis = new ArrayList<>();
		for (int distance = 1; distance <= CreateConfig.parameters.maxChassisForRotation.get(); distance++) {
			BlockPos currentPos = pos.offset(direction, distance);
			if (!world.isBlockPresent(currentPos))
				return chassis;

			BlockState state = world.getBlockState(currentPos);
			if (!AllBlocks.ROTATION_CHASSIS.typeOf(state))
				return chassis;
			if (direction.getAxis() != state.get(BlockStateProperties.AXIS))
				return chassis;

			chassis.add(new BlockInfo(currentPos, state, null));
		}
		return chassis;
	}

	private static boolean isSailBlock(BlockState state) {
		return AllBlockTags.WINDMILL_SAILS.matches(state);
	}

	public CompoundNBT writeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		ListNBT blocks = new ListNBT();
		for (BlockInfo block : this.blocks.values()) {
			CompoundNBT c = new CompoundNBT();
			c.put("Block", NBTUtil.writeBlockState(block.state));
			c.put("Pos", NBTUtil.writeBlockPos(block.pos));
			if (block.nbt != null)
				c.put("Data", block.nbt);
			blocks.add(c);
		}

		nbt.put("Blocks", blocks);
		return nbt;
	}

	public static RotationConstruct fromNBT(CompoundNBT nbt) {
		RotationConstruct construct = new RotationConstruct();
		nbt.getList("Blocks", 10).forEach(c -> {
			CompoundNBT comp = (CompoundNBT) c;
			BlockInfo info = new BlockInfo(NBTUtil.readBlockPos(comp.getCompound("Pos")),
					NBTUtil.readBlockState(comp.getCompound("Block")),
					comp.contains("Data") ? comp.getCompound("Data") : null);
			construct.blocks.put(info.pos, info);
		});

		return construct;
	}

	private static boolean canRotate(World world, BlockPos pos, Direction direction) {
		return PistonBlock.canPush(world.getBlockState(pos), world, pos, direction, true, direction)
				|| AllBlocks.ROTATION_CHASSIS.typeOf(world.getBlockState(pos));
	}

	public static boolean isFrozen() {
		return CreateConfig.parameters.freezeRotationConstructs.get();
	}

}
