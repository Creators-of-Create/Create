package com.simibubi.create.content.redstone.displayLink.target;

import java.util.List;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class SignDisplayTarget extends DisplayTarget {

	@Override
	public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
		BlockEntity be = context.getTargetBlockEntity();
		if (!(be instanceof SignBlockEntity sign))
			return;

		boolean changed = false;
		for (int i = 0; i < text.size() && i + line < 4; i++) {
			if (i == 0)
				reserve(i + line, sign, context);
			if (i > 0 && isReserved(i + line, sign, context))
				break;

			sign.setMessage(i + line, text.get(i));
			changed = true;
		}

		if (changed)
			context.level().sendBlockUpdated(context.getTargetPos(), sign.getBlockState(), sign.getBlockState(), 2);
	}

	@Override
	public DisplayTargetStats provideStats(DisplayLinkContext context) {
		return new DisplayTargetStats(4, 15, this);
	}
	
	@Override
	public boolean requiresComponentSanitization() {
		return true;
	}

}
