package com.simibubi.create.modules.curiosities.tools;

import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.item.AbstractToolItem;
import com.simibubi.create.foundation.item.AbstractToolItem.HarvestPacket;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber
public class ToolEvents {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void toolsCanModifyBlockDrops(BlockEvent.BreakEvent event) {
		if (event.isCanceled())
			return;
		PlayerEntity player = event.getPlayer();
		ItemStack held = player.getHeldItemMainhand();
		if (player.isCreative())
			return;
		if (!(held.getItem() instanceof AbstractToolItem))
			return;
		AbstractToolItem tool = (AbstractToolItem) held.getItem();
		if (!tool.modifiesDrops())
			return;
		BlockState state = event.getState();
		if (!tool.canHarvestBlock(held, state))
			return;

		IWorld world = event.getWorld();
		BlockPos pos = event.getPos();
		boolean onServer = !world.isRemote();

		if (!onServer) {
			tool.spawnParticles(world, pos, held, state);
			return;
		}

		World actualWorld = world.getWorld();
		if (!(actualWorld instanceof ServerWorld))
			return;

		List<ItemStack> drops = state.getDrops(new Builder((ServerWorld) actualWorld)
				.withRandom(actualWorld.getRandom()).withParameter(LootParameters.POSITION, pos)
				.withParameter(LootParameters.TOOL, held).withParameter(LootParameters.THIS_ENTITY, player)
				.withNullableParameter(LootParameters.BLOCK_ENTITY, world.getTileEntity(pos)));

		tool.modifyDrops(drops, world, pos, held, state);
		tool.onBlockDestroyed(held, actualWorld, state, pos, player);
		world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
		for (ItemStack dropped : drops)
			Block.spawnAsEntity(actualWorld, pos, dropped);

		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> player),
				new HarvestPacket(state, held, pos, false));
		AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
				new HarvestPacket(state, held, pos, true));
		event.setResult(Result.DENY);
	}

	@SubscribeEvent
	public static void holdingRoseQuartzToolIncreasesRange(LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof PlayerEntity))
			return;
		if (event.isCanceled())
			return;

		PlayerEntity player = (PlayerEntity) event.getEntityLiving();
		ItemStack heldItemMainhand = player.getHeldItemMainhand();
		String marker = "create_roseQuartzRange";
		CompoundNBT persistentData = player.getPersistentData();

		if (!(heldItemMainhand.getItem() instanceof RoseQuartzToolItem)) {
			if (persistentData.contains(marker)) {
				player.getAttributes().removeAttributeModifiers(RoseQuartzToolItem.rangeModifier);
				persistentData.remove(marker);
			}
			return;
		}

		if (!persistentData.contains(marker)) {
			player.getAttributes().applyAttributeModifiers(RoseQuartzToolItem.rangeModifier);
			persistentData.putBoolean(marker, true);
		}
	}

	@SubscribeEvent
	public static void shadowSteelToolsDoNotDropEntityLoot(LivingDropsEvent event) {
		if (!(event.getSource() instanceof EntityDamageSource))
			return;
		EntityDamageSource source = (EntityDamageSource) event.getSource();
		Entity trueSource = source.getTrueSource();
		if (trueSource != null && trueSource instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) trueSource;
			if (player.getHeldItemMainhand().getItem() instanceof ShadowSteelToolItem)
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void shadowSteelToolsDropMoreXPonKill(LivingExperienceDropEvent event) {
		ItemStack heldItemMainhand = event.getAttackingPlayer().getHeldItemMainhand();
		if (heldItemMainhand.getItem() instanceof ShadowSteelToolItem) {
			int level = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, heldItemMainhand);
			float modifier = 1 + event.getEntity().world.getRandom().nextFloat() * level;
			event.setDroppedExperience((int) (event.getDroppedExperience() * modifier + .4f));
		}
	}

}
