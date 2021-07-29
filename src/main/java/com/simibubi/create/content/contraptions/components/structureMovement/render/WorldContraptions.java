package com.simibubi.create.content.contraptions.components.structureMovement.render;

import static com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher.CONTRAPTION;
import static com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher.buildStructureBuffer;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;

import java.lang.ref.Reference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.state.RenderLayer;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.render.CreateContexts;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class WorldContraptions {
	private final World world;

	private int worldHolderRefreshCounter;

	public final Int2ObjectMap<RenderedContraption> flwRenderers = new Int2ObjectOpenHashMap<>();
	public final Int2ObjectMap<ContraptionRenderInfo> renderInfos = new Int2ObjectOpenHashMap<>();
	private final List<ContraptionRenderInfo> visible = new ObjectArrayList<>();
	private final List<RenderedContraption> flwVisible = new ObjectArrayList<>();

	public WorldContraptions(IWorld world) {
		this.world = (World) world;
	}

	public void tick() {
		for (RenderedContraption contraption : flwRenderers.values()) {
			ContraptionLighter<?> lighter = contraption.getLighter();
			if (lighter.getBounds().volume() < AllConfigs.CLIENT.maxContraptionLightVolume.get())
				lighter.tick(contraption);

			contraption.kinetics.tick();
		}

		worldHolderRefreshCounter++;
		if (worldHolderRefreshCounter >= 20) {
			removeDeadHolders();
			removeDeadContraptions();
			worldHolderRefreshCounter = 0;
		}

		Consumer<Contraption> setup;
		if (Backend.getInstance().available()) {
			setup = this::createRenderer;
		} else {
			setup = this::getRenderInfo;
		}

		ContraptionHandler.loadedContraptions.get(world)
				.values()
				.stream()
				.map(Reference::get)
				.filter(Objects::nonNull)
				.map(AbstractContraptionEntity::getContraption)
				.forEach(setup);
	}

	public void beginFrame(BeginFrameEvent event) {
		ActiveRenderInfo info = event.getInfo();
		double camX = info.getPosition().x;
		double camY = info.getPosition().y;
		double camZ = info.getPosition().z;

		visible.clear();
		flwVisible.clear();

		renderInfos.int2ObjectEntrySet()
				.stream()
				.map(Map.Entry::getValue)
				.forEach(renderInfo -> {
					renderInfo.beginFrame(event.getClippingHelper(), event.getStack(), camX, camY, camZ);
				});

		if (Backend.getInstance()
				.available()) {
			flwRenderers.int2ObjectEntrySet()
					.stream()
					.map(Map.Entry::getValue)
					.forEach(flwVisible::add);

			for (RenderedContraption renderer : flwVisible) {
				renderer.beginFrame(info, camX, camY, camZ);
			}
		} else {
			renderInfos.int2ObjectEntrySet()
					.stream()
					.map(Map.Entry::getValue)
					.forEach(visible::add);
		}
	}

	public void renderLayer(RenderLayerEvent event) {
		if (Backend.getInstance().available()) {
			renderLayerFlywheel(event);
		} else {
			renderLayerSBB(event);
		}
	}

	private void renderLayerFlywheel(RenderLayerEvent event) {
		if (flwVisible.isEmpty()) return;

		RenderType layer = event.getType();

		layer.setupRenderState();
		GlTextureUnit.T4.makeActive(); // the shaders expect light volumes to be in texture 4

		ContraptionProgram structureShader = CreateContexts.STRUCTURE.getProgram(AllProgramSpecs.STRUCTURE);

		structureShader.bind();
		structureShader.uploadViewProjection(event.viewProjection);
		structureShader.uploadCameraPos(event.camX, event.camY, event.camZ);

		for (RenderedContraption renderedContraption : flwVisible) {
			renderedContraption.doRenderLayer(layer, structureShader);
		}

		if (Backend.getInstance().canUseInstancing()) {
			RenderLayer renderLayer = event.getLayer();
			if (renderLayer != null) {
				for (RenderedContraption renderer : flwVisible) {
					renderer.materialManager.render(renderLayer, event.viewProjection, event.camX, event.camY, event.camZ);
				}
			}
		}

		// clear the light volume state
		GlTextureUnit.T4.makeActive();
		glBindTexture(GL_TEXTURE_3D, 0);

		layer.clearRenderState();
		GlTextureUnit.T0.makeActive();
	}

	private void renderLayerSBB(RenderLayerEvent event) {
		visible.forEach(info -> renderContraptionLayerSBB(event, info));
	}

	private void renderContraptionLayerSBB(RenderLayerEvent event, ContraptionRenderInfo renderInfo) {
		RenderType layer = event.getType();

		if (!renderInfo.isVisible()) return;

		SuperByteBuffer contraptionBuffer = CreateClient.BUFFER_CACHE.get(CONTRAPTION, Pair.of(renderInfo.contraption, layer),
				() -> buildStructureBuffer(renderInfo.renderWorld, renderInfo.contraption, layer));

		if (!contraptionBuffer.isEmpty()) {

			ContraptionMatrices matrices = renderInfo.getMatrices();
			contraptionBuffer.transform(matrices.contraptionStack)
					.light(matrices.entityMatrix)
					.hybridLight()
					.renderInto(matrices.entityStack, event.buffers.bufferSource().getBuffer(layer));
		}

	}

	private void createRenderer(Contraption c) {
		int entityId = c.entity.getId();
		RenderedContraption contraption = flwRenderers.get(entityId);

		if (contraption == null) {
			PlacementSimulationWorld renderWorld = ContraptionRenderDispatcher.setupRenderWorld(world, c);
			contraption = new RenderedContraption(c, renderWorld);
			flwRenderers.put(entityId, contraption);
			renderInfos.put(entityId, contraption);
		}

	}

	public ContraptionRenderInfo getRenderInfo(Contraption c) {
		int entityId = c.entity.getId();
		ContraptionRenderInfo renderInfo = renderInfos.get(entityId);

		if (renderInfo == null) {
			PlacementSimulationWorld renderWorld = ContraptionRenderDispatcher.setupRenderWorld(world, c);
			renderInfo = new ContraptionRenderInfo(c, renderWorld);
			renderInfos.put(entityId, renderInfo);
		}

		return renderInfo;
	}

	public void invalidate() {
		for (RenderedContraption renderer : flwRenderers.values()) {
			renderer.invalidate();
		}

		flwRenderers.clear();
		renderInfos.clear();
	}

	public void removeDeadContraptions() {
		flwRenderers.values().removeIf(renderer -> {
			if (renderer.isDead()) {
				renderer.invalidate();
				return true;
			}
			return false;
		});
	}

	public void removeDeadHolders() {
		renderInfos.values().removeIf(ContraptionRenderInfo::isDead);
	}
}
