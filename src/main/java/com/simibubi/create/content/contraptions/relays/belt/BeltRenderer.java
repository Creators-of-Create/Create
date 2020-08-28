package com.simibubi.create.content.contraptions.relays.belt;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.ShadowRenderHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class BeltRenderer extends SafeTileEntityRenderer<BeltTileEntity> {

	public BeltRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(BeltTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {

		BlockState blockState = te.getBlockState();
		if (!AllBlocks.BELT.has(blockState))
			return;

		BeltSlope beltSlope = blockState.get(BeltBlock.SLOPE);
		BeltPart part = blockState.get(BeltBlock.PART);
		Direction facing = blockState.get(BeltBlock.HORIZONTAL_FACING);
		AxisDirection axisDirection = facing.getAxisDirection();

		boolean downward = beltSlope == BeltSlope.DOWNWARD;
		boolean upward = beltSlope == BeltSlope.UPWARD;
		boolean diagonal = downward || upward;
		boolean start = part == BeltPart.START;
		boolean end = part == BeltPart.END;
		boolean sideways = beltSlope == BeltSlope.SIDEWAYS;
		boolean alongX = facing.getAxis() == Axis.X;

		MatrixStacker msr = MatrixStacker.of(ms);
		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());
		float renderTick = AnimationTickHolder.getRenderTick();

		ms.push();
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

		for (boolean bottom : Iterate.trueAndFalse) {

			AllBlockPartials beltPartial = diagonal
				? start ? AllBlockPartials.BELT_DIAGONAL_START
					: end ? AllBlockPartials.BELT_DIAGONAL_END : AllBlockPartials.BELT_DIAGONAL_MIDDLE
				: bottom
					? start ? AllBlockPartials.BELT_START_BOTTOM
						: end ? AllBlockPartials.BELT_END_BOTTOM : AllBlockPartials.BELT_MIDDLE_BOTTOM
					: start ? AllBlockPartials.BELT_START
						: end ? AllBlockPartials.BELT_END : AllBlockPartials.BELT_MIDDLE;

			SuperByteBuffer beltBuffer = beltPartial.renderOn(blockState)
				.light(light);
			SpriteShiftEntry spriteShift =
				diagonal ? AllSpriteShifts.BELT_DIAGONAL : bottom ? AllSpriteShifts.BELT_OFFSET : AllSpriteShifts.BELT;
			int cycleLength = diagonal ? 12 : 16;
			int cycleOffset = bottom ? 8 : 0;

			// UV shift
			float speed = te.getSpeed();
			if (speed != 0) {
				float time = renderTick * axisDirection.getOffset();
				if (diagonal && (downward ^ alongX) || !sideways && !diagonal && alongX
					|| sideways && axisDirection == AxisDirection.NEGATIVE)
					speed = -speed;
				int textureIndex = (int) (((speed * time / 36) + cycleOffset) % cycleLength);
				if (textureIndex < 0)
					textureIndex += cycleLength;

				beltBuffer.shiftUVtoSheet(spriteShift, (textureIndex % 4) / 4f, (textureIndex / 4) / 4f, 4);
			} else {
				beltBuffer.dontShiftUV();
			}

			beltBuffer.renderInto(ms, vb);

			// Diagonal belt do not have a separate bottom model
			if (diagonal)
				break;
		}
		ms.pop();

		if (te.hasPulley()) {
			// TODO 1.15 find a way to cache this model matrix computation
			MatrixStack modelTransform = new MatrixStack();
			Direction dir = blockState.get(BeltBlock.HORIZONTAL_FACING)
				.rotateY();
			if (sideways)
				dir = Direction.UP;
			msr = MatrixStacker.of(modelTransform);
			msr.centre();
			if (dir.getAxis() == Axis.X)
				msr.rotateY(90);
			if (dir.getAxis() == Axis.Y)
				msr.rotateX(90);
			msr.rotateX(90);
			msr.unCentre();

			SuperByteBuffer superBuffer = CreateClient.bufferCache
				.renderDirectionalPartial(AllBlockPartials.BELT_PULLEY, blockState, dir, modelTransform);
			KineticTileEntityRenderer.standardKineticRotationTransform(superBuffer, te, light)
				.renderInto(ms, vb);
		}

		renderItems(te, partialTicks, ms, buffer, light, overlay);
	}

	protected void renderItems(BeltTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		if (!te.isController())
			return;
		if (te.beltLength == 0)
			return;

		ms.push();

		Vec3i directionVec = te.getBeltFacing()
			.getDirectionVec();
		Vec3d beltStartOffset = new Vec3d(directionVec).scale(-.5)
			.add(.5, 13 / 16f + .125f, .5);
		ms.translate(beltStartOffset.x, beltStartOffset.y, beltStartOffset.z);
		BeltSlope slope = te.getBlockState()
			.get(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
		boolean slopeAlongX = te.getBeltFacing()
			.getAxis() == Axis.X;

		for (TransportedItemStack transported : te.getInventory()
			.getTransportedItems()) {
			ms.push();
			MatrixStacker.of(ms)
				.nudge(transported.angle);
			float offset = MathHelper.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition);
			float sideOffset = MathHelper.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset);
			float verticalMovement = verticality;

			if (te.getSpeed() == 0) {
				offset = transported.beltPosition;
				sideOffset = transported.sideOffset;
			}

			if (offset < .5)
				verticalMovement = 0;
			verticalMovement = verticalMovement * (Math.min(offset, te.beltLength - .5f) - .5f);
			Vec3d offsetVec = new Vec3d(directionVec).scale(offset)
				.add(0, verticalMovement, 0);
			boolean onSlope =
				slope != BeltSlope.HORIZONTAL && MathHelper.clamp(offset, .5f, te.beltLength - .5f) == offset;
			boolean tiltForward = (slope == BeltSlope.DOWNWARD ^ te.getBeltFacing()
				.getAxisDirection() == AxisDirection.POSITIVE) == (te.getBeltFacing()
					.getAxis() == Axis.Z);
			float slopeAngle = onSlope ? tiltForward ? -45 : 45 : 0;

			ms.translate(offsetVec.x, offsetVec.y, offsetVec.z);

			boolean alongX = te.getBeltFacing()
				.rotateY()
				.getAxis() == Axis.X;
			if (!alongX)
				sideOffset *= -1;
			ms.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);

			ItemRenderer itemRenderer = Minecraft.getInstance()
				.getItemRenderer();
			boolean renderUpright = BeltHelper.isItemUpright(transported.stack);
			boolean blockItem = itemRenderer.getItemModelWithOverrides(transported.stack, te.getWorld(), null)
				.isGui3d();
			int count = (int) (MathHelper.log2((int) (transported.stack.getCount()))) / 2;
			Random r = new Random(transported.angle);

			if (Minecraft.getInstance().gameSettings.fancyGraphics) {
				Vec3d shadowPos = new Vec3d(te.getPos()).add(beltStartOffset.scale(1)
					.add(offsetVec)
					.add(alongX ? sideOffset : 0, .39, alongX ? 0 : sideOffset));
				ShadowRenderHelper.renderShadow(ms, buffer, shadowPos, .75f, blockItem ? .2f : .2f);
			}
			
			if (renderUpright) {
				Entity renderViewEntity = Minecraft.getInstance().renderViewEntity;
				if (renderViewEntity != null) {
					Vec3d positionVec = renderViewEntity.getPositionVec();
					Vec3d vectorForOffset = BeltHelper.getVectorForOffset(te, offset);
					Vec3d diff = vectorForOffset.subtract(positionVec);
					float yRot = (float) MathHelper.atan2(diff.z, -diff.x);
					ms.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion((float) (yRot + Math.PI / 2)));
				}
				ms.translate(0, 3/32d, 1/16f);
			}
			if (!renderUpright) 
				ms.multiply(new Vector3f(slopeAlongX ? 0 : 1, 0, slopeAlongX ? 1 : 0).getDegreesQuaternion(slopeAngle));
			
			if (onSlope)
				ms.translate(0, 1 / 8f, 0);

			for (int i = 0; i <= count; i++) {
				ms.push();

				ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(transported.angle));
				if (!blockItem && !renderUpright) {
					ms.translate(0, -.09375, 0);
					ms.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90));
				}

				if (blockItem) {
					ms.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
				}

				ms.scale(.5f, .5f, .5f);
				itemRenderer.renderItem(transported.stack, TransformType.FIXED, light, overlay, ms, buffer);
				ms.pop();

				if (!blockItem)
					ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(10));
				ms.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);

			}

			ms.pop();
		}
		ms.pop();
	}

}
