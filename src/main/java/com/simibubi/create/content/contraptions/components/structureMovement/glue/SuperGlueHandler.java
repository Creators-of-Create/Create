package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.worldWrappers.RayTraceWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class SuperGlueHandler {

	public static Map<Direction, SuperGlueEntity> gatherGlue(LevelAccessor world, BlockPos pos) {
		List<SuperGlueEntity> entities = world.getEntitiesOfClass(SuperGlueEntity.class, new AABB(pos));
		Map<Direction, SuperGlueEntity> map = new HashMap<>();
		for (SuperGlueEntity entity : entities)
			map.put(entity.getAttachedDirection(pos), entity);
		return map;
	}

	public static InteractionResult glueListensForBlockPlacement(BlockPlaceContext context) {
		LevelAccessor world = context.getLevel();
		Entity entity = context.getPlayer();
		BlockPos pos = context.getClickedPos();

		if (entity == null || world == null || pos == null)
			return InteractionResult.PASS;
		if (world.isClientSide())
			return InteractionResult.PASS;

		Map<Direction, SuperGlueEntity> gatheredGlue = gatherGlue(world, pos);
		for (Direction direction : gatheredGlue.keySet())
			AllPackets.channel.sendToClientsTrackingAndSelf(new GlueEffectPacket(pos, direction, true), entity);

		if (entity instanceof Player)
			return glueInOffHandAppliesOnBlockPlace(context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite())), pos, (Player) entity);
		return InteractionResult.PASS;
	}

	public static InteractionResult glueInOffHandAppliesOnBlockPlace(BlockState placedAgainst, BlockPos pos, Player placer) {
		ItemStack itemstack = placer.getOffhandItem();
		if (!AllItems.SUPER_GLUE.isIn(itemstack))
			return InteractionResult.PASS;
		if (AllItems.WRENCH.isIn(placer.getMainHandItem()))
			return InteractionResult.PASS;
		if (placedAgainst == IPlacementHelper.ID)
			return InteractionResult.PASS;

		double distance = ReachEntityAttributes.getReachDistance(placer, placer.isCreative() ? 5 : 4.5);
		Vec3 start = placer.getEyePosition(1);
		Vec3 look = placer.getViewVector(1);
		Vec3 end = start.add(look.x * distance, look.y * distance, look.z * distance);
		Level world = placer.level;

		RayTraceWorld rayTraceWorld =
			new RayTraceWorld(world, (p, state) -> p.equals(pos) ? Blocks.AIR.defaultBlockState() : state);
		BlockHitResult ray = rayTraceWorld.clip(
			new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, placer));

		Direction face = ray.getDirection();
		if (face == null || ray.getType() == Type.MISS)
			return InteractionResult.PASS;

		if (!ray.getBlockPos()
			.relative(face)
			.equals(pos)) {
			return InteractionResult.SUCCESS;
		}

		SuperGlueEntity entity = new SuperGlueEntity(world, ray.getBlockPos(), face.getOpposite());
		CompoundTag compoundnbt = itemstack.getTag();
		if (compoundnbt != null)
			EntityType.updateCustomEntityTag(world, placer, entity, compoundnbt);

		if (entity.onValidSurface()) {
			if (!world.isClientSide) {
				entity.playPlaceSound();
				world.addFreshEntity(entity);
				AllPackets.channel.sendToClientsTrackingAndSelf(new GlueEffectPacket(ray.getBlockPos(), face, true), entity);
			}
			itemstack.hurtAndBreak(1, placer, SuperGlueItem::onBroken);
		}
		return InteractionResult.PASS;
	}

}
