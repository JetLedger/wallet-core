package com.jetledger.wallet.infrastructure;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.jetledger.wallet")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_layer_must_not_depend_on_infrastructure =
        noClasses()
            .that()
            .resideInAnyPackage("com.jetledger.wallet.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("com.jetledger.wallet.infrastructure..");
}
