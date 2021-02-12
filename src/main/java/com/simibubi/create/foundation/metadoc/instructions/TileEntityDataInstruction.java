package com.simibubi.create.foundation.metadoc.instructions;

import java.util.function.UnaryOperator;

import com.simibubi.create.foundation.metadoc.MetaDocScene;
import com.simibubi.create.foundation.metadoc.MetaDocWorld;
import com.simibubi.create.foundation.metadoc.Select;
import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

public class TileEntityDataInstruction extends WorldModifyInstruction {

	private boolean redraw;
	private UnaryOperator<CompoundNBT> data;
	private Class<? extends TileEntity> type;

	public TileEntityDataInstruction(Select selection, Class<? extends TileEntity> type,
		UnaryOperator<CompoundNBT> data, boolean redraw) {
		super(selection);
		this.type = type;
		this.data = data;
		this.redraw = redraw;
	}

	@Override
	protected void runModification(Select selection, MetaDocScene scene) {
		MetaDocWorld world = scene.getWorld();
		selection.all()
			.forEach(pos -> {
				if (!world.getBounds()
					.isVecInside(pos))
					return;
				TileEntity tileEntity = world.getTileEntity(pos);
				if (!type.isInstance(tileEntity))
					return;
				CompoundNBT apply = data.apply(tileEntity.write(new CompoundNBT()));
				tileEntity.read(apply);
				if (tileEntity instanceof SyncedTileEntity)
					((SyncedTileEntity) tileEntity).readClientUpdate(apply);
			});
	}

	@Override
	protected boolean needsRedraw() {
		return redraw;
	}

}
