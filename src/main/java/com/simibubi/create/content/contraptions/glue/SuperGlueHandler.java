package com.simibubi.create.content.contraptions.glue;

import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.contraption.BlockMovementChecks;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.levelWrappers.RayTraceLevel;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;

@EventBusSubscriber
public class SuperGlueHandler {

	@SubscribeEvent
	public static void glueListensForBlockPlacement(EntityPlaceEvent event) {
		LevelAccessor world = event.getLevel();
		Entity entity = event.getEntity();
		BlockPos pos = event.getPos();

		if (entity == null || world == null || pos == null)
			return;
		if (world.isClientSide())
			return;

		Set<SuperGlueEntity> cached = new HashSet<>();
		for (Direction direction : Iterate.directions) {
			BlockPos relative = pos.relative(direction);
			if (SuperGlueEntity.isGlued(world, pos, direction, cached)
				&& BlockMovementChecks.isMovementNecessary(world.getBlockState(relative), entity.level(), relative))
				CatnipServices.NETWORK.sendToClientsTrackingAndSelf(entity, new GlueEffectPacket(pos, direction, true));
		}

		if (entity instanceof Player)
			glueInOffHandAppliesOnBlockPlace(event, pos, (Player) entity);
	}

	public static void glueInOffHandAppliesOnBlockPlace(EntityPlaceEvent event, BlockPos pos, Player placer) {
		ItemStack itemstack = placer.getOffhandItem();
		AttributeInstance reachAttribute = placer.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
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
		Level world = placer.level();

		RayTraceLevel rayTraceLevel =
			new RayTraceLevel(world, (p, state) -> p.equals(pos) ? Blocks.AIR.defaultBlockState() : state);
		BlockHitResult ray =
			rayTraceLevel.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, placer));

		Direction face = ray.getDirection();
		if (face == null || ray.getType() == Type.MISS)
			return;

		BlockPos gluePos = ray.getBlockPos();
		if (!gluePos.relative(face)
			.equals(pos)) {
			event.setCanceled(true);
			return;
		}

		if (SuperGlueEntity.isGlued(world, gluePos, face, null))
			return;

		SuperGlueEntity entity = new SuperGlueEntity(world, SuperGlueEntity.span(gluePos, gluePos.relative(face)));
		CustomData customData = itemstack.get(DataComponents.CUSTOM_DATA);
		if (customData != null)
			EntityType.updateCustomEntityTag(world, placer, entity, customData);

		if (SuperGlueEntity.isValidFace(world, gluePos, face)) {
			if (!world.isClientSide) {
				world.addFreshEntity(entity);
				CatnipServices.NETWORK.sendToClientsTrackingEntity(entity,
					new GlueEffectPacket(gluePos, face, true));
			}

			itemstack.hurtAndBreak(1, placer, EquipmentSlot.MAINHAND);
		}
	}

}
