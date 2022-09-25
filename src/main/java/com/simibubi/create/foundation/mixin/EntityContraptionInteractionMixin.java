package com.simibubi.create.foundation.mixin;

import java.lang.ref.Reference;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		Set<AbstractContraptionEntity> contraptions = getIntersectionContraptionsStream().collect(Collectors.toSet());

		contraptions.addAll(self.level.getEntitiesOfClass(AbstractContraptionEntity.class, self.getBoundingBox()
			.inflate(1f)));
		return contraptions;
	}

	private Stream<AbstractContraptionEntity> getIntersectionContraptionsStream() {
		return ContraptionHandler.loadedContraptions.get(self.level)
				.values()
				.stream()
				.map(Reference::get)
				.filter(cEntity -> cEntity != null && cEntity.collidingEntities.containsKey(self));
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
		ordinal = 7), method = "move")
	private void movementStepMixin(MoverType mover, Vec3 movement, CallbackInfo ci) { // involves block step sounds on contraptions
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

	@Inject(at = @At(value = "TAIL"), method = "move")
	private void movementMixin(MoverType mover, Vec3 movement, CallbackInfo ci) {
		// involves client-side view bobbing animation on contraptions
		if (!self.level.isClientSide)
			return;
		if (self.isOnGround())
			return;
		if (self.isPassenger())
			return;

		Vec3 worldPos = self.position()
			.add(0, -0.2, 0);
		boolean onAtLeastOneContraption = getIntersectionContraptionsStream().anyMatch(cEntity -> {
			Vec3 localPos = ContraptionCollider.getWorldToLocalTranslation(worldPos, cEntity);

			localPos = worldPos.add(localPos);

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

	@Inject(method = { "spawnSprintParticle" }, at = @At(value = "TAIL"))
	private void createRunningParticlesMixin(CallbackInfo ci) {
		Vec3 worldPos = self.position()
			.add(0, -0.2, 0);
		BlockPos pos = new BlockPos(worldPos); // pos where particles are spawned

		forCollision(worldPos, (contraption, blockstate, blockpos) -> {
			if (!blockstate.addRunningEffects(self.level, blockpos, self)
				&& blockstate.getRenderShape() != RenderShape.INVISIBLE) {
				Vec3 vec3d = self.getDeltaMovement();
				self.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(pos),
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
