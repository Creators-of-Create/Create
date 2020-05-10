package com.simibubi.create.modules.curiosities.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.foundation.item.AbstractToolItem;
import com.simibubi.create.foundation.item.AllToolTypes;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class BlazingToolItem extends AbstractToolItem {

	// FIXME this does not need to be a TE
	static FurnaceTileEntity helperFurnace = new FurnaceTileEntity();

	public BlazingToolItem(float attackDamageIn, float attackSpeedIn, Properties builder, AllToolTypes... types) {
		super(attackDamageIn, attackSpeedIn, AllToolTiers.BLAZING, builder, types);
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos,
			LivingEntity entityLiving) {
		return shouldTakeDamage(worldIn, stack) ? super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving)
				: true;
	}

	@Override
	public int getBurnTime(ItemStack itemStack) {
		return itemStack.getMaxDamage() - itemStack.getDamage() + 1;
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		target.setFire(2);
		return shouldTakeDamage(attacker.world, stack) ? super.hitEntity(stack, target, attacker) : true;
	}

	static boolean shouldTakeDamage(World world, ItemStack stack) {
		return world.getDimension().getType() != DimensionType.THE_NETHER;
	}

	@Override
	public boolean modifiesDrops() {
		return true;
	}

	@Override
	public void modifyDrops(Collection<ItemStack> drops, IWorld world, BlockPos pos, ItemStack tool, BlockState state) {
		super.modifyDrops(drops, world, pos, tool, state);
		World worldIn = world.getWorld();
		int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool);
		if (state == null)
			enchantmentLevel = 0;
		List<ItemStack> smeltedStacks = smeltDrops(drops, worldIn, enchantmentLevel);
		drops.addAll(smeltedStacks);
	}

	public static List<ItemStack> smeltDrops(Collection<ItemStack> drops, World worldIn, int enchantmentLevel) {
		helperFurnace.setLocation(worldIn, BlockPos.ZERO);
		RecipeManager recipeManager = worldIn.getRecipeManager();
		List<ItemStack> smeltedStacks = new ArrayList<>();
		Iterator<ItemStack> dropper = drops.iterator();
		while (dropper.hasNext()) {
			ItemStack stack = dropper.next();
			helperFurnace.setInventorySlotContents(0, stack);
			Optional<FurnaceRecipe> smeltingRecipe =
				recipeManager.getRecipe(IRecipeType.SMELTING, helperFurnace, worldIn);
			if (!smeltingRecipe.isPresent())
				continue;
			dropper.remove();
			ItemStack out = smeltingRecipe.get().getRecipeOutput().copy();

			float modifier = 1;
			if (stack.getItem() instanceof BlockItem && !(out.getItem() instanceof BlockItem))
				modifier += worldIn.getRandom().nextFloat() * enchantmentLevel;

			out.setCount((int) (out.getCount() * modifier + .4f));
			smeltedStacks.addAll(ItemHelper.multipliedOutput(stack, out));
		}
		return smeltedStacks;
	}

	@Override
	public void spawnParticles(IWorld world, BlockPos pos, ItemStack tool, BlockState state) {
		if (!canHarvestBlock(tool, state))
			return;
		for (int i = 0; i < 10; i++) {
			Vec3d flamePos = VecHelper.offsetRandomly(VecHelper.getCenterOf(pos), world.getRandom(), .45f);
			Vec3d smokePos = VecHelper.offsetRandomly(VecHelper.getCenterOf(pos), world.getRandom(), .45f);
			world.addParticle(ParticleTypes.FLAME, flamePos.getX(), flamePos.getY(), flamePos.getZ(), 0, .01f, 0);
			world.addParticle(ParticleTypes.SMOKE, smokePos.getX(), smokePos.getY(), smokePos.getZ(), 0, .1f, 0);
		}
	}

}
