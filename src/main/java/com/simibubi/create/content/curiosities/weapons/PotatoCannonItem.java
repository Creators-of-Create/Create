package com.simibubi.create.content.curiosities.weapons;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.curiosities.armor.BackTankUtil;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.ShootableItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

public class PotatoCannonItem extends ShootableItem {

	public static ItemStack CLIENT_CURRENT_AMMO = ItemStack.EMPTY;
	public static final int MAX_DAMAGE = 100;

	public static int PREV_SHOT = 0;

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

	@Override
	public int getMaxDamage(ItemStack stack) {
		return MAX_DAMAGE;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote)
			return ActionResult.pass(stack);

		findAmmoInInventory(world, player, stack).ifPresent(itemStack -> {
			PotatoProjectileEntity projectile = AllEntityTypes.POTATO_PROJECTILE.create(world);
			Vector3d offset = VecHelper.rotate(player.getLookVec()
				.scale(1.25f), (hand == Hand.MAIN_HAND) == (player.getPrimaryHand() == HandSide.RIGHT) ? -25 : 25,
				Axis.Y);
			Vector3d vec = player.getBoundingBox()
				.getCenter()
				.add(0, player.getBoundingBox()
					.getYSize() / 5f, 0)
				.add(offset);

			projectile.setPosition(vec.x, vec.y, vec.z);
			projectile.setMotion(player.getLookVec()
				.scale(1.75f));
			projectile.setItem(itemStack);
			projectile.setShooter(player);
			world.addEntity(projectile);
			PotatoProjectileEntity.playLaunchSound(world, player.getPositionVec(), projectile.getProjectileType()
				.getSoundPitch());
			
			if (player instanceof ServerPlayerEntity)
				AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
					new PotatoCannonPacket());

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
			player.getCooldownTracker()
				.setCooldown(this, cooldown);
		});

		return ActionResult.pass(stack);
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
	public int getRange() {
		return 15;
	}

	// FIXME Temporary; use zappers way of animating
	public static void clientTick() {
		if (PREV_SHOT > 0)
			PREV_SHOT--;
	}

}
