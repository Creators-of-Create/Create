package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.*;
import com.simibubi.create.foundation.render.*;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.event.TickEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL40;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

public class ContraptionRenderDispatcher {
    public static final Int2ObjectMap<RenderedContraption> renderers = new Int2ObjectOpenHashMap<>();
    public static final Compartment<Pair<Contraption, Integer>> CONTRAPTION = new Compartment<>();
    protected static PlacementSimulationWorld renderWorld;

    public static void notifyLightUpdate(ILightReader world, LightType type, SectionPos pos) {
        for (RenderedContraption renderer : renderers.values()) {
            renderer.getLighter().lightVolume.notifyLightUpdate(world, type, pos);
        }
    }

    public static void renderTick(TickEvent.RenderTickEvent event) {
        if (!Backend.canUseVBOs()) return;

        ClientWorld world = Minecraft.getInstance().world;
        if (event.phase == TickEvent.Phase.START && world != null) {
            Map<Integer, WeakReference<AbstractContraptionEntity>> map = ContraptionHandler.loadedContraptions.get(world);

            for (WeakReference<AbstractContraptionEntity> weakReference : map.values()) {
                AbstractContraptionEntity entity = weakReference.get();

                if (entity == null) continue;

                EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();

                EntityRenderer<?> renderer = renderManager.getRenderer(entity);

                if (renderer instanceof AbstractContraptionEntityRenderer) {
                    updateTransform(entity, (AbstractContraptionEntityRenderer<? super AbstractContraptionEntity>) renderer);
                }
            }
        }
    }

    public static void renderTileEntities(World world, Contraption c, MatrixStack ms, MatrixStack msLocal,
                                             IRenderTypeBuffer buffer) {
        if (Backend.canUseInstancing()) {
            RenderedContraption renderer = getRenderer(world, c);

            TileEntityRenderHelper.renderTileEntities(world, renderer.renderWorld, c.specialRenderedTileEntities, ms, msLocal, buffer);
        } else {
            TileEntityRenderHelper.renderTileEntities(world, c.specialRenderedTileEntities, ms, msLocal, buffer);
        }
    }

    private static <C extends AbstractContraptionEntity> void updateTransform(C c, AbstractContraptionEntityRenderer<C> entityRenderer) {
        MatrixStack stack = entityRenderer.makeTransformMatrix(c, AnimationTickHolder.getPartialTicks());

        Contraption c1 = c.getContraption();
        getRenderer(c1.entity.world, c1).setRenderSettings(stack.peek().getModel());
    }

    public static void tick() {
        for (RenderedContraption contraption : renderers.values()) {
            contraption.getLighter().tick(contraption);
        }
    }

    private static RenderedContraption getRenderer(World world, Contraption c) {
        int entityId = c.entity.getEntityId();
        RenderedContraption contraption = renderers.get(entityId);

        if (contraption == null) {
            contraption = new RenderedContraption(world, c);
            renderers.put(entityId, contraption);
        }

        return contraption;
    }

    public static void renderLayer(RenderType layer, Matrix4f viewProjection, float camX, float camY, float camZ) {
        removeDeadContraptions();

        if (renderers.isEmpty()) return;

        layer.startDrawing();
        GL11.glEnable(GL13.GL_TEXTURE_3D);
        GL13.glActiveTexture(GL40.GL_TEXTURE4); // the shaders expect light volumes to be in texture 4

        if (Backend.canUseVBOs()) {
            ContraptionProgram structureShader = Backend.getProgram(AllProgramSpecs.CONTRAPTION_STRUCTURE);
            structureShader.bind(viewProjection, camX, camY, camZ, FastRenderDispatcher.getDebugMode());
            for (RenderedContraption renderer : renderers.values()) {
                renderer.doRenderLayer(layer, structureShader);
            }
        }

        if (Backend.canUseInstancing()) {
            for (RenderedContraption renderer : renderers.values()) {
                renderer.kinetics.render(layer, viewProjection, camX, camY, camZ, renderer::setup);
                renderer.teardown();
            }
        }

        layer.endDrawing();
        GL11.glDisable(GL13.GL_TEXTURE_3D);
        GL13.glActiveTexture(GL40.GL_TEXTURE0);
    }

    public static void removeDeadContraptions() {
        ArrayList<Integer> toRemove = new ArrayList<>();

        for (RenderedContraption renderer : renderers.values()) {
            if (renderer.isDead()) {
                toRemove.add(renderer.getEntityId());
                renderer.invalidate();
            }
        }


        for (Integer id : toRemove) {
            renderers.remove(id);
        }
    }

    public static void invalidateAll() {
        for (RenderedContraption renderer : renderers.values()) {
            renderer.invalidate();
        }

        renderers.clear();
    }

    public static void render(AbstractContraptionEntity entity, MatrixStack ms, IRenderTypeBuffer buffers,
                              MatrixStack msLocal, Contraption contraption) {
        if (Backend.canUseVBOs()) {
            ContraptionRenderDispatcher.renderDynamic(entity.world, contraption, ms, msLocal, buffers);
        } else {
            ContraptionRenderDispatcher.renderDynamic(entity.world, contraption, ms, msLocal, buffers);
            ContraptionRenderDispatcher.renderStructure(entity.world, contraption, ms, msLocal, buffers);
        }
    }

    public static void renderDynamic(World world, Contraption c, MatrixStack ms, MatrixStack msLocal,
                                     IRenderTypeBuffer buffer) {
        renderTileEntities(world, c, ms, msLocal, buffer);
        if (buffer instanceof IRenderTypeBuffer.Impl)
            ((IRenderTypeBuffer.Impl) buffer).draw();
        renderActors(world, c, ms, msLocal, buffer);
    }

