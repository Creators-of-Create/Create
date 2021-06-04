package com.jozufozu.flywheel.backend.instancing;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE4;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.util.BitSet;
import java.util.SortedSet;
import java.util.Vector;

import javax.annotation.Nonnull;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.CrumblingInstanceManager;
import com.jozufozu.flywheel.core.WorldContext;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.util.WorldAttached;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.LazyValue;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.IWorld;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class InstancedRenderDispatcher {

	private static final RenderType CRUMBLING = ModelBakery.BLOCK_DESTRUCTION_RENDER_LAYERS.get(0);

	private static final WorldAttached<TileInstanceManager> tileInstanceManager = new WorldAttached<>(world -> new TileInstanceManager(WorldContext.INSTANCE.getMaterialManager(world)));
	public static LazyValue<Vector<CrumblingInstanceManager>> blockBreaking = new LazyValue<>(() -> {
		Vector<CrumblingInstanceManager> renderers = new Vector<>(10);
		for (int i = 0; i < 10; i++) {
			renderers.add(new CrumblingInstanceManager());
		}
		return renderers;
	});

	@Nonnull
	public static TileInstanceManager get(IWorld world) {
		return tileInstanceManager.get(world);
	}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;

		TileInstanceManager instancer = get(world);

		Entity renderViewEntity = mc.renderViewEntity;
		instancer.tick(renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());
	}

	public static void enqueueUpdate(TileEntity te) {
		get(te.getWorld()).queueUpdate(te);
	}

	@SubscribeEvent
	public static void onBeginFrame(BeginFrameEvent event) {
		WorldContext.INSTANCE.getMaterialManager(event.getWorld())
				.checkAndShiftOrigin(event.getInfo());
		get(event.getWorld())
				.beginFrame(event.getInfo());
	}

	@SubscribeEvent
	public static void renderLayer(RenderLayerEvent event) {
		ClientWorld world = event.getWorld();
		if (!Backend.canUseInstancing(world)) return;
		MaterialManager<WorldProgram> materialManager = WorldContext.INSTANCE.getMaterialManager(world);

		event.type.startDrawing();

		materialManager.render(event.type, event.viewProjection, event.camX, event.camY, event.camZ);

		event.type.endDrawing();
	}

	@SubscribeEvent
	public static void onReloadRenderers(ReloadRenderersEvent event) {
		ClientWorld world = event.getWorld();
		if (Backend.canUseInstancing() && world != null) {
			WorldContext.INSTANCE.getMaterialManager(world).delete();

			TileInstanceManager tileRenderer = get(world);
			tileRenderer.invalidate();
			world.loadedTileEntityList.forEach(tileRenderer::add);
		}
	}

	public static void renderBreaking(ClientWorld world, Matrix4f viewProjection, double cameraX, double cameraY, double cameraZ) {
		if (!Backend.canUseInstancing(world)) return;

		WorldRenderer worldRenderer = Minecraft.getInstance().worldRenderer;
		Long2ObjectMap<SortedSet<DestroyBlockProgress>> breakingProgressions = worldRenderer.blockBreakingProgressions;

		if (breakingProgressions.isEmpty()) return;
		Vector<CrumblingInstanceManager> renderers = blockBreaking.getValue();

		BitSet bitSet = new BitSet(10);

		for (Long2ObjectMap.Entry<SortedSet<DestroyBlockProgress>> entry : breakingProgressions.long2ObjectEntrySet()) {
			BlockPos breakingPos = BlockPos.fromLong(entry.getLongKey());

			SortedSet<DestroyBlockProgress> progresses = entry.getValue();
			if (progresses != null && !progresses.isEmpty()) {
				int blockDamage = progresses.last().getPartialBlockDamage();
				bitSet.set(blockDamage);
				renderers.get(blockDamage).add(world.getTileEntity(breakingPos));
			}
		}

		TextureManager textureManager = Minecraft.getInstance().textureManager;
		ActiveRenderInfo info = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, textureManager.getTexture(PlayerContainer.BLOCK_ATLAS_TEXTURE).getGlTextureId());

		glActiveTexture(GL_TEXTURE4);

		CRUMBLING.startDrawing();
		bitSet.stream().forEach(i -> {
			Texture breaking = textureManager.getTexture(ModelBakery.BLOCK_DESTRUCTION_STAGE_TEXTURES.get(i));
			CrumblingInstanceManager renderer = renderers.get(i);
			renderer.beginFrame(info);

			if (breaking != null) {
				glBindTexture(GL_TEXTURE_2D, breaking.getGlTextureId());
				renderer.materialManager.render(RenderType.getCutoutMipped(), viewProjection, cameraX, cameraY, cameraZ);
			}

			renderer.invalidate();
		});
		CRUMBLING.endDrawing();

		glActiveTexture(GL_TEXTURE0);
		Texture breaking = textureManager.getTexture(ModelBakery.BLOCK_DESTRUCTION_STAGE_TEXTURES.get(0));
		if (breaking != null)
			glBindTexture(GL_TEXTURE_2D, breaking.getGlTextureId());
	}
}
