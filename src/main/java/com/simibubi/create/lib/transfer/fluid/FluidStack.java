package com.simibubi.create.lib.transfer.fluid;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

@SuppressWarnings({"UnstableApiUsage"})
public class FluidStack {
	public static final Codec<FluidStack> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Registry.FLUID.byNameCodec().fieldOf("FluidName").forGetter(FluidStack::getFluid),
					Codec.LONG.fieldOf("Amount").forGetter(FluidStack::getAmount),
					CompoundTag.CODEC.optionalFieldOf("Tag").forGetter(stack -> Optional.ofNullable(stack.getTag()))
			).apply(instance, (fluid, amount, tag) -> {
				FluidStack stack = new FluidStack(fluid, amount);
				tag.ifPresent(stack::setTag);
				return stack;
			})
	);

	private static final FluidStack EMPTY = new FluidStack(FluidVariant.blank(), 0) {
		@Override
		public FluidStack setAmount(long amount) {
			return this;
		}

		@Override
		public void shrink(int amount) {
		}

		@Override
		public void shrink(long amount) {
		}

		@Override
		public FluidStack copy() {
			return this;
		}
	};

	private final FluidVariant type;
	@Nullable
	private CompoundTag tag;
	private long amount;
	private String translationKey;

	public FluidStack(FluidVariant type, long amount) {
		this.type = type;
		this.amount = amount;
	}

	public FluidStack(FluidVariant type, long amount, @Nullable CompoundTag tag) {
		this(type, amount);
		this.tag = tag;
	}

	public FluidStack(Fluid type, long amount) {
		// todo: support virtual fluids
		if(!type.isSource(type.defaultFluidState())) {
			this.type = FluidVariant.blank();
			this.amount = amount;
			return;
		}
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

	public void grow(long amount) {
		setAmount(getAmount() + amount);
	}

	public FluidVariant getType() {
		return type;
	}

	public Fluid getFluid() {
		return getType().getFluid();
	}

	public long getAmount() {
		return amount;
	}

	public boolean isEmpty() {
		return amount == 0;
	}

	public void shrink(int amount) {
		setAmount(getAmount() - amount);
	}

	public void shrink(long amount) {
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

	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt.put("Variant", getType().toNbt());
		nbt.putLong("Amount", getAmount());
		if (tag != null)
			nbt.put("Tag", tag);
		return nbt;
	}

	public static FluidStack loadFluidStackFromNBT(CompoundTag tag) {
		Tag fluidTag = tag.get("Variant");
		FluidVariant fluid = FluidVariant.fromNbt((CompoundTag) fluidTag);
		FluidStack stack = new FluidStack(fluid, tag.getLong("Amount"));
		if(tag.contains("Tag", Tag.TAG_COMPOUND))
			stack.tag = tag.getCompound("Tag");
		return stack;
	}

	public CompoundTag toTag() {
		return writeToNBT(new CompoundTag());
	}

	public CompoundTag toTag(CompoundTag tag) {
		return writeToNBT(tag);
	}

	public void setTag(CompoundTag tag) {
		this.tag = tag;
	}

	@Nullable
	public CompoundTag getTag() {
		return tag;
	}

	public CompoundTag getOrCreateTag() {
		if (getTag() == null) tag = new CompoundTag();
        return tag;
	}

	public void removeChildTag(String key) {
        if (getTag() == null) return;
        getTag().remove(key);
    }

	public boolean hasTag() {
		return tag != null;
	}

	public static FluidStack fromBuffer(FriendlyByteBuf buffer) {
		FluidVariant fluid = FluidVariant.fromPacket(buffer);
		long amount = buffer.readVarLong();
		CompoundTag tag = buffer.readNbt();
		if (fluid.isBlank()) return empty();
		return new FluidStack(fluid, amount, tag);
	}

	public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
		return toBuffer(this, buffer);
	}

	public static FriendlyByteBuf toBuffer(FluidStack stack, FriendlyByteBuf buffer) {
		stack.getType().toPacket(buffer);
		buffer.writeVarLong(stack.getAmount());
		buffer.writeNbt(stack.tag);
		return buffer;
	}

	public static FluidStack empty() {
		return EMPTY.copy();
	}

	public String getTranslationKey() {
		if (translationKey != null) return translationKey;

		if (getFluid() == Fluids.EMPTY) {
			translationKey = "";
		} else if (getFluid() == Fluids.WATER) {
			translationKey = "block.minecraft.water";
		} else if (getFluid() == Fluids.LAVA) {
			translationKey = "block.minecraft.lava";
		} else {
			ResourceLocation id = Registry.FLUID.getKey(getFluid());
			String key = Util.makeDescriptionId("block", id);
			String translated = I18n.get(key);
			translationKey = translated.equals(key) ? Util.makeDescriptionId("fluid", id) : key;
		}
		return translationKey;
	}

	public FluidStack copy() {
		return new FluidStack(FluidVariant.of(getFluid(), getType().copyNbt()), getAmount());
	}
}
