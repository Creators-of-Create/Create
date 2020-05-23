package com.simibubi.create.content.curiosities.tools;

import com.simibubi.create.content.curiosities.tools.SandPaperItemRenderer.SandPaperModel;
import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.IHaveCustomItemModel;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.model.IBakedModel;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;

public class SandPaperItem extends Item implements IHaveCustomItemModel {

	public SandPaperItem(Properties properties) {
		super(properties.maxDamage(8));
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.EAT;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		return ActionResultType.PASS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		ActionResult<ItemStack> FAIL = new ActionResult<>(ActionResultType.FAIL, itemstack);

		if (itemstack.getOrCreateTag().contains("Polishing")) {
			playerIn.setActiveHand(handIn);
			return new ActionResult<>(ActionResultType.PASS, itemstack);
		}

		Hand otherHand = handIn == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
		ItemStack itemInOtherHand = playerIn.getHeldItem(otherHand);
		if (SandPaperPolishingRecipe.canPolish(worldIn, itemInOtherHand)) {
			ItemStack item = itemInOtherHand.copy();
			ItemStack toPolish = item.split(1);
			playerIn.setActiveHand(handIn);
			itemstack.getOrCreateTag().put("Polishing", toPolish.serializeNBT());
			playerIn.setHeldItem(otherHand, item);
			return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
		}

		RayTraceResult raytraceresult = rayTrace(worldIn, playerIn, RayTraceContext.FluidMode.NONE);
		if (!(raytraceresult instanceof BlockRayTraceResult))
			return FAIL;
		BlockRayTraceResult ray = (BlockRayTraceResult) raytraceresult;
		Vec3d hitVec = ray.getHitVec();
		if (hitVec == null)
			return FAIL;

		AxisAlignedBB bb = new AxisAlignedBB(hitVec, hitVec).grow(1f);
		ItemEntity pickUp = null;
		for (ItemEntity itemEntity : worldIn.getEntitiesWithinAABB(ItemEntity.class, bb)) {
			if (itemEntity.getPositionVec().distanceTo(playerIn.getPositionVec()) > 3)
				continue;
			ItemStack stack = itemEntity.getItem();
			if (!SandPaperPolishingRecipe.canPolish(worldIn, stack))
				continue;
			pickUp = itemEntity;
			break;
		}

		if (pickUp == null)
			return FAIL;

		ItemStack item = pickUp.getItem().copy();
		ItemStack toPolish = item.split(1);

		playerIn.setActiveHand(handIn);

		if (!worldIn.isRemote) {
			itemstack.getOrCreateTag().put("Polishing", toPolish.serializeNBT());
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
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
		if (!(entityLiving instanceof PlayerEntity))
			return stack;
		PlayerEntity player = (PlayerEntity) entityLiving;
		CompoundNBT tag = stack.getOrCreateTag();
		if (tag.contains("Polishing")) {
			ItemStack toPolish = ItemStack.read(tag.getCompound("Polishing"));
			ItemStack polished = SandPaperPolishingRecipe.applyPolish(worldIn, entityLiving.getPositionVec(), toPolish,
					stack);

			if (worldIn.isRemote) {
				spawnParticles(entityLiving.getEyePosition(1).add(entityLiving.getLookVec().scale(.5f)), toPolish,
						worldIn);
				return stack;
			}

			if (!polished.isEmpty()) {
				if (player instanceof FakePlayer) {
					player.dropItem(polished, false, false);
				} else {
					player.inventory.placeItemBackInInventory(worldIn, polished);
				}
			}
			tag.remove("Polishing");
			stack.damageItem(1, entityLiving, p -> p.sendBreakAnimation(p.getActiveHand()));
		}

		return stack;
	}

	public static void spawnParticles(Vec3d location, ItemStack polishedStack, World world) {
		for (int i = 0; i < 20; i++) {
			Vec3d motion = VecHelper.offsetRandomly(Vec3d.ZERO, world.rand, 1 / 8f);
			world.addParticle(new ItemParticleData(ParticleTypes.ITEM, polishedStack), location.x, location.y,
					location.z, motion.x, motion.y, motion.z);
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		if (!(entityLiving instanceof PlayerEntity))
			return;
		PlayerEntity player = (PlayerEntity) entityLiving;
		CompoundNBT tag = stack.getOrCreateTag();
		if (tag.contains("Polishing")) {
			ItemStack toPolish = ItemStack.read(tag.getCompound("Polishing"));
			player.inventory.placeItemBackInInventory(worldIn, toPolish);
			tag.remove("Polishing");
		}
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public int getItemEnchantability() {
		return 5;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public CustomRenderedItemModel createModel(IBakedModel original) {
		return new SandPaperModel(original);
	}

}
