package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.fluids.FluidPipeBehaviour;
import com.simibubi.create.content.contraptions.fluids.FluidPropagator;
import com.simibubi.create.content.contraptions.fluids.pipes.StraightPipeTileEntity.StraightPipeAttachmentBehaviour;
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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidStack;

public class SmartFluidPipeTileEntity extends SmartTileEntity {

	private FilteringBehaviour filter;

	public SmartFluidPipeTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new SmartPipeBehaviour(this));
		behaviours.add(new StraightPipeAttachmentBehaviour(this));
		behaviours.add(filter = new FilteringBehaviour(this, new SmartPipeFilterSlot()).forFluids()
			.withCallback(this::onFilterChanged));
	}

	private void onFilterChanged(ItemStack newFilter) {
		if (world.isRemote)
			return;
		FluidPropagator.propagateChangedPipe(world, pos, getBlockState());
	}

	class SmartPipeBehaviour extends FluidPipeBehaviour {

		public SmartPipeBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public boolean canTransferToward(FluidStack fluid, BlockState state, Direction direction, boolean inbound) {
			if (fluid.isEmpty() || filter != null && filter.test(fluid))
				return super.canTransferToward(fluid, state, direction, inbound);
			return false;
		}

		@Override
		public boolean isConnectedTo(BlockState state, Direction direction) {
			return state.getBlock() instanceof SmartFluidPipeBlock
				&& SmartFluidPipeBlock.getPipeAxis(state) == direction.getAxis();
		}

	}

	class SmartPipeFilterSlot extends ValueBoxTransform {

		@Override
		protected Vec3d getLocalOffset(BlockState state) {
			AttachFace face = state.get(SmartFluidPipeBlock.FACE);
			float y = face == AttachFace.CEILING ? 0.3f : face == AttachFace.WALL ? 11.3f : 15.3f;
			float z = face == AttachFace.CEILING ? 4.6f : face == AttachFace.WALL ? 0.6f : 4.6f;
			return VecHelper.rotateCentered(VecHelper.voxelSpace(8, y, z), angleY(state), Axis.Y);
		}

		@Override
		protected void rotate(BlockState state, MatrixStack ms) {
			AttachFace face = state.get(SmartFluidPipeBlock.FACE);
			MatrixStacker.of(ms)
				.rotateY(angleY(state))
				.rotateX(face == AttachFace.CEILING ? -45 : 45);
		}

		protected float angleY(BlockState state) {
			AttachFace face = state.get(SmartFluidPipeBlock.FACE);
			float horizontalAngle = AngleHelper.horizontalAngle(state.get(SmartFluidPipeBlock.HORIZONTAL_FACING));
			if (face == AttachFace.WALL)
				horizontalAngle += 180;
			return horizontalAngle;
		}

	}

}
