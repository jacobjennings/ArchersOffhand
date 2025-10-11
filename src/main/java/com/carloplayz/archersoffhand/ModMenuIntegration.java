package com.carloplayz.archersoffhand;

import com.carloplayz.archersoffhand.config.ArchersOffhandConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ArchersOffhandConfigScreen.create(parent);
    }
}