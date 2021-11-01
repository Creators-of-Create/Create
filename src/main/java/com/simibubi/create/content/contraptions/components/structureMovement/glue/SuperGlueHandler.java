package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.worldWrappers.RayTraceWorld;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber
public class SuperGlueHandler {

	public static Map<Direction, SuperGlueEntity> gatherGlue(LevelAccessor world, BlockPos pos) {
		List<SuperGlueEntity> entities = world.getEntitiesOfClass(SuperGlueEntity.class, new AABB(pos));
		Map<Direction, SuperGlueEntity> map = new HashMap<>();
		for (SuperGlueEntity entity : entities)
			map.put(entity.getAttachedDirection(pos), entity);
		return map;
	}

	@SubscribeEvent
	public static void glueListensForBlockPlacement(EntityPlaceEvent event) {
		LevelAccessor world = event.getWorld();
		Entity entity = event.getEntity();
		BlockPos pos = event.getPos();

		if (entity == null || world == null || pos == null)
			return;
		if (world.isClientSide())
			return;

		Map<Direction, SuperGlueEntity> gatheredGlue = gatherGlue(world, pos);
		for (Direction direction : gatheredGlue.keySet())
			AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
				new GlueEffectPacket(pos, direction, true));

		if (entity instanceof Player)
			glueInOffHandAppliesOnBlockPlace(event, pos, (Player) entity);
	}

	public static void glueInOffHandAppliesOnBlockPlace(EntityPlaceEvent event, BlockPos pos, Player placer) {
		ItemStack itemstack = placer.getOffhandItem();
		AttributeInstance reachAttribute = placer.getAttribute(ForgeMod.REACH_DISTANCE.get());
		if (!AllItems.SUPER_GLUE.isIn(itemstack) || reachAttribute == null)
			return;
		if (AllItems.WRENCH.isIn(placer.getMainHandItem()))
			return;
		if (event.getPlacedAgainst() == IPlacementHelper.ID)
			return;

		double distance = reachAttribute.getValue();
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
			return;

		if (!ray.getBlockPos()
			.relative(face)
			.equals(pos)) {
			event.setCanceled(true);
			return;
		}

		SuperGlueEntity entity = new SuperGlueEntity(world, ray.getBlockPos(), face.getOpposite());
		CompoundTag compoundnbt = itemstack.getTag();
		if (compoundnbt != null)
			EntityType.updateCustomEntityTag(world, placer, entity, compoundnbt);

		if (entity.onValidSurface()) {
			if (!world.isClientSide) {
				entity.playPlaceSound();
				world.addFreshEntity(entity);
				AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
					new GlueEffectPacket(ray.getBlockPos(), face, true));
			}
			itemstack.hurtAndBreak(1, placer, SuperGlueItem::onBroken);
		}
	}

}
