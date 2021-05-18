package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.core.IndexedModel;
import com.jozufozu.flywheel.backend.gl.attrib.CommonAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.instancing.IInstanceRendered;
import com.jozufozu.flywheel.backend.light.GridAlignedBB;
import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class RenderedContraption {
	public static final VertexFormat FORMAT = VertexFormat.builder()
			.addAttributes(CommonAttributes.VEC3,
					CommonAttributes.NORMAL,
					CommonAttributes.UV,
					CommonAttributes.RGBA,
					CommonAttributes.LIGHT)
			.build();

	private static final BlockModelRenderer MODEL_RENDERER = new BlockModelRenderer(Minecraft.getInstance().getBlockColors());
	private static final BlockModelShapes BLOCK_MODELS = Minecraft.getInstance().getModelManager().getBlockModelShapes();

	public Contraption contraption;
	private final ContraptionLighter<?> lighter;
	public final ContraptionKineticRenderer kinetics;
	public final PlacementSimulationWorld renderWorld;

	private final Map<RenderType, IndexedModel> renderLayers = new HashMap<>();

	private Matrix4f model;
    private AxisAlignedBB lightBox;

    public RenderedContraption(World world, Contraption contraption) {
        this.contraption = contraption;
        this.lighter = contraption.makeLighter();
        this.kinetics = new ContraptionKineticRenderer(this);
        this.renderWorld = setupRenderWorld(world, contraption);

        buildLayers();
        if (Backend.canUseInstancing()) {
            buildInstancedTiles();
            buildActors();
        }
    }

    public int getEntityId() {
        return contraption.entity.getEntityId();
    }

    public boolean isDead() {
        return !contraption.entity.isAlive();
    }

    public ContraptionLighter<?> getLighter() {
        return lighter;
    }

    public void doRenderLayer(RenderType layer, ContraptionProgram shader) {
		IndexedModel structure = renderLayers.get(layer);
        if (structure != null) {
            setup(shader);
            structure.render();
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

        entity.doLocalTransforms(pt, new MatrixStack[]{ stack });

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
		for (IndexedModel buffer : renderLayers.values()) {
			buffer.delete();
		}
        renderLayers.clear();

        lighter.lightVolume.delete();

        kinetics.invalidate();
    }

    private void buildLayers() {
		for (IndexedModel buffer : renderLayers.values()) {
			buffer.delete();
		}

        renderLayers.clear();

        List<RenderType> blockLayers = RenderType.getBlockLayers();

        for (RenderType layer : blockLayers) {
			IndexedModel layerModel = buildStructureModel(renderWorld, contraption, layer);
			if (layerModel != null) renderLayers.put(layer, layerModel);
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
	private static IndexedModel buildStructureModel(PlacementSimulationWorld renderWorld, Contraption c, RenderType layer) {
		BufferBuilderReader reader = new BufferBuilderReader(buildStructure(renderWorld, c, layer));

		int vertexCount = reader.getVertexCount();
		if (vertexCount == 0) return null;

		VertexFormat format = FORMAT;

		ByteBuffer to = ByteBuffer.allocate(format.getStride() * vertexCount);
		to.order(ByteOrder.nativeOrder());

		for (int i = 0; i < vertexCount; i++) {
			to.putFloat(reader.getX(i));
			to.putFloat(reader.getY(i));
			to.putFloat(reader.getZ(i));

			to.put(reader.getNX(i));
			to.put(reader.getNY(i));
			to.put(reader.getNZ(i));

			to.putFloat(reader.getU(i));
			to.putFloat(reader.getV(i));

			to.put(reader.getR(i));
			to.put(reader.getG(i));
			to.put(reader.getB(i));
			to.put(reader.getA(i));

			int light = reader.getLight(i);

			byte block = (byte) (LightTexture.getBlockLightCoordinates(light) << 4);
			byte sky = (byte) (LightTexture.getSkyLightCoordinates(light) << 4);

			to.put(block);
			to.put(sky);
		}

		to.rewind();

		return new IndexedModel(format, to, vertexCount);
	}

	private static PlacementSimulationWorld setupRenderWorld(World world, Contraption c) {
		PlacementSimulationWorld renderWorld = new PlacementSimulationWorld(world);

		renderWorld.setTileEntities(c.presentTileEntities.values());

		for (Template.BlockInfo info : c.getBlocks()
				.values())
			// Skip individual lighting updates to prevent lag with large contraptions
            renderWorld.setBlockState(info.pos, info.state, 128);

        renderWorld.updateLightSources();
        renderWorld.lighter.tick(Integer.MAX_VALUE, false, false);

        return renderWorld;
    }

    private static BufferBuilder buildStructure(PlacementSimulationWorld renderWorld, Contraption c, RenderType layer) {
        MatrixStack ms = new MatrixStack();
        Random random = new Random();
        BufferBuilder builder = new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize());
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        ForgeHooksClient.setRenderLayer(layer);
        BlockModelRenderer.enableCache();
        for (Template.BlockInfo info : c.getBlocks()
                                        .values()) {
            BlockState state = info.state;

            if (state.getRenderType() != BlockRenderType.MODEL)
                continue;
            if (!RenderTypeLookup.canRenderInLayer(state, layer))
                continue;

            BlockPos pos = info.pos;

            ms.push();
            ms.translate(pos.getX(), pos.getY(), pos.getZ());
            MODEL_RENDERER.renderModel(renderWorld, BLOCK_MODELS.getModel(state), state, pos, ms, builder, true,
                                       random, 42, OverlayTexture.DEFAULT_UV, EmptyModelData.INSTANCE);
            ms.pop();
        }
        BlockModelRenderer.disableCache();
        ForgeHooksClient.setRenderLayer(null);

        builder.finishDrawing();
        return builder;
    }
}
