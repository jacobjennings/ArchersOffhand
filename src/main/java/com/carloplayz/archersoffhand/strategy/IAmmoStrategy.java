package com.carloplayz.archersoffhand.strategy;

import com.carloplayz.archersoffhand.OffhandContext;

public interface IAmmoStrategy {
    void activate(OffhandContext context);

    void onTick(OffhandContext context);

    void deactivate(OffhandContext context);
}
