package com.simibubi.create.content.kinetics.drill;

import javax.annotation.Nullable;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import com.simibubi.create.foundation.damageTypes.CreateDamageSources;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DrillMovementBehaviour extends BlockBreakingMovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return super.isActive(context)
			&& !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.getValue(DrillBlock.FACING)
				.getOpposite());
	}

	@Override
	public Vec3 getActiveAreaOffset(MovementContext context) {
		return Vec3.atLowerCornerOf(context.state.getValue(DrillBlock.FACING)
			.getNormal()).scale(.65f);
	}

	@Override
	public boolean disableBlockEntityRendering() {
		return true;
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
        if (!VisualizationManager.supportsVisualization(context.world))
			DrillRenderer.renderInContraption(context, renderWorld, matrices, buffer);
	}

	@Nullable
	@Override
	public ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
		return new DrillActorVisual(visualizationContext, simulationWorld, movementContext);
	}

	@Override
	protected DamageSource getDamageSource(Level level) {
		return CreateDamageSources.drill(level);
	}

	@Override
	public boolean canBreak(Level world, BlockPos breakingPos, BlockState state) {
		return super.canBreak(world, breakingPos, state) && !state.getCollisionShape(world, breakingPos)
			.isEmpty() && !AllTags.AllBlockTags.TRACKS.matches(state);
	}

}
