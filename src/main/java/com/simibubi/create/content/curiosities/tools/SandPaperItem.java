package com.simibubi.create.content.curiosities.tools;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.foundation.utility.VecHelper;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SandPaperItem extends Item {

	public SandPaperItem(Properties properties) {
		super(properties.durability(8));
	}

	@Override
	public UseAction getUseAnimation(ItemStack stack) {
		return UseAction.EAT;
	}

	@Override
	public ActionResultType useOn(ItemUseContext context) {
		return ActionResultType.PASS;
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		ActionResult<ItemStack> FAIL = new ActionResult<>(ActionResultType.FAIL, itemstack);

		if (itemstack.getOrCreateTag()
			.contains("Polishing")) {
			playerIn.startUsingItem(handIn);
			return new ActionResult<>(ActionResultType.PASS, itemstack);
		}

		Hand otherHand = handIn == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
		ItemStack itemInOtherHand = playerIn.getItemInHand(otherHand);
		if (SandPaperPolishingRecipe.canPolish(worldIn, itemInOtherHand)) {
			ItemStack item = itemInOtherHand.copy();
			ItemStack toPolish = item.split(1);
			playerIn.startUsingItem(handIn);
			itemstack.getOrCreateTag()
				.put("Polishing", toPolish.serializeNBT());
			playerIn.setItemInHand(otherHand, item);
			return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
		}

		RayTraceResult raytraceresult = getPlayerPOVHitResult(worldIn, playerIn, RayTraceContext.FluidMode.NONE);
		if (!(raytraceresult instanceof BlockRayTraceResult))
			return FAIL;
		BlockRayTraceResult ray = (BlockRayTraceResult) raytraceresult;
		Vector3d hitVec = ray.getLocation();

		AxisAlignedBB bb = new AxisAlignedBB(hitVec, hitVec).inflate(1f);
		ItemEntity pickUp = null;
		for (ItemEntity itemEntity : worldIn.getEntitiesOfClass(ItemEntity.class, bb)) {
			if (!itemEntity.isAlive())
				continue;
			if (itemEntity.position()
				.distanceTo(playerIn.position()) > 3)
				continue;
			ItemStack stack = itemEntity.getItem();
			if (!SandPaperPolishingRecipe.canPolish(worldIn, stack))
				continue;
			pickUp = itemEntity;
			break;
		}

		if (pickUp == null)
			return FAIL;

		ItemStack item = pickUp.getItem()
			.copy();
		ItemStack toPolish = item.split(1);

		playerIn.startUsingItem(handIn);

		if (!worldIn.isClientSide) {
			itemstack.getOrCreateTag()
				.put("Polishing", toPolish.serializeNBT());
			if (item.isEmpty())
				pickUp.remove();
			else
				pickUp.setItem(item);
		}

		return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public int getItemEnchantability(ItemStack stack) {
		return 1;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, World worldIn, LivingEntity entityLiving) {
		if (!(entityLiving instanceof PlayerEntity))
			return stack;
		PlayerEntity player = (PlayerEntity) entityLiving;
		CompoundNBT tag = stack.getOrCreateTag();
		if (tag.contains("Polishing")) {
			ItemStack toPolish = ItemStack.of(tag.getCompound("Polishing"));
			ItemStack polished =
				SandPaperPolishingRecipe.applyPolish(worldIn, entityLiving.position(), toPolish, stack);

			if (worldIn.isClientSide) {
				spawnParticles(entityLiving.getEyePosition(1)
					.add(entityLiving.getLookAngle()
						.scale(.5f)),
					toPolish, worldIn);
				return stack;
			}

			if (!polished.isEmpty()) {
				if (player instanceof FakePlayer) {
					player.drop(polished, false, false);
				} else {
					player.inventory.placeItemBackInInventory(worldIn, polished);
				}
			}
			tag.remove("Polishing");
			stack.hurtAndBreak(1, entityLiving, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
		}

		return stack;
	}

	public static void spawnParticles(Vector3d location, ItemStack polishedStack, World world) {
		for (int i = 0; i < 20; i++) {
			Vector3d motion = VecHelper.offsetRandomly(Vector3d.ZERO, world.random, 1 / 8f);
			world.addParticle(new ItemParticleData(ParticleTypes.ITEM, polishedStack), location.x, location.y,
				location.z, motion.x, motion.y, motion.z);
		}
	}

	@Override
	public void releaseUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		if (!(entityLiving instanceof PlayerEntity))
			return;
		PlayerEntity player = (PlayerEntity) entityLiving;
		CompoundNBT tag = stack.getOrCreateTag();
		if (tag.contains("Polishing")) {
			ItemStack toPolish = ItemStack.of(tag.getCompound("Polishing"));
			player.inventory.placeItemBackInInventory(worldIn, toPolish);
			tag.remove("Polishing");
		}
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public int getEnchantmentValue() {
		return 5;
	}

}
