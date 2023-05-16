package com.simibubi.create.foundation.mixin.client;

import java.lang.ref.Reference;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.util.TriConsumer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionCollider;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.CapabilityProvider;

@Mixin(Entity.class)
public abstract class EntityContraptionInteractionMixin extends CapabilityProvider<Entity> {
	private EntityContraptionInteractionMixin(Class<Entity> baseClass) {
		super(baseClass);
	}

	@Shadow
	public Level level;

	@Shadow
	private Vec3 position;

	@Shadow
	private float nextStep;

	@Shadow
	@Final
	protected RandomSource random;

	@Shadow
	private EntityDimensions dimensions;

	@Shadow
	protected abstract float nextStep();

	@Shadow
	protected abstract void playStepSound(BlockPos pos, BlockState state);

	@Unique
	private Stream<AbstractContraptionEntity> create$getIntersectionContraptionsStream() {
		return ContraptionHandler.loadedContraptions.get(level)
				.values()
				.stream()
				.map(Reference::get)
				.filter(cEntity -> cEntity != null && cEntity.collidingEntities.containsKey((Entity) (Object) this));
	}

	@Unique
	private Set<AbstractContraptionEntity> create$getIntersectingContraptions() {
		Set<AbstractContraptionEntity> contraptions = create$getIntersectionContraptionsStream().collect(Collectors.toSet());

		contraptions.addAll(level.getEntitiesOfClass(AbstractContraptionEntity.class, ((Entity) (Object) this).getBoundingBox()
			.inflate(1f)));
		return contraptions;
	}

	@Unique
	private void forCollision(Vec3 worldPos, TriConsumer<Contraption, BlockState, BlockPos> action) {
		create$getIntersectingContraptions().forEach(cEntity -> {
			Vec3 localPos = ContraptionCollider.worldToLocalPos(worldPos, cEntity);

			BlockPos blockPos = new BlockPos(localPos);
			Contraption contraption = cEntity.getContraption();
			StructureTemplate.StructureBlockInfo info = contraption.getBlocks()
				.get(blockPos);

			if (info != null) {
				BlockState blockstate = info.state;
				action.accept(contraption, blockstate, blockPos);
			}
		});
	}

	// involves block step sounds on contraptions
	// IFNE line 661 injecting before `!blockstate.isAir(this.world, blockpos)`
	@Inject(method = "move", at = @At(value = "JUMP", opcode = Opcodes.IFNE, ordinal = 7))
	private void create$contraptionStepSounds(MoverType mover, Vec3 movement, CallbackInfo ci) {
		Vec3 worldPos = position.add(0, -0.2, 0);
		MutableBoolean stepped = new MutableBoolean(false);

		forCollision(worldPos, (contraption, state, pos) -> {
			playStepSound(pos, state);
			stepped.setTrue();
		});

		if (stepped.booleanValue())
			nextStep = nextStep();
	}

	// involves client-side view bobbing animation on contraptions
	@Inject(method = "move", at = @At(value = "TAIL"))
	private void create$onMove(MoverType mover, Vec3 movement, CallbackInfo ci) {
		if (!level.isClientSide)
			return;
		Entity self = (Entity) (Object) this;
		if (self.isOnGround())
			return;
		if (self.isPassenger())
			return;

		Vec3 worldPos = position.add(0, -0.2, 0);
		boolean onAtLeastOneContraption = create$getIntersectionContraptionsStream().anyMatch(cEntity -> {
			Vec3 localPos = ContraptionCollider.worldToLocalPos(worldPos, cEntity);

			BlockPos blockPos = new BlockPos(localPos);
			Contraption contraption = cEntity.getContraption();
			StructureTemplate.StructureBlockInfo info = contraption.getBlocks()
				.get(blockPos);

			if (info == null)
				return false;
			
			cEntity.registerColliding(self);
			return true;
		});

		if (!onAtLeastOneContraption)
			return;

		self.setOnGround(true);
		self.getPersistentData()
			.putBoolean("ContraptionGrounded", true);
	}

	@Inject(method = "spawnSprintParticle", at = @At(value = "TAIL"))
	private void create$onSpawnSprintParticle(CallbackInfo ci) {
		Entity self = (Entity) (Object) this;
		Vec3 worldPos = position.add(0, -0.2, 0);
		BlockPos particlePos = new BlockPos(worldPos); // pos where particles are spawned

		forCollision(worldPos, (contraption, state, pos) -> {
			if (!state.addRunningEffects(level, pos, self)
				&& state.getRenderShape() != RenderShape.INVISIBLE) {
				Vec3 speed = self.getDeltaMovement();
				level.addParticle(
					new BlockParticleOption(ParticleTypes.BLOCK, state).setPos(particlePos),
					self.getX() + ((double) random.nextFloat() - 0.5D) * (double) dimensions.width,
					self.getY() + 0.1D,
					self.getZ() + ((double) random.nextFloat() - 0.5D) * (double) dimensions.height,
					speed.x * -4.0D, 1.5D, speed.z * -4.0D
				);
			}
		});
	}
}
