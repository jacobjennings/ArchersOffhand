package com.carloplayz.archersoffhand.state;

import com.carloplayz.archersoffhand.OffhandContext;

public interface IOffhandState {
    /**
     * Called every tick to execute the logic for the current state.
     * 
     * @param context The context for the current tick.
     * @return The next state (can be 'this' if no transition occurs).
     */
    IOffhandState onTick(OffhandContext context);
}
