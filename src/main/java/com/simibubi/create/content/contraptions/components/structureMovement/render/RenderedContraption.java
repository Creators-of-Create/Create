package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.material.instancing.InstancingEngine;
import com.jozufozu.flywheel.backend.model.ArrayModelRenderer;
import com.jozufozu.flywheel.backend.model.ModelRenderer;
import com.jozufozu.flywheel.core.model.IModel;
import com.jozufozu.flywheel.core.model.WorldModel;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.foundation.render.CreateContexts;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RenderedContraption extends ContraptionRenderInfo {

	private final ContraptionLighter<?> lighter;

	public final InstancingEngine<ContraptionProgram> materialManager;
	public final ContraptionInstanceManager kinetics;

	private final Map<RenderType, ModelRenderer> renderLayers = new HashMap<>();

	private final Matrix4f modelViewPartial = new Matrix4f();
	private boolean modelViewPartialReady;
	// floats because we're uploading this to the gpu
	private AABB lightBox;

	public RenderedContraption(Contraption contraption, PlacementSimulationWorld renderWorld) {
		super(contraption, renderWorld);
		this.lighter = contraption.makeLighter();
		this.materialManager = InstancingEngine.builder(CreateContexts.CWORLD)
				.setGroupFactory(ContraptionGroup.forContraption(this))
				.setIgnoreOriginCoordinate(true)
				.build();
		this.kinetics = new ContraptionInstanceManager(this, materialManager);
		this.materialManager.addListener(this.kinetics);

		buildLayers();
		if (Backend.getInstance().canUseInstancing()) {
			buildInstancedTiles();
			buildActors();
		}
	}

	public ContraptionLighter<?> getLighter() {
		return lighter;
	}

	public void doRenderLayer(RenderType layer, ContraptionProgram shader) {
		ModelRenderer structure = renderLayers.get(layer);
		if (structure != null) {
			setup(shader);
			structure.draw();
		}
	}

	public void beginFrame(BeginFrameEvent event) {
		super.beginFrame(event);

		modelViewPartial.setIdentity();
		modelViewPartialReady = false;

		if (!isVisible()) return;

		kinetics.beginFrame(event.getInfo());

		Vec3 cameraPos = event.getCameraPos();

		lightBox = lighter.lightVolume.toAABB()
				.move(-cameraPos.x, -cameraPos.y, -cameraPos.z);
	}

	@Override
	public void setupMatrices(PoseStack viewProjection, double camX, double camY, double camZ) {
		super.setupMatrices(viewProjection, camX, camY, camZ);

		if (!modelViewPartialReady) {
			setupModelViewPartial(modelViewPartial, getMatrices().getModel().last().pose(), contraption.entity, camX, camY, camZ, AnimationTickHolder.getPartialTicks());
			modelViewPartialReady = true;
		}
	}

	void setup(ContraptionProgram shader) {
		if (!modelViewPartialReady || lightBox == null) return;
		shader.bind(modelViewPartial, lightBox);
		lighter.lightVolume.bind();
	}

	public void invalidate() {
		for (ModelRenderer buffer : renderLayers.values()) {
			buffer.delete();
		}
		renderLayers.clear();

		lighter.delete();

		materialManager.delete();
		kinetics.invalidate();
	}

	private void buildLayers() {
		for (ModelRenderer buffer : renderLayers.values()) {
			buffer.delete();
		}

		renderLayers.clear();

		List<RenderType> blockLayers = RenderType.chunkBufferLayers();

		for (RenderType layer : blockLayers) {
			Supplier<IModel> layerModel = () -> new WorldModel(renderWorld, layer, contraption.getBlocks().values(), layer + "_" + contraption.entity.getId());

			ModelRenderer renderer;
			if (Backend.getInstance().compat.vertexArrayObjectsSupported())
				renderer = new ArrayModelRenderer(layerModel);
			else
				renderer = new ModelRenderer(layerModel);

			renderLayers.put(layer, renderer);
		}
	}

	private void buildInstancedTiles() {
		Collection<BlockEntity> tileEntities = contraption.maybeInstancedTileEntities;
		if (!tileEntities.isEmpty()) {
			for (BlockEntity te : tileEntities) {
				if (InstancedRenderRegistry.getInstance()
						.canInstance(te.getType())) {
					Level world = te.getLevel();
					te.setLevel(renderWorld);
					kinetics.add(te);
					te.setLevel(world);
				}
			}
		}
	}

	private void buildActors() {
		contraption.getActors().forEach(kinetics::createActor);
	}

	public static void setupModelViewPartial(Matrix4f matrix, Matrix4f modelMatrix, AbstractContraptionEntity entity, double camX, double camY, double camZ, float pt) {
		float x = (float) (Mth.lerp(pt, entity.xOld, entity.getX()) - camX);
		float y = (float) (Mth.lerp(pt, entity.yOld, entity.getY()) - camY);
		float z = (float) (Mth.lerp(pt, entity.zOld, entity.getZ()) - camZ);
		matrix.setTranslation(x, y, z);
		matrix.multiply(modelMatrix);
	}

}
