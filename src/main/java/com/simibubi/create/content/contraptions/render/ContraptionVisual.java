package com.simibubi.create.content.contraptions.render;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.Contraption.RenderedBlocks;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedBlockAndTintGetter;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual;
import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualEmbedding;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.baked.ForgeMultiBlockModelBuilder;
import dev.engine_room.flywheel.lib.task.ForEachPlan;
import dev.engine_room.flywheel.lib.task.NestedPlan;
import dev.engine_room.flywheel.lib.task.PlanMap;
import dev.engine_room.flywheel.lib.task.RunnablePlan;
import dev.engine_room.flywheel.lib.visual.AbstractEntityVisual;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.client.model.data.ModelData;

public class ContraptionVisual<E extends AbstractContraptionEntity> extends AbstractEntityVisual<E> implements DynamicVisual, TickableVisual, LightUpdatedVisual, ShaderLightVisual {
	protected static final int LIGHT_PADDING = 1;

	protected final VisualEmbedding embedding;
	protected final List<BlockEntityVisual<?>> children = new ArrayList<>();
	protected final List<ActorVisual> actors = new ArrayList<>();
	protected final PlanMap<DynamicVisual, DynamicVisual.Context> dynamicVisuals = new PlanMap<>();
	protected final PlanMap<TickableVisual, TickableVisual.Context> tickableVisuals = new PlanMap<>();
	protected VirtualRenderWorld virtualRenderWorld;
	protected Model model;
	protected TransformedInstance structure;
	protected SectionCollector sectionCollector;
	protected long minSection, maxSection;
	protected long minBlock, maxBlock;

	private final PoseStack contraptionMatrix = new PoseStack();

	public ContraptionVisual(VisualizationContext ctx, E entity, float partialTick) {
		super(ctx, entity, partialTick);
		embedding = ctx.createEmbedding(Vec3i.ZERO);

		init(partialTick);
    }

	protected void init(float partialTick) {
		setEmbeddingMatrices(partialTick);

		Contraption contraption = entity.getContraption();
		virtualRenderWorld = ContraptionRenderInfo.setupRenderWorld(level, contraption);

		RenderedBlocks blocks = contraption.getRenderedBlocks();
		BlockAndTintGetter modelWorld = new WrappedBlockAndTintGetter(virtualRenderWorld) {
			@Override
			public BlockState getBlockState(BlockPos pos) {
				return blocks.lookup().apply(pos);
			}
		};

		model = new ForgeMultiBlockModelBuilder(modelWorld, blocks.positions())
				.modelDataLookup(pos -> contraption.modelData.getOrDefault(pos, ModelData.EMPTY))
				.build();

		structure = embedding.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, model)
				.createInstance();

		for (BlockEntity be : contraption.getRenderedBEs()) {
			setupVisualizer(be, partialTick);
		}

