package com.simibubi.create.modules.curiosities.tools;

import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.item.AbstractToolItem;
import com.simibubi.create.foundation.item.AbstractToolItem.HarvestPacket;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TieredItem;
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
		if (!tool.canHarvestBlock(held, state) && !state.getMaterial().isToolNotRequired())
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
		Item item = heldItemMainhand.getItem();
		boolean holdingRoseQuartz =
			item instanceof TieredItem && ((TieredItem) item).getTier() == AllToolTiers.ROSE_QUARTZ;

		if (!holdingRoseQuartz) {
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
	public static void toolsMayModifyEntityLoot(LivingDropsEvent event) {
		if (!(event.getSource() instanceof EntityDamageSource))
			return;

		EntityDamageSource source = (EntityDamageSource) event.getSource();
		Entity target = event.getEntity();
		Entity trueSource = source.getTrueSource();
		World world = target.getEntityWorld();

		if (trueSource != null && trueSource instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) trueSource;
			ItemStack heldItemMainhand = player.getHeldItemMainhand();
			Item item = heldItemMainhand.getItem();
			IItemTier tier = item instanceof TieredItem ? ((TieredItem) item).getTier() : null;

			if (tier == AllToolTiers.SHADOW_STEEL)
				event.setCanceled(true);

			if (tier == AllToolTiers.BLAZING) {
				List<ItemStack> drops = event.getDrops().stream().map(entity -> {
					ItemStack stack = entity.getItem();
					entity.remove();
					return stack;
				}).collect(Collectors.toList());

				drops = BlazingToolItem.smeltDrops(drops, world, 0);

				event.getDrops().clear();
				drops.stream().map(stack -> {
					ItemEntity entity = new ItemEntity(world, target.posX, target.posY, target.posZ, stack);
					world.addEntity(entity);
					return entity;
				}).forEach(event.getDrops()::add);
			}

		}
	}

	@SubscribeEvent
	public static void shadowSteelToolsDropMoreXPonKill(LivingExperienceDropEvent event) {
		PlayerEntity attackingPlayer = event.getAttackingPlayer();
		if (attackingPlayer == null)
			return;
		ItemStack heldItemMainhand = attackingPlayer.getHeldItemMainhand();
		Item item = heldItemMainhand.getItem();
		IItemTier tier = item instanceof TieredItem ? ((TieredItem) item).getTier() : null;
		
		if (tier == AllToolTiers.SHADOW_STEEL) {
			int level = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, heldItemMainhand);
			float modifier = 1 + event.getEntity().world.getRandom().nextFloat() * level;
			event.setDroppedExperience((int) (event.getDroppedExperience() * modifier + .4f));
		}
	}

}
