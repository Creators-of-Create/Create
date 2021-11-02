package com.simibubi.create.foundation.ponder.instructions;

import java.util.function.UnaryOperator;

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityDataInstruction extends WorldModifyInstruction {

	private boolean redraw;
	private UnaryOperator<CompoundTag> data;
	private Class<? extends BlockEntity> type;

	public TileEntityDataInstruction(Selection selection, Class<? extends BlockEntity> type,
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
			BlockEntity tileEntity = world.getBlockEntity(pos);
			if (!type.isInstance(tileEntity))
				return;
			CompoundTag apply = data.apply(tileEntity.save(new CompoundTag()));
			BlockState state = world.getBlockState(pos);
			if (tileEntity instanceof SyncedTileEntity)
				((SyncedTileEntity) tileEntity).readClientUpdate(state, apply);
			tileEntity.load(state, apply);
		});
	}

	@Override
	protected boolean needsRedraw() {
		return redraw;
	}

}
