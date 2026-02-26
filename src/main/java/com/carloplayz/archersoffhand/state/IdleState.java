package com.carloplayz.archersoffhand.state;

import com.carloplayz.archersoffhand.OffhandContext;

public class IdleState implements IOffhandState {
    private int ticksHoldingWeapon = 0;

    @Override
    public IOffhandState onTick(OffhandContext context) {
        if (context.hasWeapon()) {
            ticksHoldingWeapon++;
            if (ticksHoldingWeapon >= context.config.getEquipDelayTicks()) {
                return new TrackingState(context);
            }
        } else {
            ticksHoldingWeapon = 0; // Reset if weapon is unequipped before delay finishes
        }
        return this;
    }
}
