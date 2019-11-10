package com.simibubi.create.modules.contraptions.receivers;

import static com.simibubi.create.modules.contraptions.receivers.SawBlock.RUNNING;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.logistics.block.IHaveFilter;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SawTileEntity extends KineticTileEntity implements IHaveFilter {

	public ProcessingInventory inventory;
	private int recipeIndex;
	private ItemStack filter;

	public SawTileEntity() {
		super(AllTileEntities.SAW.type);
		inventory = new ProcessingInventory();
		inventory.remainingTime = -1;
		filter = ItemStack.EMPTY;
		recipeIndex = 0;
	}

	@Override
	public boolean hasFastRenderer() {
		return false;
	}

	@Override
	public void onSpeedChanged() {
		boolean shouldRun = Math.abs(getSpeed()) > 1 / 64f;
		boolean running = getBlockState().get(RUNNING);
		if (shouldRun != running)
			world.setBlockState(pos, getBlockState().with(RUNNING, shouldRun), 2 | 16);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		inventory.write(compound);
		compound.put("Filter", filter.write(new CompoundNBT()));
		compound.putInt("RecipeIndex", recipeIndex);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		inventory = ProcessingInventory.read(compound);
		recipeIndex = compound.getInt("RecipeIndex");
		filter = ItemStack.read(compound.getCompound("Filter"));
	}

	@Override
	public void tick() {
		super.tick();
		if (!canProcess())
			return;
		if (getSpeed() == 0)
			return;
		if (inventory.remainingTime == -1)
			return;

		float processingSpeed = MathHelper.clamp(Math.abs(getSpeed()) / 32, 1, 128);
		inventory.remainingTime -= processingSpeed;
		spawnParticles(inventory.getStackInSlot(0));

		if (world.isRemote)
			return;

		if (inventory.remainingTime < 20 && !inventory.appliedRecipe) {
			applyRecipe();
			inventory.appliedRecipe = true;
			sendData();
			return;
		}

		Vec3d outPos = VecHelper.getCenterOf(pos).add(getItemMovementVec().scale(.5f).add(0, .5, 0));
		Vec3d outMotion = getItemMovementVec().scale(.0625).add(0, .125, 0);

		if (inventory.remainingTime <= 0) {
			for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
				ItemStack stack = inventory.getStackInSlot(slot);
				if (stack.isEmpty())
					continue;
				ItemEntity entityIn = new ItemEntity(world, outPos.x, outPos.y, outPos.z, stack);
				entityIn.setMotion(outMotion);
				world.addEntity(entityIn);
			}
			inventory.clear();
			inventory.remainingTime = -1;
			sendData();
			return;
		}

		return;
	}

	protected void spawnParticles(ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return;

		IParticleData particleData = null;
		float speed = 1;
		if (stack.getItem() instanceof BlockItem)
			particleData = new BlockParticleData(ParticleTypes.BLOCK,
					((BlockItem) stack.getItem()).getBlock().getDefaultState());
		else {
			particleData = new ItemParticleData(ParticleTypes.ITEM, stack);
			speed = .125f;
		}

		Random r = world.rand;
		Vec3d vec = getItemMovementVec();
		Vec3d pos = VecHelper.getCenterOf(this.pos);
		float offset = inventory.recipeDuration != 0 ? (float) (inventory.remainingTime) / inventory.recipeDuration : 0;
		offset -= .5f;
		world.addParticle(particleData, pos.getX() + -vec.x * offset, pos.getY() + .45f, pos.getZ() + -vec.z * offset,
				vec.x * speed, r.nextFloat() * speed, vec.z * speed);
	}

	public Vec3d getItemMovementVec() {
		boolean alongX = !getBlockState().get(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
		int offset = getSpeed() < 0 ? -1 : 1;
		return new Vec3d(offset * (alongX ? 1 : 0), 0, offset * (alongX ? 0 : -1));
	}

	private void applyRecipe() {
		List<IRecipe<?>> recipes = getRecipes();
		if (recipes.isEmpty())
			return;
		if (recipeIndex >= recipes.size())
			recipeIndex = 0;

		IRecipe<?> recipe = recipes.get(recipeIndex);

		int rolls = inventory.getStackInSlot(0).getCount();
		inventory.clear();

		for (int roll = 0; roll < rolls; roll++) {
			List<ItemStack> results = new LinkedList<ItemStack>();
			if (recipe instanceof CuttingRecipe)
				results = ((CuttingRecipe) recipe).rollResults();
			else if (recipe instanceof StonecuttingRecipe)
				results.add(recipe.getRecipeOutput().copy());

			for (int i = 0; i < results.size(); i++) {
				ItemStack stack = results.get(i);

				for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
					stack = inventory.getItems().insertItem(slot, stack, false);

					if (stack.isEmpty())
						break;
				}
			}
		}

	}

	private List<IRecipe<?>> getRecipes() {
		List<IRecipe<?>> recipes = world.getRecipeManager().getRecipes().parallelStream()
				.filter(r -> r.getType() == IRecipeType.STONECUTTING || r.getType() == AllRecipes.Types.CUTTING)
				.filter(r -> filter.isEmpty() || ItemStack.areItemsEqual(filter, r.getRecipeOutput()))
				.filter(r -> !r.getIngredients().isEmpty()
						&& r.getIngredients().get(0).test(inventory.getStackInSlot(0)))
				.collect(Collectors.toList());
		return recipes;
	}

	public void insertItem(ItemEntity entity) {
		if (!canProcess())
			return;
		if (!inventory.isEmpty())
			return;
		if (world.isRemote)
			return;

		inventory.clear();
		inventory.setInventorySlotContents(0, entity.getItem().copy());

		List<IRecipe<?>> recipes = getRecipes();
		boolean valid = !recipes.isEmpty();
		int time = 100;

		if (recipes.isEmpty()) {
			inventory.remainingTime = inventory.recipeDuration = 10;
			inventory.appliedRecipe = false;
			entity.remove();
			sendData();
			return;
		}

		if (valid) {
			recipeIndex++;
			if (recipeIndex >= recipes.size())
				recipeIndex = 0;
		}

		IRecipe<?> recipe = recipes.get(recipeIndex);
		if (recipe instanceof CuttingRecipe) {
			time = ((CuttingRecipe) recipe).getProcessingDuration();
		}

		inventory.remainingTime = time * Math.max(1, (entity.getItem().getCount() / 5));
		inventory.recipeDuration = inventory.remainingTime;
		inventory.appliedRecipe = false;
		entity.remove();

		sendData();
	}

	protected boolean canProcess() {
		return getBlockState().get(SawBlock.FACING) == Direction.UP;
	}

	@Override
	public void setFilter(ItemStack stack) {
		filter = stack.copy();
		markDirty();
		sendData();
	}

	@Override
	public ItemStack getFilter() {
		return filter;
	}

}
