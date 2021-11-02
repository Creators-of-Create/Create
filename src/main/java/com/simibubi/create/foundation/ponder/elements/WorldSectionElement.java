package com.simibubi.create.foundation.ponder.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.render.Compartment;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.render.TileEntityRenderHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class WorldSectionElement extends AnimatedSceneElement {

	public static final Compartment<Pair<Integer, Integer>> DOC_WORLD_SECTION = new Compartment<>();

	List<TileEntity> renderedTileEntities;
	List<TileEntity> tickableTileEntities;
	Selection section;
	boolean redraw;

	Vector3d prevAnimatedOffset = Vector3d.ZERO;
	Vector3d animatedOffset = Vector3d.ZERO;
	Vector3d prevAnimatedRotation = Vector3d.ZERO;
	Vector3d animatedRotation = Vector3d.ZERO;
	Vector3d centerOfRotation = Vector3d.ZERO;
	Vector3d stabilizationAnchor = null;

	BlockPos selectedBlock;

	public WorldSectionElement() {}

	public WorldSectionElement(Selection section) {
		this.section = section.copy();
		centerOfRotation = section.getCenter();
	}

	public void mergeOnto(WorldSectionElement other) {
		setVisible(false);
		if (other.isEmpty())
			other.set(section);
		else
			other.add(section);
	}

	public void set(Selection selection) {
		applyNewSelection(selection.copy());
	}

	public void add(Selection toAdd) {
		applyNewSelection(this.section.add(toAdd));
	}

	public void erase(Selection toErase) {
		applyNewSelection(this.section.substract(toErase));
	}

	private void applyNewSelection(Selection selection) {
		this.section = selection;
		queueRedraw();
	}

	public void setCenterOfRotation(Vector3d center) {
		centerOfRotation = center;
	}

	public void stabilizeRotation(Vector3d anchor) {
		stabilizationAnchor = anchor;
	}

	@Override
	public void reset(PonderScene scene) {
		super.reset(scene);
		resetAnimatedTransform();
		resetSelectedBlock();
	}

	public void selectBlock(BlockPos pos) {
		selectedBlock = pos;
	}

	public void resetSelectedBlock() {
		selectedBlock = null;
	}

	public void resetAnimatedTransform() {
		prevAnimatedOffset = Vector3d.ZERO;
		animatedOffset = Vector3d.ZERO;
		prevAnimatedRotation = Vector3d.ZERO;
		animatedRotation = Vector3d.ZERO;
	}

	public void queueRedraw() {
		redraw = true;
	}

	public boolean isEmpty() {
		return section == null;
	}

	public void setEmpty() {
		section = null;
	}

	public void setAnimatedRotation(Vector3d eulerAngles, boolean force) {
		this.animatedRotation = eulerAngles;
		if (force)
			prevAnimatedRotation = animatedRotation;
	}

	public Vector3d getAnimatedRotation() {
		return animatedRotation;
	}

	public void setAnimatedOffset(Vector3d offset, boolean force) {
		this.animatedOffset = offset;
		if (force)
			prevAnimatedOffset = animatedOffset;
	}

	public Vector3d getAnimatedOffset() {
		return animatedOffset;
	}

	@Override
	public boolean isVisible() {
		return super.isVisible() && !isEmpty();
	}

	class WorldSectionRayTraceResult {
		Vector3d actualHitVec;
		BlockPos worldPos;
	}

	public Pair<Vector3d, BlockPos> rayTrace(PonderWorld world, Vector3d source, Vector3d target) {
		world.setMask(this.section);
		Vector3d transformedTarget = reverseTransformVec(target);
		BlockRayTraceResult rayTraceBlocks = world.clip(new RayTraceContext(reverseTransformVec(source),
			transformedTarget, BlockMode.OUTLINE, FluidMode.NONE, null));
		world.clearMask();

		if (rayTraceBlocks == null)
			return null;
		if (rayTraceBlocks.getLocation() == null)
			return null;

		double t = rayTraceBlocks.getLocation()
			.subtract(transformedTarget)
			.lengthSqr()
			/ source.subtract(target)
				.lengthSqr();
		Vector3d actualHit = VecHelper.lerp((float) t, target, source);
		return Pair.of(actualHit, rayTraceBlocks.getBlockPos());
	}

	private Vector3d reverseTransformVec(Vector3d in) {
		float pt = AnimationTickHolder.getPartialTicks();
		in = in.subtract(VecHelper.lerp(pt, prevAnimatedOffset, animatedOffset));
		if (!animatedRotation.equals(Vector3d.ZERO) || !prevAnimatedRotation.equals(Vector3d.ZERO)) {
			if (centerOfRotation == null)
				centerOfRotation = section.getCenter();
			double rotX = MathHelper.lerp(pt, prevAnimatedRotation.x, animatedRotation.x);
			double rotZ = MathHelper.lerp(pt, prevAnimatedRotation.z, animatedRotation.z);
			double rotY = MathHelper.lerp(pt, prevAnimatedRotation.y, animatedRotation.y);
			in = in.subtract(centerOfRotation);
			in = VecHelper.rotate(in, -rotX, Axis.X);
			in = VecHelper.rotate(in, -rotZ, Axis.Z);
			in = VecHelper.rotate(in, -rotY, Axis.Y);
			in = in.add(centerOfRotation);
			if (stabilizationAnchor != null) {
				in = in.subtract(stabilizationAnchor);
				in = VecHelper.rotate(in, rotX, Axis.X);
				in = VecHelper.rotate(in, rotZ, Axis.Z);
				in = VecHelper.rotate(in, rotY, Axis.Y);
				in = in.add(stabilizationAnchor);
			}
		}
		return in;
	}

	public void transformMS(MatrixStack ms, float pt) {
		MatrixTransformStack.of(ms)
			.translate(VecHelper.lerp(pt, prevAnimatedOffset, animatedOffset));
		if (!animatedRotation.equals(Vector3d.ZERO) || !prevAnimatedRotation.equals(Vector3d.ZERO)) {
			if (centerOfRotation == null)
				centerOfRotation = section.getCenter();
			double rotX = MathHelper.lerp(pt, prevAnimatedRotation.x, animatedRotation.x);
			double rotZ = MathHelper.lerp(pt, prevAnimatedRotation.z, animatedRotation.z);
			double rotY = MathHelper.lerp(pt, prevAnimatedRotation.y, animatedRotation.y);
			MatrixTransformStack.of(ms)
				.translate(centerOfRotation)
				.rotateX(rotX)
				.rotateZ(rotZ)
				.rotateY(rotY)
				.translateBack(centerOfRotation);
			if (stabilizationAnchor != null) {
				MatrixTransformStack.of(ms)
					.translate(stabilizationAnchor)
					.rotateX(-rotX)
					.rotateZ(-rotZ)
					.rotateY(-rotY)
					.translateBack(stabilizationAnchor);
			}
		}
	}

	public void tick(PonderScene scene) {
		prevAnimatedOffset = animatedOffset;
		prevAnimatedRotation = animatedRotation;
		if (!isVisible())
			return;
		loadTEsIfMissing(scene.getWorld());
		renderedTileEntities.removeIf(te -> scene.getWorld()
			.getBlockEntity(te.getBlockPos()) != te);
		tickableTileEntities.removeIf(te -> scene.getWorld()
			.getBlockEntity(te.getBlockPos()) != te);
		tickableTileEntities.forEach(te -> {
			if (te instanceof ITickableTileEntity)
				((ITickableTileEntity) te).tick();
		});
	}

	@Override
	public void whileSkipping(PonderScene scene) {
		if (redraw) {
			renderedTileEntities = null;
			tickableTileEntities = null;
		}
		redraw = false;
	}

	protected void loadTEsIfMissing(PonderWorld world) {
		if (renderedTileEntities != null)
			return;
		tickableTileEntities = new ArrayList<>();
		renderedTileEntities = new ArrayList<>();
		section.forEach(pos -> {
			TileEntity tileEntity = world.getBlockEntity(pos);
			if (tileEntity == null)
				return;
			tickableTileEntities.add(tileEntity);
			renderedTileEntities.add(tileEntity);
			tileEntity.clearCache();
		});
	}

	@Override
	protected void renderLayer(PonderWorld world, IRenderTypeBuffer buffer, RenderType type, MatrixStack ms, float fade,
		float pt) {
		transformMS(ms, pt);
		renderStructure(world, ms, buffer, type, fade);
	}

	@Override
	public void renderFirst(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade, float pt) {
		int light = -1;
		if (fade != 1)
			light = (int) (MathHelper.lerp(fade, 5, 14));
		if (redraw) {
			renderedTileEntities = null;
			tickableTileEntities = null;
		}
		transformMS(ms, pt);
		world.pushFakeLight(light);
		renderTileEntities(world, ms, buffer, pt);
		world.popLight();

		Map<BlockPos, Integer> blockBreakingProgressions = world.getBlockBreakingProgressions();
		MatrixStack overlayMS = null;

		for (Entry<BlockPos, Integer> entry : blockBreakingProgressions.entrySet()) {
			BlockPos pos = entry.getKey();
			if (!section.test(pos))
				continue;
			if (overlayMS == null) {
				overlayMS = new MatrixStack();
				world.scene.getTransform()
					.apply(overlayMS, pt, true);
				transformMS(overlayMS, pt);
			}
			ms.pushPose();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());
			IVertexBuilder builder = new MatrixApplyingVertexBuilder(
				buffer.getBuffer(ModelBakery.DESTROY_TYPES.get(entry.getValue())),
					overlayMS.last().pose(),
					overlayMS.last().normal());
			Minecraft.getInstance()
				.getBlockRenderer()
				.renderModel(world.getBlockState(pos), pos, world, ms, builder, true, world.random, EmptyModelData.INSTANCE);
			ms.popPose();
		}
	}

	protected void renderStructure(PonderWorld world, MatrixStack ms, IRenderTypeBuffer buffer, RenderType type,
		float fade) {
		SuperByteBufferCache bufferCache = CreateClient.BUFFER_CACHE;
		int code = hashCode() ^ world.hashCode();

		Pair<Integer, Integer> key = Pair.of(code, RenderType.chunkBufferLayers()
				.indexOf(type));
		if (redraw)
			bufferCache.invalidate(DOC_WORLD_SECTION, key);
		SuperByteBuffer contraptionBuffer =
				bufferCache.get(DOC_WORLD_SECTION, key, () -> buildStructureBuffer(world, type));
		if (contraptionBuffer.isEmpty())
			return;

		int light = lightCoordsFromFade(fade);
		contraptionBuffer.light(light)
			.renderInto(ms, buffer.getBuffer(type));
	}

	@Override
	protected void renderLast(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade, float pt) {
		redraw = false;
		if (selectedBlock == null)
			return;
		BlockState blockState = world.getBlockState(selectedBlock);
		if (blockState.isAir())
			return;
		VoxelShape shape =
			blockState.getShape(world, selectedBlock, ISelectionContext.of(Minecraft.getInstance().player));
		if (shape.isEmpty())
			return;

		ms.pushPose();
		transformMS(ms, pt);
		RenderSystem.disableTexture();
		WorldRenderer.renderLineBox(ms, buffer.getBuffer(RenderType.lines()), shape.bounds()
			.move(selectedBlock), 1, 1, 1, 0.6f);
		ms.popPose();
	}

	private void renderTileEntities(PonderWorld world, MatrixStack ms, IRenderTypeBuffer buffer, float pt) {
		loadTEsIfMissing(world);
		TileEntityRenderHelper.renderTileEntities(world, renderedTileEntities, ms, buffer, pt);
	}

	private SuperByteBuffer buildStructureBuffer(PonderWorld world, RenderType layer) {
		ForgeHooksClient.setRenderLayer(layer);
		MatrixStack ms = new MatrixStack();
		BlockRendererDispatcher dispatcher = Minecraft.getInstance()
			.getBlockRenderer();
		BlockModelRenderer blockRenderer = dispatcher.getModelRenderer();
		Random random = new Random();
		BufferBuilder builder = new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize());
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		world.setMask(this.section);

		section.forEach(pos -> {
			BlockState state = world.getBlockState(pos);
			FluidState ifluidstate = world.getFluidState(pos);

			ms.pushPose();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());

			if (state.getRenderShape() != BlockRenderType.ENTITYBLOCK_ANIMATED && state.getBlock() != Blocks.AIR
				&& RenderTypeLookup.canRenderInLayer(state, layer)) {
				TileEntity tileEntity = world.getBlockEntity(pos);
				blockRenderer.renderModel(world, dispatcher.getBlockModel(state), state, pos, ms, builder, true,
					random, 42, OverlayTexture.NO_OVERLAY,
					tileEntity != null ? tileEntity.getModelData() : EmptyModelData.INSTANCE);
			}

			if (!ifluidstate.isEmpty() && RenderTypeLookup.canRenderInLayer(ifluidstate, layer))
				dispatcher.renderLiquid(pos, world, builder, ifluidstate);

			ms.popPose();
		});

		world.clearMask();
		builder.end();
		ForgeHooksClient.setRenderLayer(null);
		return new SuperByteBuffer(builder);
	}

}
