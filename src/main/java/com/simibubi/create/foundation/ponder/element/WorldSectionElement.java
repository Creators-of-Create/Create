package com.simibubi.create.foundation.ponder.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Consumer;

import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.core.model.ShadeSeparatedBufferBuilder;
import com.jozufozu.flywheel.core.model.ShadeSeparatingVertexConsumer;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.render.TileEntityRenderHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class WorldSectionElement extends AnimatedSceneElement {

	public static final SuperByteBufferCache.Compartment<Pair<Integer, Integer>> DOC_WORLD_SECTION =
		new SuperByteBufferCache.Compartment<>();

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	List<BlockEntity> renderedTileEntities;
	List<Pair<BlockEntity, Consumer<Level>>> tickableTileEntities;
	Selection section;
	boolean redraw;

	Vec3 prevAnimatedOffset = Vec3.ZERO;
	Vec3 animatedOffset = Vec3.ZERO;
	Vec3 prevAnimatedRotation = Vec3.ZERO;
	Vec3 animatedRotation = Vec3.ZERO;
	Vec3 centerOfRotation = Vec3.ZERO;
	Vec3 stabilizationAnchor = null;

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

	public void setCenterOfRotation(Vec3 center) {
		centerOfRotation = center;
	}

	public void stabilizeRotation(Vec3 anchor) {
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
		prevAnimatedOffset = Vec3.ZERO;
		animatedOffset = Vec3.ZERO;
		prevAnimatedRotation = Vec3.ZERO;
		animatedRotation = Vec3.ZERO;
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

	public void setAnimatedRotation(Vec3 eulerAngles, boolean force) {
		this.animatedRotation = eulerAngles;
		if (force)
			prevAnimatedRotation = animatedRotation;
	}

	public Vec3 getAnimatedRotation() {
		return animatedRotation;
	}

	public void setAnimatedOffset(Vec3 offset, boolean force) {
		this.animatedOffset = offset;
		if (force)
			prevAnimatedOffset = animatedOffset;
	}

	public Vec3 getAnimatedOffset() {
		return animatedOffset;
	}

	@Override
	public boolean isVisible() {
		return super.isVisible() && !isEmpty();
	}

	class WorldSectionRayTraceResult {
		Vec3 actualHitVec;
		BlockPos worldPos;
	}

	public Pair<Vec3, BlockHitResult> rayTrace(PonderWorld world, Vec3 source, Vec3 target) {
		world.setMask(this.section);
		Vec3 transformedTarget = reverseTransformVec(target);
		BlockHitResult rayTraceBlocks = world.clip(new ClipContext(reverseTransformVec(source), transformedTarget,
			ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null));
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
		Vec3 actualHit = VecHelper.lerp((float) t, target, source);
		return Pair.of(actualHit, rayTraceBlocks);
	}

	private Vec3 reverseTransformVec(Vec3 in) {
		float pt = AnimationTickHolder.getPartialTicks();
		in = in.subtract(VecHelper.lerp(pt, prevAnimatedOffset, animatedOffset));
		if (!animatedRotation.equals(Vec3.ZERO) || !prevAnimatedRotation.equals(Vec3.ZERO)) {
			if (centerOfRotation == null)
				centerOfRotation = section.getCenter();
			double rotX = Mth.lerp(pt, prevAnimatedRotation.x, animatedRotation.x);
			double rotZ = Mth.lerp(pt, prevAnimatedRotation.z, animatedRotation.z);
			double rotY = Mth.lerp(pt, prevAnimatedRotation.y, animatedRotation.y);
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

	public void transformMS(PoseStack ms, float pt) {
		TransformStack.cast(ms)
			.translate(VecHelper.lerp(pt, prevAnimatedOffset, animatedOffset));
		if (!animatedRotation.equals(Vec3.ZERO) || !prevAnimatedRotation.equals(Vec3.ZERO)) {
			if (centerOfRotation == null)
				centerOfRotation = section.getCenter();
			double rotX = Mth.lerp(pt, prevAnimatedRotation.x, animatedRotation.x);
			double rotZ = Mth.lerp(pt, prevAnimatedRotation.z, animatedRotation.z);
			double rotY = Mth.lerp(pt, prevAnimatedRotation.y, animatedRotation.y);
			TransformStack.cast(ms)
				.translate(centerOfRotation)
				.rotateX(rotX)
				.rotateZ(rotZ)
				.rotateY(rotY)
				.translateBack(centerOfRotation);
			if (stabilizationAnchor != null) {
				TransformStack.cast(ms)
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
			.getBlockEntity(te.getFirst()
				.getBlockPos()) != te.getFirst());
		tickableTileEntities.forEach(te -> te.getSecond()
			.accept(scene.getWorld()));
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
			BlockEntity tileEntity = world.getBlockEntity(pos);
			BlockState blockState = world.getBlockState(pos);
			Block block = blockState.getBlock();
			if (tileEntity == null)
				return;
			if (!(block instanceof EntityBlock))
				return;
			tileEntity.setBlockState(world.getBlockState(pos));
			BlockEntityTicker<?> ticker = ((EntityBlock) block).getTicker(world, blockState, tileEntity.getType());
			if (ticker != null)
				addTicker(tileEntity, ticker);
			renderedTileEntities.add(tileEntity);
		});
	}

	@SuppressWarnings("unchecked")
	private <T extends BlockEntity> void addTicker(T tileEntity, BlockEntityTicker<?> ticker) {
		tickableTileEntities.add(Pair.of(tileEntity, w -> ((BlockEntityTicker<T>) ticker).tick(w,
			tileEntity.getBlockPos(), tileEntity.getBlockState(), tileEntity)));
	}

	@Override
	public void renderFirst(PonderWorld world, MultiBufferSource buffer, PoseStack ms, float fade, float pt) {
		int light = -1;
		if (fade != 1)
			light = (int) (Mth.lerp(fade, 5, 14));
		if (redraw) {
			renderedTileEntities = null;
			tickableTileEntities = null;
		}

		ms.pushPose();
		transformMS(ms, pt);
		world.pushFakeLight(light);
		renderTileEntities(world, ms, buffer, pt);
		world.popLight();

		Map<BlockPos, Integer> blockBreakingProgressions = world.getBlockBreakingProgressions();
		PoseStack overlayMS = null;

		for (Entry<BlockPos, Integer> entry : blockBreakingProgressions.entrySet()) {
			BlockPos pos = entry.getKey();
			if (!section.test(pos))
				continue;

			if (overlayMS == null) {
				overlayMS = new PoseStack();
				overlayMS.last().pose().load(ms.last().pose());
				overlayMS.last().normal().load(ms.last().normal());

				float scaleFactor = world.scene.getScaleFactor();
				float f = (float) Math.pow(30 * scaleFactor, -1.2);
				overlayMS.scale(f, f, f);
			}

			VertexConsumer builder = new SheetedDecalTextureGenerator(
				buffer.getBuffer(ModelBakery.DESTROY_TYPES.get(entry.getValue())), overlayMS.last()
					.pose(),
				overlayMS.last()
					.normal());

			ms.pushPose();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());
			ModelUtil.VANILLA_RENDERER
				.renderBreakingTexture(world.getBlockState(pos), pos, world, ms, builder, EmptyModelData.INSTANCE);
			ms.popPose();
		}

		ms.popPose();
	}

	@Override
	protected void renderLayer(PonderWorld world, MultiBufferSource buffer, RenderType type, PoseStack ms, float fade,
		float pt) {
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

		transformMS(contraptionBuffer.getTransforms(), pt);
		int light = lightCoordsFromFade(fade);
		contraptionBuffer
			.light(light)
			.renderInto(ms, buffer.getBuffer(type));
	}

	@Override
	protected void renderLast(PonderWorld world, MultiBufferSource buffer, PoseStack ms, float fade, float pt) {
		redraw = false;
		if (selectedBlock == null)
			return;
		BlockState blockState = world.getBlockState(selectedBlock);
		if (blockState.isAir())
			return;
		VoxelShape shape =
			blockState.getShape(world, selectedBlock, CollisionContext.of(Minecraft.getInstance().player));
		if (shape.isEmpty())
			return;

		ms.pushPose();
		transformMS(ms, pt);
		ms.translate(selectedBlock.getX(), selectedBlock.getY(), selectedBlock.getZ());

		AABBOutline aabbOutline = new AABBOutline(shape.bounds());
		aabbOutline.getParams()
			.lineWidth(1 / 64f)
			.colored(0xefefef)
			.disableNormals();
		aabbOutline.render(ms, (SuperRenderTypeBuffer) buffer, pt);

		ms.popPose();
	}

	private void renderTileEntities(PonderWorld world, PoseStack ms, MultiBufferSource buffer, float pt) {
		loadTEsIfMissing(world);
		TileEntityRenderHelper.renderTileEntities(world, renderedTileEntities, ms, buffer, pt);
	}

	private SuperByteBuffer buildStructureBuffer(PonderWorld world, RenderType layer) {
		BlockRenderDispatcher dispatcher = ModelUtil.VANILLA_RENDERER;
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		PoseStack poseStack = objects.poseStack;
		Random random = objects.random;
		ShadeSeparatingVertexConsumer shadeSeparatingWrapper = objects.shadeSeparatingWrapper;
		ShadeSeparatedBufferBuilder builder = new ShadeSeparatedBufferBuilder(512);
		BufferBuilder unshadedBuilder = objects.unshadedBuilder;

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		unshadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		shadeSeparatingWrapper.prepare(builder, unshadedBuilder);

		world.setMask(this.section);
		ForgeHooksClient.setRenderType(layer);
		ModelBlockRenderer.enableCaching();
		section.forEach(pos -> {
			BlockState state = world.getBlockState(pos);
			FluidState fluidState = world.getFluidState(pos);

			poseStack.pushPose();
			poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

			if (state.getRenderShape() == RenderShape.MODEL && ItemBlockRenderTypes.canRenderInLayer(state, layer)) {
				BlockEntity tileEntity = world.getBlockEntity(pos);
				dispatcher.renderBatched(state, pos, world, poseStack, shadeSeparatingWrapper, true, random,
					tileEntity != null ? tileEntity.getModelData() : EmptyModelData.INSTANCE);
			}

			if (!fluidState.isEmpty() && ItemBlockRenderTypes.canRenderInLayer(fluidState, layer))
				dispatcher.renderLiquid(pos, world, builder, state, fluidState);

			poseStack.popPose();
		});
		ModelBlockRenderer.clearCache();
		ForgeHooksClient.setRenderType(null);
		world.clearMask();

		shadeSeparatingWrapper.clear();
		unshadedBuilder.end();
		builder.appendUnshadedVertices(unshadedBuilder);
		builder.end();

		return new SuperByteBuffer(builder);
	}

	private static class ThreadLocalObjects {
		public final PoseStack poseStack = new PoseStack();
		public final Random random = new Random();
		public final ShadeSeparatingVertexConsumer shadeSeparatingWrapper = new ShadeSeparatingVertexConsumer();
		public final BufferBuilder unshadedBuilder = new BufferBuilder(512);
	}

}
