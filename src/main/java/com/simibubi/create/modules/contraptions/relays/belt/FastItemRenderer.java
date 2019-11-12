package com.simibubi.create.modules.contraptions.relays.belt;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.foundation.utility.BufferManipulator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;

public class FastItemRenderer extends BufferManipulator {

	public FastItemRenderer(ByteBuffer original) {
		super(original);
	}

	public ByteBuffer getTranslatedAndRotated(World world, float x, float y, float z, float yaw, float pitch) {
		original.rewind();
		mutable.rewind();

		float cosYaw = MathHelper.cos(yaw);
		float sinYaw = MathHelper.sin(yaw);
		float cosPitch = MathHelper.cos(pitch);
		float sinPitch = MathHelper.sin(pitch);

		for (int vertex = 0; vertex < vertexCount(original); vertex++) {
			float xL = getX(original, vertex); // - (float) rotationOffset.x;
			float yL = getY(original, vertex); // - (float) rotationOffset.y;
			float zL = getZ(original, vertex); // - (float) rotationOffset.z;

			float xL2 = rotateX(xL, yL, zL, sinPitch, cosPitch, Axis.X);
			float yL2 = rotateY(xL, yL, zL, sinPitch, cosPitch, Axis.X);
			float zL2 = rotateZ(xL, yL, zL, sinPitch, cosPitch, Axis.X);
			//
			xL = rotateX(xL2, yL2, zL2, sinYaw, cosYaw, Axis.Y);
			yL = rotateY(xL2, yL2, zL2, sinYaw, cosYaw, Axis.Y);
			zL = rotateZ(xL2, yL2, zL2, sinYaw, cosYaw, Axis.Y);

			float xPos = xL + x; // + (float) (offset.x + rotationOffset.x);
			float yPos = yL + y; // + (float) (offset.y + rotationOffset.y);
			float zPos = zL + z; // + (float) (offset.z + rotationOffset.z);
			putPos(mutable, vertex, xPos, yPos, zPos);
			BlockPos pos = new BlockPos(xPos + .5f, yPos + .5f, zPos + .5f);
			putLight(mutable, vertex, world.getCombinedLight(pos, 15));
		}

		return mutable;
	}

	protected static Cache<Item, FastItemRenderer> cachedItems;

	public static void renderItem(BufferBuilder buffer, World world, ItemStack stack, float x, float y, float z,
			float yaw, float pitch) {
		if (stack.isEmpty())
			return;

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		IBakedModel model = itemRenderer.getModelWithOverrides(stack);

		if (model.isBuiltInRenderer()) {
			renderItemIntoBuffer(stack, itemRenderer, model, 0, buffer);
			return;
		}

		cacheIfMissing(stack);
		FastItemRenderer renderer = cachedItems.getIfPresent(stack.getItem());
		if (renderer == null)
			return;
		buffer.putBulkData(renderer.getTranslatedAndRotated(world, x, y +1, z, yaw, pitch));
	}

	protected static void cacheIfMissing(ItemStack stack) {
		if (cachedItems == null)
			cachedItems = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build();
		if (cachedItems.getIfPresent(stack.getItem()) != null)
			return;

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		IBakedModel model = itemRenderer.getModelWithOverrides(stack);

		int color = 0;
		BufferBuilder bufferbuilder = new BufferBuilder(0);
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		renderItemIntoBuffer(stack, itemRenderer, model, color, bufferbuilder);
		bufferbuilder.finishDrawing();
		cachedItems.put(stack.getItem(), new FastItemRenderer(bufferbuilder.getByteBuffer()));
	}

	protected static void renderItemIntoBuffer(ItemStack stack, ItemRenderer itemRenderer, IBakedModel model, int color,
			BufferBuilder bufferbuilder) {
		Random random = new Random(42L);
		for (Direction direction : Direction.values())
			itemRenderer.renderQuads(bufferbuilder, model.getQuads(null, direction, random, EmptyModelData.INSTANCE),
					color, stack);
		itemRenderer.renderQuads(bufferbuilder, model.getQuads(null, null, random, EmptyModelData.INSTANCE), color,
				stack);
	}

	public static void invalidateCache() {
		if (cachedItems != null)
			cachedItems.invalidateAll();
	}

}
