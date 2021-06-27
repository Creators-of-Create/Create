package com.simibubi.create.content.curiosities.bell;

import java.util.List;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import com.sun.org.apache.regexp.internal.RE;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CursedBellTileEntity extends AbstractBellTileEntity {

	public static final int DISTANCE = 5;
	public static final int RECHARGE_TICKS = 60;
	public int rechargeTicks = 0;

	public CursedBellTileEntity(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) { }

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		rechargeTicks = compound.getInt("Recharge");
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putInt("Recharge", rechargeTicks);
	}

	@Override
	public void tick() {
		super.tick();
		if (rechargeTicks > 0)
			rechargeTicks--;
	}

	@Override
	public boolean ring(World world, BlockPos pos, Direction direction) {
		if (rechargeTicks > 0)
			return false;

		if (!world.isRemote) {
			SoulPulseEffectHandler.sendPulsePacket(world, pos, DISTANCE, true);
			rechargeTicks = RECHARGE_TICKS;
			sendData();
		}
		return super.ring(world, pos, direction);
	}

}
