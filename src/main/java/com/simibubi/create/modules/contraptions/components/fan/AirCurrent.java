package com.simibubi.create.modules.contraptions.components.fan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.particle.AirFlowParticleData;
import com.simibubi.create.modules.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.logistics.InWorldProcessing;
import com.simibubi.create.modules.logistics.InWorldProcessing.Type;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

public class AirCurrent {

	private static DamageSource damageSourceFire =
		new DamageSource("create.fan_fire").setDifficultyScaled().setFireDamage();
	private static DamageSource damageSourceLava =
		new DamageSource("create.fan_lava").setDifficultyScaled().setFireDamage();

	public final EncasedFanTileEntity source;
	public AxisAlignedBB bounds = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
	public List<AirCurrentSegment> segments = new ArrayList<>();
	public Direction direction;
	public boolean pushing;
	public float maxDistance;

	protected List<Pair<BeltTileEntity, InWorldProcessing.Type>> affectedBelts = new ArrayList<>();
	protected List<Entity> caughtEntities = new ArrayList<>();

	public AirCurrent(EncasedFanTileEntity source) {
		this.source = source;
	}

	public void tick() {
		World world = source.getWorld();
		Direction facing = direction;
		if (world.isRemote) {
			float offset = pushing ? 0.5f : maxDistance + .5f;
			Vec3d pos = VecHelper.getCenterOf(source.getPos()).add(new Vec3d(facing.getDirectionVec()).scale(offset));
			if (world.rand.nextFloat() < AllConfigs.CLIENT.fanParticleDensity.get())
				world.addParticle(new AirFlowParticleData(source.getPos()), pos.x, pos.y, pos.z, 0, 0, 0);
		}

		for (Iterator<Entity> iterator = caughtEntities.iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			if (!entity.getBoundingBox().intersects(bounds)) {
				iterator.remove();
				continue;
			}

			Vec3d center = VecHelper.getCenterOf(source.getPos());
			Vec3i flow = (pushing ? facing : facing.getOpposite()).getDirectionVec();

			float sneakModifier = entity.isSneaking() ? 4096f : 512f;
			float speed = Math.abs(source.getSpeed());
			double entityDistance = entity.getPositionVec().distanceTo(center);
			float acceleration = (float) (speed / sneakModifier / (entityDistance / maxDistance));
			Vec3d previousMotion = entity.getMotion();
			float maxAcceleration = 5;

			double xIn =
				MathHelper.clamp(flow.getX() * acceleration - previousMotion.x, -maxAcceleration, maxAcceleration);
			double yIn =
				MathHelper.clamp(flow.getY() * acceleration - previousMotion.y, -maxAcceleration, maxAcceleration);
			double zIn =
				MathHelper.clamp(flow.getZ() * acceleration - previousMotion.z, -maxAcceleration, maxAcceleration);

			entity.setMotion(previousMotion.add(new Vec3d(xIn, yIn, zIn).scale(1 / 8f)));
			entity.fallDistance = 0;

			if (InWorldProcessing.isFrozen())
				return;

			entityDistance -= .5f;
			InWorldProcessing.Type processingType = getSegmentAt((float) entityDistance);
			if (processingType != null) {
				if (entity instanceof ItemEntity) {
					InWorldProcessing.spawnParticlesForProcessing(world, entity.getPositionVec(), processingType);
					if (InWorldProcessing.canProcess(((ItemEntity) entity), processingType))
						InWorldProcessing.applyProcessing((ItemEntity) entity, processingType);

				} else {
					switch (processingType) {
					case BLASTING:
						entity.setFire(10);
						entity.attackEntityFrom(damageSourceLava, 4);
						break;
					case SMOKING:
						entity.setFire(2);
						entity.attackEntityFrom(damageSourceFire, 2);
						break;
					case SPLASHING:
						if (!entity.isBurning())
							break;
						entity.extinguish();
						world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
								SoundCategory.NEUTRAL, 0.7F,
								1.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F);
						break;
					default:
						break;
					}
				}
			}

		}

