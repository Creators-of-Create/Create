package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.attrib.CommonAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.instancing.IInstanceRendered;
import com.jozufozu.flywheel.core.model.ArrayModelRenderer;
import com.jozufozu.flywheel.core.model.BufferedModel;
import com.jozufozu.flywheel.core.model.IndexedModel;
import com.jozufozu.flywheel.core.model.ModelRenderer;
import com.jozufozu.flywheel.light.GridAlignedBB;
import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.jozufozu.flywheel.util.QuadConverter;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;

public class RenderedContraption extends ContraptionWorldHolder {
	public static final VertexFormat FORMAT = VertexFormat.builder()
			.addAttributes(CommonAttributes.VEC3,
					CommonAttributes.NORMAL,
					CommonAttributes.UV,
					CommonAttributes.RGBA,
					CommonAttributes.LIGHT)
			.build();

	private final ContraptionLighter<?> lighter;
	public final ContraptionKineticRenderer kinetics;

	private final Map<RenderType, ModelRenderer> renderLayers = new HashMap<>();

	private Matrix4f model;
	private AxisAlignedBB lightBox;

	public RenderedContraption(World world, PlacementSimulationWorld renderWorld, Contraption contraption) {
		super(contraption, renderWorld);
		this.lighter = contraption.makeLighter();
		this.kinetics = new ContraptionKineticRenderer(this);

		buildLayers();
		if (Backend.canUseInstancing()) {
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
			teardown();
		}
	}

	public void beginFrame(ActiveRenderInfo info, double camX, double camY, double camZ) {
		kinetics.beginFrame(info);

		AbstractContraptionEntity entity = contraption.entity;
		float pt = AnimationTickHolder.getPartialTicks();

		MatrixStack stack = new MatrixStack();

		double x = MathHelper.lerp(pt, entity.lastTickPosX, entity.getX()) - camX;
		double y = MathHelper.lerp(pt, entity.lastTickPosY, entity.getY()) - camY;
		double z = MathHelper.lerp(pt, entity.lastTickPosZ, entity.getZ()) - camZ;
		stack.translate(x, y, z);

		entity.doLocalTransforms(pt, new MatrixStack[] { stack });

		model = stack.peek().getModel();

		AxisAlignedBB lightBox = GridAlignedBB.toAABB(lighter.lightVolume.getTextureVolume());

		this.lightBox = lightBox.offset(-camX, -camY, -camZ);
	}

	void setup(ContraptionProgram shader) {
		if (model == null || lightBox == null) return;
		shader.bind(model, lightBox);
		lighter.lightVolume.bind();
	}

	void teardown() {
		lighter.lightVolume.unbind();
	}

	void invalidate() {
		for (ModelRenderer buffer : renderLayers.values()) {
			buffer.delete();
		}
		renderLayers.clear();

		lighter.lightVolume.delete();

		kinetics.invalidate();
	}

	private void buildLayers() {
		for (ModelRenderer buffer : renderLayers.values()) {
			buffer.delete();
		}

		renderLayers.clear();

		List<RenderType> blockLayers = RenderType.getBlockLayers();

		for (RenderType layer : blockLayers) {
			BufferedModel layerModel = buildStructureModel(renderWorld, contraption, layer);

			if (layerModel != null) {
				if (Backend.compat.vertexArrayObjectsSupported())
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
				if (te instanceof IInstanceRendered) {
					World world = te.getWorld();
					BlockPos pos = te.getPos();
					te.setLocation(renderWorld, pos);
					kinetics.add(te);
					te.setLocation(world, pos);
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

			byte block = (byte) (LightTexture.getBlockLightCoordinates(light) << 4);
			byte sky = (byte) (LightTexture.getSkyLightCoordinates(light) << 4);

			vertices.put(block);
			vertices.put(sky);
		}

		vertices.rewind();

		return new IndexedModel(GlPrimitive.TRIANGLES, format, vertices, vertexCount, QuadConverter.getInstance().getEboForNQuads(vertexCount / 4));
	}
}
