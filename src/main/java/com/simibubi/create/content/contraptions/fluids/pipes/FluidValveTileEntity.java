package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.List;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.StraightPipeTileEntity.StraightPipeFluidTransportBehaviour;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;

public class FluidValveTileEntity extends KineticTileEntity {

	LerpedFloat pointer;

	public FluidValveTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		pointer = LerpedFloat.linear()
			.startWithValue(0)
			.chase(0, 0, Chaser.LINEAR);
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);
		float speed = getSpeed();
		pointer.chase(speed > 0 ? 1 : 0, getChaseSpeed(), Chaser.LINEAR);
		sendData();
	}

	@Override
	public void tick() {
		super.tick();
		pointer.tickChaser();

		if (world.isRemote)
			return;

		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof FluidValveBlock))
			return;
		boolean stateOpen = blockState.get(FluidValveBlock.ENABLED);

		if (stateOpen && pointer.getValue() == 0) {
			switchToBlockState(world, pos, blockState.with(FluidValveBlock.ENABLED, false));
			return;
		}
		if (!stateOpen && pointer.getValue() == 1) {
			switchToBlockState(world, pos, blockState.with(FluidValveBlock.ENABLED, true));
			return;
		}
	}

	private float getChaseSpeed() {
		return MathHelper.clamp(Math.abs(getSpeed()) / 16 / 20, 0, 1);
	}

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.put("Pointer", pointer.writeNBT());
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		pointer.readNBT(compound.getCompound("Pointer"), clientPacket);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new ValvePipeBehaviour(this));
	}

	@Override
	public boolean shouldRenderNormally() {
		return true;
	}

	class ValvePipeBehaviour extends StraightPipeFluidTransportBehaviour {

		public ValvePipeBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public boolean canHaveFlowToward(BlockState state, Direction direction) {
			return FluidValveBlock.getPipeAxis(state) == direction.getAxis();
		}

		@Override
		public boolean canPullFluidFrom(FluidStack fluid, BlockState state, Direction direction) {
			if (state.contains(FluidValveBlock.ENABLED) && state.get(FluidValveBlock.ENABLED))
				return super.canPullFluidFrom(fluid, state, direction);
			return false;
		}

	}

}
