package com.simibubi.create.content.contraptions;

import static java.lang.Math.abs;
import static net.minecraft.world.entity.Entity.collideBoundingBox;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.simibubi.create.content.contraptions.render.RotateLocalPlayer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

import net.minecraft.world.phys.Vec2;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.base.Predicates;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity.ContraptionRotationState;
import com.simibubi.create.content.contraptions.ContraptionColliderLockPacket.ContraptionColliderLockPacketRequest;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.sync.ClientMotionPacket;
import com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.collision.ContinuousOBBCollider.ContinuousSeparationManifold;
import com.simibubi.create.foundation.collision.Matrix3d;
import com.simibubi.create.foundation.collision.OrientedBB;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ContraptionCollider {

	enum PlayerType {
		NONE, CLIENT, REMOTE, SERVER
	}

	private static MutablePair<WeakReference<AbstractContraptionEntity>, Double> safetyLock = new MutablePair<>();
	private static Map<AbstractContraptionEntity, Map<Player, Double>> remoteSafetyLocks = new WeakHashMap<>();

	static void collideEntities(AbstractContraptionEntity contraptionEntity) {
		Level world = contraptionEntity.getCommandSenderWorld();
		Contraption contraption = contraptionEntity.getContraption();
		AABB bounds = contraptionEntity.getBoundingBox();

		if (contraption == null)
			return;
		if (bounds == null)
			return;

		Vec3 contraptionPosition = contraptionEntity.position();
		Vec3 contraptionMotion = contraptionPosition.subtract(contraptionEntity.getPrevPositionVec());
		var old = contraption.entity.getRotationVec(contraption.entity.getPrevRotationState());
		var current = contraption.entity.getRotationVec(contraption.entity.getRotationState());
		Vec3 contraptionRotation = old.subtract(current);
//		if (abs(old.x - current.x) >= 180)
//			contraptionRotation.add(new Vec3(360, 0, 0));
//		if (abs(old.y - current.y) >= 180)
//			contraptionRotation.add(new Vec3(0, 360, 0));
		Vec3 anchorVec = contraptionEntity.getAnchorVec();
		ContraptionRotationState rotation = null;

		if (world.isClientSide() && safetyLock.left != null && safetyLock.left.get() == contraptionEntity)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> saveClientPlayerFromClipping(contraptionEntity, contraptionMotion));

		// After death, multiple refs to the client player may show up in the area
		boolean skipClientPlayer = false;

		List<Entity> entitiesWithinAABB = world.getEntitiesOfClass(Entity.class, bounds.inflate(2)
			.expandTowards(0, 32, 0), contraptionEntity::canCollideWith);
		for (Entity entity : entitiesWithinAABB) {
			if (!entity.isAlive())
				continue;

			PlayerType playerType = getPlayerType(entity);
			if (playerType == PlayerType.REMOTE) {
				if (!(contraption instanceof TranslatingContraption))
					continue;
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
					() -> () -> saveRemotePlayerFromClipping((Player) entity, contraptionEntity, contraptionMotion));
				continue;
			}

			entity.getSelfAndPassengers()
				.forEach(e -> {
					if (e instanceof ServerPlayer)
						((ServerPlayer) e).connection.aboveGroundTickCount = 0;
				});

			if (playerType == PlayerType.SERVER)
				continue;

			if (playerType == PlayerType.CLIENT) {
				if (skipClientPlayer)
					continue;
				else
					skipClientPlayer = true;
			}

			// Init matrix
			if (rotation == null)
				rotation = contraptionEntity.getRotationState();
			Matrix3d rotationMatrix = rotation.asMatrix();

			// Transform entity position and motion to local space
			Vec3 entityPosition = entity.position();
			AABB entityBounds = entity.getBoundingBox();
			Vec3 motion = entity.getDeltaMovement();
			float yawOffset = rotation.getYawOffset();
			Vec3 position = getWorldToLocalTranslation(entity, anchorVec, rotationMatrix, yawOffset);

			motion = motion.subtract(contraptionMotion);
			motion = rotationMatrix.transform(motion);

			// Prepare entity bounds
			AABB localBB = entityBounds.move(position)
				.inflate(1.0E-7D);

			OrientedBB obb = new OrientedBB(localBB);
			obb.setRotation(rotationMatrix);

			// Use simplified bbs when present
			final Vec3 motionCopy = motion;
			List<AABB> collidableBBs = contraption.getSimplifiedEntityColliders()
				.orElseGet(() -> {

					// Else find 'nearby' individual block shapes to collide with
					List<AABB> bbs = new ArrayList<>();
					List<VoxelShape> potentialHits =
						getPotentiallyCollidedShapes(world, contraption, localBB.expandTowards(motionCopy));
					potentialHits.forEach(shape -> shape.toAabbs()
						.forEach(bbs::add));
					return bbs;

				});

			MutableObject<Vec3> collisionResponse = new MutableObject<>(Vec3.ZERO);
			MutableObject<Vec3> normal = new MutableObject<>(Vec3.ZERO);
			MutableObject<Vec3> location = new MutableObject<>(Vec3.ZERO);
			MutableBoolean surfaceCollision = new MutableBoolean(false);
			MutableFloat temporalResponse = new MutableFloat(1);
			Vec3 obbCenter = obb.getCenter();

			// Apply separation maths
			boolean doHorizontalPass = !rotation.hasVerticalRotation();
			for (boolean horizontalPass : Iterate.trueAndFalse) {
				boolean verticalPass = !horizontalPass || !doHorizontalPass;

				for (AABB bb : collidableBBs) {
					Vec3 currentResponse = collisionResponse.getValue();
					Vec3 currentCenter = obbCenter.add(currentResponse);

					if (abs(currentCenter.x - bb.getCenter().x) - entityBounds.getXsize() - 1 > bb.getXsize() / 2)
						continue;
					if (abs((currentCenter.y + motion.y) - bb.getCenter().y) - entityBounds.getYsize()
						- 1 > bb.getYsize() / 2)
						continue;
					if (abs(currentCenter.z - bb.getCenter().z) - entityBounds.getZsize() - 1 > bb.getZsize() / 2)
						continue;

					obb.setCenter(currentCenter);
					ContinuousSeparationManifold intersect = obb.intersect(bb, motion);

					if (intersect == null)
						continue;
					if (verticalPass && surfaceCollision.isFalse())
						surfaceCollision.setValue(intersect.isSurfaceCollision());

					double timeOfImpact = intersect.getTimeOfImpact();
					boolean isTemporal = timeOfImpact > 0 && timeOfImpact < 1;
					Vec3 collidingNormal = intersect.getCollisionNormal();
					Vec3 collisionPosition = intersect.getCollisionPosition();

					if (!isTemporal) {
						Vec3 separation = intersect.asSeparationVec(entity.getStepHeight());
						if (separation != null && !separation.equals(Vec3.ZERO)) {
							collisionResponse.setValue(currentResponse.add(separation));
							timeOfImpact = 0;
						}
					}

					boolean nearest = timeOfImpact >= 0 && temporalResponse.getValue() > timeOfImpact;
					if (collidingNormal != null && nearest)
						normal.setValue(collidingNormal);
					if (collisionPosition != null && nearest)
						location.setValue(collisionPosition);

					if (isTemporal) {
						if (temporalResponse.getValue() > timeOfImpact)
							temporalResponse.setValue(timeOfImpact);
					}
				}

				if (verticalPass)
					break;

				boolean noVerticalMotionResponse = temporalResponse.getValue() == 1;
				boolean noVerticalCollision = collisionResponse.getValue().y == 0;
				if (noVerticalCollision && noVerticalMotionResponse)
					break;

				// Re-run collisions with horizontal offset
				collisionResponse.setValue(collisionResponse.getValue()
					.multiply(129 / 128f, 0, 129 / 128f));
				continue;
			}

			// Resolve collision
			Vec3 entityMotion = entity.getDeltaMovement();
			Vec3 entityMotionNoTemporal = entityMotion;
			Vec3 collisionNormal = normal.getValue();
			Vec3 collisionLocation = location.getValue();
			Vec3 totalResponse = collisionResponse.getValue();
			boolean hardCollision = !totalResponse.equals(Vec3.ZERO);
			boolean temporalCollision = temporalResponse.getValue() != 1;
			Vec3 motionResponse = !temporalCollision ? motion
				: motion.normalize()
					.scale(motion.length() * temporalResponse.getValue());

			rotationMatrix.transpose();
			motionResponse = rotationMatrix.transform(motionResponse)
				.add(contraptionMotion);
			totalResponse = rotationMatrix.transform(totalResponse);
			totalResponse = VecHelper.rotate(totalResponse, yawOffset, Axis.Y);
			collisionNormal = rotationMatrix.transform(collisionNormal);
			collisionNormal = VecHelper.rotate(collisionNormal, yawOffset, Axis.Y);
			collisionNormal = collisionNormal.normalize();
			collisionLocation = rotationMatrix.transform(collisionLocation);
			collisionLocation = VecHelper.rotate(collisionLocation, yawOffset, Axis.Y);
			rotationMatrix.transpose();

			double bounce = 0;
			double slide = 0;

			if (!collisionLocation.equals(Vec3.ZERO)) {
				collisionLocation = collisionLocation.add(entity.position()
					.add(entity.getBoundingBox()
						.getCenter())
					.scale(.5f));
				if (temporalCollision)
					collisionLocation = collisionLocation.add(0, motionResponse.y, 0);

				BlockPos pos = new BlockPos(contraptionEntity.toLocalVector(entity.position(), 0));
				if (contraption.getBlocks()
					.containsKey(pos)) {
					BlockState blockState = contraption.getBlocks()
						.get(pos).state;
					if (blockState.is(BlockTags.CLIMBABLE)) {
						surfaceCollision.setTrue();
						totalResponse = totalResponse.add(0, .1f, 0);
					}
				}

				pos = new BlockPos(contraptionEntity.toLocalVector(collisionLocation, 0));
				if (contraption.getBlocks()
					.containsKey(pos)) {
					BlockState blockState = contraption.getBlocks()
						.get(pos).state;

					MovingInteractionBehaviour movingInteractionBehaviour = contraption.interactors.get(pos);
					if (movingInteractionBehaviour != null)
						movingInteractionBehaviour.handleEntityCollision(entity, pos, contraptionEntity);

					bounce = BlockHelper.getBounceMultiplier(blockState.getBlock());
					slide = Math.max(0, blockState.getFriction(contraption.world, pos, entity) - .6f);
				}
			}

			boolean hasNormal = !collisionNormal.equals(Vec3.ZERO);
			boolean anyCollision = hardCollision || temporalCollision;

			if (bounce > 0 && hasNormal && anyCollision
				&& bounceEntity(entity, collisionNormal, contraptionEntity, bounce)) {
				entity.level.playSound(playerType == PlayerType.CLIENT ? (Player) entity : null, entity.getX(),
					entity.getY(), entity.getZ(), SoundEvents.SLIME_BLOCK_FALL, SoundSource.BLOCKS, .5f, 1);
				continue;
			}

			if (temporalCollision) {
				double idealVerticalMotion = motionResponse.y;
				if (idealVerticalMotion != entityMotion.y) {
					entity.setDeltaMovement(entityMotion.multiply(1, 0, 1)
						.add(0, idealVerticalMotion, 0));
					entityMotion = entity.getDeltaMovement();
				}
			}

			if (hardCollision) {
				double motionX = entityMotion.x();
				double motionY = entityMotion.y();
				double motionZ = entityMotion.z();
				double intersectX = totalResponse.x();
				double intersectY = totalResponse.y();
				double intersectZ = totalResponse.z();

				double horizonalEpsilon = 1 / 128f;
				if (motionX != 0 && abs(intersectX) > horizonalEpsilon && motionX > 0 == intersectX < 0)
					entityMotion = entityMotion.multiply(0, 1, 1);
				if (motionY != 0 && intersectY != 0 && motionY > 0 == intersectY < 0)
					entityMotion = entityMotion.multiply(1, 0, 1)
						.add(0, contraptionMotion.y, 0);
				if (motionZ != 0 && abs(intersectZ) > horizonalEpsilon && motionZ > 0 == intersectZ < 0)
					entityMotion = entityMotion.multiply(1, 1, 0);

			}

			if (bounce == 0 && slide > 0 && hasNormal && anyCollision && rotation.hasVerticalRotation()) {
				double slideFactor = collisionNormal.multiply(1, 0, 1)
					.length() * 1.25f;
				Vec3 motionIn = entityMotionNoTemporal.multiply(0, .9, 0)
					.add(0, -.01f, 0);
				Vec3 slideNormal = collisionNormal.cross(motionIn.cross(collisionNormal))
					.normalize();
				Vec3 newMotion = entityMotion.multiply(.85, 0, .85)
					.add(slideNormal.scale((.2f + slide) * motionIn.length() * slideFactor)
						.add(0, -.1f - collisionNormal.y * .125f, 0));
				entity.setDeltaMovement(newMotion);
				entityMotion = entity.getDeltaMovement();
			}

			if (!hardCollision && surfaceCollision.isFalse())
				continue;

			Vec3 allowedMovement = collide(totalResponse, entity);
//			if (!world.isClientSide())
			entity.setPos(entityPosition.x + allowedMovement.x, entityPosition.y + allowedMovement.y,
					entityPosition.z + allowedMovement.z);
			entityPosition = entity.position();
			entityMotion =
				handleDamageFromTrain(world, contraptionEntity, contraptionMotion, entity, entityMotion, playerType);

			RotateLocalPlayer.deltaXROT += (float) contraptionRotation.x;
			RotateLocalPlayer.deltaYROT += (float) contraptionRotation.y;

			entity.hurtMarked = true;
			Vec3 contactPointMotion = Vec3.ZERO;

			if (surfaceCollision.isTrue()) {
				contraptionEntity.registerColliding(entity);
				entity.fallDistance = 0;
				for (Entity rider : entity.getIndirectPassengers())
					if (getPlayerType(rider) == PlayerType.CLIENT)
						AllPackets.getChannel()
							.sendToServer(new ClientMotionPacket(rider.getDeltaMovement(), true, 0));
				boolean canWalk = bounce != 0 || slide == 0;
				if (canWalk || !rotation.hasVerticalRotation()) {
					if (canWalk)
						entity.setOnGround(true);
					if (entity instanceof ItemEntity)
						entityMotion = entityMotion.multiply(.5f, 1, .5f);
				}
				contactPointMotion = contraptionEntity.getContactPointMotion(entityPosition);
				allowedMovement = collide(contactPointMotion, entity);
				entity.setPos(entityPosition.x + allowedMovement.x, entityPosition.y,
					entityPosition.z + allowedMovement.z);
			}

			entity.setDeltaMovement(entityMotion);

			if (playerType != PlayerType.CLIENT)
				continue;

			double d0 = entity.getX() - entity.xo - contactPointMotion.x;
			double d1 = entity.getZ() - entity.zo - contactPointMotion.z;
			float limbSwing = Mth.sqrt((float) (d0 * d0 + d1 * d1)) * 4.0F;
			if (limbSwing > 1.0F)
				limbSwing = 1.0F;
//			AllPackets.getChannel()
//				.sendToServer(new ClientMotionPacket(entityMotion, true, limbSwing));

			if (entity.isOnGround() && contraption instanceof TranslatingContraption) {
				safetyLock.setLeft(new WeakReference<>(contraptionEntity));
				safetyLock.setRight(entity.getY() - contraptionEntity.getY());
			}
		}

	}

	private static int packetCooldown = 0;

	@OnlyIn(Dist.CLIENT)
	private static void saveClientPlayerFromClipping(AbstractContraptionEntity contraptionEntity,
		Vec3 contraptionMotion) {
		LocalPlayer entity = Minecraft.getInstance().player;
		if (entity.isPassenger())
			return;

		double prevDiff = safetyLock.right;
		double currentDiff = entity.getY() - contraptionEntity.getY();
		double motion = contraptionMotion.subtract(entity.getDeltaMovement()).y;
		double trend = Math.signum(currentDiff - prevDiff);

		ClientPacketListener handler = entity.connection;
		if (handler.getOnlinePlayers()
			.size() > 1) {
			if (packetCooldown > 0)
				packetCooldown--;
			if (packetCooldown == 0) {
				AllPackets.getChannel()
					.sendToServer(new ContraptionColliderLockPacketRequest(contraptionEntity.getId(), currentDiff));
				packetCooldown = 3;
			}
		}

		if (trend == 0)
			return;
		if (trend == Math.signum(motion))
			return;

		double speed = contraptionMotion.multiply(0, 1, 0)
			.lengthSqr();
		if (trend > 0 && speed < 0.1)
			return;
		if (speed < 0.05)
			return;

		if (!savePlayerFromClipping(entity, contraptionEntity, contraptionMotion, prevDiff))
			safetyLock.setLeft(null);
	}

	@OnlyIn(Dist.CLIENT)
	public static void lockPacketReceived(int contraptionId, int remotePlayerId, double suggestedOffset) {
		ClientLevel level = Minecraft.getInstance().level;
		if (!(level.getEntity(contraptionId) instanceof ControlledContraptionEntity contraptionEntity))
			return;
		if (!(level.getEntity(remotePlayerId) instanceof RemotePlayer player))
			return;
		remoteSafetyLocks.computeIfAbsent(contraptionEntity, $ -> new WeakHashMap<>())
			.put(player, suggestedOffset);
	}

	@OnlyIn(Dist.CLIENT)
	private static void saveRemotePlayerFromClipping(Player entity, AbstractContraptionEntity contraptionEntity,
		Vec3 contraptionMotion) {
		if (entity.isPassenger())
			return;

		Map<Player, Double> locksOnThisContraption =
			remoteSafetyLocks.getOrDefault(contraptionEntity, Collections.emptyMap());
		double prevDiff = locksOnThisContraption.getOrDefault(entity, entity.getY() - contraptionEntity.getY());
		if (!savePlayerFromClipping(entity, contraptionEntity, contraptionMotion, prevDiff))
			if (locksOnThisContraption.containsKey(entity))
				locksOnThisContraption.remove(entity);
	}

	@OnlyIn(Dist.CLIENT)
	private static boolean savePlayerFromClipping(Player entity, AbstractContraptionEntity contraptionEntity,
		Vec3 contraptionMotion, double yStartOffset) {
		AABB bb = entity.getBoundingBox()
			.deflate(1 / 4f, 0, 1 / 4f);
		double shortestDistance = Double.MAX_VALUE;
		double yStart = entity.getStepHeight() + contraptionEntity.getY() + yStartOffset;
		double rayLength = Math.max(5, abs(entity.getY() - yStart));

		for (int rayIndex = 0; rayIndex < 4; rayIndex++) {
			Vec3 start = new Vec3(rayIndex / 2 == 0 ? bb.minX : bb.maxX, yStart, rayIndex % 2 == 0 ? bb.minZ : bb.maxZ);
			Vec3 end = start.add(0, -rayLength, 0);

			BlockHitResult hitResult = ContraptionHandlerClient.rayTraceContraption(start, end, contraptionEntity);
			if (hitResult == null)
				continue;

			Vec3 hit = contraptionEntity.toGlobalVector(hitResult.getLocation(), 1);
			double hitDiff = start.y - hit.y;
			if (shortestDistance > hitDiff)
				shortestDistance = hitDiff;
		}

		if (shortestDistance > rayLength)
			return false;
		entity.setPos(entity.getX(), yStart - shortestDistance, entity.getZ());
		return true;
	}

	private static Vec3 handleDamageFromTrain(Level world, AbstractContraptionEntity contraptionEntity,
		Vec3 contraptionMotion, Entity entity, Vec3 entityMotion, PlayerType playerType) {

		if (!(contraptionEntity instanceof CarriageContraptionEntity cce))
			return entityMotion;
		if (!entity.isOnGround())
			return entityMotion;

		CompoundTag persistentData = entity.getPersistentData();
		if (persistentData.contains("ContraptionGrounded")) {
			persistentData.remove("ContraptionGrounded");
			return entityMotion;
		}

		if (cce.collidingEntities.containsKey(entity))
			return entityMotion;
		if (entity instanceof ItemEntity)
			return entityMotion;
		if (cce.nonDamageTicks != 0)
			return entityMotion;
		if (!AllConfigs.server().trains.trainsCauseDamage.get())
			return entityMotion;

		Vec3 diffMotion = contraptionMotion.subtract(entity.getDeltaMovement());

		if (diffMotion.length() <= 0.35f || contraptionMotion.length() <= 0.35f)
			return entityMotion;

		EntityDamageSource pSource = new EntityDamageSource("create.run_over", contraptionEntity);
		double damage = diffMotion.length();
		if (entity.getClassification(false) == MobCategory.MONSTER)
			damage *= 2;

		if (entity instanceof Player p && (p.isCreative() || p.isSpectator()))
			return entityMotion;

		if (playerType == PlayerType.CLIENT) {
			AllPackets.getChannel()
				.sendToServer(new TrainCollisionPacket((int) (damage * 16), contraptionEntity.getId()));
			world.playSound((Player) entity, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
				SoundSource.NEUTRAL, 1, .75f);
		} else {
			entity.hurt(pSource, (int) (damage * 16));
			world.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL, 1, .75f);
			if (!entity.isAlive())
				contraptionEntity.getControllingPlayer()
					.map(world::getPlayerByUUID)
					.ifPresent(AllAdvancements.TRAIN_ROADKILL::awardTo);
		}

		Vec3 added = entityMotion.add(contraptionMotion.multiply(1, 0, 1)
			.normalize()
			.add(0, .25, 0)
			.scale(damage * 4))
			.add(diffMotion);

		return VecHelper.clamp(added, 3);
	}

	static boolean bounceEntity(Entity entity, Vec3 normal, AbstractContraptionEntity contraption, double factor) {
		if (factor == 0)
			return false;
		if (entity.isSuppressingBounce())
			return false;

		Vec3 contactPointMotion = contraption.getContactPointMotion(entity.position());
		Vec3 motion = entity.getDeltaMovement()
			.subtract(contactPointMotion);
		Vec3 deltav = normal.scale(factor * 2 * motion.dot(normal));
		if (deltav.dot(deltav) < 0.1f)
			return false;
		entity.setDeltaMovement(entity.getDeltaMovement()
			.subtract(deltav));
		return true;
	}

	public static Vec3 getWorldToLocalTranslation(Entity entity, Vec3 anchorVec, Matrix3d rotationMatrix,
		float yawOffset) {
		Vec3 entityPosition = entity.position();
		Vec3 centerY = new Vec3(0, entity.getBoundingBox()
			.getYsize() / 2, 0);
		Vec3 position = entityPosition;
		position = position.add(centerY);
		position = worldToLocalPos(position, anchorVec, rotationMatrix, yawOffset);
		position = position.subtract(centerY);
		position = position.subtract(entityPosition);
		return position;
	}

	public static Vec3 worldToLocalPos(Vec3 worldPos, AbstractContraptionEntity contraptionEntity) {
		return worldToLocalPos(worldPos, contraptionEntity.getAnchorVec(),
			contraptionEntity.getRotationState());
	}

	public static Vec3 worldToLocalPos(Vec3 worldPos, Vec3 anchorVec, ContraptionRotationState rotation) {
		return worldToLocalPos(worldPos, anchorVec, rotation.asMatrix(), rotation.getYawOffset());
	}

	public static Vec3 worldToLocalPos(Vec3 worldPos, Vec3 anchorVec, Matrix3d rotationMatrix, float yawOffset) {
		Vec3 localPos = worldPos;
		localPos = localPos.subtract(anchorVec);
		localPos = localPos.subtract(VecHelper.CENTER_OF_ORIGIN);
		localPos = VecHelper.rotate(localPos, -yawOffset, Axis.Y);
		localPos = rotationMatrix.transform(localPos);
		localPos = localPos.add(VecHelper.CENTER_OF_ORIGIN);
		return localPos;
	}

	/** From Entity#collide **/
	static Vec3 collide(Vec3 p_20273_, Entity e) {
		AABB aabb = e.getBoundingBox();
		List<VoxelShape> list = e.level.getEntityCollisions(e, aabb.expandTowards(p_20273_));
		Vec3 vec3 = p_20273_.lengthSqr() == 0.0D ? p_20273_ : collideBoundingBox(e, p_20273_, aabb, e.level, list);
		boolean flag = p_20273_.x != vec3.x;
		boolean flag1 = p_20273_.y != vec3.y;
		boolean flag2 = p_20273_.z != vec3.z;
		boolean flag3 = flag1 && p_20273_.y < 0.0D;
		if (e.getStepHeight() > 0.0F && flag3 && (flag || flag2)) {
			Vec3 vec31 = collideBoundingBox(e, new Vec3(p_20273_.x, (double) e.getStepHeight(), p_20273_.z), aabb,
				e.level, list);
			Vec3 vec32 = collideBoundingBox(e, new Vec3(0.0D, (double) e.getStepHeight(), 0.0D),
				aabb.expandTowards(p_20273_.x, 0.0D, p_20273_.z), e.level, list);
			if (vec32.y < (double) e.getStepHeight()) {
				Vec3 vec33 =
					collideBoundingBox(e, new Vec3(p_20273_.x, 0.0D, p_20273_.z), aabb.move(vec32), e.level, list)
						.add(vec32);
				if (vec33.horizontalDistanceSqr() > vec31.horizontalDistanceSqr()) {
					vec31 = vec33;
				}
			}

			if (vec31.horizontalDistanceSqr() > vec3.horizontalDistanceSqr()) {
				return vec31.add(collideBoundingBox(e, new Vec3(0.0D, -vec31.y + p_20273_.y, 0.0D), aabb.move(vec31),
					e.level, list));
			}
		}

		return vec3;
	}

	private static PlayerType getPlayerType(Entity entity) {
		if (!(entity instanceof Player))
			return PlayerType.NONE;
		if (!entity.level.isClientSide)
			return PlayerType.SERVER;
		MutableBoolean isClient = new MutableBoolean(false);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> isClient.setValue(isClientPlayerEntity(entity)));
		return isClient.booleanValue() ? PlayerType.CLIENT : PlayerType.REMOTE;
	}

	@OnlyIn(Dist.CLIENT)
	private static boolean isClientPlayerEntity(Entity entity) {
		return entity instanceof LocalPlayer;
	}

	private static List<VoxelShape> getPotentiallyCollidedShapes(Level world, Contraption contraption, AABB localBB) {

		double height = localBB.getYsize();
		double width = localBB.getXsize();
		double horizontalFactor = (height > width && width != 0) ? height / width : 1;
		double verticalFactor = (width > height && height != 0) ? width / height : 1;
		AABB blockScanBB = localBB.inflate(0.5f);
		blockScanBB = blockScanBB.inflate(horizontalFactor, verticalFactor, horizontalFactor);

		BlockPos min = new BlockPos(blockScanBB.minX, blockScanBB.minY, blockScanBB.minZ);
		BlockPos max = new BlockPos(blockScanBB.maxX, blockScanBB.maxY, blockScanBB.maxZ);

		List<VoxelShape> potentialHits = BlockPos.betweenClosedStream(min, max)
			.filter(contraption.getBlocks()::containsKey)
			.filter(Predicates.not(contraption::isHiddenInPortal))
			.map(p -> {
				BlockState blockState = contraption.getBlocks()
					.get(p).state;
				BlockPos pos = contraption.getBlocks()
					.get(p).pos;
				VoxelShape collisionShape = blockState.getCollisionShape(world, p);
				return collisionShape.move(pos.getX(), pos.getY(), pos.getZ());
			})
			.filter(Predicates.not(VoxelShape::isEmpty))
			.toList();

		return potentialHits;
	}

	public static boolean collideBlocks(AbstractContraptionEntity contraptionEntity) {
		if (!contraptionEntity.supportsTerrainCollision())
			return false;

		Level world = contraptionEntity.getCommandSenderWorld();
		Vec3 motion = contraptionEntity.getDeltaMovement();
		TranslatingContraption contraption = (TranslatingContraption) contraptionEntity.getContraption();
		AABB bounds = contraptionEntity.getBoundingBox();
		Vec3 position = contraptionEntity.position();
		BlockPos gridPos = new BlockPos(position);

		if (contraption == null)
			return false;
		if (bounds == null)
			return false;
		if (motion.equals(Vec3.ZERO))
			return false;

		Direction movementDirection = Direction.getNearest(motion.x, motion.y, motion.z);

		// Blocks in the world
		if (movementDirection.getAxisDirection() == AxisDirection.POSITIVE)
			gridPos = gridPos.relative(movementDirection);
		if (isCollidingWithWorld(world, contraption, gridPos, movementDirection))
			return true;

		// Other moving Contraptions
		for (ControlledContraptionEntity otherContraptionEntity : world.getEntitiesOfClass(
			ControlledContraptionEntity.class, bounds.inflate(1), e -> !e.equals(contraptionEntity))) {

			if (!otherContraptionEntity.supportsTerrainCollision())
				continue;

			Vec3 otherMotion = otherContraptionEntity.getDeltaMovement();
			TranslatingContraption otherContraption = (TranslatingContraption) otherContraptionEntity.getContraption();
			AABB otherBounds = otherContraptionEntity.getBoundingBox();
			Vec3 otherPosition = otherContraptionEntity.position();

			if (otherContraption == null)
				return false;
			if (otherBounds == null)
				return false;

			if (!bounds.move(motion)
				.intersects(otherBounds.move(otherMotion)))
				continue;

			for (BlockPos colliderPos : contraption.getOrCreateColliders(world, movementDirection)) {
				colliderPos = colliderPos.offset(gridPos)
					.subtract(new BlockPos(otherPosition));
				if (!otherContraption.getBlocks()
					.containsKey(colliderPos))
					continue;
				return true;
			}
		}

		return false;
	}

	public static boolean isCollidingWithWorld(Level world, TranslatingContraption contraption, BlockPos anchor,
		Direction movementDirection) {
		for (BlockPos pos : contraption.getOrCreateColliders(world, movementDirection)) {
			BlockPos colliderPos = pos.offset(anchor);

			if (!world.isLoaded(colliderPos))
				return true;

			BlockState collidedState = world.getBlockState(colliderPos);
			StructureBlockInfo blockInfo = contraption.getBlocks()
				.get(pos);
			boolean emptyCollider = collidedState.getCollisionShape(world, pos)
				.isEmpty();

			if (collidedState.getBlock() instanceof CocoaBlock)
				continue;

			MovementBehaviour movementBehaviour = AllMovementBehaviours.getBehaviour(blockInfo.state);
			if (movementBehaviour != null) {
				if (movementBehaviour instanceof BlockBreakingMovementBehaviour) {
					BlockBreakingMovementBehaviour behaviour = (BlockBreakingMovementBehaviour) movementBehaviour;
					if (!behaviour.canBreak(world, colliderPos, collidedState) && !emptyCollider)
						return true;
					continue;
				}
				if (movementBehaviour instanceof HarvesterMovementBehaviour) {
					HarvesterMovementBehaviour harvesterMovementBehaviour =
						(HarvesterMovementBehaviour) movementBehaviour;
					if (!harvesterMovementBehaviour.isValidCrop(world, colliderPos, collidedState)
						&& !harvesterMovementBehaviour.isValidOther(world, colliderPos, collidedState)
						&& !emptyCollider)
						return true;
					continue;
				}
			}

			if (AllBlocks.PULLEY_MAGNET.has(collidedState) && pos.equals(BlockPos.ZERO)
				&& movementDirection == Direction.UP)
				continue;
			if (!collidedState.getMaterial()
				.isReplaceable() && !emptyCollider) {
				return true;
			}

		}
		return false;
	}

}
