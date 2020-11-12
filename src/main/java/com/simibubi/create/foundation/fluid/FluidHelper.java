package com.simibubi.create.foundation.fluid;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
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
	
	public static boolean hasBlockState(Fluid fluid) {
		BlockState blockState = fluid.getDefaultState().getBlockState();
		return blockState != null && blockState != Blocks.AIR.getDefaultState();
	}

	public static Fluid convertToFlowing(Fluid fluid) {
		if (fluid == Fluids.WATER)
			return Fluids.FLOWING_WATER;
		if (fluid == Fluids.LAVA)
			return Fluids.FLOWING_LAVA;
		if (fluid instanceof ForgeFlowingFluid)
			return ((ForgeFlowingFluid) fluid).getFlowingFluid();
		return fluid;
	}

	public static Fluid convertToStill(Fluid fluid) {
		if (fluid == Fluids.FLOWING_WATER)
			return Fluids.WATER;
		if (fluid == Fluids.FLOWING_LAVA)
			return Fluids.LAVA;
		if (fluid instanceof ForgeFlowingFluid)
			return ((ForgeFlowingFluid) fluid).getStillFluid();
		return fluid;
	}

	public static JsonElement serializeFluidStack(FluidStack stack) {
		JsonObject json = new JsonObject();
		json.addProperty("fluid", stack.getFluid()
			.getRegistryName()
			.toString());
		json.addProperty("amount", stack.getAmount());
		if (stack.hasTag())
			json.addProperty("nbt", stack.getTag()
				.toString());
		return json;
	}

	public static FluidStack deserializeFluidStack(JsonObject json) {
		ResourceLocation id = new ResourceLocation(JSONUtils.getString(json, "fluid"));
		Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
		if (fluid == null)
			throw new JsonSyntaxException("Unknown fluid '" + id + "'");
		int amount = JSONUtils.getInt(json, "amount");
		FluidStack stack = new FluidStack(fluid, amount);

		if (!json.has("nbt"))
			return stack;

		try {
			JsonElement element = json.get("nbt");
			stack.setTag(JsonToNBT.getTagFromJson(
				element.isJsonObject() ? Create.GSON.toJson(element) : JSONUtils.getString(element, "nbt")));

		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}

		return stack;
	}

	public static boolean tryEmptyItemIntoTE(World worldIn, PlayerEntity player, Hand handIn, ItemStack heldItem,
		SmartTileEntity te) {
		if (!EmptyingByBasin.canItemBeEmptied(worldIn, heldItem))
			return false;

		Pair<FluidStack, ItemStack> emptyingResult = EmptyingByBasin.emptyItem(worldIn, heldItem, true);
		LazyOptional<IFluidHandler> capability = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		IFluidHandler tank = capability.orElse(null);
		FluidStack fluidStack = emptyingResult.getFirst();

		if (tank == null || fluidStack.getAmount() != tank.fill(fluidStack, FluidAction.SIMULATE))
			return false;
		if (worldIn.isRemote)
			return true;

		ItemStack copyOfHeld = heldItem.copy();
		emptyingResult = EmptyingByBasin.emptyItem(worldIn, copyOfHeld, false);
		tank.fill(fluidStack, FluidAction.EXECUTE);

		if (!player.isCreative()) {
			if (copyOfHeld.isEmpty())
				player.setHeldItem(handIn, emptyingResult.getSecond());
			else {
				player.setHeldItem(handIn, copyOfHeld);
				player.inventory.placeItemBackInInventory(worldIn, emptyingResult.getSecond());
			}
		}
		return true;
	}

	public static boolean tryFillItemFromTE(World world, PlayerEntity player, Hand handIn, ItemStack heldItem,
		SmartTileEntity te) {
		if (!GenericItemFilling.canItemBeFilled(world, heldItem))
			return false;

		LazyOptional<IFluidHandler> capability = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
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

			if (world.isRemote)
				return true;

			if (player.isCreative())
				heldItem = heldItem.copy();
			ItemStack out = GenericItemFilling.fillItem(world, requiredAmountForItem, heldItem, fluid.copy());

			FluidStack copy = fluid.copy();
			copy.setAmount(requiredAmountForItem);
			tank.drain(copy, FluidAction.EXECUTE);

			if (!player.isCreative())
				player.inventory.placeItemBackInInventory(world, out);
			te.notifyUpdate();
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
