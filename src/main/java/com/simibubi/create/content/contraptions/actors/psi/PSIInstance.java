package com.simibubi.create.content.contraptions.actors.psi;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;

import net.createmod.catnip.utility.AnimationTickHolder;

public class PSIInstance extends BlockEntityInstance<PortableStorageInterfaceBlockEntity> implements DynamicInstance, TickableInstance {

	private final PIInstance instance;

	public PSIInstance(MaterialManager materialManager, PortableStorageInterfaceBlockEntity blockEntity) {
		super(materialManager, blockEntity);

		instance = new PIInstance(materialManager, blockState, getInstancePosition());
	}

	@Override
	public void init() {
		instance.init(isLit());
	}

	@Override
	public void tick() {
		instance.tick(isLit());
	}

	@Override
	public void beginFrame() {
		instance.beginFrame(blockEntity.getExtensionDistance(AnimationTickHolder.getPartialTicks()));
	}

	@Override
	public void updateLight() {
		relight(pos, instance.middle, instance.top);
	}

	@Override
	public void remove() {
		instance.remove();
	}

	private boolean isLit() {
		return blockEntity.isConnected();
	}

}
