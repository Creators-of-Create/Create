package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.AllItemsNew;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.worldWrappers.RayTraceWorld;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber
public class SuperGlueHandler {

	public static Map<Direction, SuperGlueEntity> gatherGlue(IWorld world, BlockPos pos) {
		List<SuperGlueEntity> entities = world.getEntitiesWithinAABB(SuperGlueEntity.class, new AxisAlignedBB(pos));
		Map<Direction, SuperGlueEntity> map = new HashMap<>();
		for (SuperGlueEntity entity : entities)
			map.put(entity.getAttachedDirection(pos), entity);
		return map;
	}

	@SubscribeEvent
	public static void glueListensForBlockPlacement(EntityPlaceEvent event) {
		IWorld world = event.getWorld();
		Entity entity = event.getEntity();
		BlockPos pos = event.getPos();

		if (entity == null || world == null || pos == null)
			return;
		if (event.isCanceled())
			return;
		if (world.isRemote())
			return;

		Map<Direction, SuperGlueEntity> gatheredGlue = gatherGlue(world, pos);
		for (Direction direction : gatheredGlue.keySet())
			AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
				new GlueEffectPacket(pos, direction, true));

		if (entity instanceof PlayerEntity)
			glueInOffHandAppliesOnBlockPlace(event, pos, (PlayerEntity) entity);
	}

	public static void glueInOffHandAppliesOnBlockPlace(EntityPlaceEvent event, BlockPos pos, PlayerEntity placer) {
		ItemStack itemstack = placer.getHeldItemOffhand();
		if (!AllItemsNew.typeOf(AllItemsNew.SUPER_GLUE, itemstack))
			return;

		double distance = placer.getAttribute(PlayerEntity.REACH_DISTANCE)
			.getValue();
		Vec3d start = placer.getEyePosition(1);
		Vec3d look = placer.getLook(1);
		Vec3d end = start.add(look.x * distance, look.y * distance, look.z * distance);
		World world = placer.world;

		RayTraceWorld rayTraceWorld =
			new RayTraceWorld(world, (p, state) -> p.equals(pos) ? Blocks.AIR.getDefaultState() : state);
		BlockRayTraceResult ray = rayTraceWorld.rayTraceBlocks(
			new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, placer));

		Direction face = ray.getFace();
		if (ray == null || face == null || ray.getType() == Type.MISS)
			return;

		if (!ray.getPos()
			.offset(face)
			.equals(pos)) {
			event.setCanceled(true);
			return;
		}

		SuperGlueEntity entity = new SuperGlueEntity(world, ray.getPos(), face.getOpposite());
		CompoundNBT compoundnbt = itemstack.getTag();
		if (compoundnbt != null)
			EntityType.applyItemNBT(world, placer, entity, compoundnbt);

		if (entity.onValidSurface()) {
			if (!world.isRemote) {
				entity.playPlaceSound();
				world.addEntity(entity);
				AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
					new GlueEffectPacket(ray.getPos(), face, true));
			}
			itemstack.damageItem(1, placer, SuperGlueItem::onBroken);
		}
	}

}
