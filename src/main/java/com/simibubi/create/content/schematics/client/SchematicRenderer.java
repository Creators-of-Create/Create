package com.simibubi.create.content.schematics.client;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.level.wrapper.SchematicLevel;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SchematicRenderer {

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private final Map<RenderType, SuperByteBuffer> bufferCache = new LinkedHashMap<>();
	private boolean changed;
	protected final SchematicLevel schematic;
	private final BlockPos anchor;
	private final List<BlockEntity> renderedBlockEntities = new ArrayList<>();
	private final BitSet shouldRenderBlockEntities = new BitSet();
	private final BitSet scratchErroredBlockEntities = new BitSet();

	public SchematicRenderer(SchematicLevel world) {
		this.anchor = world.anchor;
		this.schematic = world;
		this.changed = true;

		for (var renderedBlockEntity : schematic.getRenderedBlockEntities()) {
			renderedBlockEntities.add(renderedBlockEntity);
		}
		shouldRenderBlockEntities.set(0, renderedBlockEntities.size());
	}

	public void update() {
		changed = true;
	}

	public void render(PoseStack ms, SuperRenderTypeBuffer buffers) {
		// TODO 26.2: Rebuild schematic preview rendering on the render-state block model pipeline.
	}

	protected void redraw() {
		bufferCache.clear();
	}

	protected SuperByteBuffer drawLayer(RenderType layer) {
		return null;
	}

	private static int getLayerCount() {
		return 0;
	}

	private static class ThreadLocalObjects {
	}

}
