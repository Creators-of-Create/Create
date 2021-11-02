package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.material.MaterialManagerImpl;
import com.jozufozu.flywheel.backend.model.ArrayModelRenderer;
import com.jozufozu.flywheel.backend.model.ModelRenderer;
import com.jozufozu.flywheel.core.model.IModel;
import com.jozufozu.flywheel.core.model.WorldModel;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.foundation.render.CreateContexts;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.phys.AABB;

public class RenderedContraption extends ContraptionRenderInfo {

	private final ContraptionLighter<?> lighter;

	public final MaterialManagerImpl<ContraptionProgram> materialManager;
	public final ContraptionInstanceManager kinetics;

	private final Map<RenderType, ModelRenderer> renderLayers = new HashMap<>();

	private final Matrix4f modelViewPartial = new Matrix4f();
	private boolean modelViewPartialReady;
	private AABB lightBox;

	public RenderedContraption(Contraption contraption, PlacementSimulationWorld renderWorld) {
		super(contraption, renderWorld);
		this.lighter = contraption.makeLighter();
		this.materialManager = MaterialManagerImpl.builder(CreateContexts.CWORLD)
				.setGroupFactory(ContraptionGroup.forContraption(this))
				.setIgnoreOriginCoordinate(true)
				.build();
		this.kinetics = new ContraptionInstanceManager(this, materialManager);

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

		Vector3d cameraPos = event.getCameraPos();

		lightBox = lighter.lightVolume.toAABB()
				.move(-cameraPos.x, -cameraPos.y, -cameraPos.z);
	}

	@Override
	public void setupMatrices(MatrixStack viewProjection, double camX, double camY, double camZ) {
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
			Supplier<IModel> layerModel = () -> new WorldModel(renderWorld, layer, contraption.getBlocks().values());

			ModelRenderer renderer;
			if (Backend.getInstance().compat.vertexArrayObjectsSupported())
				renderer = new ArrayModelRenderer(layerModel);
			else
				renderer = new ModelRenderer(layerModel);

			renderLayers.put(layer, renderer);
		}
	}

	private void buildInstancedTiles() {
		Collection<TileEntity> tileEntities = contraption.maybeInstancedTileEntities;
		if (!tileEntities.isEmpty()) {
			for (TileEntity te : tileEntities) {
				if (InstancedRenderRegistry.getInstance()
						.canInstance(te.getType())) {
					World world = te.getLevel();
					BlockPos pos = te.getBlockPos();
					te.setLevelAndPosition(renderWorld, pos);
					kinetics.add(te);
					te.setLevelAndPosition(world, pos);
				}
			}
		}
	}

	private void buildActors() {
		contraption.getActors().forEach(kinetics::createActor);
	}

	public static void setupModelViewPartial(Matrix4f matrix, Matrix4f modelMatrix, AbstractContraptionEntity entity, double camX, double camY, double camZ, float pt) {
		float x = (float) (MathHelper.lerp(pt, entity.xOld, entity.getX()) - camX);
		float y = (float) (MathHelper.lerp(pt, entity.yOld, entity.getY()) - camY);
		float z = (float) (MathHelper.lerp(pt, entity.zOld, entity.getZ()) - camZ);
		matrix.setTranslation(x, y, z);
		matrix.multiply(modelMatrix);
	}

}
