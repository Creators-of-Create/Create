package com.simibubi.create.foundation.mixin;

import java.lang.ref.Reference;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.TriConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionCollider;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.lib.block.CustomRunningEffectsBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class EntityContraptionInteractionMixin {
	private final Entity self = (Entity) (Object) this;

	private AbstractContraptionEntity contraption;

	@Final
	@Shadow
	protected Random random;

	@Shadow
	private float nextStep;

	@Shadow
	protected abstract float nextStep();

	@Shadow
	protected abstract void playStepSound(BlockPos p_180429_1_, BlockState p_180429_2_);

	private Set<AbstractContraptionEntity> getIntersectingContraptions() {
		Set<AbstractContraptionEntity> contraptions = ContraptionHandler.loadedContraptions.get(self.level)
			.values()
			.stream()
			.map(Reference::get)
			.filter(cEntity -> cEntity != null && cEntity.collidingEntities.containsKey(self))
			.collect(Collectors.toSet());

		contraptions.addAll(self.level.getEntitiesOfClass(AbstractContraptionEntity.class, self.getBoundingBox()
			.inflate(1f)));
		return contraptions;
	}

	private void forCollision(Vec3 anchorPos, TriConsumer<Contraption, BlockState, BlockPos> action) {
		getIntersectingContraptions().forEach(cEntity -> {
			Vec3 localPos = ContraptionCollider.getWorldToLocalTranslation(anchorPos, cEntity);

			localPos = anchorPos.add(localPos);

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

	@Inject(at = @At(value = "JUMP", opcode = 154, // IFNE line 661 injecting before `!blockstate.isAir(this.world, blockpos)`
		ordinal = 5), method = "move")
	private void movementMixin(MoverType mover, Vec3 movement, CallbackInfo ci) {
		Vec3 worldPos = self.position()
			.add(0, -0.2, 0);
		AtomicBoolean stepped = new AtomicBoolean(false);

		forCollision(worldPos, (contraption, blockstate, blockPos) -> {
			bindContraption(contraption);
			playStepSound(blockPos, blockstate);
			unbindContraption();
			stepped.set(true);
		});

		if (stepped.get())
			this.nextStep = this.nextStep();
	}

	@Inject(method = { "spawnSprintParticle" }, at = @At(value = "TAIL"))
	private void createRunningParticlesMixin(CallbackInfo ci) {
		Vec3 worldPos = self.position()
			.add(0, -0.2, 0);
		BlockPos pos = new BlockPos(worldPos); // pos where particles are spawned

		forCollision(worldPos, (contraption, blockstate, blockpos) -> {
			boolean particles = blockstate.getRenderShape() != RenderShape.INVISIBLE;
			if (blockstate.getBlock() instanceof CustomRunningEffectsBlock custom &&
					custom.addRunningEffects(blockstate, self.level, blockpos, self)) {
				particles = false;
			}

			if (particles) {
				Vec3 vec3d = self.getDeltaMovement();
				self.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate),
					self.getX() + ((double) random.nextFloat() - 0.5D) * (double) self.getBbWidth(), self.getY() + 0.1D,
					self.getZ() + ((double) random.nextFloat() - 0.5D) * (double) self.getBbWidth(), vec3d.x * -4.0D, 1.5D,
					vec3d.z * -4.0D);
			}
		});
	}

	@Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
	private void playSoundShifted(SoundEvent event, float pitch, float volume, CallbackInfo ci) {
		if (this.contraption != null && (!self.isSilent() || self instanceof Player)) {
			double x = self.getX();
			double y = self.getY();
			double z = self.getZ();
			Vec3 worldPos = ContraptionCollider.getWorldToLocalTranslation(new Vec3(x, y, z), this.contraption);

			worldPos = worldPos.add(x, y, z);

			self.level.playSound(null, worldPos.x + x, worldPos.y + y, worldPos.z + z, event, self.getSoundSource(),
				pitch, volume);

			ci.cancel();
		}
	}

	private void bindContraption(Contraption contraption) {
		bindContraption(contraption.entity);
	}

	private void bindContraption(AbstractContraptionEntity contraption) {
		this.contraption = contraption;
	}

	private void unbindContraption() {
		this.contraption = null;
	}
}
