package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.mojang.math.Matrix4f;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
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
			buildInstancedTiles();
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

		modelViewPartial.setIdentity();
		modelViewPartialReady = false;

		if (!isVisible()) return;

		instanceWorld.tileInstanceManager.beginFrame(SerialTaskEngine.INSTANCE, event.getCamera());

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
		for (ArrayModelRenderer buffer : renderLayers.values()) {
			buffer.delete();
		}
		renderLayers.clear();

		lighter.delete();

		instanceWorld.delete();
	}

	private void buildLayers() {
		for (ArrayModelRenderer buffer : renderLayers.values()) {
			buffer.delete();
		}

		renderLayers.clear();

		List<RenderType> blockLayers = RenderType.chunkBufferLayers();
		Collection<StructureBlockInfo> renderedBlocks = contraption.getRenderedBlocks();

		for (RenderType layer : blockLayers) {
			Model layerModel = new WorldModelBuilder(layer).withRenderWorld(renderWorld)
					.withModelData(contraption.modelData)
					.withBlocks(renderedBlocks)
					.intoMesh(layer + "_" + contraption.entity.getId());
			renderLayers.put(layer, new ArrayModelRenderer(layerModel));
		}
	}

	private void buildInstancedTiles() {
		for (BlockEntity te : contraption.maybeInstancedTileEntities) {
			if (!InstancedRenderRegistry.canInstance(te.getType())) {
				continue;
			}

			Level world = te.getLevel();
			te.setLevel(renderWorld);
			instanceWorld.tileInstanceManager.add(te);
			te.setLevel(world);
		}
	}

	private void buildActors() {
		contraption.getActors().forEach(instanceWorld.tileInstanceManager::createActor);
	}

	public static void setupModelViewPartial(Matrix4f matrix, Matrix4f modelMatrix, AbstractContraptionEntity entity, double camX, double camY, double camZ, float pt) {
		float x = (float) (Mth.lerp(pt, entity.xOld, entity.getX()) - camX);
		float y = (float) (Mth.lerp(pt, entity.yOld, entity.getY()) - camY);
		float z = (float) (Mth.lerp(pt, entity.zOld, entity.getZ()) - camZ);
		matrix.setTranslation(x, y, z);
		matrix.multiply(modelMatrix);
	}

	public void tick() {
		instanceWorld.tileInstanceManager.tick();
	}

	public static class ContraptionInstanceWorld {

		private final Engine engine;
		private final ContraptionInstanceManager tileInstanceManager;

		public ContraptionInstanceWorld(FlwContraption parent) {
			switch (Backend.getBackendType()) {
			case INSTANCING -> {
				InstancingEngine<ContraptionProgram> engine = InstancingEngine.builder(CreateContexts.CWORLD)
						.setGroupFactory(ContraptionGroup.forContraption(parent))
						.setIgnoreOriginCoordinate(true)
						.build();
				tileInstanceManager = new ContraptionInstanceManager(engine, parent.renderWorld, parent.contraption);
				engine.addListener(tileInstanceManager);

				this.engine = engine;
			}
			case BATCHING -> {
				engine = new BatchingEngine();
				tileInstanceManager = new ContraptionInstanceManager(engine, parent.renderWorld, parent.contraption);
			}
			default -> throw new IllegalArgumentException("Unknown engine type");
			}
		}

		public void delete() {
			engine.delete();
			tileInstanceManager.invalidate();
		}
	}
}
