package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.attrib.CommonAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.backend.model.ArrayModelRenderer;
import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.backend.model.IndexedModel;
import com.jozufozu.flywheel.backend.model.ModelRenderer;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.light.GridAlignedBB;
import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.foundation.render.CreateContexts;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class RenderedContraption extends ContraptionRenderInfo {
	public static final VertexFormat FORMAT = VertexFormat.builder()
			.addAttributes(CommonAttributes.VEC3,
					CommonAttributes.NORMAL,
					CommonAttributes.UV,
					CommonAttributes.RGBA,
					CommonAttributes.LIGHT)
			.build();

	private final ContraptionLighter<?> lighter;

	public final MaterialManager<ContraptionProgram> materialManager;
	public final ContraptionInstanceManager kinetics;

	private final Map<RenderType, ModelRenderer> renderLayers = new HashMap<>();

	private Matrix4f model;
	private AxisAlignedBB lightBox;

	public RenderedContraption(Contraption contraption, PlacementSimulationWorld renderWorld) {
		super(contraption, renderWorld);
		this.lighter = contraption.makeLighter();
		this.materialManager = MaterialManager.builder(CreateContexts.CWORLD)
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

		if (!isVisible()) return;

		kinetics.beginFrame(event.getInfo());

		AbstractContraptionEntity entity = contraption.entity;
		float pt = AnimationTickHolder.getPartialTicks();
		AxisAlignedBB lightBox = GridAlignedBB.toAABB(lighter.lightVolume.getTextureVolume());

		Vector3d cameraPos = event.getInfo()
				.getPosition();

		float x = (float) (MathHelper.lerp(pt, entity.xOld, entity.getX()) - cameraPos.x);
		float y = (float) (MathHelper.lerp(pt, entity.yOld, entity.getY()) - cameraPos.y);
		float z = (float) (MathHelper.lerp(pt, entity.zOld, entity.getZ()) - cameraPos.z);
		model = Matrix4f.createTranslateMatrix(x, y, z);

		model.multiply(getMatrices().contraptionPose());

		this.lightBox = lightBox.move(-cameraPos.x, -cameraPos.y, -cameraPos.z);
	}

	void setup(ContraptionProgram shader) {
		if (model == null || lightBox == null) return;
		shader.bind(model, lightBox);
		lighter.lightVolume.bind();
	}

	void invalidate() {
		for (ModelRenderer buffer : renderLayers.values()) {
			buffer.delete();
		}
		renderLayers.clear();

		lighter.lightVolume.delete();

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
			BufferedModel layerModel = buildStructureModel(renderWorld, contraption, layer);

			if (layerModel != null) {
				if (Backend.getInstance().compat.vertexArrayObjectsSupported())
					renderLayers.put(layer, new ArrayModelRenderer(layerModel));
				else
					renderLayers.put(layer, new ModelRenderer(layerModel));
			}
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

	@Nullable
	private static BufferedModel buildStructureModel(PlacementSimulationWorld renderWorld, Contraption c, RenderType layer) {
		BufferBuilderReader reader = new BufferBuilderReader(ContraptionRenderDispatcher.buildStructure(renderWorld, c, layer));

		int vertexCount = reader.getVertexCount();
		if (vertexCount == 0) return null;

		VertexFormat format = FORMAT;

		ByteBuffer vertices = ByteBuffer.allocate(format.getStride() * vertexCount);
		vertices.order(ByteOrder.nativeOrder());

		for (int i = 0; i < vertexCount; i++) {
			vertices.putFloat(reader.getX(i));
			vertices.putFloat(reader.getY(i));
			vertices.putFloat(reader.getZ(i));

			vertices.put(reader.getNX(i));
			vertices.put(reader.getNY(i));
			vertices.put(reader.getNZ(i));

			vertices.putFloat(reader.getU(i));
			vertices.putFloat(reader.getV(i));

			vertices.put(reader.getR(i));
			vertices.put(reader.getG(i));
			vertices.put(reader.getB(i));
			vertices.put(reader.getA(i));

			int light = reader.getLight(i);

			byte block = (byte) (LightTexture.block(light) << 4);
			byte sky = (byte) (LightTexture.sky(light) << 4);

			vertices.put(block);
			vertices.put(sky);
		}

		vertices.rewind();

		return IndexedModel.fromSequentialQuads(format, vertices, vertexCount);
	}
}
