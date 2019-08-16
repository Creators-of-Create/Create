package com.simibubi.create.modules.contraptions.receivers.constructs;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.utility.TessellatorHelper;

import net.minecraft.block.material.PushReaction;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ConstructEntityHelper {

	static List<AxisAlignedBB> renderedBBs = new LinkedList<>();

	public static void moveEntities(MechanicalPistonTileEntity te, float movementSpeed, Direction movementDirection,
			float newOffset) {
		World world = te.getWorld();
		Vec3d movementVec = new Vec3d(te.getBlockState().get(BlockStateProperties.FACING).getDirectionVec());
		Construct construct = te.movingConstruct;

		if (world.isRemote) {
			renderedBBs.clear();
			if (construct.collisionBoxFront != null)
				renderedBBs.add(construct.collisionBoxFront.offset(te.getConstructOffset(0)));
			if (construct.collisionBoxBack != null)
				renderedBBs.add(construct.collisionBoxBack.offset(te.getConstructOffset(0)));
			
		}

		if (construct.getCollisionBoxFront() != null) {
			AxisAlignedBB constructBB = construct.getCollisionBoxFront().offset(te.getConstructOffset(0)).grow(.5f);

			if (world.isRemote) {
				renderedBBs.add(constructBB);
			}

			for (Entity entity : world.getEntitiesWithinAABB((EntityType<?>) null, constructBB,
					e -> e.getPushReaction() == PushReaction.NORMAL)) {

				AxisAlignedBB entityBB = entity.getBoundingBox().offset(movementVec.scale(-1 * newOffset)).grow(.5f);
				BlockPos min = new BlockPos(entityBB.minX, entityBB.minY, entityBB.minZ);// .add(-1, -1, -1);
				BlockPos max = new BlockPos(entityBB.maxX, entityBB.maxY, entityBB.maxZ);// .add(1, 1, 1);

				Stream<VoxelShape> hits = BlockPos.getAllInBox(min, max).filter(construct.blocks::containsKey)
						.map(pos -> {
							Vec3d vec = new Vec3d(pos).add(te.getConstructOffset(0));
							return construct.blocks.get(pos).state.getShape(world, new BlockPos(vec)).withOffset(vec.x,
									vec.y, vec.z);
						});
				ReuseableStream<VoxelShape> potentialHits = new ReuseableStream<>(hits);

				// TODO: debug output
				if (!world.isRemote) {
					if (entity instanceof PlayerEntity)
						((PlayerEntity) entity).sendStatusMessage(
								new StringTextComponent("Potential Hits: " + potentialHits.createStream().count()),
								true);
				}
				/////////////////

				if (world.isRemote) {
					for (Object shape : potentialHits.createStream().toArray())
						renderedBBs.add(((VoxelShape) shape).getBoundingBox());
					renderedBBs
							.add(entity.getBoundingBox().offset(movementVec.scale(Math.signum(movementSpeed) * -.2f)));
				}

				Vec3d movement = new Vec3d(movementDirection.getDirectionVec()).scale(-movementSpeed)
						.add(entity.getMotion());
				Vec3d allowedMovement = Entity.getAllowedMovement(movement,
						entity.getBoundingBox().offset(movementVec.scale(Math.signum(movementSpeed) * -.2f)), world,
						ISelectionContext.forEntity(entity), potentialHits);

				if (!allowedMovement.equals(movement)) {
					entity.setMotion(allowedMovement.subtract(movement.subtract(entity.getMotion())));
				}

			}
		}
	}

	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event) {
//		for (AxisAlignedBB bb : renderedBBs) {
//			TessellatorHelper.prepareForDrawing();
//			GlStateManager.disableTexture();
//			GlStateManager.lineWidth(3);
//			WorldRenderer.drawSelectionBoundingBox(bb.grow(1 / 256f), .5f, 1, .5f, 1);
//			GlStateManager.lineWidth(1);
//			GlStateManager.enableTexture();
//			TessellatorHelper.cleanUpAfterDrawing();
//		}
	}

}
