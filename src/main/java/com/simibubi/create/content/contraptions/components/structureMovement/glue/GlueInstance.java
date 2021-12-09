package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.api.instance.ITickableInstance;
import com.jozufozu.flywheel.backend.api.Instancer;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstance;
import com.jozufozu.flywheel.backend.api.MaterialGroup;
import com.jozufozu.flywheel.backend.api.MaterialManager;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.instancing.ConditionalInstance;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.jozufozu.flywheel.core.model.Model;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllStitchedTextures;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class GlueInstance extends EntityInstance<SuperGlueEntity> implements ITickableInstance {

	private static final boolean USE_ATLAS = false;
	private static final ResourceLocation TEXTURE = Create.asResource("textures/entity/super_glue/slime.png");

	private final Quaternion rotation;
	protected ConditionalInstance<OrientedData> model;

	public GlueInstance(MaterialManager materialManager, SuperGlueEntity entity) {
		super(materialManager, entity);

		Instancer<OrientedData> instancer = getInstancer(materialManager, entity);

		Direction face = entity.getFacingDirection();
		rotation = new Quaternion(AngleHelper.verticalAngle(face), AngleHelper.horizontalAngle(face), 0, true);

		model = new ConditionalInstance<>(instancer)
				.withCondition(this::shouldShow)
				.withSetupFunc(this::positionModel)
				.update();
	}

	private Instancer<OrientedData> getInstancer(MaterialManager materialManager, SuperGlueEntity entity) {
		MaterialGroup group = USE_ATLAS ? materialManager.defaultCutout() : materialManager.cutout(RenderType.entityCutout(TEXTURE));

		return group.material(Materials.ORIENTED).model(entity.getType(), GlueModel::new);
	}

	@Override
	public void tick() {
		model.update();
	}

	@Override
	public void remove() {
		model.delete();
	}

	private void positionModel(OrientedData model) {

		model.setPosition(getInstancePosition())
				.setPivot(0, 0, 0)
				.setRotation(rotation);

		updateLight(model);
	}

	@Override
	public void updateLight() {
		model.get().ifPresent(this::updateLight);
	}

	private void updateLight(OrientedData model) {
		BlockPos pos = entity.getHangingPosition();
		model.setBlockLight(world.getBrightness(LightLayer.BLOCK, pos))
				.setSkyLight(world.getBrightness(LightLayer.SKY, pos));
	}

	private boolean shouldShow() {
		Player player = Minecraft.getInstance().player;

		return entity.isVisible()
				|| AllItems.SUPER_GLUE.isIn(player.getMainHandItem())
				|| AllItems.SUPER_GLUE.isIn(player.getOffhandItem());
	}

	public static class GlueModel implements Model {

		@Override
		public String name() {
			return "glue";
		}

		@Override
		public void buffer(VertexConsumer buffer) {
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
			buffer.vertex(a1.x, a1.y, a1.z).normal(0, 0, -1f).uv(maxU, minV).endVertex();
			buffer.vertex(a2.x, a2.y, a2.z).normal(0, 0, -1f).uv(maxU, maxV).endVertex();
			buffer.vertex(a3.x, a3.y, a3.z).normal(0, 0, -1f).uv(minU, maxV).endVertex();
			buffer.vertex(a4.x, a4.y, a4.z).normal(0, 0, -1f).uv(minU, minV).endVertex();
			// outside quad
			buffer.vertex(b4.x, b4.y, b4.z).normal(0, 0, 1f).uv(minU, minV).endVertex();
			buffer.vertex(b3.x, b3.y, b3.z).normal(0, 0, 1f).uv(minU, maxV).endVertex();
			buffer.vertex(b2.x, b2.y, b2.z).normal(0, 0, 1f).uv(maxU, maxV).endVertex();
			buffer.vertex(b1.x, b1.y, b1.z).normal(0, 0, 1f).uv(maxU, minV).endVertex();
		}

		@Override
		public int vertexCount() {
			return 8;
		}

		@Override
		public VertexFormat format() {
			return Formats.UNLIT_MODEL;
		}
	}
}
