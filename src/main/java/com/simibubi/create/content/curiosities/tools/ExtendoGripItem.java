package com.simibubi.create.content.curiosities.tools;

import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.curiosities.armor.BackTankUtil;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.LazyValue;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import net.minecraft.item.Item.Properties;

@EventBusSubscriber
public class ExtendoGripItem extends Item {
	private static DamageSource lastActiveDamageSource;

	public static final int MAX_DAMAGE = 200;

	public static final AttributeModifier singleRangeAttributeModifier =
		new AttributeModifier(UUID.fromString("7f7dbdb2-0d0d-458a-aa40-ac7633691f66"), "Range modifier", 3,
			AttributeModifier.Operation.ADDITION);
	public static final AttributeModifier doubleRangeAttributeModifier =
		new AttributeModifier(UUID.fromString("8f7dbdb2-0d0d-458a-aa40-ac7633691f66"), "Range modifier", 5,
			AttributeModifier.Operation.ADDITION);

	static LazyValue<Multimap<Attribute, AttributeModifier>> rangeModifier = new LazyValue<>(() ->
	// Holding an ExtendoGrip
	ImmutableMultimap.of(ForgeMod.REACH_DISTANCE.get(), singleRangeAttributeModifier));

	static LazyValue<Multimap<Attribute, AttributeModifier>> doubleRangeModifier = new LazyValue<>(() ->
	// Holding two ExtendoGrips o.O
	ImmutableMultimap.of(ForgeMod.REACH_DISTANCE.get(), doubleRangeAttributeModifier));

	public ExtendoGripItem(Properties properties) {
		super(properties.stacksTo(1)
			.rarity(Rarity.UNCOMMON));
	}

	public static final String EXTENDO_MARKER = "createExtendo";
	public static final String DUAL_EXTENDO_MARKER = "createDualExtendo";

