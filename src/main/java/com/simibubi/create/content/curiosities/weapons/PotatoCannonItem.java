package com.simibubi.create.content.curiosities.weapons;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.curiosities.armor.BackTankUtil;
import com.simibubi.create.content.curiosities.zapper.ShootableGadgetItemMethods;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotatoCannonItem extends ShootableItem {

	public static ItemStack CLIENT_CURRENT_AMMO = ItemStack.EMPTY;
	public static final int MAX_DAMAGE = 100;

	public PotatoCannonItem(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	@Override
	public boolean canPlayerBreakBlockWhileHolding(BlockState p_195938_1_, World p_195938_2_, BlockPos p_195938_3_,
		PlayerEntity p_195938_4_) {
		return false;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		return onItemRightClick(context.getWorld(), context.getPlayer(), context.getHand()).getType();
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
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

	private int maxUses() {
		return AllConfigs.SERVER.curiosities.maxPotatoCannonShots.get();
	}

	@Override
	public boolean isDamageable() {
		return true;
	}

	public boolean isCannon(ItemStack stack) {
		return stack.getItem() instanceof PotatoCannonItem;
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return MAX_DAMAGE;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		return findAmmoInInventory(world, player, stack).map(itemStack -> {

			if (ShootableGadgetItemMethods.shouldSwap(player, stack, hand, this::isCannon))
				return ActionResult.fail(stack);

			if (world.isRemote) {
				CreateClient.POTATO_CANNON_RENDER_HANDLER.dontAnimateItem(hand);
				return ActionResult.success(stack);
			}

			Vector3d barrelPos = ShootableGadgetItemMethods.getGunBarrelVec(player, hand == Hand.MAIN_HAND,
				new Vector3d(.75f, -0.3f, 1.5f));
			Vector3d correction =
				ShootableGadgetItemMethods.getGunBarrelVec(player, hand == Hand.MAIN_HAND, new Vector3d(-.05f, 0, 0))
					.subtract(player.getPositionVec()
						.add(0, player.getEyeHeight(), 0));

			PotatoCannonProjectileTypes projectileType = PotatoCannonProjectileTypes.getProjectileTypeOf(itemStack)
				.orElse(PotatoCannonProjectileTypes.FALLBACK);
			Vector3d lookVec = player.getLookVec();
			Vector3d motion = lookVec.add(correction).normalize().scale(projectileType.getVelocityMultiplier());

			float soundPitch = projectileType.getSoundPitch() + (Create.RANDOM.nextFloat() - .5f) / 4f;

			boolean spray = projectileType.getSplit() > 1;
			Vector3d sprayBase = VecHelper.rotate(new Vector3d(0,0.1,0),
					360*Create.RANDOM.nextFloat(), Axis.Z);
			float sprayChange = 360f / projectileType.getSplit();

			for (int i = 0; i < projectileType.getSplit(); i++) {
				PotatoProjectileEntity projectile = AllEntityTypes.POTATO_PROJECTILE.create(world);
				projectile.setItem(itemStack);

				Vector3d splitMotion = motion;
				if (spray) {
					float imperfection = 40*(Create.RANDOM.nextFloat() - 0.5f);
					Vector3d sprayOffset = VecHelper.rotate(sprayBase, i * sprayChange + imperfection, Axis.Z);
					splitMotion = splitMotion.add(VecHelper.lookAt(sprayOffset, motion));
				}

				projectile.setPosition(barrelPos.x, barrelPos.y, barrelPos.z);
				projectile.setMotion(splitMotion);
				projectile.setShooter(player);
				world.addEntity(projectile);
			}

			if (!player.isCreative()) {
				itemStack.shrink(1);
				if (itemStack.isEmpty())
					player.inventory.deleteStack(itemStack);
			}

			if (!BackTankUtil.canAbsorbDamage(player, maxUses()))
				stack.damageItem(1, player, p -> p.sendBreakAnimation(hand));

			Integer cooldown =
				findAmmoInInventory(world, player, stack).flatMap(PotatoCannonProjectileTypes::getProjectileTypeOf)
					.map(PotatoCannonProjectileTypes::getReloadTicks)
					.orElse(10);

			ShootableGadgetItemMethods.applyCooldown(player, stack, hand, this::isCannon, cooldown);
			ShootableGadgetItemMethods.sendPackets(player,
				b -> new PotatoCannonPacket(barrelPos, lookVec.normalize(), itemStack, hand, soundPitch, b));
			return ActionResult.success(stack);
		})
			.orElse(ActionResult.pass(stack));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || newStack.getItem() != oldStack.getItem();
	}

	private Optional<ItemStack> findAmmoInInventory(World world, PlayerEntity player, ItemStack held) {
		ItemStack findAmmo = player.findAmmo(held);
		return PotatoCannonProjectileTypes.getProjectileTypeOf(findAmmo)
			.map($ -> findAmmo);
	}

	@OnlyIn(Dist.CLIENT)
	public static Optional<ItemStack> getAmmoforPreview(ItemStack cannon) {
		if (AnimationTickHolder.getTicks() % 3 != 0)
			return Optional.of(CLIENT_CURRENT_AMMO)
				.filter(stack -> !stack.isEmpty());

		ClientPlayerEntity player = Minecraft.getInstance().player;
		CLIENT_CURRENT_AMMO = ItemStack.EMPTY;
		if (player == null)
			return Optional.empty();
		ItemStack findAmmo = player.findAmmo(cannon);
		Optional<ItemStack> found = PotatoCannonProjectileTypes.getProjectileTypeOf(findAmmo)
			.map($ -> findAmmo);
		found.ifPresent(stack -> CLIENT_CURRENT_AMMO = stack);
		return found;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		getAmmoforPreview(stack).ifPresent(ammo -> {
			tooltip.add(new StringTextComponent(""));
			tooltip.add(new TranslationTextComponent(ammo.getTranslationKey()).append(new StringTextComponent(":"))
				.formatted(TextFormatting.GRAY));
			PotatoCannonProjectileTypes type = PotatoCannonProjectileTypes.getProjectileTypeOf(ammo)
				.get();
			StringTextComponent spacing = new StringTextComponent(" ");
			TextFormatting darkGreen = TextFormatting.DARK_GREEN;
			tooltip.add(spacing.copy()
				.append(new StringTextComponent(type.getDamage() + " Attack Damage").formatted(darkGreen)));
			tooltip.add(spacing.copy()
				.append(new StringTextComponent(type.getReloadTicks() + " Reload Ticks").formatted(darkGreen)));
			tooltip.add(spacing.copy()
				.append(new StringTextComponent(type.getKnockback() + " Knockback").formatted(darkGreen)));
		});
		super.addInformation(stack, world, tooltip, flag);
	}

	@Override
	public Predicate<ItemStack> getInventoryAmmoPredicate() {
		return stack -> PotatoCannonProjectileTypes.getProjectileTypeOf(stack)
			.isPresent();
	}

	@Override
	public int getItemEnchantability() {
		return 1;
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		return true;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.NONE;
	}

	@Override
	public int getRange() {
		return 15;
	}

}
