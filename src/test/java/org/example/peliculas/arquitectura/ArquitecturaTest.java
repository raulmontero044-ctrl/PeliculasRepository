package org.example.peliculas.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "org.example.peliculas", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    @ArchTest
    static final ArchRule el_dominio_es_puro = classes()
            .that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("java..", "org.example.peliculas.domain..");

    @ArchTest
    static final ArchRule controllers_no_dependen_de_persistence = noClasses()
            .that().resideInAPackage("..controllers..")
            .should().dependOnClassesThat().resideInAPackage("..persistence..");

    @ArchTest
    static final ArchRule persistence_no_depende_de_controllers = noClasses()
            .that().resideInAPackage("..persistence..")
            .should().dependOnClassesThat().resideInAPackage("..controllers..");

    @ArchTest
    static final ArchRule los_dtos_solo_viven_en_controllers_dto = classes()
            .that().haveSimpleNameEndingWith("Request")
            .or().haveSimpleNameEndingWith("Response")
            .or().haveSimpleName("ErrorRespuesta")
            .or().haveSimpleName("ErrorCampo")
            .should().resideInAPackage("..controllers.dto");
}
