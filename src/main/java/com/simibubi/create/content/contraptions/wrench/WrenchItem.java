package com.simibubi.create.content.contraptions.wrench;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.IHaveCustomItemModel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WrenchItem extends Item implements IHaveCustomItemModel {

	public WrenchItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		PlayerEntity player = context.getPlayer();
		if (!player.isAllowEdit())
			return super.onItemUse(context);

		World world = context.getWorld();
		BlockPos pos = context.getPos();
		BlockState state = world.getBlockState(pos);
		if (!(state.getBlock() instanceof IWrenchable))
			return super.onItemUse(context);
		IWrenchable actor = (IWrenchable) state.getBlock();

		if (player.isSneaking()) {
			if (world instanceof ServerWorld) {
				if (!player.isCreative())
					Block.getDrops(state, (ServerWorld) world, pos, world.getTileEntity(pos), player, context.getItem())
							.forEach(itemStack -> {
								player.inventory.placeItemBackInInventory(world, itemStack);
							});
				state.spawnAdditionalDrops(world, pos, ItemStack.EMPTY);
				world.destroyBlock(pos, false);
			}
			return ActionResultType.SUCCESS;
		}

		return actor.onWrenched(state, context);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public CustomRenderedItemModel createModel(IBakedModel original) {
		return new WrenchModel(original);
	}

}
