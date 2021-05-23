package com.jozufozu.flywheel.backend.core;

import java.util.ArrayList;

import com.jozufozu.flywheel.backend.core.shader.ShaderCallback;
import com.jozufozu.flywheel.backend.core.shader.WorldProgram;
import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;

public class WorldTileRenderer<P extends WorldProgram> extends InstancedTileRenderer<P> {
	public static int MAX_ORIGIN_DISTANCE = 100;

	public BlockPos originCoordinate = BlockPos.ZERO;

	public WorldTileRenderer(WorldContext<P> context) {
		super(context);
	}

	@Override
	public BlockPos getOriginCoordinate() {
		return originCoordinate;
	}

	@Override
	public void beginFrame(ActiveRenderInfo info) {
		int cX = MathHelper.floor(info.getProjectedView().x);
		int cY = MathHelper.floor(info.getProjectedView().y);
		int cZ = MathHelper.floor(info.getProjectedView().z);

		int dX = Math.abs(cX - originCoordinate.getX());
		int dY = Math.abs(cY - originCoordinate.getY());
		int dZ = Math.abs(cZ - originCoordinate.getZ());

		if (dX > MAX_ORIGIN_DISTANCE || dY > MAX_ORIGIN_DISTANCE || dZ > MAX_ORIGIN_DISTANCE) {

			originCoordinate = new BlockPos(cX, cY, cZ);

			ArrayList<TileEntity> instancedTiles = new ArrayList<>(instances.keySet());
			invalidate();
			instancedTiles.forEach(this::add);
		}

		super.beginFrame(info);
	}

	@Override
	public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ,
					   ShaderCallback<P> callback) {
		BlockPos originCoordinate = getOriginCoordinate();

		camX -= originCoordinate.getX();
		camY -= originCoordinate.getY();
		camZ -= originCoordinate.getZ();

		Matrix4f translate = Matrix4f.translate((float) -camX, (float) -camY, (float) -camZ);

		translate.multiplyBackward(viewProjection);

		super.render(layer, translate, camX, camY, camZ, callback);
	}
}