	@SubscribeEvent
	public static void holdingExtendoGripIncreasesRange(LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof PlayerEntity))
			return;

		PlayerEntity player = (PlayerEntity) event.getEntityLiving();

		CompoundNBT persistentData = player.getPersistentData();
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
				if (player instanceof ServerPlayerEntity)
					AllTriggers.EXTENDO.trigger((ServerPlayerEntity) player);
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
				if (player instanceof ServerPlayerEntity)
					AllTriggers.GIGA_EXTENDO.trigger((ServerPlayerEntity) player);
				player.getAttributes()
					.addTransientAttributeModifiers(doubleRangeModifier.get());
				persistentData.putBoolean(DUAL_EXTENDO_MARKER, true);
			}
		}

	}

	@SubscribeEvent
	public static void addReachToJoiningPlayersHoldingExtendo(PlayerEvent.PlayerLoggedInEvent event) {
		PlayerEntity player = event.getPlayer();
		CompoundNBT persistentData = player.getPersistentData();

		if (persistentData.contains(DUAL_EXTENDO_MARKER))
			player.getAttributes()
				.addTransientAttributeModifiers(doubleRangeModifier.get());
		else if (persistentData.contains(EXTENDO_MARKER))
			player.getAttributes()
				.addTransientAttributeModifiers(rangeModifier.get());
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void dontMissEntitiesWhenYouHaveHighReachDistance(ClickInputEvent event) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		if (mc.level == null || player == null)
			return;
		if (!isHoldingExtendoGrip(player))
			return;
		if (mc.hitResult instanceof BlockRayTraceResult && mc.hitResult.getType() != Type.MISS)
			return;

		// Modified version of GameRenderer#getMouseOver
		double d0 = player.getAttribute(ForgeMod.REACH_DISTANCE.get())
			.getValue();
		if (!player.isCreative())
			d0 -= 0.5f;
		Vector3d Vector3d = player.getEyePosition(AnimationTickHolder.getPartialTicks());
		Vector3d Vector3d1 = player.getViewVector(1.0F);
		Vector3d Vector3d2 = Vector3d.add(Vector3d1.x * d0, Vector3d1.y * d0, Vector3d1.z * d0);
		AxisAlignedBB axisalignedbb = player.getBoundingBox()
			.expandTowards(Vector3d1.scale(d0))
			.inflate(1.0D, 1.0D, 1.0D);
		EntityRayTraceResult entityraytraceresult =
			ProjectileHelper.getEntityHitResult(player, Vector3d, Vector3d2, axisalignedbb, (e) -> {
				return !e.isSpectator() && e.isPickable();
			}, d0 * d0);
		if (entityraytraceresult != null) {
			Entity entity1 = entityraytraceresult.getEntity();
			Vector3d Vector3d3 = entityraytraceresult.getLocation();
			double d2 = Vector3d.distanceToSqr(Vector3d3);
			if (d2 < d0 * d0 || mc.hitResult == null || mc.hitResult.getType() == Type.MISS) {
				mc.hitResult = entityraytraceresult;
				if (entity1 instanceof LivingEntity || entity1 instanceof ItemFrameEntity)
					mc.crosshairPickEntity = entity1;
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void consumeDurabilityOnBlockBreak(BreakEvent event) {
		findAndDamageExtendoGrip(event.getPlayer());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void consumeDurabilityOnPlace(EntityPlaceEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof PlayerEntity)
			findAndDamageExtendoGrip((PlayerEntity) entity);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void consumeDurabilityOnPlace(PlayerInteractEvent event) {
//		findAndDamageExtendoGrip(event.getPlayer());
	}

	private static void findAndDamageExtendoGrip(PlayerEntity player) {
		if (player == null)
			return;
		if (player.level.isClientSide)
			return;
		Hand hand = Hand.MAIN_HAND;
		ItemStack extendo = player.getMainHandItem();
		if (!AllItems.EXTENDO_GRIP.isIn(extendo)) {
			extendo = player.getOffhandItem();
			hand = Hand.OFF_HAND;
		}
		if (!AllItems.EXTENDO_GRIP.isIn(extendo))
			return;
		final Hand h = hand;
		if (!BackTankUtil.canAbsorbDamage(player, maxUses()))
			extendo.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(h));
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return BackTankUtil.getRGBDurabilityForDisplay(stack, maxUses());
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return BackTankUtil.getDurabilityForDisplay(stack, maxUses());
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return BackTankUtil.showDurabilityBar(stack, maxUses());
	}

	private static int maxUses() {
		return AllConfigs.SERVER.curiosities.maxExtendoGripActions.get();
	}

	@Override
	public boolean canBeDepleted() {
		return true;
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return MAX_DAMAGE;
	}

	@SubscribeEvent
	public static void bufferLivingAttackEvent(LivingAttackEvent event) {
		// Workaround for removed patch to get the attacking entity.
		lastActiveDamageSource = event.getSource();
		
		DamageSource source = event.getSource();
		if (source == null)
			return;
		Entity trueSource = source.getEntity();
		if (trueSource instanceof PlayerEntity)
			findAndDamageExtendoGrip((PlayerEntity) trueSource);
	}

	@SubscribeEvent
	public static void attacksByExtendoGripHaveMoreKnockback(LivingKnockBackEvent event) {
		if (lastActiveDamageSource == null)
			return;
		Entity entity = lastActiveDamageSource.getDirectEntity();
		if (!(entity instanceof PlayerEntity))
			return;
		PlayerEntity player = (PlayerEntity) entity;
		if (!isHoldingExtendoGrip(player))
			return;
		event.setStrength(event.getStrength() + 2);
	}

	private static boolean isUncaughtClientInteraction(Entity entity, Entity target) {
		// Server ignores entity interaction further than 6m
		if (entity.distanceToSqr(target) < 36)
			return false;
		if (!entity.level.isClientSide)
			return false;
		if (!(entity instanceof PlayerEntity))
			return false;
		return true;
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void notifyServerOfLongRangeAttacks(AttackEntityEvent event) {
		Entity entity = event.getEntity();
		Entity target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		PlayerEntity player = (PlayerEntity) entity;
		if (isHoldingExtendoGrip(player))
			AllPackets.channel.sendToServer(new ExtendoGripInteractionPacket(target));
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void notifyServerOfLongRangeInteractions(PlayerInteractEvent.EntityInteract event) {
		Entity entity = event.getEntity();
		Entity target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		PlayerEntity player = (PlayerEntity) entity;
		if (isHoldingExtendoGrip(player))
			AllPackets.channel.sendToServer(new ExtendoGripInteractionPacket(target, event.getHand()));
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void notifyServerOfLongRangeSpecificInteractions(PlayerInteractEvent.EntityInteractSpecific event) {
		Entity entity = event.getEntity();
		Entity target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		PlayerEntity player = (PlayerEntity) entity;
		if (isHoldingExtendoGrip(player))
			AllPackets.channel
				.sendToServer(new ExtendoGripInteractionPacket(target, event.getHand(), event.getLocalPos()));
	}

	public static boolean isHoldingExtendoGrip(PlayerEntity player) {
		boolean inOff = AllItems.EXTENDO_GRIP.isIn(player.getOffhandItem());
		boolean inMain = AllItems.EXTENDO_GRIP.isIn(player.getMainHandItem());
		boolean holdingGrip = inOff || inMain;
		return holdingGrip;
	}

}
