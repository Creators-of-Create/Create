package com.simibubi.create.content.fluids;

import org.joml.Vector4f;
import org.joml.Vector4fc;

import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.vertex.MutableVertexList;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.QuadMesh;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class FluidMesh {
	private static final RendererReloadCache<TextureAtlasSprite, Model> STREAM = new RendererReloadCache<>(sprite -> new SingleMeshModel(new FluidStreamMesh(sprite), material(sprite)));

	private static final RendererReloadCache<SurfaceKey, Model> SURFACE = new RendererReloadCache<>(sprite -> new SingleMeshModel(new FluidSurfaceMesh(sprite.texture(), sprite.width()), material(sprite.texture())));
	public static final float PIPE_RADIUS = 3f / 16f;

	// TODO: width parameter here too
	public static Model stream(TextureAtlasSprite sprite) {
		return STREAM.get(sprite);
	}

	public static Model surface(TextureAtlasSprite sprite, float width) {
		return SURFACE.get(new SurfaceKey(sprite, width));
	}

	private static SimpleMaterial material(TextureAtlasSprite sprite) {
		return SimpleMaterial.builder()
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.texture(sprite.atlasLocation())
			.transparency(Transparency.ORDER_INDEPENDENT)
			.build();
	}

	private record SurfaceKey(TextureAtlasSprite texture, float width) {
	}

	public record FluidSurfaceMesh(TextureAtlasSprite texture, float width) implements QuadMesh {
		@Override
		public int vertexCount() {
			int quadWidth = Mth.ceil(width) - Mth.floor(-width);
			return 4 * quadWidth * quadWidth;
		}

		@Override
		public void write(MutableVertexList vertexList) {
			for (int i = 0; i < vertexCount(); i++) {
				vertexList.r(i, 1);
				vertexList.g(i, 1);
				vertexList.b(i, 1);
				vertexList.a(i, 1);
				vertexList.light(i, 0);
				vertexList.overlay(i, OverlayTexture.NO_OVERLAY);

				vertexList.normalX(i, 0);
				vertexList.normalY(i, 1);
				vertexList.normalZ(i, 0);

				vertexList.y(i, 0);
			}

			float textureScale = 1 / 16f;

			float left = -width;
			float right = width;
			float down = -width;
			float up = width;

			int vertex = 0;

			float shrink = texture.uvShrinkRatio() * 0.25f * textureScale;
			float centerU = texture.getU0() + (texture.getU1() - texture.getU0()) * 0.5f;
			float centerV = texture.getV0() + (texture.getV1() - texture.getV0()) * 0.5f;

			float x2;
			float y2;
			for (float x1 = left; x1 < right; x1 = x2) {
				float x1floor = Mth.floor(x1);
				x2 = Math.min(x1floor + 1, right);
				float u1 = texture.getU((x1 - x1floor) * 16 * textureScale);
				float u2 = texture.getU((x2 - x1floor) * 16 * textureScale);
				u1 = Mth.lerp(shrink, u1, centerU);
				u2 = Mth.lerp(shrink, u2, centerU);
				for (float y1 = down; y1 < up; y1 = y2) {
					float y1floor = Mth.floor(y1);
					y2 = Math.min(y1floor + 1, up);
					float v1 = texture.getV((y1 - y1floor) * 16 * textureScale);
					float v2 = texture.getV((y2 - y1floor) * 16 * textureScale);
					v1 = Mth.lerp(shrink, v1, centerV);
					v2 = Mth.lerp(shrink, v2, centerV);

					vertexList.x(vertex, x1);
					vertexList.z(vertex, y1);
					vertexList.u(vertex, u1);
					vertexList.v(vertex, v1);

					vertexList.x(vertex + 1, x1);
					vertexList.z(vertex + 1, y2);
					vertexList.u(vertex + 1, u1);
					vertexList.v(vertex + 1, v2);

					vertexList.x(vertex + 2, x2);
					vertexList.z(vertex + 2, y2);
					vertexList.u(vertex + 2, u2);
					vertexList.v(vertex + 2, v2);

					vertexList.x(vertex + 3, x2);
					vertexList.z(vertex + 3, y1);
					vertexList.u(vertex + 3, u2);
					vertexList.v(vertex + 3, v1);
					vertex += 4;
				}
			}
		}

		@Override
		public Vector4fc boundingSphere() {
			return new Vector4f(0, 0, 0, width / Mth.SQRT_OF_TWO);
		}
	}

	public record FluidStreamMesh(TextureAtlasSprite texture) implements QuadMesh {
		@Override
		public int vertexCount() {
			return 4 * 2 * 4;
		}

		@Override
		public void write(MutableVertexList vertexList) {
			for (int i = 0; i < vertexCount(); i++) {
				vertexList.r(i, 1);
				vertexList.g(i, 1);
				vertexList.b(i, 1);
				vertexList.a(i, 1);
				vertexList.light(i, 0);
				vertexList.overlay(i, OverlayTexture.NO_OVERLAY);

				vertexList.v(i, 0);
			}

			float textureScale = 1 / 32f;

			float shrink = texture.uvShrinkRatio() * 0.25f * textureScale;
			float centerU = texture.getU0() + (texture.getU1() - texture.getU0()) * 0.5f;

			float radius = PIPE_RADIUS;
			float left = -radius;
			float right = radius;

			int vertex = 0;

			for (var horizontalDirection : Iterate.horizontalDirections) {
				float x2;
				for (float x1 = left; x1 < right; x1 = x2) {
					float x1floor = Mth.floor(x1);
					x2 = Math.min(x1floor + 1, right);
					float u1 = texture.getU((x1 - x1floor) * 16 * textureScale);
					float u2 = texture.getU((x2 - x1floor) * 16 * textureScale);
					u1 = Mth.lerp(shrink, u1, centerU);
					u2 = Mth.lerp(shrink, u2, centerU);

					putQuad(vertexList, vertex, horizontalDirection, radius, x1, x2, u1, u2);
					vertex += 4;
				}
			}
		}

		private static void putQuad(MutableVertexList vertexList, int i, Direction horizontal, float radius, float p0, float p1, float u0, float u1) {
			float xStart;
			float xEnd;
			float zStart;
			float zEnd;

			switch (horizontal) {
			case NORTH:
				xStart = p1;
				xEnd = p0;
				zStart = zEnd = -radius;
				break;
			case SOUTH:
				xStart = p0;
				xEnd = p1;
				zStart = zEnd = radius;
				break;
			case WEST:
				zStart = p0;
				zEnd = p1;
				xStart = xEnd = -radius;
				break;
			case EAST:
				zStart = p1;
				zEnd = p0;
				xStart = xEnd = radius;
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + horizontal);
			}

			vertexList.x(i, xStart);
			vertexList.y(i, 1);
			vertexList.z(i, zStart);
			vertexList.u(i, u0);

			vertexList.x(i + 1, xStart);
			vertexList.y(i + 1, 0);
			vertexList.z(i + 1, zStart);
			vertexList.u(i + 1, u0);

			vertexList.x(i + 2, xEnd);
			vertexList.y(i + 2, 0);
			vertexList.z(i + 2, zEnd);
			vertexList.u(i + 2, u1);

			vertexList.x(i + 3, xEnd);
			vertexList.y(i + 3, 1);
			vertexList.z(i + 3, zEnd);
			vertexList.u(i + 3, u1);

			for (int j = 0; j < 4; j++) {
				vertexList.normalX(i + j, horizontal.getStepX());
				vertexList.normalY(i + j, horizontal.getStepY());
				vertexList.normalZ(i + j, horizontal.getStepZ());
			}
		}

		@Override
		public Vector4fc boundingSphere() {
			return new Vector4f(0, 0.5f, 0, 1);
		}
	}
}
