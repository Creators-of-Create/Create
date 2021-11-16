package com.simibubi.create.lib.transfer.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

@SuppressWarnings({"UnstableApiUsage", "deprecation"})
public class FluidStack {
	private static final FluidStack EMPTY = new FluidStack(FluidVariant.blank(), 0);

	private FluidVariant type;
	private long amount;

	public FluidStack(FluidVariant type, long amount) {
		this.type = type;
		this.amount = amount;
	}

	public FluidStack(Fluid type, long amount) {
		this.type = FluidVariant.of(type);
		this.amount = amount;
	}

	public FluidStack(Fluid type, long amount, CompoundTag nbt) {
		this.type = FluidVariant.of(type, nbt);
		this.amount = amount;
	}

	public FluidStack(FluidStack copy, long amount) {
		this(copy.getType(), amount);
	}

	public FluidStack setAmount(long amount) {
		this.amount = amount;
		return this;
	}

	public FluidVariant getType() {
		return type;
	}

	public Fluid getFluid() {
		return getType().getFluid();
	}

	public Fluid getRawFluid() {
		return getFluid();
	}

	public long getAmount() {
		return amount;
	}

	public boolean isEmpty() {
		return amount == 0;
	}

	public void shrink (int amount) {
		setAmount(getAmount() - amount);
	}

	public void shrink (long amount) {
		setAmount(getAmount() - amount);
	}

	public boolean isFluidEqual(FluidStack other) {
		if (this == other) return true;
		if (other == null) return false;

		FluidVariant mine = getType();
		FluidVariant theirs = other.getType();
		boolean fluidsEqual = mine.isOf(theirs.getFluid());


		CompoundTag myTag = mine.getNbt();
		CompoundTag theirTag = theirs.getNbt();

		if (myTag == null) {
			return theirTag == null && fluidsEqual;
		} else if (theirTag == null) {
			return false;
		}

		boolean tagsEqual = myTag.equals(theirTag);

		return fluidsEqual && tagsEqual;
	}

	public CompoundTag writeToNBT(CompoundTag tag) {
		tag.put("Fluid", getType().toNbt());
		tag.putLong("Amount", getAmount());
		return tag;
	}

	public static FluidStack loadFluidStackFromNBT(CompoundTag tag) {
		Tag fluidTag = tag.get("Fluid");
		FluidVariant fluid = FluidVariant.fromNbt((CompoundTag) fluidTag);
		long amount = tag.getLong("Amount");
		return new FluidStack(fluid, amount);
	}

	public CompoundTag toTag() {
		return writeToNBT(new CompoundTag());
	}

	public CompoundTag toTag(CompoundTag tag) {
		return writeToNBT(tag);
	}

	public CompoundTag getTag() {
		return getType().copyNbt();
	}

	public static FluidStack fromBuffer(FriendlyByteBuf buffer) {
		Fluid fluid = Registry.FLUID.get(buffer.readResourceLocation());
		long amount = buffer.readVarLong();
		CompoundTag tag = buffer.readNbt();
		if (fluid == Fluids.EMPTY) {
			return EMPTY;
		}
		return new FluidStack(FluidVariant.of(fluid, tag), amount);
	}

	public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
		return toBuffer(this, buffer);
	}

	public static FriendlyByteBuf toBuffer(FluidStack stack, FriendlyByteBuf buffer) {
		buffer.writeResourceLocation(Registry.FLUID.getKey(stack.getFluid()));
		buffer.writeVarLong(stack.getAmount());
		buffer.writeNbt(stack.type.copyNbt());
		return buffer;
	}

	public static FluidStack empty() {
		return EMPTY.copy();
	}

	public String getTranslationKey() {
		return Util.makeDescriptionId("fluid", Registry.FLUID.getKey(getFluid()));
	}

	public FluidStack copy() {
		return new FluidStack(FluidVariant.of(getFluid(), getType().copyNbt()), getAmount());
	}
}
