package com.simibubi.create.content.logistics.trains.track;

import java.util.function.Consumer;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class TrackBlockOutline {

	@SubscribeEvent
	public static void drawCustomBlockSelection(DrawSelectionEvent.HighlightBlock event) {
		Minecraft mc = Minecraft.getInstance();
		BlockHitResult target = event.getTarget();
		BlockPos pos = target.getBlockPos();
		BlockState blockstate = mc.level.getBlockState(pos);

		if (!(blockstate.getBlock() instanceof TrackBlock))
			return;
		if (!mc.level.getWorldBorder()
			.isWithinBounds(pos))
			return;

		VertexConsumer vb = event.getMultiBufferSource()
			.getBuffer(RenderType.lines());
		PoseStack ms = event.getPoseStack();

		ms.pushPose();
		Vec3 camPos = event.getCamera()
			.getPosition();

		ms.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);

		walkShapes(blockstate.getValue(TrackBlock.SHAPE), TransformStack.cast(ms), s -> {
			event.setCanceled(true);
			PoseStack.Pose transform = ms.last();
			s.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
				float xDiff = (float) (x2 - x1);
				float yDiff = (float) (y2 - y1);
				float zDiff = (float) (z2 - z1);
				float length = Mth.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);

				xDiff /= length;
				yDiff /= length;
				zDiff /= length;

				vb.vertex(transform.pose(), (float) x1, (float) y1, (float) z1)
					.color(0f, 0f, 0f, .4f)
					.normal(transform.normal(), xDiff, yDiff, zDiff)
					.endVertex();
				vb.vertex(transform.pose(), (float) x2, (float) y2, (float) z2)
					.color(0f, 0f, 0f, .4f)
					.normal(transform.normal(), xDiff, yDiff, zDiff)
					.endVertex();

			});
		});

		ms.popPose();

	}

	private static final VoxelShape LONG_CROSS =
		Shapes.or(TrackVoxelShapes.longOrthogonalZ(), TrackVoxelShapes.longOrthogonalX());
	private static final VoxelShape LONG_ORTHO = TrackVoxelShapes.longOrthogonalZ();

	private static void walkShapes(TrackShape shape, TransformStack msr, Consumer<VoxelShape> renderer) {
		float angle45 = Mth.PI / 4;

		if (shape == TrackShape.XO || shape == TrackShape.CR_NDX || shape == TrackShape.CR_PDX)
			renderer.accept(AllShapes.TRACK_ORTHO.get(Direction.EAST));
		else if (shape == TrackShape.ZO || shape == TrackShape.CR_NDZ || shape == TrackShape.CR_PDZ)
			renderer.accept(AllShapes.TRACK_ORTHO.get(Direction.SOUTH));

		if (shape == TrackShape.PD || shape == TrackShape.CR_PDX || shape == TrackShape.CR_PDZ) {
			msr.rotateCentered(Direction.UP, angle45);
			renderer.accept(LONG_ORTHO);
		} else if (shape == TrackShape.ND || shape == TrackShape.CR_NDX || shape == TrackShape.CR_NDZ) {
			msr.rotateCentered(Direction.UP, -Mth.PI / 4);
			renderer.accept(LONG_ORTHO);
		}

		if (shape == TrackShape.CR_O)
			renderer.accept(AllShapes.TRACK_CROSS);
		else if (shape == TrackShape.CR_D) {
			msr.rotateCentered(Direction.UP, angle45);
			renderer.accept(LONG_CROSS);
		}

		if (!(shape == TrackShape.AE || shape == TrackShape.AN || shape == TrackShape.AW || shape == TrackShape.AS))
			return;

		msr.translate(0, 1, 0);
		msr.rotateCentered(Direction.UP, Mth.PI - AngleHelper.rad(shape.getModelRotation()));
		msr.rotateXRadians(angle45);
		msr.translate(0, -3 / 16f, 1 / 16f);
		renderer.accept(LONG_ORTHO);
	}

}
