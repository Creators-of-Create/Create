package com.simibubi.create.foundation.fluid;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.Create;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidHelper {

	public static enum FluidExchange {
		ITEM_TO_TANK, TANK_TO_ITEM;
	}

	public static boolean isWater(Fluid fluid) {
		return convertToStill(fluid) == Fluids.WATER;
	}

	public static boolean isLava(Fluid fluid) {
		return convertToStill(fluid) == Fluids.LAVA;
	}
	
	public static boolean isSame(FluidStack fluidStack, FluidStack fluidStack2) {
		return fluidStack.getFluid() == fluidStack2.getFluid();
	}
	
	public static boolean isSame(FluidStack fluidStack, Fluid fluid) {
		return fluidStack.getFluid() == fluid;
	}

	@SuppressWarnings("deprecation")
	public static boolean isTag(Fluid fluid, TagKey<Fluid> tag) {
		return fluid.is(tag);
	}

	public static boolean isTag(FluidState fluid, TagKey<Fluid> tag) {
		return fluid.is(tag);
	}

	public static boolean isTag(FluidStack fluid, TagKey<Fluid> tag) {
		return isTag(fluid.getFluid(), tag);
	}

	public static SoundEvent getFillSound(FluidStack fluid) {
		SoundEvent soundevent = fluid.getFluid()
			.getFluidType()
			.getSound(fluid, SoundActions.BUCKET_FILL);
		if (soundevent == null)
			soundevent =
				FluidHelper.isTag(fluid, FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
		return soundevent;
	}

	public static SoundEvent getEmptySound(FluidStack fluid) {
		SoundEvent soundevent = fluid.getFluid()
			.getFluidType()
			.getSound(fluid, SoundActions.BUCKET_EMPTY);
		if (soundevent == null)
			soundevent =
				FluidHelper.isTag(fluid, FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
		return soundevent;
	}

	public static boolean hasBlockState(Fluid fluid) {
		BlockState blockState = fluid.defaultFluidState()
			.createLegacyBlock();
		return blockState != null && blockState != Blocks.AIR.defaultBlockState();
	}

	public static FluidStack copyStackWithAmount(FluidStack fs, int amount) {
		if (amount <= 0)
			return FluidStack.EMPTY;
		if (fs.isEmpty())
			return FluidStack.EMPTY;
		FluidStack copy = fs.copy();
		copy.setAmount(amount);
		return copy;
	}

	public static Fluid convertToFlowing(Fluid fluid) {
		if (fluid == Fluids.WATER)
			return Fluids.FLOWING_WATER;
		if (fluid == Fluids.LAVA)
			return Fluids.FLOWING_LAVA;
		if (fluid instanceof ForgeFlowingFluid)
			return ((ForgeFlowingFluid) fluid).getFlowing();
		return fluid;
	}

	public static Fluid convertToStill(Fluid fluid) {
		if (fluid == Fluids.FLOWING_WATER)
			return Fluids.WATER;
		if (fluid == Fluids.FLOWING_LAVA)
			return Fluids.LAVA;
		if (fluid instanceof ForgeFlowingFluid)
			return ((ForgeFlowingFluid) fluid).getSource();
		return fluid;
	}

	public static JsonElement serializeFluidStack(FluidStack stack) {
		JsonObject json = new JsonObject();
		json.addProperty("fluid", RegisteredObjects.getKeyOrThrow(stack.getFluid())
			.toString());
		json.addProperty("amount", stack.getAmount());
		if (stack.hasTag())
			json.addProperty("nbt", stack.getTag()
				.toString());
		return json;
	}

	public static FluidStack deserializeFluidStack(JsonObject json) {
		ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
		Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
		if (fluid == null)
			throw new JsonSyntaxException("Unknown fluid '" + id + "'");
		int amount = GsonHelper.getAsInt(json, "amount");
		FluidStack stack = new FluidStack(fluid, amount);

		if (!json.has("nbt"))
			return stack;

		try {
			JsonElement element = json.get("nbt");
			stack.setTag(TagParser.parseTag(
				element.isJsonObject() ? Create.GSON.toJson(element) : GsonHelper.convertToString(element, "nbt")));

		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}

		return stack;
	}

	public static boolean tryEmptyItemIntoBE(Level worldIn, Player player, InteractionHand handIn, ItemStack heldItem,
		SmartBlockEntity be) {
		if (!GenericItemEmptying.canItemBeEmptied(worldIn, heldItem))
			return false;

		Pair<FluidStack, ItemStack> emptyingResult = GenericItemEmptying.emptyItem(worldIn, heldItem, true);
		LazyOptional<IFluidHandler> capability = be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		IFluidHandler tank = capability.orElse(null);
		FluidStack fluidStack = emptyingResult.getFirst();

		if (tank == null || fluidStack.getAmount() != tank.fill(fluidStack, FluidAction.SIMULATE))
			return false;
		if (worldIn.isClientSide)
			return true;

		ItemStack copyOfHeld = heldItem.copy();
		emptyingResult = GenericItemEmptying.emptyItem(worldIn, copyOfHeld, false);
		tank.fill(fluidStack, FluidAction.EXECUTE);

		if (!player.isCreative() && !(be instanceof CreativeFluidTankBlockEntity)) {
			if (copyOfHeld.isEmpty())
				player.setItemInHand(handIn, emptyingResult.getSecond());
			else {
				player.setItemInHand(handIn, copyOfHeld);
				player.getInventory()
					.placeItemBackInInventory(emptyingResult.getSecond());
			}
		}
		return true;
	}

	public static boolean tryFillItemFromBE(Level world, Player player, InteractionHand handIn, ItemStack heldItem,
		SmartBlockEntity be) {
		if (!GenericItemFilling.canItemBeFilled(world, heldItem))
			return false;

		LazyOptional<IFluidHandler> capability = be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		IFluidHandler tank = capability.orElse(null);

		if (tank == null)
			return false;

		for (int i = 0; i < tank.getTanks(); i++) {
			FluidStack fluid = tank.getFluidInTank(i);
			if (fluid.isEmpty())
				continue;
			int requiredAmountForItem = GenericItemFilling.getRequiredAmountForItem(world, heldItem, fluid.copy());
			if (requiredAmountForItem == -1)
				continue;
			if (requiredAmountForItem > fluid.getAmount())
				continue;

			if (world.isClientSide)
				return true;

			if (player.isCreative() || be instanceof CreativeFluidTankBlockEntity)
				heldItem = heldItem.copy();
			ItemStack out = GenericItemFilling.fillItem(world, requiredAmountForItem, heldItem, fluid.copy());

			FluidStack copy = fluid.copy();
			copy.setAmount(requiredAmountForItem);
			tank.drain(copy, FluidAction.EXECUTE);

			if (!player.isCreative())
				player.getInventory()
					.placeItemBackInInventory(out);
			be.notifyUpdate();
			return true;
		}

		return false;
	}

	@Nullable
	public static FluidExchange exchange(IFluidHandler fluidTank, IFluidHandlerItem fluidItem, FluidExchange preferred,
		int maxAmount) {
		return exchange(fluidTank, fluidItem, preferred, true, maxAmount);
	}

	@Nullable
	public static FluidExchange exchangeAll(IFluidHandler fluidTank, IFluidHandlerItem fluidItem,
		FluidExchange preferred) {
		return exchange(fluidTank, fluidItem, preferred, false, Integer.MAX_VALUE);
	}

	@Nullable
	private static FluidExchange exchange(IFluidHandler fluidTank, IFluidHandlerItem fluidItem, FluidExchange preferred,
		boolean singleOp, int maxTransferAmountPerTank) {

		// Locks in the transfer direction of this operation
		FluidExchange lockedExchange = null;

		for (int tankSlot = 0; tankSlot < fluidTank.getTanks(); tankSlot++) {
			for (int slot = 0; slot < fluidItem.getTanks(); slot++) {

				FluidStack fluidInTank = fluidTank.getFluidInTank(tankSlot);
				int tankCapacity = fluidTank.getTankCapacity(tankSlot) - fluidInTank.getAmount();
				boolean tankEmpty = fluidInTank.isEmpty();

				FluidStack fluidInItem = fluidItem.getFluidInTank(tankSlot);
				int itemCapacity = fluidItem.getTankCapacity(tankSlot) - fluidInItem.getAmount();
				boolean itemEmpty = fluidInItem.isEmpty();

				boolean undecided = lockedExchange == null;
				boolean canMoveToTank = (undecided || lockedExchange == FluidExchange.ITEM_TO_TANK) && tankCapacity > 0;
				boolean canMoveToItem = (undecided || lockedExchange == FluidExchange.TANK_TO_ITEM) && itemCapacity > 0;

				// Incompatible Liquids
				if (!tankEmpty && !itemEmpty && !fluidInItem.isFluidEqual(fluidInTank))
					continue;

				// Transfer liquid to tank
				if (((tankEmpty || itemCapacity <= 0) && canMoveToTank)
					|| undecided && preferred == FluidExchange.ITEM_TO_TANK) {

					int amount = fluidTank.fill(
						fluidItem.drain(Math.min(maxTransferAmountPerTank, tankCapacity), FluidAction.EXECUTE),
						FluidAction.EXECUTE);
					if (amount > 0) {
						lockedExchange = FluidExchange.ITEM_TO_TANK;
						if (singleOp)
							return lockedExchange;
						continue;
					}
				}

				// Transfer liquid from tank
				if (((itemEmpty || tankCapacity <= 0) && canMoveToItem)
					|| undecided && preferred == FluidExchange.TANK_TO_ITEM) {

					int amount = fluidItem.fill(
						fluidTank.drain(Math.min(maxTransferAmountPerTank, itemCapacity), FluidAction.EXECUTE),
						FluidAction.EXECUTE);
					if (amount > 0) {
						lockedExchange = FluidExchange.TANK_TO_ITEM;
						if (singleOp)
							return lockedExchange;
						continue;
					}

				}

			}
		}

		return null;
	}

}
