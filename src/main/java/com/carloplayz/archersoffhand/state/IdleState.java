package com.carloplayz.archersoffhand.state;

import com.carloplayz.archersoffhand.OffhandContext;

public class IdleState implements IOffhandState {
    @Override
    public IOffhandState onTick(OffhandContext context) {
        if (context.hasWeapon()) {
            return new TrackingState(context);
        }
        return this;
    }
}
