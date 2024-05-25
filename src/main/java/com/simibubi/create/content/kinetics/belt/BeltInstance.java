package com.simibubi.create.content.kinetics.belt;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import com.simibubi.create.content.kinetics.base.KineticInstance;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class BeltInstance extends KineticInstance {
    public final Quaternionf rotation = new Quaternionf();
    public float sourceU;
    public float sourceV;
    public float minU;
    public float minV;
    public float maxU;
    public float maxV;
    public float scrollMult;

	public BeltInstance(InstanceType<? extends KineticInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public BeltInstance setRotation(Quaternionfc q) {
        this.rotation.set(q);
        return this;
    }

    public BeltInstance setScrollTexture(SpriteShiftEntry spriteShift) {
        TextureAtlasSprite source = spriteShift.getOriginal();
        TextureAtlasSprite target = spriteShift.getTarget();

        this.sourceU = source.getU0();
        this.sourceV = source.getV0();
        this.minU = target.getU0();
        this.minV = target.getV0();
        this.maxU = target.getU1();
        this.maxV = target.getV1();

		return this;
	}

	public BeltInstance setScrollMult(float scrollMult) {
		this.scrollMult = scrollMult;
		return this;
	}
}
