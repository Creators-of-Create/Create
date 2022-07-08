package com.simibubi.create.content.contraptions.components.actors;

import java.util.function.Consumer;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PortableStorageInterfaceRenderer extends SafeTileEntityRenderer<PortableStorageInterfaceTileEntity> {

	public PortableStorageInterfaceRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(PortableStorageInterfaceTileEntity te, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		if (Backend.canUseInstancing(te.getLevel()))
			return;

		BlockState blockState = te.getBlockState();
		float progress = te.getExtensionDistance(partialTicks);
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());
		render(blockState, te.isConnected(), progress, null, sbb -> sbb.light(light)
			.renderInto(ms, vb));
	}

	public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
		BlockState blockState = context.state;
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());
		float renderPartialTicks = AnimationTickHolder.getPartialTicks();

		LerpedFloat animation = PortableStorageInterfaceMovement.getAnimation(context);
		float progress = animation.getValue(renderPartialTicks);
		boolean lit = animation.settled();
		render(blockState, lit, progress, matrices.getModel(),
			sbb -> sbb
				.light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
				.renderInto(matrices.getViewProjection(), vb));
	}

	private static void render(BlockState blockState, boolean lit, float progress, PoseStack local,
		Consumer<SuperByteBuffer> drawCallback) {
		SuperByteBuffer middle = CachedBufferer.partial(getMiddleForState(blockState, lit), blockState);
		SuperByteBuffer top = CachedBufferer.partial(getTopForState(blockState), blockState);

		if (local != null) {
			middle.transform(local);
			top.transform(local);
		}
		Direction facing = blockState.getValue(PortableStorageInterfaceBlock.FACING);
		rotateToFacing(middle, facing);
		rotateToFacing(top, facing);
		middle.translate(0, progress * 0.5f + 0.375f, 0);
		top.translate(0, progress, 0);

		drawCallback.accept(middle);
		drawCallback.accept(top);
	}

	private static void rotateToFacing(SuperByteBuffer buffer, Direction facing) {
		buffer.centre()
			.rotateY(AngleHelper.horizontalAngle(facing))
			.rotateX(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
			.unCentre();
	}

	static PortableStorageInterfaceTileEntity getTargetPSI(MovementContext context) {
		String _workingPos_ = PortableStorageInterfaceMovement._workingPos_;
		if (!context.data.contains(_workingPos_))
			return null;

		BlockPos pos = NbtUtils.readBlockPos(context.data.getCompound(_workingPos_));
		BlockEntity tileEntity = context.world.getBlockEntity(pos);
		if (!(tileEntity instanceof PortableStorageInterfaceTileEntity psi))
			return null;

		if (!psi.isTransferring())
			return null;
		return psi;
	}

	static PartialModel getMiddleForState(BlockState state, boolean lit) {
		if (AllBlocks.PORTABLE_FLUID_INTERFACE.has(state))
			return lit ? AllBlockPartials.PORTABLE_FLUID_INTERFACE_MIDDLE_POWERED
				: AllBlockPartials.PORTABLE_FLUID_INTERFACE_MIDDLE;
		return lit ? AllBlockPartials.PORTABLE_STORAGE_INTERFACE_MIDDLE_POWERED
			: AllBlockPartials.PORTABLE_STORAGE_INTERFACE_MIDDLE;
	}

	static PartialModel getTopForState(BlockState state) {
		if (AllBlocks.PORTABLE_FLUID_INTERFACE.has(state))
			return AllBlockPartials.PORTABLE_FLUID_INTERFACE_TOP;
		return AllBlockPartials.PORTABLE_STORAGE_INTERFACE_TOP;
	}

}
