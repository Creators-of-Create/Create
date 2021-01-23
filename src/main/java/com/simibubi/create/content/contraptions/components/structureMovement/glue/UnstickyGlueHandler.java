package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.worldWrappers.RayTraceWorld;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber
public class UnstickyGlueHandler {

	public static Map<Direction, UnstickyGlueEntity> gatherGlue(IWorld world, BlockPos pos) {
		List<UnstickyGlueEntity> entities = world.getEntitiesWithinAABB(UnstickyGlueEntity.class, new AxisAlignedBB(pos));
		Map<Direction, UnstickyGlueEntity> map = new HashMap<>();
		for (UnstickyGlueEntity entity : entities)
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
		if (world.isRemote())
			return;

		Map<Direction, UnstickyGlueEntity> gatheredGlue = gatherGlue(world, pos);
		for (Direction direction : gatheredGlue.keySet())
			AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
				new UnstickyGlueEffectPacket(pos, direction, true));

		if (entity instanceof PlayerEntity)
			glueInOffHandAppliesOnBlockPlace(event, pos, (PlayerEntity) entity);
	}

	public static void glueInOffHandAppliesOnBlockPlace(EntityPlaceEvent event, BlockPos pos, PlayerEntity placer) {
		ItemStack itemstack = placer.getHeldItemOffhand();
		ModifiableAttributeInstance reachAttribute = placer.getAttribute(ForgeMod.REACH_DISTANCE.get());
		if (!AllItems.UNSTICKY_GLUE.isIn(itemstack) || reachAttribute == null)
			return;
		if (AllItems.WRENCH.isIn(placer.getHeldItemMainhand()))
			return;

		double distance = reachAttribute.getValue();
		Vector3d start = placer.getEyePosition(1);
		Vector3d look = placer.getLook(1);
		Vector3d end = start.add(look.x * distance, look.y * distance, look.z * distance);
		World world = placer.world;

		RayTraceWorld rayTraceWorld =
			new RayTraceWorld(world, (p, state) -> p.equals(pos) ? Blocks.AIR.getDefaultState() : state);
		BlockRayTraceResult ray = rayTraceWorld.rayTraceBlocks(
			new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, placer));

		Direction face = ray.getFace();
		if (face == null || ray.getType() == Type.MISS)
			return;

		if (!ray.getPos()
			.offset(face)
			.equals(pos)) {
			event.setCanceled(true);
			return;
		}

		UnstickyGlueEntity entity = new UnstickyGlueEntity(world, ray.getPos(), face.getOpposite());
		CompoundNBT compoundnbt = itemstack.getTag();
		if (compoundnbt != null)
			EntityType.applyItemNBT(world, placer, entity, compoundnbt);

		if (entity.onValidSurface()) {
			if (!world.isRemote) {
				entity.playPlaceSound();
				world.addEntity(entity);
				AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
					new UnstickyGlueEffectPacket(ray.getPos(), face, true));
			}
			itemstack.damageItem(1, placer, UnstickyGlueItem::onBroken);
		}
	}

}
