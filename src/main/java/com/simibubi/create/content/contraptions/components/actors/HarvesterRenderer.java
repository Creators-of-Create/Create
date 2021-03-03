package com.simibubi.create.content.contraptions.components.actors;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.contraptions.components.structureMovement.render.RenderedContraption;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.RenderMaterial;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

public class HarvesterRenderer extends SafeTileEntityRenderer<HarvesterTileEntity> {

	public HarvesterRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(HarvesterTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		BlockState blockState = te.getBlockState();
		SuperByteBuffer superBuffer = AllBlockPartials.HARVESTER_BLADE.renderOnHorizontal(blockState);
		superBuffer.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.getCutoutMipped()));
	}

	public static void addInstanceForContraption(RenderedContraption contraption, MovementContext context) {
		RenderMaterial<?, InstancedModel<ContraptionActorData>> renderMaterial = contraption.getActorMaterial();

		BlockState state = context.state;
		InstancedModel<ContraptionActorData> model = renderMaterial.getModel(AllBlockPartials.HARVESTER_BLADE, state);

		model.setupInstance(data -> {
			Direction facing = state.get(HORIZONTAL_FACING);
			float originOffset = 1 / 16f;
			Vector3f rotOffset = new Vector3f(0.5f, -2 * originOffset + 0.5f, originOffset + 0.5f);
			data.setPosition(context.localPos)
				.setBlockLight(contraption.renderWorld.getLightLevel(LightType.BLOCK, context.localPos))
				.setRotationOffset(0)
				.setRotationCenter(rotOffset)
				.setRotationAxis(-1, 0, 0)
				.setLocalRotation(0, facing.getHorizontalAngle(), 0);
		});
	}

	public static void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffers) {
		BlockState blockState = context.state;
		Direction facing = blockState.get(HORIZONTAL_FACING);
		SuperByteBuffer superBuffer = AllBlockPartials.HARVESTER_BLADE.renderOn(blockState);
		float speed = (float) (!VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite())
			? context.getAnimationSpeed()
			: 0);

		if (context.contraption.stalled)
			speed = 0;
		float time = AnimationTickHolder.getRenderTime() / 20;
		float angle = (time * speed) % 360;
		float originOffset = 1 / 16f;
		Vec3d rotOffset = new Vec3d(0, -2 * originOffset, originOffset).add(VecHelper.getCenterOf(BlockPos.ZERO));

		superBuffer.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing)))
			.translate(rotOffset.x, rotOffset.y, rotOffset.z)
			.rotate(Direction.WEST, AngleHelper.rad(angle))
			.translate(-rotOffset.x, -rotOffset.y, -rotOffset.z)
			.light(msLocal.peek()
				.getModel(), ContraptionRenderDispatcher.getLightOnContraption(context))
			.renderInto(ms, buffers.getBuffer(RenderType.getCutoutMipped()));
	}

	public static void transformHead(MatrixStack ms, float angle) {}

}
