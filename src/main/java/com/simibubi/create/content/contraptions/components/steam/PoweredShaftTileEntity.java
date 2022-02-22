package com.simibubi.create.content.contraptions.components.steam;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.repack.joml.Math;
import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PoweredShaftTileEntity extends GeneratingKineticTileEntity {

	public Map<BlockPos, Integer> sources;

	public PoweredShaftTileEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		sources = new HashMap<>();
	}

	public void update(BlockPos sourcePos, int score) {
		BlockPos key = worldPosition.subtract(sourcePos);
		Integer prev = sources.put(key, score);
		if (prev != null && prev.intValue() == score)
			return;
		updateGeneratedRotation();
	}

	public void remove(BlockPos sourcePos) {
		BlockPos key = worldPosition.subtract(sourcePos);
		Integer prev = sources.remove(key);
		if (prev == null)
			return;
		updateGeneratedRotation();
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		compound.put("Sources", NBTHelper.writeCompoundList(sources.entrySet(), e -> {
			CompoundTag nbt = new CompoundTag();
			nbt.put("Pos", NbtUtils.writeBlockPos(e.getKey()));
			nbt.putInt("Value", e.getValue());
			return nbt;
		}));
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		sources.clear();
		NBTHelper.iterateCompoundList(compound.getList("Sources", Tag.TAG_COMPOUND),
			c -> sources.put(NbtUtils.readBlockPos(c.getCompound("Pos")), c.getInt("Value")));
	}

	@Override
	public float getGeneratedSpeed() {
		int max = 0;
		for (Integer integer : sources.values())
			if (Math.abs(integer) > max)
				max = integer;
		return 8 * max;
	}

	@Override
	public int getRotationAngleOffset(Axis axis) {
		int combinedCoords = axis.choose(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
		return super.getRotationAngleOffset(axis) + (combinedCoords % 2 == 0 ? 180 : 0);
	}

}
