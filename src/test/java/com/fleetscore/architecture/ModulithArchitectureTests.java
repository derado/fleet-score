package com.fleetscore.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import com.fleetscore.FleetScoreApplication;

class ModulithArchitectureTests {

    @Test
    void verifyModularStructure() {
        ApplicationModules modules = ApplicationModules.of(FleetScoreApplication.class);
        modules.verify();
    }
}
