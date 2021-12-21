package com.simibubi.create.foundation.fluid;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.lib.transfer.TransferUtil;
import com.simibubi.create.lib.transfer.fluid.FluidStack;
import com.simibubi.create.lib.transfer.fluid.IFluidHandler;
import com.simibubi.create.lib.transfer.fluid.IFluidHandlerItem;
import com.simibubi.create.lib.util.LazyOptional;

import net.minecraft.core.Registry;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

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
		BlockState blockState = fluid.defaultFluidState()
			.createLegacyBlock();
		return blockState != null && blockState != Blocks.AIR.defaultBlockState();
	}

	public static FluidStack copyStackWithAmount(FluidStack fs, long amount) {
		if (amount <= 0)
			return FluidStack.empty();
		if (fs.isEmpty())
			return FluidStack.empty();
		FluidStack copy = fs.copy();
		copy.setAmount(amount);
		return copy;
	}

	public static Fluid convertToFlowing(Fluid fluid) {
		if (fluid == Fluids.WATER)
			return Fluids.FLOWING_WATER;
		if (fluid == Fluids.LAVA)
			return Fluids.FLOWING_LAVA;
		if (fluid instanceof FlowingFluid)
			return ((FlowingFluid) fluid).getFlowing();
		return fluid;
	}

	public static Fluid convertToStill(Fluid fluid) {
		if (fluid == Fluids.FLOWING_WATER)
			return Fluids.WATER;
		if (fluid == Fluids.FLOWING_LAVA)
			return Fluids.LAVA;
		if (fluid instanceof FlowingFluid)
			return ((FlowingFluid) fluid).getSource();
		return fluid;
	}

	public static JsonElement serializeFluidStack(FluidStack stack) {
		JsonObject json = new JsonObject();
		json.addProperty("fluid", Registry.FLUID.getKey(stack.getFluid())
			.toString());
		json.addProperty("amount", stack.getAmount());
		if (stack.hasTag())
			json.addProperty("nbt", stack.getTag()
				.toString());
		return json;
	}

	public static FluidStack deserializeFluidStack(JsonObject json) {
		ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
		Fluid fluid = Registry.FLUID.get(id);
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

	public static boolean tryEmptyItemIntoTE(Level worldIn, Player player, InteractionHand handIn, ItemStack heldItem,
		SmartTileEntity te) {
		if (!EmptyingByBasin.canItemBeEmptied(worldIn, heldItem))
			return false;

		Pair<FluidStack, ItemStack> emptyingResult = EmptyingByBasin.emptyItem(worldIn, heldItem, true);
		LazyOptional<IFluidHandler> capability = TransferUtil.getFluidHandler(te);
		IFluidHandler tank = capability.orElse(null);
		FluidStack fluidStack = emptyingResult.getFirst();

		if (tank == null || fluidStack.getAmount() != tank.fill(fluidStack, true))
			return false;
		if (worldIn.isClientSide)
			return true;

		ItemStack copyOfHeld = heldItem.copy();
		emptyingResult = EmptyingByBasin.emptyItem(worldIn, copyOfHeld, false);
		tank.fill(fluidStack, false);

		if (!player.isCreative() && !(te instanceof CreativeFluidTankTileEntity)) {
			if (copyOfHeld.isEmpty())
				player.setItemInHand(handIn, emptyingResult.getSecond());
			else {
				player.setItemInHand(handIn, copyOfHeld);
				player.getInventory().placeItemBackInInventory(emptyingResult.getSecond());
			}
		}
		return true;
	}

	public static boolean tryFillItemFromTE(Level world, Player player, InteractionHand handIn, ItemStack heldItem,
		SmartTileEntity te) {
		if (!GenericItemFilling.canItemBeFilled(world, heldItem))
			return false;

		LazyOptional<IFluidHandler> capability = TransferUtil.getFluidHandler(te);
		IFluidHandler tank = capability.orElse(null);

		if (tank == null)
			return false;

		for (int i = 0; i < tank.getTanks(); i++) {
			FluidStack fluid = tank.getFluidInTank(i);
			if (fluid.isEmpty())
				continue;
			long requiredAmountForItem = GenericItemFilling.getRequiredAmountForItem(world, heldItem, fluid.copy());
			if (requiredAmountForItem == -1)
				continue;
			if (requiredAmountForItem > fluid.getAmount())
				continue;

			if (world.isClientSide)
				return true;

			if (player.isCreative() || te instanceof CreativeFluidTankTileEntity)
				heldItem = heldItem.copy();
			ItemStack out = GenericItemFilling.fillItem(world, requiredAmountForItem, heldItem, fluid.copy());

			FluidStack copy = fluid.copy();
			copy.setAmount(requiredAmountForItem);
			tank.drain(copy, false);

			if (!player.isCreative())
				player.getInventory().placeItemBackInInventory(out);
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
				long tankCapacity = fluidTank.getTankCapacity(tankSlot) - fluidInTank.getAmount();
				boolean tankEmpty = fluidInTank.isEmpty();

				FluidStack fluidInItem = fluidItem.getFluidInTank(tankSlot);
				long itemCapacity = fluidItem.getTankCapacity(tankSlot) - fluidInItem.getAmount();
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

					long amount = fluidTank.fill(
						fluidItem.drain(Math.min(maxTransferAmountPerTank, tankCapacity), false),
						false);
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

					long amount = fluidItem.fill(
						fluidTank.drain(Math.min(maxTransferAmountPerTank, itemCapacity), false),
						false);
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
