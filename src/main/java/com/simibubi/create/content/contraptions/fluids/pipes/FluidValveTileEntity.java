package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.fluids.FluidPipeAttachmentBehaviour;
import com.simibubi.create.content.contraptions.fluids.FluidPipeBehaviour;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ILightReader;
import net.minecraftforge.fluids.FluidStack;

public class FluidValveTileEntity extends KineticTileEntity {

	LerpedFloat pointer;

	public FluidValveTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		pointer = LerpedFloat.linear()
			.startWithValue(0).chase(0, 0, Chaser.LINEAR);
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
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		pointer.readNBT(compound.getCompound("Pointer"), clientPacket);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new ValvePipeBehaviour(this));
		behaviours.add(new ValvePipeAttachmentBehaviour(this));
	}

	class ValvePipeBehaviour extends FluidPipeBehaviour {

		public ValvePipeBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public boolean isConnectedTo(BlockState state, Direction direction) {
			return FluidValveBlock.getPipeAxis(state) == direction.getAxis();
		}

		@Override
		public boolean canTransferToward(FluidStack fluid, BlockState state, Direction direction, boolean inbound) {
			if (state.has(FluidValveBlock.ENABLED) && state.get(FluidValveBlock.ENABLED))
				return super.canTransferToward(fluid, state, direction, inbound);
			return false;
		}
		
	}

	class ValvePipeAttachmentBehaviour extends FluidPipeAttachmentBehaviour {

		public ValvePipeAttachmentBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public AttachmentTypes getAttachment(ILightReader world, BlockPos pos, BlockState state, Direction direction) {
			AttachmentTypes attachment = super.getAttachment(world, pos, state, direction);

			BlockState facingState = world.getBlockState(pos.offset(direction));
			if (AllBlocks.FLUID_VALVE.has(facingState)
				&& FluidValveBlock.getPipeAxis(facingState) == FluidValveBlock.getPipeAxis(state)
				&& direction.getAxisDirection() == AxisDirection.NEGATIVE)
				return AttachmentTypes.NONE;

			return attachment;
		}

	}

}
