package com.simibubi.create.foundation.ponder.instruction;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.simibubi.create.content.contraptions.components.deployer.DeployerBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.IBearingBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlockEntity;
import com.simibubi.create.content.logistics.trains.track.StandardBogeyBlockEntity;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AnimateBlockEntityInstruction extends TickingInstruction {

	protected double deltaPerTick;
	protected double totalDelta;
	protected double target;
	protected final BlockPos location;

	private BiConsumer<PonderWorld, Float> setter;
	private Function<PonderWorld, Float> getter;

	public static AnimateBlockEntityInstruction bearing(BlockPos location, float totalDelta, int ticks) {
		return new AnimateBlockEntityInstruction(location, totalDelta, ticks,
			(w, f) -> castIfPresent(w, location, IBearingBlockEntity.class).ifPresent(bte -> bte.setAngle(f)),
			(w) -> castIfPresent(w, location, IBearingBlockEntity.class).map(bte -> bte.getInterpolatedAngle(0))
				.orElse(0f));
	}

	public static AnimateBlockEntityInstruction bogey(BlockPos location, float totalDelta, int ticks) {
		float movedPerTick = totalDelta / ticks;
		return new AnimateBlockEntityInstruction(location, totalDelta, ticks,
			(w, f) -> castIfPresent(w, location, StandardBogeyBlockEntity.class)
				.ifPresent(bte -> bte.animate(f.equals(totalDelta) ? 0 : movedPerTick)),
			(w) -> 0f);
	}

	public static AnimateBlockEntityInstruction pulley(BlockPos location, float totalDelta, int ticks) {
		return new AnimateBlockEntityInstruction(location, totalDelta, ticks,
			(w, f) -> castIfPresent(w, location, PulleyBlockEntity.class).ifPresent(pulley -> pulley.animateOffset(f)),
			(w) -> castIfPresent(w, location, PulleyBlockEntity.class).map(pulley -> pulley.offset)
				.orElse(0f));
	}

	public static AnimateBlockEntityInstruction deployer(BlockPos location, float totalDelta, int ticks) {
		return new AnimateBlockEntityInstruction(location, totalDelta, ticks,
			(w, f) -> castIfPresent(w, location, DeployerBlockEntity.class)
				.ifPresent(deployer -> deployer.setAnimatedOffset(f)),
			(w) -> castIfPresent(w, location, DeployerBlockEntity.class).map(deployer -> deployer.getHandOffset(1))
				.orElse(0f));
	}

	protected AnimateBlockEntityInstruction(BlockPos location, float totalDelta, int ticks,
		BiConsumer<PonderWorld, Float> setter, Function<PonderWorld, Float> getter) {
		super(false, ticks);
		this.location = location;
		this.setter = setter;
		this.getter = getter;
		this.deltaPerTick = totalDelta * (1d / ticks);
		this.totalDelta = totalDelta;
		this.target = totalDelta;
	}

	@Override
	protected final void firstTick(PonderScene scene) {
		super.firstTick(scene);
		target = getter.apply(scene.getWorld()) + totalDelta;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		PonderWorld world = scene.getWorld();
		float current = getter.apply(world);
		float next = (float) (remainingTicks == 0 ? target : current + deltaPerTick);
		setter.accept(world, next);
		if (remainingTicks == 0) // lock interpolation
			setter.accept(world, next);
	}

	private static <T> Optional<T> castIfPresent(PonderWorld world, BlockPos pos, Class<T> beType) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (beType.isInstance(blockEntity))
			return Optional.of(beType.cast(blockEntity));
		return Optional.empty();
	}

}
