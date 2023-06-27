package com.simibubi.create.foundation.ponder.instruction;

import java.util.function.UnaryOperator;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.Selection;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityDataInstruction extends WorldModifyInstruction {

	private boolean redraw;
	private UnaryOperator<CompoundTag> data;
	private Class<? extends BlockEntity> type;

	public BlockEntityDataInstruction(Selection selection, Class<? extends BlockEntity> type,
		UnaryOperator<CompoundTag> data, boolean redraw) {
		super(selection);
		this.type = type;
		this.data = data;
		this.redraw = redraw;
	}

	@Override
	protected void runModification(Selection selection, PonderScene scene) {
		PonderWorld world = scene.getWorld();
		selection.forEach(pos -> {
			if (!world.getBounds()
				.isInside(pos))
				return;
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (!type.isInstance(blockEntity))
				return;
			CompoundTag apply = data.apply(blockEntity.saveWithFullMetadata());
			if (blockEntity instanceof SyncedBlockEntity)
				((SyncedBlockEntity) blockEntity).readClient(apply);
			blockEntity.load(apply);
		});
	}

	@Override
	protected boolean needsRedraw() {
		return redraw;
	}

}
