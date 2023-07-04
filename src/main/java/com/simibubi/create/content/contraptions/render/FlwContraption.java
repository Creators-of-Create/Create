package com.simibubi.create.content.contraptions.render;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.SerialTaskEngine;
import com.jozufozu.flywheel.backend.instancing.batching.BatchingEngine;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancingEngine;
import com.jozufozu.flywheel.backend.model.ArrayModelRenderer;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.core.model.WorldModelBuilder;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.render.CreateContexts;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FlwContraption extends ContraptionRenderInfo {

	private final ContraptionLighter<?> lighter;

	private final Map<RenderType, ArrayModelRenderer> renderLayers = new HashMap<>();

	private final Matrix4f modelViewPartial = new Matrix4f();
	private final ContraptionInstanceWorld instanceWorld;
	private boolean modelViewPartialReady;
	// floats because we upload this to the gpu
	private AABB lightBox;

	public FlwContraption(Contraption contraption, VirtualRenderWorld renderWorld) {
		super(contraption, renderWorld);
		this.lighter = contraption.makeLighter();

		instanceWorld = new ContraptionInstanceWorld(this);

		var restoreState = GlStateTracker.getRestoreState();
		buildLayers();
		if (ContraptionRenderDispatcher.canInstance()) {
			buildInstancedBlockEntities();
			buildActors();
		}
		restoreState.restore();
	}

	public ContraptionLighter<?> getLighter() {
		return lighter;
	}

	public void renderStructureLayer(RenderType layer, ContraptionProgram shader) {
		ArrayModelRenderer structure = renderLayers.get(layer);
		if (structure != null) {
			setup(shader);
			structure.draw();
		}
	}

	public void renderInstanceLayer(RenderLayerEvent event) {

		event.stack.pushPose();
		float partialTicks = AnimationTickHolder.getPartialTicks();
		AbstractContraptionEntity entity = contraption.entity;
		double x = Mth.lerp(partialTicks, entity.xOld, entity.getX());
		double y = Mth.lerp(partialTicks, entity.yOld, entity.getY());
		double z = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
		event.stack.translate(x - event.camX, y - event.camY, z - event.camZ);
		ContraptionMatrices.transform(event.stack, getMatrices().getModel());
		instanceWorld.engine.render(SerialTaskEngine.INSTANCE, event);

		event.stack.popPose();
	}

	public void beginFrame(BeginFrameEvent event) {
		super.beginFrame(event);

		modelViewPartial.identity();
		modelViewPartialReady = false;

		if (!isVisible()) return;

		instanceWorld.blockEntityInstanceManager.beginFrame(SerialTaskEngine.INSTANCE, event.getCamera());

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
		for (ArrayModelRenderer renderer : renderLayers.values()) {
			renderer.delete();
			renderer.getModel().delete();
		}
		renderLayers.clear();

		lighter.delete();

		instanceWorld.delete();
	}

	private void buildLayers() {
		for (ArrayModelRenderer renderer : renderLayers.values()) {
			renderer.delete();
			renderer.getModel().delete();
		}

		renderLayers.clear();

		List<RenderType> blockLayers = RenderType.chunkBufferLayers();
		Collection<StructureBlockInfo> renderedBlocks = contraption.getRenderedBlocks();

		for (RenderType layer : blockLayers) {
			Model layerModel = new WorldModelBuilder(layer).withRenderWorld(renderWorld)
					.withModelData(contraption.modelData)
					.withBlocks(renderedBlocks)
					.toModel(layer + "_" + contraption.entity.getId());
			renderLayers.put(layer, new ArrayModelRenderer(layerModel));
		}
	}

	private void buildInstancedBlockEntities() {
		for (BlockEntity be : contraption.maybeInstancedBlockEntities) {
			if (!InstancedRenderRegistry.canInstance(be.getType())) {
				continue;
			}

			Level world = be.getLevel();
			be.setLevel(renderWorld);
			instanceWorld.blockEntityInstanceManager.add(be);
			be.setLevel(world);
		}
	}

	private void buildActors() {
		contraption.getActors().forEach(instanceWorld.blockEntityInstanceManager::createActor);
	}

	public static void setupModelViewPartial(Matrix4f matrix, Matrix4f modelMatrix, AbstractContraptionEntity entity, double camX, double camY, double camZ, float pt) {
		float x = (float) (Mth.lerp(pt, entity.xOld, entity.getX()) - camX);
		float y = (float) (Mth.lerp(pt, entity.yOld, entity.getY()) - camY);
		float z = (float) (Mth.lerp(pt, entity.zOld, entity.getZ()) - camZ);
		matrix.setTranslation(x, y, z);
		matrix.mul(modelMatrix);
	}

	public void tick() {
		instanceWorld.blockEntityInstanceManager.tick();
	}

	public static class ContraptionInstanceWorld {

		private final Engine engine;
		private final ContraptionInstanceManager blockEntityInstanceManager;

		public ContraptionInstanceWorld(FlwContraption parent) {
			switch (Backend.getBackendType()) {
			case INSTANCING -> {
				InstancingEngine<ContraptionProgram> engine = InstancingEngine.builder(CreateContexts.CWORLD)
						.setGroupFactory(ContraptionGroup.forContraption(parent))
						.setIgnoreOriginCoordinate(true)
						.build();
				blockEntityInstanceManager = new ContraptionInstanceManager(engine, parent.renderWorld, parent.contraption);
				engine.addListener(blockEntityInstanceManager);

				this.engine = engine;
			}
			case BATCHING -> {
				engine = new BatchingEngine();
				blockEntityInstanceManager = new ContraptionInstanceManager(engine, parent.renderWorld, parent.contraption);
			}
			default -> throw new IllegalArgumentException("Unknown engine type");
			}
		}

		public void delete() {
			engine.delete();
			blockEntityInstanceManager.invalidate();
		}
	}
}
