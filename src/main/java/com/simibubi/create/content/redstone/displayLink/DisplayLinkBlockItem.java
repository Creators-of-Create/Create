package com.simibubi.create.content.redstone.displayLink;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DisplayLinkBlockItem extends ClickToLinkBlockItem {

	public DisplayLinkBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@OnlyIn(Dist.CLIENT)
	public AABB getSelectionBounds(BlockPos pos) {
		Level world = Minecraft.getInstance().level;
		DisplayTarget target = DisplayTarget.get(world, pos);
		if (target != null)
			return target.getMultiblockBounds(world, pos);
		return super.getSelectionBounds(pos);
	}

	@Override
	public int getMaxDistanceFromSelection() {
		return AllConfigs.server().logistics.displayLinkRange.get();
	}

	@Override
	public String getMessageTranslationKey() {
		return "display_link";
	}

}
