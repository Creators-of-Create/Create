package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.fluids.FluidPropagator;
import com.simibubi.create.content.contraptions.fluids.pipes.StraightPipeTileEntity.StraightPipeFluidTransportBehaviour;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fluids.FluidStack;

public class SmartFluidPipeTileEntity extends SmartTileEntity {

	private FilteringBehaviour filter;

	public SmartFluidPipeTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new SmartPipeBehaviour(this));
		behaviours.add(filter = new FilteringBehaviour(this, new SmartPipeFilterSlot()).forFluids()
			.withCallback(this::onFilterChanged));
	}

	private void onFilterChanged(ItemStack newFilter) {
		if (level.isClientSide)
			return;
		FluidPropagator.propagateChangedPipe(level, worldPosition, getBlockState());
	}

	class SmartPipeBehaviour extends StraightPipeFluidTransportBehaviour {

		public SmartPipeBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public boolean canPullFluidFrom(FluidStack fluid, BlockState state, Direction direction) {
			if (fluid.isEmpty() || filter != null && filter.test(fluid))
				return super.canPullFluidFrom(fluid, state, direction);
			return false;
		}

		@Override
		public boolean canHaveFlowToward(BlockState state, Direction direction) {
			return state.getBlock() instanceof SmartFluidPipeBlock
				&& SmartFluidPipeBlock.getPipeAxis(state) == direction.getAxis();
		}

	}

	class SmartPipeFilterSlot extends ValueBoxTransform {

		@Override
		protected Vector3d getLocalOffset(BlockState state) {
			AttachFace face = state.getValue(SmartFluidPipeBlock.FACE);
			float y = face == AttachFace.CEILING ? 0.3f : face == AttachFace.WALL ? 11.3f : 15.3f;
			float z = face == AttachFace.CEILING ? 4.6f : face == AttachFace.WALL ? 0.6f : 4.6f;
			return VecHelper.rotateCentered(VecHelper.voxelSpace(8, y, z), angleY(state), Axis.Y);
		}

		@Override
		protected void rotate(BlockState state, MatrixStack ms) {
			AttachFace face = state.getValue(SmartFluidPipeBlock.FACE);
			MatrixStacker.of(ms)
				.rotateY(angleY(state))
				.rotateX(face == AttachFace.CEILING ? -45 : 45);
		}

		protected float angleY(BlockState state) {
			AttachFace face = state.getValue(SmartFluidPipeBlock.FACE);
			float horizontalAngle = AngleHelper.horizontalAngle(state.getValue(SmartFluidPipeBlock.FACING));
			if (face == AttachFace.WALL)
				horizontalAngle += 180;
			return horizontalAngle;
		}

	}

}
