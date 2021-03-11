package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderInstruction;
import com.simibubi.create.foundation.ponder.PonderScene;

public class KeyframeInstruction extends PonderInstruction {

    public static final KeyframeInstruction INSTANCE = new KeyframeInstruction();

    private KeyframeInstruction() { }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(PonderScene scene) { }

    @Override
    public void onScheduled(PonderScene scene) {
        scene.markKeyframe();
    }
}
