package com.simibubi.create.content.contraptions.relays.belt;

import java.util.Random;
import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.ShadowRenderHelper;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;

public class BeltRenderer extends SafeTileEntityRenderer<BeltTileEntity> {

	public BeltRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public boolean shouldRenderOffScreen(BeltTileEntity te) {
		return te.isController();
	}

	@Override
	protected void renderSafe(BeltTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {

		if (!Backend.getInstance().canUseInstancing(te.getLevel())) {

			BlockState blockState = te.getBlockState();
			if (!AllBlocks.BELT.has(blockState)) return;

			BeltSlope beltSlope = blockState.getValue(BeltBlock.SLOPE);
			BeltPart part = blockState.getValue(BeltBlock.PART);
			Direction facing = blockState.getValue(BeltBlock.HORIZONTAL_FACING);
			AxisDirection axisDirection = facing.getAxisDirection();

			boolean downward = beltSlope == BeltSlope.DOWNWARD;
			boolean upward = beltSlope == BeltSlope.UPWARD;
			boolean diagonal = downward || upward;
			boolean start = part == BeltPart.START;
			boolean end = part == BeltPart.END;
			boolean sideways = beltSlope == BeltSlope.SIDEWAYS;
			boolean alongX = facing.getAxis() == Axis.X;

			MatrixStack localTransforms = new MatrixStack();
			MatrixTransformStack msr = MatrixTransformStack.of(localTransforms);
			IVertexBuilder vb = buffer.getBuffer(RenderType.solid());
			float renderTick = AnimationTickHolder.getRenderTime(te.getLevel());

			msr.centre();
			msr.rotateY(AngleHelper.horizontalAngle(facing) + (upward ? 180 : 0) + (sideways ? 270 : 0));
			msr.rotateZ(sideways ? 90 : 0);
			msr.rotateX(!diagonal && beltSlope != BeltSlope.HORIZONTAL ? 90 : 0);
			msr.unCentre();

			if (downward || beltSlope == BeltSlope.VERTICAL && axisDirection == AxisDirection.POSITIVE) {
				boolean b = start;
				start = end;
				end = b;
			}

			DyeColor color = te.color.orElse(null);

			for (boolean bottom : Iterate.trueAndFalse) {

				PartialModel beltPartial = getBeltPartial(diagonal, start, end, bottom);

				SuperByteBuffer beltBuffer = PartialBufferer.get(beltPartial, blockState)
						.light(light);

				SpriteShiftEntry spriteShift = getSpriteShiftEntry(color, diagonal, bottom);

				// UV shift
				float speed = te.getSpeed();
				if (speed != 0 || te.color.isPresent()) {
					float time = renderTick * axisDirection.getStep();
					if (diagonal && (downward ^ alongX) || !sideways && !diagonal && alongX || sideways && axisDirection == AxisDirection.NEGATIVE)
						speed = -speed;

					float scrollMult = diagonal ? 3f / 8f : 0.5f;

					float spriteSize = spriteShift.getTarget().getV1() - spriteShift.getTarget().getV0();

					double scroll = speed * time / (31.5 * 16) + (bottom ? 0.5 : 0.0);
					scroll = scroll - Math.floor(scroll);
					scroll = scroll * spriteSize * scrollMult;

					beltBuffer.shiftUVScrolling(spriteShift, (float) scroll);
				}

				beltBuffer
						.transform(localTransforms)
						.renderInto(ms, vb);

				// Diagonal belt do not have a separate bottom model
				if (diagonal) break;
			}

			if (te.hasPulley()) {
				Direction dir = sideways ? Direction.UP : blockState.getValue(BeltBlock.HORIZONTAL_FACING).getClockWise();

				Supplier<MatrixStack> matrixStackSupplier = () -> {
					MatrixStack stack = new MatrixStack();
					MatrixTransformStack stacker = MatrixTransformStack.of(stack);
					stacker.centre();
					if (dir.getAxis() == Axis.X) stacker.rotateY(90);
					if (dir.getAxis() == Axis.Y) stacker.rotateX(90);
					stacker.rotateX(90);
					stacker.unCentre();
					return stack;
				};

				SuperByteBuffer superBuffer = CreateClient.BUFFER_CACHE.renderDirectionalPartial(AllBlockPartials.BELT_PULLEY, blockState, dir, matrixStackSupplier);
				KineticTileEntityRenderer.standardKineticRotationTransform(superBuffer, te, light).renderInto(ms, vb);
			}
		}

		renderItems(te, partialTicks, ms, buffer, light, overlay);
	}

	public static SpriteShiftEntry getSpriteShiftEntry(DyeColor color, boolean diagonal, boolean bottom) {
		if (color != null) {
			return (diagonal ? AllSpriteShifts.DYED_DIAGONAL_BELTS
					: bottom ? AllSpriteShifts.DYED_OFFSET_BELTS : AllSpriteShifts.DYED_BELTS).get(color);
		} else
			return diagonal ? AllSpriteShifts.BELT_DIAGONAL
					: bottom ? AllSpriteShifts.BELT_OFFSET : AllSpriteShifts.BELT;
	}

	public static PartialModel getBeltPartial(boolean diagonal, boolean start, boolean end, boolean bottom) {
		if (diagonal) {
			if (start) return AllBlockPartials.BELT_DIAGONAL_START;
			if (end) return AllBlockPartials.BELT_DIAGONAL_END;
			return AllBlockPartials.BELT_DIAGONAL_MIDDLE;
		} else if (bottom) {
			if (start) return AllBlockPartials.BELT_START_BOTTOM;
			if (end) return AllBlockPartials.BELT_END_BOTTOM;
			return AllBlockPartials.BELT_MIDDLE_BOTTOM;
		} else {
			if (start) return AllBlockPartials.BELT_START;
			if (end) return AllBlockPartials.BELT_END;
			return AllBlockPartials.BELT_MIDDLE;
		}
	}

	protected void renderItems(BeltTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		if (!te.isController())
			return;
		if (te.beltLength == 0)
			return;

		ms.pushPose();

		Direction beltFacing = te.getBeltFacing();
		Vector3i directionVec = beltFacing
							   .getNormal();
		Vector3d beltStartOffset = Vector3d.atLowerCornerOf(directionVec).scale(-.5)
			.add(.5, 15 / 16f, .5);
		ms.translate(beltStartOffset.x, beltStartOffset.y, beltStartOffset.z);
		BeltSlope slope = te.getBlockState()
			.getValue(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
		boolean slopeAlongX = beltFacing
								.getAxis() == Axis.X;

		boolean onContraption = te.getLevel() instanceof WrappedWorld;

		for (TransportedItemStack transported : te.getInventory()
			.getTransportedItems()) {
			ms.pushPose();
			MatrixTransformStack.of(ms)
				.nudge(transported.angle);

			float offset;
			float sideOffset;
			float verticalMovement;

			if (te.getSpeed() == 0) {
				offset = transported.beltPosition;
				sideOffset = transported.sideOffset;
			} else {
				offset = MathHelper.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition);
				sideOffset = MathHelper.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset);
			}

			if (offset < .5)
				verticalMovement = 0;
			else
				verticalMovement = verticality * (Math.min(offset, te.beltLength - .5f) - .5f);
			Vector3d offsetVec = Vector3d.atLowerCornerOf(directionVec).scale(offset);
			if (verticalMovement != 0)
				offsetVec = offsetVec.add(0, verticalMovement, 0);
			boolean onSlope =
				slope != BeltSlope.HORIZONTAL && MathHelper.clamp(offset, .5f, te.beltLength - .5f) == offset;
			boolean tiltForward = (slope == BeltSlope.DOWNWARD ^ beltFacing
																   .getAxisDirection() == AxisDirection.POSITIVE) == (beltFacing
																														.getAxis() == Axis.Z);
			float slopeAngle = onSlope ? tiltForward ? -45 : 45 : 0;

			ms.translate(offsetVec.x, offsetVec.y, offsetVec.z);

			boolean alongX = beltFacing
							   .getClockWise()
							   .getAxis() == Axis.X;
			if (!alongX)
				sideOffset *= -1;
			ms.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);

			int stackLight = onContraption ? light : getPackedLight(te, offset);
			ItemRenderer itemRenderer = Minecraft.getInstance()
				.getItemRenderer();
			boolean renderUpright = BeltHelper.isItemUpright(transported.stack);
			boolean blockItem = itemRenderer.getModel(transported.stack, te.getLevel(), null)
				.isGui3d();
			int count = (int) (MathHelper.log2((int) (transported.stack.getCount()))) / 2;
			Random r = new Random(transported.angle);

			boolean slopeShadowOnly = renderUpright && onSlope;
			float slopeOffset = 1 / 8f;
			if (slopeShadowOnly)
				ms.pushPose();
			if (!renderUpright || slopeShadowOnly)
				ms.mulPose(new Vector3f(slopeAlongX ? 0 : 1, 0, slopeAlongX ? 1 : 0).rotationDegrees(slopeAngle));
			if (onSlope)
				ms.translate(0, slopeOffset, 0);
//			ms.pushPose();
//			ms.translate(0, -1 / 8f + 0.005f, 0);
//			ShadowRenderHelper.renderShadow(ms, buffer, .75f, .2f);
//			ms.popPose();
			if (slopeShadowOnly) {
				ms.popPose();
				ms.translate(0, slopeOffset, 0);
			}
			
			if (renderUpright) {
				Entity renderViewEntity = Minecraft.getInstance().cameraEntity;
				if (renderViewEntity != null) {
					Vector3d positionVec = renderViewEntity.position();
					Vector3d vectorForOffset = BeltHelper.getVectorForOffset(te, offset);
					Vector3d diff = vectorForOffset.subtract(positionVec);
					float yRot = (float) (MathHelper.atan2(diff.x, diff.z) + Math.PI);
					ms.mulPose(Vector3f.YP.rotation(yRot));
				}
				ms.translate(0, 3 / 32d, 1 / 16f);
			}

			for (int i = 0; i <= count; i++) {
				ms.pushPose();

				ms.mulPose(Vector3f.YP.rotationDegrees(transported.angle));
				if (!blockItem && !renderUpright) {
					ms.translate(0, -.09375, 0);
					ms.mulPose(Vector3f.XP.rotationDegrees(90));
				}

				if (blockItem) {
					ms.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
				}

				ms.scale(.5f, .5f, .5f);
				itemRenderer.renderStatic(null, transported.stack, TransformType.FIXED, false, ms, buffer, te.getLevel(), stackLight, overlay);
				ms.popPose();

				if (!renderUpright) {
					if (!blockItem)
						ms.mulPose(Vector3f.YP.rotationDegrees(10));
					ms.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
				} else
					ms.translate(0, 0, -1 / 16f);

			}

			ms.popPose();
		}
		ms.popPose();
	}

	protected int getPackedLight(BeltTileEntity controller, float beltPos) {
		int segment = (int) Math.floor(beltPos) * 2;
		if (controller.light == null || segment >= controller.light.length || segment < 0)
			return 0;

		return LightTexture.pack(controller.light[segment], controller.light[segment + 1]);
	}

}
