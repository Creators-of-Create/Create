package com.simibubi.create.foundation.particle;

import java.util.List;

import net.createmod.catnip.data.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Fallback for items missing particle textures (e.g. spyglass) and fixes missing color tints.
 */
public class ItemStackParticle extends BreakingItemParticle {

  public ItemStackParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz,
    ItemStack stack) {
    super(level, x, y, z, vx, vy, vz, stack);

    Minecraft mc = Minecraft.getInstance();
    BakedModel model = mc.getItemRenderer()
      .getItemModelShaper()
      .getItemModel(stack);
    BakedModel resolved = model.getOverrides()
      .resolve(model, stack, level, null, 0);
    if (resolved != null)
      model = resolved;
    setSprite(model.getParticleIcon(ModelData.EMPTY));

    if (isMissing() && stack.getItem() instanceof BlockItem blockItem) {
      setSprite(mc.getBlockRenderer()
        .getBlockModelShaper()
        .getParticleIcon(blockItem.getBlock()
          .defaultBlockState()));
      return;
    }

    int tintIndex = getTintIndex(model);
    if (tintIndex != -1)
      applyTint(mc.getItemColors()
        .getColor(stack, tintIndex));
  }

  private boolean isMissing() {
    return sprite.contents()
      .name()
      .equals(MissingTextureAtlasSprite.getLocation());
  }

  private int getTintIndex(BakedModel model) {
    RandomSource random = RandomSource.create(42L);
    int tintIndex = getTintIndex(model.getQuads(null, null, random));
    for (Direction side : Iterate.directions)
      if (tintIndex == -1)
        tintIndex = getTintIndex(model.getQuads(null, side, random));
    return tintIndex;
  }

  private int getTintIndex(List<BakedQuad> quads) {
    for (BakedQuad quad : quads) {
      if (quad.getSprite() == sprite && quad.isTinted()) {
        return quad.getTintIndex();
      }
    }
    return -1;
  }

  /**
   * The color by value is `0xAARRGGBB`, a 24-bit RGB or 32-bit ARGB integer.
   * To apply the tint, we shift and mask the RGB parts and apply those to the particle.
   */
  private void applyTint(int color) {
    rCol *= (float) (color >> 16 & 255) / 255.0F;
    gCol *= (float) (color >> 8 & 255) / 255.0F;
    bCol *= (float) (color & 255) / 255.0F;
  }

}