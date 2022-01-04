package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialGroup;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.instancing.ConditionalInstance;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.mojang.math.Quaternion;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;

public class GlueInstance extends EntityInstance<SuperGlueEntity> implements TickableInstance {

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
		MaterialGroup group = GlueModel.USE_ATLAS ? materialManager.defaultCutout() : materialManager.cutout(RenderType.entityCutout(TEXTURE));

		return group.material(Materials.ORIENTED).model(entity.getType(), GlueModel::get);
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

}
