package com.simibubi.create.modules.contraptions.base;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.SuperByteBufferCache.Compartment;
import com.simibubi.create.modules.contraptions.KineticDebugger;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.animation.TileEntityRendererFast;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class KineticTileEntityRenderer extends TileEntityRendererFast<KineticTileEntity> {

	public static final Compartment<BlockState> KINETIC_TILE = new Compartment<>();
	public static boolean rainbowMode = false;

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		renderRotatingKineticBlock(te, getWorld(), getRenderedBlockState(te), x, y, z, buffer);
	}

	public static void renderRotatingKineticBlock(KineticTileEntity te, World world, BlockState renderedState, double x,
			double y, double z, BufferBuilder buffer) {
		SuperByteBuffer superByteBuffer = CreateClient.bufferCache.renderBlockState(KINETIC_TILE, renderedState);
		buffer.putBulkData(standardKineticRotationTransform(superByteBuffer, te, world).translate(x, y, z).build());
	}

	public static float getAngleForTe(KineticTileEntity te, final BlockPos pos, Axis axis) {
		float time = AnimationTickHolder.getRenderTick();
		float offset = getRotationOffsetForPosition(te, pos, axis);
		float angle = (float) (((time * te.getSpeed() + offset) % 360) / 180 * (float) Math.PI);
		return angle;
	}

	public static SuperByteBuffer standardKineticRotationTransform(SuperByteBuffer buffer, KineticTileEntity te,
			World world) {
		final BlockPos pos = te.getPos();
		Axis axis = ((IRotate) te.getBlockState().getBlock()).getRotationAxis(te.getBlockState());
		return kineticRotationTransform(buffer, te, axis, getAngleForTe(te, pos, axis), world);
	}

	public static SuperByteBuffer kineticRotationTransform(SuperByteBuffer buffer, KineticTileEntity te, Axis axis,
			float angle, World world) {
		int packedLightmapCoords = te.getBlockState().getPackedLightmapCoords(world, te.getPos());
		buffer.light(packedLightmapCoords);
		buffer.rotateCentered(axis, angle);

		if (KineticDebugger.isActive()) {
			rainbowMode = true;
			if (te.hasNetwork())
				buffer.color(ColorHelper.colorFromUUID(te.getNetworkID()));
			else
				buffer.color(0xFFFFFF);
		}

		return buffer;
	}

	protected static float getRotationOffsetForPosition(KineticTileEntity te, final BlockPos pos, final Axis axis) {
		float offset = AllBlocks.LARGE_COGWHEEL.typeOf(te.getBlockState()) ? 11.25f : 0;
		double d = (((axis == Axis.X) ? 0 : pos.getX()) + ((axis == Axis.Y) ? 0 : pos.getY())
				+ ((axis == Axis.Z) ? 0 : pos.getZ())) % 2;
		if (d == 0) {
			offset = 22.5f;
		}
		return offset;
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return te.getBlockState();
	}

}
