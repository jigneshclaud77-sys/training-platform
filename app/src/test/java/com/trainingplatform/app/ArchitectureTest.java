package com.trainingplatform.app;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * A domain may depend on another domain's {@code api} package, never on its internals.
 * Rules allow an empty match so they stay green while a domain has no classes yet.
 */
@AnalyzeClasses(packages = "com.trainingplatform")
class ArchitectureTest {

    @ArchTest
    static final ArchRule catalog_does_not_use_other_domain_internals =
            internalsOf("catalog", "enrollments", "notifications");

    @ArchTest
    static final ArchRule enrollments_does_not_use_other_domain_internals =
            internalsOf("enrollments", "catalog", "notifications");

    @ArchTest
    static final ArchRule notifications_does_not_use_other_domain_internals =
            internalsOf("notifications", "catalog", "enrollments");

    @ArchTest
    static final ArchRule catalog_api_is_free_of_spring_and_jpa =
            noClasses().that().resideInAPackage("com.trainingplatform.catalog.api..")
                    .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule enrollments_api_is_free_of_spring_and_jpa = apiIsFreeOfSpringAndJpa("enrollments");

    @ArchTest
    static final ArchRule notifications_api_is_free_of_spring_and_jpa = apiIsFreeOfSpringAndJpa("notifications");

    @ArchTest
    static final ArchRule enrollments_and_catalog_do_not_depend_on_notifications =
            noClasses().that().resideInAnyPackage("com.trainingplatform.enrollments..", "com.trainingplatform.catalog..")
                    .should().dependOnClassesThat().resideInAPackage("com.trainingplatform.notifications..");

    private static ArchRule apiIsFreeOfSpringAndJpa(String domain) {
        return noClasses().that().resideInAPackage("com.trainingplatform." + domain + ".api..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")
                .allowEmptyShould(true);
    }

    private static ArchRule internalsOf(String domain, String... otherDomains) {
        DescribedPredicate<JavaClass> otherInternals = DescribedPredicate.alwaysFalse();
        for (String other : otherDomains) {
            otherInternals = otherInternals.or(
                    resideInAPackage("com.trainingplatform." + other + "..")
                            .and(not(resideInAPackage("com.trainingplatform." + other + ".api.."))));
        }
        return noClasses().that().resideInAPackage("com.trainingplatform." + domain + "..")
                .should().dependOnClassesThat(otherInternals)
                .allowEmptyShould(true);
    }
}
