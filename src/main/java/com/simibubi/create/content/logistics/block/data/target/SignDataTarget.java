package com.simibubi.create.content.logistics.block.data.target;

import java.util.List;

import com.simibubi.create.content.logistics.block.data.DataGathererContext;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class SignDataTarget extends DataGathererTarget {

	@Override
	public void acceptText(int line, List<MutableComponent> text, DataGathererContext context) {
		BlockEntity te = context.getTargetTE();
		if (!(te instanceof SignBlockEntity sign))
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

		if (changed && context.level()instanceof Level level)
			level.sendBlockUpdated(context.getTargetPos(), sign.getBlockState(), sign.getBlockState(), 2);
	}

	@Override
	public DataTargetStats provideStats(DataGathererContext context) {
		return new DataTargetStats(4, 15, this);
	}

}