		tickBelts();
	}

	public void rebuild() {
		if (source.getSpeed() == 0) {
			maxDistance = 0;
			segments.clear();
			bounds = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
			return;
		}

		World world = source.getWorld();
		BlockPos start = source.getPos();
		direction = source.getBlockState().get(BlockStateProperties.FACING);
		pushing = source.getAirFlowDirection() == direction;
		Vec3d directionVec = new Vec3d(direction.getDirectionVec());
		Vec3d planeVec = VecHelper.planeByNormal(directionVec);

		// 4 Rays test for holes in the shapes blocking the flow
		float offsetDistance = .25f;
		Vec3d[] offsets = new Vec3d[] { planeVec.mul(offsetDistance, offsetDistance, offsetDistance),
				planeVec.mul(-offsetDistance, -offsetDistance, offsetDistance),
				planeVec.mul(offsetDistance, -offsetDistance, -offsetDistance),
				planeVec.mul(-offsetDistance, offsetDistance, -offsetDistance), };

		maxDistance = source.getMaxDistance();
		float limitedDistance = 0;

		// Determine the distance of the air flow
		Outer: for (int i = 1; i < maxDistance; i++) {
			BlockPos currentPos = start.offset(direction, i);
			if (!world.isBlockPresent(currentPos))
				break;
			BlockState state = world.getBlockState(currentPos);
			if (shouldAlwaysPass(state))
				continue;
			VoxelShape voxelshape = state.getCollisionShape(world, currentPos, ISelectionContext.dummy());
			if (voxelshape.isEmpty())
				continue;
			if (voxelshape == VoxelShapes.fullCube()) {
				maxDistance = i - 1;
				break;
			}

			for (Vec3d offset : offsets) {
				Vec3d rayStart =
					VecHelper.getCenterOf(currentPos).subtract(directionVec.scale(.5f + 1 / 32f)).add(offset);
				Vec3d rayEnd = rayStart.add(directionVec.scale(1 + 1 / 32f));
				BlockRayTraceResult blockraytraceresult =
					world.rayTraceBlocks(rayStart, rayEnd, currentPos, voxelshape, state);
				if (blockraytraceresult == null)
					continue Outer;

				double distance = i - 1 + blockraytraceresult.getHitVec().distanceTo(rayStart);
				if (limitedDistance < distance)
					limitedDistance = (float) distance;
			}

			maxDistance = limitedDistance;
			break;
		}

		// Determine segments with transported fluids/gases
		AirCurrentSegment currentSegment = new AirCurrentSegment();
		segments.clear();
		currentSegment.startOffset = 0;
		InWorldProcessing.Type type = null;

		int limit = (int) (maxDistance + .5f);
		int searchStart = pushing ? 0 : limit;
		int searchEnd = pushing ? limit : 0;
		int searchStep = pushing ? 1 : -1;

		for (int i = searchStart; i * searchStep <= searchEnd * searchStep; i += searchStep) {
			BlockPos currentPos = start.offset(direction, i);
			InWorldProcessing.Type newType = InWorldProcessing.Type.byBlock(world, currentPos);
			if (newType != null)
				type = newType;
			if (currentSegment.type != type || currentSegment.startOffset == 0) {
				currentSegment.endOffset = i;
				if (currentSegment.startOffset != 0)
					segments.add(currentSegment);
				currentSegment = new AirCurrentSegment();
				currentSegment.startOffset = i;
				currentSegment.type = type;
			}
		}
		currentSegment.endOffset = searchEnd + searchStep;
		segments.add(currentSegment);

		// Build Bounding Box
		if (maxDistance < 0.25f)
			bounds = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		else {
			float factor = maxDistance - 1;
			Vec3d scale = directionVec.scale(factor);
			if (factor > 0)
				bounds = new AxisAlignedBB(start.offset(direction)).expand(scale);
			else {
				bounds = new AxisAlignedBB(start.offset(direction)).contract(scale.x, scale.y, scale.z).offset(scale);
			}
		}
		findAffectedBelts();
	}

	public void findEntities() {
		caughtEntities.clear();
		caughtEntities = source.getWorld().getEntitiesWithinAABBExcludingEntity(null, bounds);
	}

	public void findAffectedBelts() {
		World world = source.getWorld();
		BlockPos start = source.getPos();
		affectedBelts.clear();
		for (int i = 0; i < maxDistance + 1; i++) {
			Type type = getSegmentAt(i);
			if (type == null)
				continue;
			BlockPos pos = start.offset(direction, i);
			TileEntity te = world.getTileEntity(pos);
			if (te != null && (te instanceof BeltTileEntity))
				affectedBelts.add(Pair.of((BeltTileEntity) te, type));
			if (direction.getAxis().isVertical())
				continue;

			pos = pos.down();
			te = world.getTileEntity(pos);
			if (te == null || !(te instanceof BeltTileEntity))
				continue;
			affectedBelts.add(Pair.of((BeltTileEntity) te, type));
		}
	}

	public void tickBelts() {
		for (Pair<BeltTileEntity, Type> pair : affectedBelts) {
			BeltTileEntity belt = pair.getKey();
			World world = belt.getWorld();
			InWorldProcessing.Type processingType = pair.getRight();

			BeltTileEntity controller = belt.getControllerTE();
			if (controller == null)
				continue;

			controller.getInventory().forEachWithin(belt.index + .5f, .51f, (transported) -> {
				InWorldProcessing.spawnParticlesForProcessing(world,
						BeltHelper.getVectorForOffset(controller, transported.beltPosition), processingType);
				if (world.isRemote)
					return null;
				return InWorldProcessing.applyProcessing(transported, belt, processingType);
			});
		}
	}

	public void writeToNBT(CompoundNBT nbt) {

	}

	public void readFromNBT(CompoundNBT nbt) {

	}

	private static boolean shouldAlwaysPass(BlockState state) {
		if (state.isIn(Tags.Blocks.FENCES))
			return true;
		if (state.getBlock() == Blocks.IRON_BARS)
			return true;
		return false;
	}

	public InWorldProcessing.Type getSegmentAt(float offset) {
		for (AirCurrentSegment airCurrentSegment : segments) {
			if (offset > airCurrentSegment.endOffset && pushing)
				continue;
			if (offset < airCurrentSegment.endOffset && !pushing)
				continue;
			return airCurrentSegment.type;
		}
		return null;
	}

	public static class AirCurrentSegment {
		InWorldProcessing.Type type;
		int startOffset;
		int endOffset;

	}

}