    public static void renderStructure(World world, Contraption c, MatrixStack ms, MatrixStack msLocal,
                                          IRenderTypeBuffer buffer) {
        SuperByteBufferCache bufferCache = CreateClient.bufferCache;
        List<RenderType> blockLayers = RenderType.getBlockLayers();

        buffer.getBuffer(RenderType.getSolid());
        for (int i = 0; i < blockLayers.size(); i++) {
            RenderType layer = blockLayers.get(i);
            Pair<Contraption, Integer> key = Pair.of(c, i);
            SuperByteBuffer contraptionBuffer = bufferCache.get(CONTRAPTION, key, () -> buildStructureBuffer(c, layer));
            if (contraptionBuffer.isEmpty())
                continue;
            Matrix4f model = msLocal.peek()
                .getModel();
            contraptionBuffer.light(model)
                .renderInto(ms, buffer.getBuffer(layer));
        }
    }

    private static SuperByteBuffer buildStructureBuffer(Contraption c, RenderType layer) {
        BufferBuilder builder = buildStructure(c, layer);
        return new SuperByteBuffer(builder);
    }

    public static BufferBuilder buildStructure(Contraption c, RenderType layer) {
        if (renderWorld == null || renderWorld.getWorld() != Minecraft.getInstance().world)
            renderWorld = new PlacementSimulationWorld(Minecraft.getInstance().world);

        ForgeHooksClient.setRenderLayer(layer);
        MatrixStack ms = new MatrixStack();
        BlockRendererDispatcher dispatcher = Minecraft.getInstance()
                                                      .getBlockRendererDispatcher();
        BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
        Random random = new Random();
        BufferBuilder builder = new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize());
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        renderWorld.setTileEntities(c.presentTileEntities.values());

        for (Template.BlockInfo info : c.getBlocks()
                                        .values())
            renderWorld.setBlockState(info.pos, info.state);

        for (Template.BlockInfo info : c.getBlocks()
                                        .values()) {
            BlockState state = info.state;

            if (state.getRenderType() == BlockRenderType.ENTITYBLOCK_ANIMATED)
                continue;
            if (!RenderTypeLookup.canRenderInLayer(state, layer))
                continue;

            IBakedModel originalModel = dispatcher.getModelForState(state);
            ms.push();
            ms.translate(info.pos.getX(), info.pos.getY(), info.pos.getZ());
            blockRenderer.renderModel(renderWorld, originalModel, state, info.pos, ms, builder, true, random, 42,
                                      OverlayTexture.DEFAULT_UV, EmptyModelData.INSTANCE);
            ms.pop();
        }

        builder.finishDrawing();
        renderWorld.clear();
        renderWorld = null;
        return builder;
    }

    protected static void renderActors(World world, Contraption c, MatrixStack ms, MatrixStack msLocal,
                                       IRenderTypeBuffer buffer) {
        MatrixStack[] matrixStacks = new MatrixStack[] { ms, msLocal };
        for (Pair<Template.BlockInfo, MovementContext> actor : c.getActors()) {
            MovementContext context = actor.getRight();
            if (context == null)
                continue;
            if (context.world == null)
                context.world = world;
            Template.BlockInfo blockInfo = actor.getLeft();
            for (MatrixStack m : matrixStacks) {
                m.push();
                MatrixStacker.of(m)
                             .translate(blockInfo.pos);
            }

            MovementBehaviour movementBehaviour = AllMovementBehaviours.of(blockInfo.state);
            if (movementBehaviour != null)
                movementBehaviour.renderInContraption(context, ms, msLocal, buffer);

            for (MatrixStack m : matrixStacks)
                m.pop();
        }
    }

    public static int getLight(World world, float lx, float ly, float lz) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        float sky = 0, block = 0;
        float offset = 1 / 8f;

        for (float zOffset = offset; zOffset >= -offset; zOffset -= 2 * offset)
            for (float yOffset = offset; yOffset >= -offset; yOffset -= 2 * offset)
                for (float xOffset = offset; xOffset >= -offset; xOffset -= 2 * offset) {
                    pos.setPos(lx + xOffset, ly + yOffset, lz + zOffset);
                    sky += world.getLightLevel(LightType.SKY, pos) / 8f;
                    block += world.getLightLevel(LightType.BLOCK, pos) / 8f;
                }

        return ((int) sky) << 20 | ((int) block) << 4;
    }

    public static int getLightOnContraption(World world, PlacementSimulationWorld renderWorld, BlockPos pos, BlockPos lightPos) {
        int worldLight = WorldRenderer.getLightmapCoordinates(world, lightPos);

        if (renderWorld != null)
            return getMaxBlockLight(worldLight, renderWorld.getLightLevel(LightType.BLOCK, pos));

        return worldLight;
    }

    public static int getMaxBlockLight(int packedLight, int blockLightValue) {
        int unpackedBlockLight = LightTexture.getBlockLightCoordinates(packedLight);

        if (blockLightValue > unpackedBlockLight) {
            packedLight = (packedLight & 0xFFFF0000) | (blockLightValue << 4);
        }

        return packedLight;
    }

    public static int getLightOnContraption(MovementContext context) {
        int entityId = context.contraption.entity.getEntityId();

        RenderedContraption renderedContraption = renderers.get(entityId);
        if (renderedContraption != null) {
            return renderedContraption.renderWorld.getLightLevel(LightType.BLOCK, context.localPos);
        } else {
            return -1;
        }
    }
}
