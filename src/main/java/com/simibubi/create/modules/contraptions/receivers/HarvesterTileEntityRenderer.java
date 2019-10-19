package com.simibubi.create.modules.contraptions.receivers;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import java.nio.ByteBuffer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.BufferManipulator;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.constructs.IHaveMovementBehavior.MovementContext;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.Animation;

public class HarvesterTileEntityRenderer extends TileEntityRenderer<HarvesterTileEntity> {

	protected static class HarvesterRenderer extends BufferManipulator {

		public HarvesterRenderer(ByteBuffer original) {
			super(original);
		}

		public ByteBuffer getTransformed(float xIn, float yIn, float zIn, float angle, Direction facing,
				int packedLightCoords) {
			original.rewind();
			mutable.rewind();
			float cos = MathHelper.cos(angle);
			float sin = MathHelper.sin(angle);
			float x, y, z = 0;
			Axis axis = facing.rotateYCCW().getAxis();
			int axisDirection = -facing.getAxisDirection().getOffset();

			float originOffset = 1 / 16f;
			float xOffset = axis == Axis.X ? 0 : originOffset * axisDirection;
			float zOffset = axis == Axis.Z ? 0 : originOffset * axisDirection;

			for (int vertex = 0; vertex < vertexCount(original); vertex++) {
				x = getX(original, vertex) - .5f + xOffset;
				y = getY(original, vertex) - .5f + 2 * originOffset;
				z = getZ(original, vertex) - .5f + zOffset;

				putPos(mutable, vertex, rotateX(x, y, z, sin, cos, axis) + .5f - xOffset + xIn,
						rotateY(x, y, z, sin, cos, axis) + .5f - 2 * originOffset + yIn,
						rotateZ(x, y, z, sin, cos, axis) + .5f - zOffset + zIn);
				putLight(mutable, vertex, packedLightCoords);
			}

			return mutable;
		}

	}

	public static void renderInConstruct(MovementContext context, double x, double y, double z, BufferBuilder buffer) {
		World world = context.world;
		BlockState state = context.state;
		BlockPos pos = context.currentGridPos;
		Direction facing = context.getMovementDirection();

		float speed = facing == state.get(HORIZONTAL_FACING) ? 100 * facing.getAxisDirection().getOffset() : 0;
		if (facing.getAxis() == Axis.X)
			speed = -speed;
		
		float time = Animation.getWorldTime(Minecraft.getInstance().world,
				Minecraft.getInstance().getRenderPartialTicks());
		float angle = (float) (((time * speed) % 360) / 180 * (float) Math.PI);
		render(world, state, pos, x, y, z, angle, buffer);
	}

	@Override
	public void renderTileEntityFast(HarvesterTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		render(te.getWorld(), te.getBlockState(), te.getPos(), x, y, z, 0, buffer);
	}

	public static void render(World world, BlockState state, BlockPos pos, double x, double y, double z, float angle,
			BufferBuilder buffer) {
		if (!AllBlocks.HARVESTER.typeOf(state))
			return;

		BlockState renderedState = AllBlocks.HARVESTER_BLADE.get().getDefaultState().with(HORIZONTAL_FACING,
				state.get(HORIZONTAL_FACING));

		KineticTileEntityRenderer.cacheIfMissing(renderedState, world, HarvesterRenderer::new);
		HarvesterRenderer renderer = (HarvesterRenderer) KineticTileEntityRenderer.getBuffer(renderedState);
		buffer.putBulkData(renderer.getTransformed((float) x, (float) y, (float) z, angle, state.get(HORIZONTAL_FACING),
				state.getPackedLightmapCoords(world, pos)));
	}

}
