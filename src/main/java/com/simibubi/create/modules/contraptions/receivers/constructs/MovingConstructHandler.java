package com.simibubi.create.modules.contraptions.receivers.constructs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.simibubi.create.Create;

import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class MovingConstructHandler {

	static List<AxisAlignedBB> renderedBBs = new LinkedList<>();
	static Map<IWorld, List<MechanicalPistonTileEntity>> movingPistons = new HashMap<>();

	public void onLoadWorld(IWorld world) {
		movingPistons.put(world, new ArrayList<>());
		Create.logger.debug("Prepared Construct List for " + world.getDimension().getType().getRegistryName());
	}

	public void onUnloadWorld(IWorld world) {
		movingPistons.remove(world);
		Create.logger.debug("Removed Construct List for " + world.getDimension().getType().getRegistryName());
	}

	public static void moveEntities(MechanicalPistonTileEntity te, float movementSpeed, Direction movementDirection,
			float newOffset) {
		if (PistonContraption.isFrozen())
			return;

		World world = te.getWorld();
		Vec3d movementVec = new Vec3d(te.getBlockState().get(BlockStateProperties.FACING).getDirectionVec());
		Contraption construct = te.movedContraption;

//		if (world.isRemote) {
//			renderedBBs.clear();
//			if (construct.pistonCollisionBox != null)
//				renderedBBs.add(construct.pistonCollisionBox.offset(te.getConstructOffset(0)));
//			if (construct.constructCollisionBox != null)
//				renderedBBs.add(construct.constructCollisionBox.offset(te.getConstructOffset(0)));
//
//		}

		if (construct.getCollisionBoxFront() != null) {
			AxisAlignedBB constructBB = construct.getCollisionBoxFront().offset(te.getConstructOffset(0)).grow(.5f);

			for (Entity entity : world.getEntitiesWithinAABB((EntityType<?>) null, constructBB,
					e -> e.getPushReaction() == PushReaction.NORMAL)) {

				AxisAlignedBB entityScanBB = entity.getBoundingBox().offset(movementVec.scale(-1 * newOffset))
						.grow(.5f);
				BlockPos min = new BlockPos(entityScanBB.minX, entityScanBB.minY, entityScanBB.minZ);
				BlockPos max = new BlockPos(entityScanBB.maxX, entityScanBB.maxY, entityScanBB.maxZ);

				Stream<VoxelShape> hits = BlockPos.getAllInBox(min, max).filter(construct.blocks::containsKey)
						.map(pos -> {
							Vec3d vec = new Vec3d(pos).add(te.getConstructOffset(te.getMovementSpeed() > 0 ? 1 : 0));
							return construct.blocks.get(pos).state.getShape(world, new BlockPos(vec)).withOffset(vec.x,
									vec.y, vec.z);
						});
				ReuseableStream<VoxelShape> potentialHits = new ReuseableStream<>(hits);

				AxisAlignedBB entityBB = entity.getBoundingBox();
				Vec3d motion = entity.getMotion();
				Vec3d movement = new Vec3d(movementDirection.getDirectionVec()).scale(-movementSpeed).add(motion);
				Vec3d allowedMovement = Entity.getAllowedMovement(movement, entityBB, world,
						ISelectionContext.forEntity(entity), potentialHits);

				for (Object shape : potentialHits.createStream().toArray()) {
					VoxelShape voxelShape = (VoxelShape) shape;
					if (!entityBB.intersects(voxelShape.getBoundingBox()))
						continue;

					Direction bestSide = Direction.DOWN;
					double bestOffset = 100;
					double finalOffset = 0;

					for (Direction face : Direction.values()) {
						Axis axis = face.getAxis();
						double d = axis == Axis.X ? entityBB.getXSize()
								: axis == Axis.Y ? entityBB.getYSize() : entityBB.getZSize();
						d = d + 1.5f;

						Vec3d nudge = new Vec3d(face.getDirectionVec()).scale(d);
						AxisAlignedBB nudgedBB = entityBB.offset(nudge.getX(), nudge.getY(), nudge.getZ());
						double nudgeDistance = face.getAxisDirection() == AxisDirection.POSITIVE ? -d : d;
						double offset = voxelShape.getAllowedOffset(face.getAxis(), nudgedBB, nudgeDistance);
						double abs = Math.abs(nudgeDistance - offset);
						if (abs < Math.abs(bestOffset) && abs != 0) {
							bestOffset = abs;
							finalOffset = abs;
							bestSide = face;
						}
					}

					if (bestOffset != 0) {
						entity.move(MoverType.SELF, new Vec3d(bestSide.getDirectionVec()).scale(finalOffset));
						switch (bestSide.getAxis()) {
						case X:
							entity.setMotion(0, motion.y, motion.z);
							break;
						case Y:
							entity.setMotion(motion.x, bestSide == Direction.UP ? movementSpeed + 1 / 8f : 0, motion.z);
							entity.fall(entity.fallDistance, 1);
							entity.fallDistance = 0;
							entity.onGround = true;
							break;
						case Z:
							entity.setMotion(motion.x, motion.y, 0);
							break;
						}

						break;
					}
				}

				if (!allowedMovement.equals(movement)) {
					if (allowedMovement.y != movement.y) {
						entity.fall(entity.fallDistance, 1);
						entity.fallDistance = 0;
						entity.onGround = true;
					}
					if (entity instanceof PlayerEntity && !world.isRemote)
						return;
					entity.setMotion(allowedMovement.subtract(movement.subtract(motion)));
					entity.velocityChanged = true;
				}

			}
		}
	}

	public void add(MechanicalPistonTileEntity mechanicalPistonTileEntity) {
		movingPistons.get(mechanicalPistonTileEntity.getWorld()).add(mechanicalPistonTileEntity);
	}

	public void remove(MechanicalPistonTileEntity mechanicalPistonTileEntity) {
		movingPistons.get(mechanicalPistonTileEntity.getWorld()).remove(mechanicalPistonTileEntity);
	}

	public List<MechanicalPistonTileEntity> getOtherMovingPistonsInWorld(
			MechanicalPistonTileEntity mechanicalPistonTileEntity) {
		return movingPistons.get(mechanicalPistonTileEntity.getWorld());
	}

//	@SubscribeEvent
//	@OnlyIn(value = Dist.CLIENT)
//	public static void onRenderWorld(RenderWorldLastEvent event) {
//		for (AxisAlignedBB bb : renderedBBs) {
//			TessellatorHelper.prepareForDrawing();
//			GlStateManager.disableTexture();
//			GlStateManager.lineWidth(3);
//			int color = ColorHelper.rainbowColor(renderedBBs.indexOf(bb) * 170);
//			WorldRenderer.drawSelectionBoundingBox(bb.grow(1 / 256f), (color >> 16 & 0xFF) / 256f,
//					(color >> 8 & 0xFF) / 256f, (color & 0xFF) / 256f, 1);
//			GlStateManager.lineWidth(1);
//			GlStateManager.enableTexture();
//			TessellatorHelper.cleanUpAfterDrawing();
//		}
//	}

}
