package com.simibubi.create.foundation.ponder.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.render.Compartment;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.render.TileEntityRenderHelper;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class WorldSectionElement extends AnimatedSceneElement {

	public static final Compartment<Pair<Integer, Integer>> DOC_WORLD_SECTION = new Compartment<>();

	List<TileEntity> renderedTileEntities;
	Selection section;
	boolean redraw;

	Vec3d prevAnimatedOffset = Vec3d.ZERO;
	Vec3d animatedOffset = Vec3d.ZERO;
	Vec3d prevAnimatedRotation = Vec3d.ZERO;
	Vec3d animatedRotation = Vec3d.ZERO;
	Vec3d centerOfRotation = Vec3d.ZERO;

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
		centerOfRotation = this.section.getCenter();
		queueRedraw();
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
		prevAnimatedOffset = Vec3d.ZERO;
		animatedOffset = Vec3d.ZERO;
		prevAnimatedRotation = Vec3d.ZERO;
		animatedRotation = Vec3d.ZERO;
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

	public void setAnimatedRotation(Vec3d eulerAngles) {
		this.animatedRotation = eulerAngles;
	}

	public Vec3d getAnimatedRotation() {
		return animatedRotation;
	}

	public void setAnimatedOffset(Vec3d offset) {
		this.animatedOffset = offset;
	}

	public Vec3d getAnimatedOffset() {
		return animatedOffset;
	}

	@Override
	public boolean isVisible() {
		return super.isVisible() && !isEmpty();
	}

	class WorldSectionRayTraceResult {
		Vec3d actualHitVec;
		BlockPos worldPos;
	}

	public Pair<Vec3d, BlockPos> rayTrace(PonderWorld world, Vec3d source, Vec3d target) {
		world.setMask(this.section);
		Vec3d transformedTarget = reverseTransformVec(target);
		BlockRayTraceResult rayTraceBlocks = world.rayTraceBlocks(new RayTraceContext(reverseTransformVec(source),
			transformedTarget, BlockMode.OUTLINE, FluidMode.NONE, null));
		world.clearMask();

		if (rayTraceBlocks == null)
			return null;
		if (rayTraceBlocks.getHitVec() == null)
			return null;

		double t = rayTraceBlocks.getHitVec()
			.subtract(transformedTarget)
			.lengthSquared()
			/ source.subtract(target)
				.lengthSquared();
		Vec3d actualHit = VecHelper.lerp((float) t, target, source);
		return Pair.of(actualHit, rayTraceBlocks.getPos());
	}

	private Vec3d reverseTransformVec(Vec3d in) {
		float pt = AnimationTickHolder.getPartialTicks();
		in = in.subtract(VecHelper.lerp(pt, prevAnimatedOffset, animatedOffset));
		if (!animatedRotation.equals(Vec3d.ZERO) || !prevAnimatedRotation.equals(Vec3d.ZERO)) {
			in = in.subtract(centerOfRotation);
			in = VecHelper.rotate(in, -MathHelper.lerp(pt, prevAnimatedRotation.x, animatedRotation.x), Axis.X);
			in = VecHelper.rotate(in, -MathHelper.lerp(pt, prevAnimatedRotation.z, animatedRotation.z), Axis.Z);
			in = VecHelper.rotate(in, -MathHelper.lerp(pt, prevAnimatedRotation.y, animatedRotation.y), Axis.Y);
			in = in.add(centerOfRotation);
		}
		return in;
	}

	public void transformMS(MatrixStack ms, float pt) {
		MatrixStacker.of(ms)
			.translate(VecHelper.lerp(pt, prevAnimatedOffset, animatedOffset));
		if (!animatedRotation.equals(Vec3d.ZERO) || !prevAnimatedRotation.equals(Vec3d.ZERO))
			MatrixStacker.of(ms)
				.translate(centerOfRotation)
				.rotateX(MathHelper.lerp(pt, prevAnimatedRotation.x, animatedRotation.x))
				.rotateZ(MathHelper.lerp(pt, prevAnimatedRotation.z, animatedRotation.z))
				.rotateY(MathHelper.lerp(pt, prevAnimatedRotation.y, animatedRotation.y))
				.translateBack(centerOfRotation);
	}

	public void tick(PonderScene scene) {
		prevAnimatedOffset = animatedOffset;
		prevAnimatedRotation = animatedRotation;
		if (!isVisible())
			return;
		if (renderedTileEntities == null)
			return;
		renderedTileEntities.forEach(te -> {
			if (te instanceof ITickableTileEntity)
				((ITickableTileEntity) te).tick();
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
		if (redraw)
			renderedTileEntities = null;
		transformMS(ms, pt);
		world.pushFakeLight(light);
		renderTileEntities(world, ms, buffer, pt);
		world.popLight();
	}

	protected void renderStructure(PonderWorld world, MatrixStack ms, IRenderTypeBuffer buffer, RenderType type,
		float fade) {
		SuperByteBufferCache bufferCache = CreateClient.bufferCache;
		int code = hashCode() ^ world.hashCode();

		Pair<Integer, Integer> key = Pair.of(code, RenderType.getBlockLayers()
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
		if (blockState.isAir(world, selectedBlock))
			return;
		VoxelShape shape =
			blockState.getShape(world, selectedBlock, ISelectionContext.forEntity(Minecraft.getInstance().player));
		if (shape.isEmpty())
			return;

		ms.push();
		transformMS(ms, pt);
		RenderSystem.disableTexture();
		WorldRenderer.drawBox(ms, buffer.getBuffer(RenderType.getLines()), shape.getBoundingBox()
			.offset(selectedBlock), 1, 1, 1, 1);
		if (buffer instanceof SuperRenderTypeBuffer)
			((SuperRenderTypeBuffer) buffer).draw(RenderType.getLines());
		RenderSystem.enableTexture();
		ms.pop();
	}

	private void renderTileEntities(PonderWorld world, MatrixStack ms, IRenderTypeBuffer buffer, float pt) {
		if (renderedTileEntities == null) {
			renderedTileEntities = new ArrayList<>();
			section.forEach(pos -> {
				TileEntity tileEntity = world.getTileEntity(pos);
				if (tileEntity != null)
					renderedTileEntities.add(tileEntity);
			});
		} else
			renderedTileEntities.removeIf(te -> world.getTileEntity(te.getPos()) != te);
		TileEntityRenderHelper.renderTileEntities(world, renderedTileEntities, ms, new MatrixStack(), buffer, pt);
	}

	private SuperByteBuffer buildStructureBuffer(PonderWorld world, RenderType layer) {
		ForgeHooksClient.setRenderLayer(layer);
		MatrixStack ms = new MatrixStack();
		BlockRendererDispatcher dispatcher = Minecraft.getInstance()
			.getBlockRendererDispatcher();
		BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
		Random random = new Random();
		BufferBuilder builder = new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize());
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		world.setMask(this.section);

		section.forEach(pos -> {
			BlockState state = world.getBlockState(pos);
			IFluidState ifluidstate = world.getFluidState(pos);

			ms.push();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());

			if (state.getRenderType() != BlockRenderType.ENTITYBLOCK_ANIMATED && state.getBlock() != Blocks.AIR
				&& RenderTypeLookup.canRenderInLayer(state, layer))
				blockRenderer.renderModel(world, dispatcher.getModelForState(state), state, pos, ms, builder, true,
					random, 42, OverlayTexture.DEFAULT_UV, EmptyModelData.INSTANCE);

			if (!ifluidstate.isEmpty() && RenderTypeLookup.canRenderInLayer(ifluidstate, layer))
				dispatcher.renderFluid(pos, world, builder, ifluidstate);

			ms.pop();
		});

		world.clearMask();
		builder.finishDrawing();
		return new SuperByteBuffer(builder);
	}

}
