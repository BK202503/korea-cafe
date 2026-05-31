package com.harness.cafe.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures.layeredArchitecture

@AnalyzeClasses(
    packages = ["com.harness.cafe"],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class LayerArchitectureTest {
    @ArchTest
    fun layeredArchitectureIsRespected(classes: JavaClasses) {
        layeredArchitecture()
            .consideringAllDependencies()
            .layer("Interfaces")
            .definedBy("..interfaces..")
            .layer("Application")
            .definedBy("..application..")
            .layer("Domain")
            .definedBy("..domain..")
            .layer("Infrastructure")
            .definedBy("..infrastructure..")
            .whereLayer("Interfaces")
            .mayNotBeAccessedByAnyLayer()
            .whereLayer("Infrastructure")
            .mayNotBeAccessedByAnyLayer()
            .whereLayer("Application")
            .mayOnlyBeAccessedByLayers("Interfaces", "Infrastructure")
            .whereLayer("Domain")
            .mayOnlyBeAccessedByLayers("Interfaces", "Application", "Infrastructure")
            .check(classes)
    }

    @ArchTest
    fun domainShouldNotDependOnInterfacesOrApplication(classes: JavaClasses) {
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..interfaces..", "..application..")
            .check(classes)
    }
}
