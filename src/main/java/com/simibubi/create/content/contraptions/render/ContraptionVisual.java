package com.simibubi.create.content.contraptions.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

import org.apache.commons.lang3.tuple.MutablePair;

import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.context.Shader;
import com.jozufozu.flywheel.api.context.TextureSource;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.visual.BlockEntityVisual;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.LitVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visual.VisualTickContext;
import com.jozufozu.flywheel.api.visualization.BlockEntityVisualizer;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.api.visualization.VisualizerRegistry;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.baked.MultiBlockModelBuilder;
import com.jozufozu.flywheel.lib.task.ForEachPlan;
import com.jozufozu.flywheel.lib.task.NestedPlan;
import com.jozufozu.flywheel.lib.task.PlanMap;
import com.jozufozu.flywheel.lib.task.RunnablePlan;
import com.jozufozu.flywheel.lib.visual.AbstractEntityVisual;
import com.jozufozu.flywheel.lib.visual.SimpleDynamicVisual;
import com.jozufozu.flywheel.lib.visual.SimpleTickableVisual;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.render.AllContextShaders;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ContraptionVisual<E extends AbstractContraptionEntity> extends AbstractEntityVisual<E> implements DynamicVisual, TickableVisual, LitVisual {
	protected final ContraptionContext context;
	protected final VisualizationContext visualizationContext;
	private final List<BlockEntityVisual<?>> children = new ArrayList<>();
	private final List<ActorVisual> actors = new ArrayList<>();
	private final PlanMap<DynamicVisual, VisualFrameContext> dynamicVisuals = new PlanMap<>();
	private final List<SimpleDynamicVisual> simpleDynamicVisuals = new ArrayList<>();
	private final PlanMap<TickableVisual, VisualTickContext> tickableVisuals = new PlanMap<>();
	private final List<SimpleTickableVisual> simpleTickableVisuals = new ArrayList<>();
	private VirtualRenderWorld virtualRenderWorld;
	private Notifier notifier;
	private Model model;
	private TransformedInstance structure;

	private final PoseStack contraptionMatrix = new PoseStack();

	public ContraptionVisual(VisualizationContext ctx, E entity) {
		super(ctx, entity);
        context = new ContraptionContext();
		visualizationContext = ctx.withContext(context, Vec3i.ZERO);
    }

	@Override
	public void init(float partialTick) {
        Contraption contraption = entity.getContraption();
		virtualRenderWorld = ContraptionRenderDispatcher.setupRenderWorld(level, contraption);

		model = new MultiBlockModelBuilder(contraption.getRenderedBlocks())
				.modelDataMap(contraption.modelData)
				.renderWorld(virtualRenderWorld)
				.build();

		structure = visualizationContext.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, model)
				.createInstance();

		for (BlockEntity be : contraption.maybeInstancedBlockEntities) {
			setupVisualizer(be, partialTick);
		}

		for (var actor : contraption.getActors()) {
			setupActor(actor, partialTick);
		}
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
		var instance = movementBehaviour.createInstance(this.visualizationContext, virtualRenderWorld, context);

		if (instance == null) {
			return;
		}

		instance.init(partialTick);

		actors.add(instance);
	}

	@SuppressWarnings("unchecked")
	protected  <T extends BlockEntity> void setupVisualizer(T be, float partialTicks) {
		BlockEntityVisualizer<? super T> visualizer = (BlockEntityVisualizer<? super T>) VisualizerRegistry.getVisualizer(be.getType());
		if (visualizer == null) {
			return;
		}

		Level world = be.getLevel();
		be.setLevel(virtualRenderWorld);
		BlockEntityVisual<? super T> visual = visualizer.createVisual(this.visualizationContext, be);

		visual.init(partialTicks);

		children.add(visual);

		if (visual instanceof DynamicVisual dynamic) {
			if (dynamic instanceof SimpleDynamicVisual simple) {
				simpleDynamicVisuals.add(simple);
			} else {
				dynamicVisuals.add(dynamic, dynamic.planFrame());
			}
		}

		be.setLevel(world);
	}

	@Override
	public Plan<VisualTickContext> planTick() {
		return NestedPlan.of(
				ForEachPlan.of(() -> actors, ActorVisual::tick),
				ForEachPlan.of(() -> simpleTickableVisuals, SimpleTickableVisual::tick),
				tickableVisuals
		);
	}

	@Override
	public Plan<VisualFrameContext> planFrame() {
		return NestedPlan.of(
				RunnablePlan.of(this::beginFrame),
				ForEachPlan.of(() -> actors, ActorVisual::beginFrame),
				ForEachPlan.of(() -> simpleDynamicVisuals, SimpleDynamicVisual::beginFrame),
				dynamicVisuals
		);
	}

	@Override
	protected void _delete() {
		children.forEach(BlockEntityVisual::delete);

		actors.forEach(ActorVisual::delete);

		if (model != null) {
			model.delete();
		}

		if (structure != null) {
			structure.delete();
		}
	}

	@Override
	public void updateLight() {

	}

	protected void beginFrame(VisualFrameContext context) {
		double x = Mth.lerp(context.partialTick(), entity.xOld, entity.getX());
		double y = Mth.lerp(context.partialTick(), entity.yOld, entity.getY());
		double z = Mth.lerp(context.partialTick(), entity.zOld, entity.getZ());

		contraptionMatrix.setIdentity();
		contraptionMatrix.translate(x, y, z);
		entity.applyLocalTransforms(contraptionMatrix, context.partialTick());
	}

	@Override
	public void collectLightSections(LongConsumer consumer) {
		var boundingBox = entity.getBoundingBox();
	}

	@Override
	public void initLightSectionNotifier(Notifier notifier) {
		this.notifier = notifier;
	}

	public class ContraptionContext implements Context {

		@Override
		public ContextShader contextShader() {
			return AllContextShaders.CONTRAPTION;
		}

		@Override
		public void prepare(Material material, Shader shader, TextureSource textureSource) {
//			shader.setVec3("create_oneOverLightBoxSize");
//			shader.setVec3("create_lightVolumeMin");
			shader.setMat4("create_model", contraptionMatrix.last().pose());
		}
	}
}
