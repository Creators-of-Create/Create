package com.simibubi.create.content.equipment.extendoGrip;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.api.platform.CatnipServices;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;

@EventBusSubscriber
public class ExtendoGripItem extends Item {
	public static final int MAX_DAMAGE = 200;

	public static final AttributeModifier singleRangeAttributeModifier =
		new AttributeModifier(Create.asResource("single_range_attribute_modifier"), 3,
			AttributeModifier.Operation.ADD_VALUE);
	public static final AttributeModifier doubleRangeAttributeModifier =
		new AttributeModifier(Create.asResource("double_range_attribute_modifier"), 5,
			AttributeModifier.Operation.ADD_VALUE);

	private static final Supplier<Multimap<Holder<Attribute>, AttributeModifier>> rangeModifier = Suppliers.memoize(() ->
		// Holding an ExtendoGrip
		ImmutableMultimap.of(Attributes.BLOCK_INTERACTION_RANGE, singleRangeAttributeModifier));
	private static final Supplier<Multimap<Holder<Attribute>, AttributeModifier>> doubleRangeModifier = Suppliers.memoize(() ->
		// Holding two ExtendoGrips o.O
		ImmutableMultimap.of(Attributes.BLOCK_INTERACTION_RANGE, doubleRangeAttributeModifier));

	private static DamageSource lastActiveDamageSource;

	public ExtendoGripItem(Properties properties) {
		super(properties.durability(MAX_DAMAGE));
	}

	public static final String EXTENDO_MARKER = "createExtendo";
	public static final String DUAL_EXTENDO_MARKER = "createDualExtendo";

	@SubscribeEvent
	public static void holdingExtendoGripIncreasesRange(EntityTickEvent.Pre event) {
		if (!(event.getEntity() instanceof Player player))
			return;

		CompoundTag persistentData = player.getPersistentData();
		boolean inOff = AllItems.EXTENDO_GRIP.isIn(player.getOffhandItem());
		boolean inMain = AllItems.EXTENDO_GRIP.isIn(player.getMainHandItem());
		boolean holdingDualExtendo = inOff && inMain;
		boolean holdingExtendo = inOff ^ inMain;
		holdingExtendo &= !holdingDualExtendo;
		boolean wasHoldingExtendo = persistentData.contains(EXTENDO_MARKER);
		boolean wasHoldingDualExtendo = persistentData.contains(DUAL_EXTENDO_MARKER);

		if (holdingExtendo != wasHoldingExtendo) {
			if (!holdingExtendo) {
				player.getAttributes()
					.removeAttributeModifiers(rangeModifier.get());
				persistentData.remove(EXTENDO_MARKER);
			} else {
				AllAdvancements.EXTENDO_GRIP.awardTo(player);
				player.getAttributes()
					.addTransientAttributeModifiers(rangeModifier.get());
				persistentData.putBoolean(EXTENDO_MARKER, true);
			}
		}

		if (holdingDualExtendo != wasHoldingDualExtendo) {
			if (!holdingDualExtendo) {
				player.getAttributes()
					.removeAttributeModifiers(doubleRangeModifier.get());
				persistentData.remove(DUAL_EXTENDO_MARKER);
			} else {
				AllAdvancements.EXTENDO_GRIP_DUAL.awardTo(player);
				player.getAttributes()
					.addTransientAttributeModifiers(doubleRangeModifier.get());
				persistentData.putBoolean(DUAL_EXTENDO_MARKER, true);
			}
		}

	}

	@SubscribeEvent
	public static void addReachToJoiningPlayersHoldingExtendo(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		CompoundTag persistentData = player.getPersistentData();

		if (persistentData.contains(DUAL_EXTENDO_MARKER))
			player.getAttributes()
				.addTransientAttributeModifiers(doubleRangeModifier.get());
		else if (persistentData.contains(EXTENDO_MARKER))
			player.getAttributes()
				.addTransientAttributeModifiers(rangeModifier.get());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void consumeDurabilityOnBlockBreak(BreakBlockEvent event) {
		findAndDamageExtendoGrip(event.getPlayer());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void consumeDurabilityOnPlace(EntityPlaceEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player)
			findAndDamageExtendoGrip((Player) entity);
	}

//	@SubscribeEvent(priority = EventPriority.LOWEST)
//	public static void consumeDurabilityOnPlace(PlayerInteractEvent event) {
//		findAndDamageExtendoGrip(event.getPlayer());
//	}

	private static void findAndDamageExtendoGrip(Player player) {
		if (player == null)
			return;
		if (player.level().isClientSide())
			return;
		EquipmentSlot equipmentSlot = EquipmentSlot.MAINHAND;
		ItemStack extendo = player.getMainHandItem();
		if (!AllItems.EXTENDO_GRIP.isIn(extendo)) {
			extendo = player.getOffhandItem();
			equipmentSlot = EquipmentSlot.OFFHAND;
		}
		if (!AllItems.EXTENDO_GRIP.isIn(extendo))
			return;
		if (!BacktankUtil.canAbsorbDamage(player, maxUses()))
			extendo.hurtAndBreak(1, player, equipmentSlot);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return BacktankUtil.isBarVisible(stack, maxUses());
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return BacktankUtil.getBarWidth(stack, maxUses());
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return BacktankUtil.getBarColor(stack, maxUses());
	}

	private static int maxUses() {
		return AllConfigs.server().equipment.maxExtendoGripActions.get();
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
		return true;
	}

	@SubscribeEvent
	public static void bufferLivingAttackEvent(LivingIncomingDamageEvent event) {
		// Workaround for removed patch to get the attacking entity.
		lastActiveDamageSource = event.getSource();

		DamageSource source = event.getSource();
		if (source == null)
			return;
		Entity trueSource = source.getEntity();
		if (trueSource instanceof Player)
			findAndDamageExtendoGrip((Player) trueSource);
	}

	@SubscribeEvent
	public static void attacksByExtendoGripHaveMoreKnockback(LivingKnockBackEvent event) {
		if (lastActiveDamageSource == null)
			return;
		Entity entity = lastActiveDamageSource.getDirectEntity();
		lastActiveDamageSource = null;
		if (!(entity instanceof Player player))
			return;
		if (!isHoldingExtendoGrip(player))
			return;
		event.setStrength(event.getStrength() + 2);
	}

	public static boolean isHoldingExtendoGrip(Player player) {
		boolean inOff = AllItems.EXTENDO_GRIP.isIn(player.getOffhandItem());
		boolean inMain = AllItems.EXTENDO_GRIP.isIn(player.getMainHandItem());
		boolean holdingGrip = inOff || inMain;
		return holdingGrip;
	}

	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		ExtendoGripClient.initializeClient(this, consumer);
	}

}
