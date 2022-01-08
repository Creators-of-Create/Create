package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.core.vertex.PosTexNormalWriterUnsafe;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.simibubi.create.AllStitchedTextures;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class GlueModel implements Model {

	public static final GlueModel INSTANCE = new GlueModel();
	static final boolean USE_ATLAS = false;

	public static GlueModel get() {
		return INSTANCE;
	}

	private final VertexList reader;

	private GlueModel() {
		PosTexNormalWriterUnsafe writer = Formats.POS_TEX_NORMAL.createWriter(MemoryTracker.create(size()));
		createGlueModel(writer);
		reader = writer.intoReader();
	}

	@Override
	public String name() {
		return "glue";
	}

	@Override
	public int vertexCount() {
		return 8;
	}

	@Override
	public VertexList getReader() {
		return reader;
	}

	public static void createGlueModel(PosTexNormalWriterUnsafe buffer) {
		Vec3 diff = Vec3.atLowerCornerOf(Direction.SOUTH.getNormal());
		Vec3 extension = diff.normalize()
				.scale(1 / 32f - 1 / 128f);

		Vec3 plane = VecHelper.axisAlingedPlaneOf(diff);
		Direction.Axis axis = Direction.getNearest(diff.x, diff.y, diff.z)
				.getAxis();

		Vec3 start = Vec3.ZERO.subtract(extension);
		Vec3 end = Vec3.ZERO.add(extension);

		plane = plane.scale(1 / 2f);
		Vec3 a1 = plane.add(start);
		Vec3 b1 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3 a2 = plane.add(start);
		Vec3 b2 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3 a3 = plane.add(start);
		Vec3 b3 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3 a4 = plane.add(start);
		Vec3 b4 = plane.add(end);

		float minU;
		float maxU;
		float minV;
		float maxV;

		if (USE_ATLAS) {
			TextureAtlasSprite sprite = AllStitchedTextures.SUPER_GLUE.get();
			minU = sprite.getU0();
			maxU = sprite.getU1();
			minV = sprite.getV0();
			maxV = sprite.getV1();
		} else {
			minU = minV = 0;
			maxU = maxV = 1;
		}

		// inside quad
		buffer.putVertex((float) a1.x, (float) a1.y, (float) a1.z, 0, 0, -1, maxU, minV);
		buffer.putVertex((float) a2.x, (float) a2.y, (float) a2.z, 0, 0, -1, maxU, maxV);
		buffer.putVertex((float) a3.x, (float) a3.y, (float) a3.z, 0, 0, -1, minU, maxV);
		buffer.putVertex((float) a4.x, (float) a4.y, (float) a4.z, 0, 0, -1, minU, minV);
		// outside quad
		buffer.putVertex((float) b4.x, (float) b4.y, (float) b4.z, 0, 0, 1f, minU, minV);
		buffer.putVertex((float) b3.x, (float) b3.y, (float) b3.z, 0, 0, 1f, minU, maxV);
		buffer.putVertex((float) b2.x, (float) b2.y, (float) b2.z, 0, 0, 1f, maxU, maxV);
		buffer.putVertex((float) b1.x, (float) b1.y, (float) b1.z, 0, 0, 1f, maxU, minV);
	}
}
