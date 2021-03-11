package com.simibubi.create.foundation.ponder.instructions;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.simibubi.create.content.contraptions.components.structureMovement.bearing.IBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyTileEntity;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class AnimateTileEntityInstruction extends TickingInstruction {

	protected double deltaPerTick;
	protected double totalDelta;
	protected double target;
	protected final BlockPos location;

	private BiConsumer<PonderWorld, Float> setter;
	private Function<PonderWorld, Float> getter;

	public static AnimateTileEntityInstruction bearing(BlockPos location, float totalDelta, int ticks) {
		return new AnimateTileEntityInstruction(location, totalDelta, ticks,
			(w, f) -> castIfPresent(w, location, IBearingTileEntity.class).ifPresent(bte -> bte.setAngle(f)),
			(w) -> castIfPresent(w, location, IBearingTileEntity.class).map(bte -> bte.getInterpolatedAngle(0))
				.orElse(0f));
	}

	public static AnimateTileEntityInstruction pulley(BlockPos location, float totalDelta, int ticks) {
		return new AnimateTileEntityInstruction(location, totalDelta, ticks,
			(w, f) -> castIfPresent(w, location, PulleyTileEntity.class).ifPresent(pulley -> pulley.animateOffset(f)),
			(w) -> castIfPresent(w, location, PulleyTileEntity.class).map(pulley -> pulley.offset)
				.orElse(0f));
	}

	protected AnimateTileEntityInstruction(BlockPos location, float totalDelta, int ticks,
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
		setter.accept(world, (float) (remainingTicks == 0 ? target : getter.apply(world) + deltaPerTick));
	}

	private static <T> Optional<T> castIfPresent(PonderWorld world, BlockPos pos, Class<T> teType) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (teType.isInstance(tileEntity))
			return Optional.of(teType.cast(tileEntity));
		return Optional.empty();
	}

}
