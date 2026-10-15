package com.simibubi.create.foundation.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllParticleTypes;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Replaces vanilla item particle with {@link ItemStackParticle}.
 */
public class ItemParticleData implements ParticleOptions, ICustomParticleData<ItemParticleData> {

  private static final Codec<ItemStack> ITEM_CODEC =
    Codec.withAlternative(ItemStack.SINGLE_ITEM_CODEC, ItemStack.ITEM_NON_AIR_CODEC, ItemStack::new);

  public static final MapCodec<ItemParticleData> CODEC = ITEM_CODEC
    .xmap(ItemParticleData::new, p -> p.stack)
    .fieldOf("item");

  public static final StreamCodec<RegistryFriendlyByteBuf, ItemParticleData> STREAM_CODEC = ItemStack.STREAM_CODEC
    .map(ItemParticleData::new, p -> p.stack);

  private ItemStack stack;

  public ItemParticleData() {
  }

  public ItemParticleData(ItemStack stack) {
    this.stack = stack.copy();
  }

  @Override
  public ParticleType<?> getType() {
    return AllParticleTypes.ITEM.get();
  }

  @Override
  public MapCodec<ItemParticleData> getCodec(ParticleType<ItemParticleData> type) {
    return CODEC;
  }

  @Override
  public StreamCodec<? super RegistryFriendlyByteBuf, ItemParticleData> getStreamCodec() {
    return STREAM_CODEC;
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public ParticleProvider<ItemParticleData> getFactory() {
    return (data, level, x, y, z, vx, vy, vz) -> new ItemStackParticle(level, x, y, z, vx, vy, vz, data.stack);
  }
}