package com.simibubi.create.content.contraptions.relays.belt;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.ShadowRenderHelper;
import com.simibubi.create.foundation.render.instancing.BeltData;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
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
import net.minecraft.world.LightType;

import java.util.Random;
import java.util.function.Supplier;

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

//		addInstanceData(new InstanceContext.World<>(te));
		renderItems(te, partialTicks, ms, buffer, light, overlay);
	}

	@Override
	public boolean isGlobalRenderer(BeltTileEntity te) {
		return te.isController();
	}

	public static SpriteShiftEntry getSpriteShiftEntry(boolean diagonal, boolean bottom) {
		if (diagonal) return AllSpriteShifts.BELT_DIAGONAL;
		if (bottom) return AllSpriteShifts.BELT_OFFSET;
		return AllSpriteShifts.BELT;
	}

	public static AllBlockPartials getBeltPartial(boolean diagonal, boolean start, boolean end, boolean bottom) {
		if (diagonal) {
			if (start) return AllBlockPartials.BELT_DIAGONAL_START;
			if (end) return AllBlockPartials.BELT_DIAGONAL_END;
			return AllBlockPartials.BELT_DIAGONAL_MIDDLE;
		} else {
			if (bottom) {
				if (start) return AllBlockPartials.BELT_START_BOTTOM;
				if (end) return AllBlockPartials.BELT_END_BOTTOM;
				return AllBlockPartials.BELT_MIDDLE_BOTTOM;
			}
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

			int stackLight = getPackedLight(te, offset);

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
				ms.translate(0, 3 / 32d, 1 / 16f);
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
				itemRenderer.renderItem(transported.stack, TransformType.FIXED, stackLight, overlay, ms, buffer);
				ms.pop();

				if (!renderUpright) {
					if (!blockItem)
						ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(10));
					ms.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
				} else
					ms.translate(0, 0, -1 / 16f);

			}

			ms.pop();
		}
		ms.pop();
	}

	protected int getPackedLight(BeltTileEntity controller, float beltPos) {
		BeltTileEntity belt = BeltHelper.getBeltForOffset(controller, beltPos);

		if (belt == null) return 0;

		return (belt.skyLight << 20) | (belt.blockLight << 4);
	}
}
