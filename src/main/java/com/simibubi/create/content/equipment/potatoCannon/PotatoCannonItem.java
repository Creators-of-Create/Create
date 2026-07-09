package com.simibubi.create.content.equipment.potatoCannon;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.equipment.zapper.ShootableGadgetItemMethods;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.GlobalRegistryAccess;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class PotatoCannonItem extends ProjectileWeaponItem {
	private static final Predicate<ItemStack> AMMO_PREDICATE = s ->
		PotatoCannonProjectileType.getTypeForItem(GlobalRegistryAccess.getOrThrow(), s.getItem()).isPresent();

	public PotatoCannonItem(Properties properties) {
		super(properties);
	}

	@Nullable
	public static Ammo getAmmo(Player player, ItemStack heldStack) {
		ItemStack ammoStack = player.getProjectile(heldStack);
		if (ammoStack.isEmpty()) {
			return null;
		}

		return PotatoCannonProjectileType.getTypeForItem(player.level().registryAccess(), ammoStack.getItem())
			.map(r -> new Ammo(ammoStack, r.value()))
			.orElse(null);
	}

	@Override
	protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
	}

	@Override
	protected void shoot(ServerLevel level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, List<ItemStack> projectileItems, float velocity, float inaccuracy, boolean isCrit, @Nullable LivingEntity target) {
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		return use(context.getLevel(), context.getPlayer(), context.getHand());
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack heldStack = player.getItemInHand(hand);
		if (ShootableGadgetItemMethods.shouldSwap(player, heldStack, hand, s -> s.getItem() instanceof PotatoCannonItem)) {
			return InteractionResult.FAIL;
		}

		Ammo ammo = getAmmo(player, heldStack);
		if (ammo == null) {
			return InteractionResult.PASS;
		}
		ItemStack ammoStack = ammo.stack();
		PotatoCannonProjectileType projectileType = ammo.type();

		if (level.isClientSide()) {
			PotatoCannonClient.dontAnimateItem(hand);
			return InteractionResult.SUCCESS.heldItemTransformedTo(heldStack);
		}

		Vec3 barrelPos = ShootableGadgetItemMethods.getGunBarrelVec(player, hand == InteractionHand.MAIN_HAND,
			new Vec3(.75f, -0.15f, 1.5f));
		Vec3 correction =
			ShootableGadgetItemMethods.getGunBarrelVec(player, hand == InteractionHand.MAIN_HAND, new Vec3(-.05f, 0, 0))
				.subtract(player.position()
					.add(0, player.getEyeHeight(), 0));

		Vec3 lookVec = player.getLookAngle();
		Vec3 motion = lookVec.add(correction)
			.normalize()
			.scale(2)
			.scale(projectileType.velocityMultiplier());

		float soundPitch = projectileType.soundPitch() + (level.getRandom().nextFloat() - .5f) / 4f;

		boolean spray = projectileType.split() > 1;
		Vec3 sprayBase = VecHelper.rotate(new Vec3(0, 0.1, 0), 360 * level.getRandom().nextFloat(), Axis.Z);
		float sprayChange = 360f / projectileType.split();

		ItemStack ammoStackCopy = ammoStack.copy();

		for (int i = 0; i < projectileType.split(); i++) {
			PotatoProjectileEntity projectile =
				AllEntityTypes.POTATO_PROJECTILE.get().create(level, EntitySpawnReason.TRIGGERED);
			if (projectile == null)
				continue;
			projectile.setItem(ammoStackCopy);
			projectile.setEnchantmentEffectsFromCannon(heldStack);

			Vec3 splitMotion = motion;
			if (spray) {
				float imperfection = 40 * (level.getRandom().nextFloat() - 0.5f);
				Vec3 sprayOffset = VecHelper.rotate(sprayBase, i * sprayChange + imperfection, Axis.Z);
				splitMotion = splitMotion.add(VecHelper.lookAt(sprayOffset, motion));
			}

			if (i != 0)
				projectile.recoveryChance = 0;

			projectile.setPos(barrelPos.x, barrelPos.y, barrelPos.z);
			projectile.setDeltaMovement(splitMotion);
			projectile.setOwner(player);
			level.addFreshEntity(projectile);
		}

		if (!player.isCreative()) {
			ammoStack.shrink(1);
			if (ammoStack.isEmpty())
				player.getInventory().removeItem(ammoStack);
		}

		if (!BacktankUtil.canAbsorbDamage(player, maxUses()))
			heldStack.hurtAndBreak(1, player, hand);

		ShootableGadgetItemMethods.applyCooldown(player, heldStack, hand, s -> s.getItem() instanceof PotatoCannonItem, projectileType.reloadTicks());
		ShootableGadgetItemMethods.sendPackets(player,
			b -> new PotatoCannonPacket(barrelPos, lookVec.normalize(), ammoStack, hand, soundPitch, b));
		return InteractionResult.SUCCESS.heldItemTransformedTo(heldStack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
		Consumer<Component> tooltip, TooltipFlag flag) {
		if (!PotatoCannonClient.addToTooltip(stack, context, tooltip))
			super.appendHoverText(stack, context, display, tooltip, flag);
	}

	@Override
	public boolean canDestroyBlock(ItemStack stack, BlockState state, Level world, BlockPos pos, LivingEntity entity) {
		return false;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || newStack.getItem() != oldStack.getItem();
	}

	@Override
	public Predicate<ItemStack> getAllSupportedProjectiles() {
		return AMMO_PREDICATE;
	}

	@Override
	public int getDefaultProjectileRange() {
		return 15;
	}

	@Override
	public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
		if (enchantment.is(Enchantments.INFINITY))
			return false;
		if (enchantment.is(Enchantments.LOOTING))
			return true;
		return super.supportsEnchantment(stack, enchantment);
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

	static int maxUses() {
		return AllConfigs.server().equipment.maxPotatoCannonShots.get();
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
		return true;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.NONE;
	}

	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		PotatoCannonClient.initializeClient(this, consumer);
	}

	public record Ammo(ItemStack stack, PotatoCannonProjectileType type) {
	}
}
