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

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.common.capabilities.CapabilityProvider;

@Mixin(Entity.class)
public abstract class EntityContraptionInteractionMixin extends CapabilityProvider<Entity> {
	private EntityContraptionInteractionMixin(Class<Entity> baseClass) {
		super(baseClass);
	}

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

	private void forCollision(Vector3d anchorPos, TriConsumer<Contraption, BlockState, BlockPos> action) {
		getIntersectingContraptions().forEach(cEntity -> {
			Vector3d localPos = ContraptionCollider.getWorldToLocalTranslation(anchorPos, cEntity);

			localPos = anchorPos.add(localPos);

			BlockPos blockPos = new BlockPos(localPos);
			Contraption contraption = cEntity.getContraption();
			Template.BlockInfo info = contraption.getBlocks()
				.get(blockPos);

			if (info != null) {
				BlockState blockstate = info.state;
				action.accept(contraption, blockstate, blockPos);
			}
		});
	}

	@Inject(at = @At(value = "JUMP", opcode = 154, // IFNE line 587 injecting before `!blockstate.isAir(this.world, blockpos)`
		ordinal = 4), method = "move")
	private void movementMixin(MoverType mover, Vector3d movement, CallbackInfo ci) {
		Vector3d worldPos = self.position()
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
		Vector3d worldPos = self.position()
			.add(0, -0.2, 0);
		BlockPos pos = new BlockPos(worldPos); // pos where particles are spawned

		forCollision(worldPos, (contraption, blockstate, blockpos) -> {
			if (!blockstate.addRunningEffects(self.level, blockpos, self)
				&& blockstate.getRenderShape() != BlockRenderType.INVISIBLE) {
				Vector3d vec3d = self.getDeltaMovement();
				self.level.addParticle(new BlockParticleData(ParticleTypes.BLOCK, blockstate).setPos(pos),
					self.getX() + ((double) random.nextFloat() - 0.5D) * (double) self.getBbWidth(), self.getY() + 0.1D,
					self.getZ() + ((double) random.nextFloat() - 0.5D) * (double) self.getBbWidth(), vec3d.x * -4.0D, 1.5D,
					vec3d.z * -4.0D);
			}
		});
	}

	@Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
	private void playSoundShifted(SoundEvent event, float pitch, float volume, CallbackInfo ci) {
		if (this.contraption != null && (!self.isSilent() || self instanceof PlayerEntity)) {
			double x = self.getX();
			double y = self.getY();
			double z = self.getZ();
			Vector3d worldPos = ContraptionCollider.getWorldToLocalTranslation(new Vector3d(x, y, z), this.contraption);

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
