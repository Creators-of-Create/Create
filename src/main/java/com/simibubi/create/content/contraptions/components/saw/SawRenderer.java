package com.simibubi.create.content.contraptions.components.saw;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SawRenderer extends SafeTileEntityRenderer<SawTileEntity> {

	public SawRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}
	
	@Override
	protected void renderSafe(SawTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light,
			int overlay) {
		renderBlade(te, ms, buffer, light);
		renderItems(te, partialTicks, ms, buffer, light, overlay);
		FilteringRenderer.renderOnTileEntity(te, partialTicks, ms, buffer, light, overlay);

		if (FastRenderDispatcher.available(te.getWorld())) return;

		renderShaft(te, ms, buffer, light, overlay);
	}

	protected void renderBlade(SawTileEntity te, MatrixStack ms, IRenderTypeBuffer buffer, int light){
		BlockState blockState = te.getBlockState();
		SuperByteBuffer superBuffer;
		AllBlockPartials partial;
		float speed = te.getSpeed();

		ms.push();

		if(SawBlock.isHorizontal(blockState)) {
			if(speed > 0) {
				partial = AllBlockPartials.SAW_BLADE_HORIZONTAL_ACTIVE;
			} else if(speed < 0) {
				partial = AllBlockPartials.SAW_BLADE_HORIZONTAL_REVERSED;
			} else {
				partial = AllBlockPartials.SAW_BLADE_HORIZONTAL_INACTIVE;
			}
		} else {
			if(te.getSpeed() > 0) {
				partial = AllBlockPartials.SAW_BLADE_VERTICAL_ACTIVE;
			} else if(speed < 0) {
				partial = AllBlockPartials.SAW_BLADE_VERTICAL_REVERSED;
			} else {
				partial = AllBlockPartials.SAW_BLADE_VERTICAL_INACTIVE;
			}

			if(!blockState.get(SawBlock.AXIS_ALONG_FIRST_COORDINATE))
				MatrixStacker.of(ms).centre().rotateY(90).unCentre();
		}
		superBuffer = partial.renderOnDirectionalSouth(blockState);
		superBuffer.light(light).renderInto(ms, buffer.getBuffer(RenderType.getCutoutMipped()));

		ms.pop();
	}

	protected void renderShaft(SawTileEntity te, MatrixStack ms, IRenderTypeBuffer buffer, int light,
			int overlay) {
		KineticTileEntityRenderer.renderRotatingBuffer(te, getRotatedModel(te), ms, buffer.getBuffer(RenderType.getSolid()), light);
	}

	protected void renderItems(SawTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light,
			int overlay) {
		boolean processingMode = te.getBlockState().get(SawBlock.FACING) == Direction.UP;
		if (processingMode && !te.inventory.isEmpty()) {
			boolean alongZ = !te.getBlockState().get(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
			ms.push();

			boolean moving = te.inventory.recipeDuration != 0;
			float offset = moving ? (float) (te.inventory.remainingTime) / te.inventory.recipeDuration : 0;
			float processingSpeed = MathHelper.clamp(Math.abs(te.getSpeed()) / 32, 1, 128);
			if (moving)
				offset = MathHelper.clamp(offset + ((-partialTicks + .5f) * processingSpeed) / te.inventory.recipeDuration, 0, 1);

			if (te.getSpeed() == 0)
				offset = .5f;
			if (te.getSpeed() < 0 ^ alongZ)
				offset = 1 - offset;

			for (int i = 0; i < te.inventory.getSlots(); i++) {
				ItemStack stack = te.inventory.getStackInSlot(i);
				if (stack.isEmpty())
					continue;
				
				ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
				IBakedModel modelWithOverrides = itemRenderer.getItemModelWithOverrides(stack, te.getWorld(), null);
				boolean blockItem = modelWithOverrides.isGui3d();
				
				ms.translate(alongZ ? offset : .5, blockItem ? .925f : 13f / 16f, alongZ ? .5 : offset);
				
				ms.scale(.5f, .5f, .5f);
				if (alongZ)
					ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90));
				ms.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90));
				itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, light, overlay, ms, buffer);
				break;
			}
			
			ms.pop();
		}
	}

	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		BlockState state = te.getBlockState();
		if (state.get(FACING).getAxis().isHorizontal())
			return AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouth(state.rotate(Rotation.CLOCKWISE_180));
		return CreateClient.bufferCache.renderBlockIn(KineticTileEntityRenderer.KINETIC_TILE,
				getRenderedBlockState(te));
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return KineticTileEntityRenderer.shaft(KineticTileEntityRenderer.getRotationAxisOf(te));
	}

	public static void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
										   IRenderTypeBuffer buffer) {
		MatrixStack[] matrixStacks = new MatrixStack[] { ms, msLocal };
		BlockState state = context.state;
		SuperByteBuffer superBuffer;
		Direction facing = state.get(SawBlock.FACING);

		Vec3d facingVec = new Vec3d(context.state.get(SawBlock.FACING).getDirectionVec());
		facingVec = context.rotation.apply(facingVec);

		Direction closestToFacing = Direction.getFacingFromVector(facingVec.x, facingVec.y, facingVec.z);

		boolean horizontal = closestToFacing.getAxis().isHorizontal();
		boolean backwards = VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite());
		boolean moving = context.getAnimationSpeed() != 0;
		boolean shouldAnimate = (context.contraption.stalled && horizontal)
				|| (!context.contraption.stalled && !backwards && moving);

		if(SawBlock.isHorizontal(state)) {
			if(shouldAnimate)
				superBuffer = AllBlockPartials.SAW_BLADE_HORIZONTAL_ACTIVE.renderOn(state);
			else
				superBuffer = AllBlockPartials.SAW_BLADE_HORIZONTAL_INACTIVE.renderOn(state);
		} else {
			if(shouldAnimate)
				superBuffer = AllBlockPartials.SAW_BLADE_VERTICAL_ACTIVE.renderOn(state);
			else
				superBuffer = AllBlockPartials.SAW_BLADE_VERTICAL_INACTIVE.renderOn(state);
		}

		for (MatrixStack m : matrixStacks) {
			MatrixStacker.of(m)
				.centre()
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(AngleHelper.verticalAngle(facing));
			if(!SawBlock.isHorizontal(state))
				MatrixStacker.of(m).rotateZ(state.get(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? 0 : 90);
			MatrixStacker.of(m).unCentre();
		}

		superBuffer
			.light(msLocal.peek()
			.getModel())
			.renderInto(ms, buffer.getBuffer(RenderType.getCutoutMipped()));
	}

}
