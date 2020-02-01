package com.simibubi.create.modules.contraptions.components.mixer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.AllRecipes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.modules.contraptions.processing.BasinOperatingTileEntity;
import com.simibubi.create.modules.contraptions.processing.BasinTileEntity.BasinInventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.IItemHandler;

public class MechanicalMixerTileEntity extends BasinOperatingTileEntity {

	private static Object shapelessOrMixingRecipesKey = new Object();

	public int runningTicks;
	public int processingTicks;
	public boolean running;

	public int minIngredients;
	public int currentValue;
	public int lastModified;

	public MechanicalMixerTileEntity() {
		super(AllTileEntities.MECHANICAL_MIXER.type);
		minIngredients = currentValue = 1;
		lastModified = -1;
		processingTicks = -1;
	}

	public float getRenderedHeadOffset(float partialTicks) {
		int localTick = 0;
		float offset = 0;
		if (running) {
			if (runningTicks < 20) {
				localTick = runningTicks;
				float num = (localTick + partialTicks) / 20f;
				num = ((2 - MathHelper.cos((float) (num * Math.PI))) / 2);
				offset = num - .5f;
			} else if (runningTicks <= 20) {
				offset = 1;
			} else if (runningTicks > 20) {
				localTick = 40 - runningTicks;
				float num = (localTick - partialTicks) / 20f;
				num = ((2 - MathHelper.cos((float) (num * Math.PI))) / 2);
				offset = num - .5f;
			}
		}
		return offset + 7 / 16f;
	}

	public float getRenderedHeadRotationSpeed(float partialTicks) {
		float speed = getSpeed();
		if (running) {
			if (runningTicks < 15) {
				return speed;
			}
			if (runningTicks <= 20) {
				return speed * 2;
			}
			if (runningTicks > 20) {
				return speed;
			}
		}
		return speed / 2;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos).expand(0, -1.5, 0);
	}

	@Override
	public void read(CompoundNBT compound) {
		running = compound.getBoolean("Running");
		runningTicks = compound.getInt("Ticks");
		currentValue = minIngredients = compound.getInt("MinIngredients");
		super.read(compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("Running", running);
		compound.putInt("Ticks", runningTicks);
		compound.putInt("MinIngredients", minIngredients);
		return super.write(compound);
	}

	public void setMinIngredientsLazily(int minIngredients) {
		this.currentValue = MathHelper.clamp(minIngredients, 1, 9);
		if (currentValue == this.minIngredients)
			return;
		this.lastModified = 0;
	}

	@Override
	public void tick() {

		if (world.isRemote && lastModified != -1) {
			if (lastModified++ > 10) {
				lastModified = -1;
				AllPackets.channel.sendToServer(new ConfigureMixerPacket(pos, currentValue));
			}
		}

		super.tick();

		if (runningTicks >= 40) {
			running = false;
			runningTicks = 0;
			return;
		}

		float speed = Math.abs(getSpeed());
		if (running) {
			if (world.isRemote && runningTicks == 20)
				renderParticles();

			if (!world.isRemote && runningTicks == 20) {
				if (processingTicks < 0) {
					processingTicks = MathHelper.clamp((MathHelper.log2((int) (512 / speed))) * 15 + 1, 1, 512);
				} else {
					processingTicks--;
					if (processingTicks == 0) {
						runningTicks++;
						processingTicks = -1;
						applyBasinRecipe();
						sendData();
					}
				}
			}

			if (runningTicks != 20)
				runningTicks++;
		}

	}

	public void renderParticles() {
		IItemHandler itemHandler = basinInv.orElse(null);
		if (itemHandler != null) {
			BasinInventory inv = (BasinInventory) itemHandler;

			for (int slot = 0; slot < inv.getInputHandler().getSlots(); slot++) {
				ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
				if (stackInSlot.isEmpty())
					continue;

				ItemParticleData data = new ItemParticleData(ParticleTypes.ITEM, stackInSlot);
				float angle = world.rand.nextFloat() * 360;
				Vec3d offset = new Vec3d(0, 0, 0.25f);
				offset = VecHelper.rotate(offset, angle, Axis.Y);
				Vec3d target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Axis.Y).add(0, .25f, 0);

				Vec3d center = offset.add(VecHelper.getCenterOf(pos));
				target = VecHelper.offsetRandomly(target.subtract(offset), world.rand, 1 / 128f);
				world.addParticle(data, center.x, center.y - 2, center.z, target.x, target.y, target.z);
			}
		}
	}

	@Override
	protected <C extends IInventory> boolean matchStaticFilters(IRecipe<C> r) {
		return (r.getSerializer() == IRecipeSerializer.CRAFTING_SHAPELESS || r.getType() == AllRecipes.MIXING.type)
				&& !MechanicalPressTileEntity.canCompress(r.getIngredients());
	}

	@Override
	protected <C extends IInventory> boolean matchBasinRecipe(IRecipe<C> recipe) {
		if (recipe == null)
			return false;
		if (recipe.getIngredients().size() < minIngredients)
			return false;

		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		if (!ingredients.stream().allMatch(Ingredient::isSimple))
			return false;

		List<ItemStack> remaining = new ArrayList<>();
		inputs.forEach(stack -> remaining.add(stack.copy()));

		// sort by leniency
		List<Ingredient> sortedIngredients = new LinkedList<>(ingredients);
		sortedIngredients.sort((i1, i2) -> i1.getMatchingStacks().length - i2.getMatchingStacks().length);
		Ingredients: for (Ingredient ingredient : sortedIngredients) {
			for (ItemStack stack : remaining) {
				if (stack.isEmpty())
					continue;
				if (ingredient.test(stack)) {
					stack.shrink(1);
					continue Ingredients;
				}
			}
			return false;
		}
		return true;
	}

	@Override
	public void startProcessingBasin() {
		if (running)
			return;
		super.startProcessingBasin();
		running = true;
		runningTicks = 0;
	}

	@Override
	public boolean continueWithPreviousRecipe() {
		runningTicks = 20;
		return true;
	}

	@Override
	protected void basinRemoved() {
		super.basinRemoved();
		if (running) {
			runningTicks = 40;
			running = false;
		}
	}

	@Override
	protected Object getRecipeCacheKey() {
		return shapelessOrMixingRecipesKey;
	}

}