		for (var actor : contraption.getActors()) {
			setupActor(actor, partialTick);
		}
	}

	@SuppressWarnings("unchecked")
	protected  <T extends BlockEntity> void setupVisualizer(T be, float partialTicks) {
		BlockEntityVisualizer<? super T> visualizer = (BlockEntityVisualizer<? super T>) VisualizerRegistry.getVisualizer(be.getType());
		if (visualizer == null) {
			return;
		}

		Level level = be.getLevel();
		be.setLevel(virtualRenderWorld);
		BlockEntityVisual<? super T> visual = visualizer.createVisual(this.embedding, be, partialTicks);

		children.add(visual);

		if (visual instanceof DynamicVisual dynamic) {
			dynamicVisuals.add(dynamic, dynamic.planFrame());
		}

		if (visual instanceof TickableVisual tickable) {
			tickableVisuals.add(tickable, tickable.planTick());
		}

		be.setLevel(level);
	}

	private void setupActor(MutablePair<StructureTemplate.StructureBlockInfo, MovementContext> actor, float partialTick) {
		MovementContext context = actor.getRight();
		if (context == null) {
			return;
		}
		if (context.world == null) {
			context.world = level;
		}

		StructureTemplate.StructureBlockInfo blockInfo = actor.getLeft();

		MovementBehaviour movementBehaviour = AllMovementBehaviours.getBehaviour(blockInfo.state());
		if (movementBehaviour == null) {
			return;
		}
		var visual = movementBehaviour.createVisual(this.embedding, virtualRenderWorld, context);

		if (visual == null) {
			return;
		}

		actors.add(visual);
	}

	@Override
	public Plan<TickableVisual.Context> planTick() {
		return NestedPlan.of(
				ForEachPlan.of(() -> actors, ActorVisual::tick),
				tickableVisuals
		);
	}

	@Override
	public Plan<DynamicVisual.Context> planFrame() {
		return NestedPlan.of(
				RunnablePlan.of(this::beginFrame),
				ForEachPlan.of(() -> actors, ActorVisual::beginFrame),
				dynamicVisuals
		);
	}

	protected void beginFrame(DynamicVisual.Context context) {
		var partialTick = context.partialTick();
		setEmbeddingMatrices(partialTick);

		if (hasMovedSections()) {
 			sectionCollector.sections(collectLightSections());
		}

		if (hasMovedBlocks()) {
			updateLight(partialTick);
		}
	}

	private void setEmbeddingMatrices(float partialTick) {
		double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
		double y = Mth.lerp(partialTick, entity.yOld, entity.getY());
		double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());

		contraptionMatrix.setIdentity();
		contraptionMatrix.translate(x, y, z);
		entity.applyLocalTransforms(contraptionMatrix, partialTick);

		embedding.transforms(contraptionMatrix.last().pose(), contraptionMatrix.last().normal());
	}

	@Override
	public void updateLight(float partialTick) {
	}

	public LongSet collectLightSections() {
		var boundingBox = entity.getBoundingBox();

		var minSectionX = minLightSection(boundingBox.minX);
		var minSectionY = minLightSection(boundingBox.minY);
		var minSectionZ = minLightSection(boundingBox.minZ);
		int maxSectionX = maxLightSection(boundingBox.maxX);
		int maxSectionY = maxLightSection(boundingBox.maxY);
		int maxSectionZ = maxLightSection(boundingBox.maxZ);

		minSection = SectionPos.asLong(minSectionX, minSectionY, minSectionZ);
		maxSection = SectionPos.asLong(maxSectionX, maxSectionY, maxSectionZ);

		LongSet longSet = new LongArraySet();

		for (int x = 0; x <= maxSectionX - minSectionX; x++) {
			for (int y = 0; y <= maxSectionY - minSectionY; y++) {
				for (int z = 0; z <= maxSectionZ - minSectionZ; z++) {
					longSet.add(SectionPos.offset(minSection, x, y, z));
				}
			}
		}

		return longSet;
	}

	protected boolean hasMovedBlocks() {
		var boundingBox = entity.getBoundingBox();

		int minX = minLight(boundingBox.minX);
		int minY = minLight(boundingBox.minY);
		int minZ = minLight(boundingBox.minZ);
		int maxX = maxLight(boundingBox.maxX);
		int maxY = maxLight(boundingBox.maxY);
		int maxZ = maxLight(boundingBox.maxZ);

		return minBlock != BlockPos.asLong(minX, minY, minZ) || maxBlock !=  BlockPos.asLong(maxX, maxY, maxZ);
	}

	protected boolean hasMovedSections() {
		var boundingBox = entity.getBoundingBox();

        var minSectionX = minLightSection(boundingBox.minX);
		var minSectionY = minLightSection(boundingBox.minY);
		var minSectionZ = minLightSection(boundingBox.minZ);
		int maxSectionX = maxLightSection(boundingBox.maxX);
		int maxSectionY = maxLightSection(boundingBox.maxY);
		int maxSectionZ = maxLightSection(boundingBox.maxZ);

		return minSection != SectionPos.asLong(minSectionX, minSectionY, minSectionZ) || maxSection != SectionPos.asLong(maxSectionX, maxSectionY, maxSectionZ);
	}

	@Override
	public void setSectionCollector(SectionCollector collector) {
		this.sectionCollector = collector;
	}

	@Override
	protected void _delete() {
		children.forEach(BlockEntityVisual::delete);

		actors.forEach(ActorVisual::delete);

		if (structure != null) {
			structure.delete();
		}
	}

	public static int minLight(double aabbPos) {
		return Mth.floor(aabbPos) - LIGHT_PADDING;
	}

	public static int maxLight(double aabbPos) {
		return Mth.ceil(aabbPos) + LIGHT_PADDING;
	}

	public static int minLightSection(double aabbPos) {
		return SectionPos.blockToSectionCoord(minLight(aabbPos));
	}

	public static int maxLightSection(double aabbPos) {
		return SectionPos.blockToSectionCoord(maxLight(aabbPos));
	}
}
