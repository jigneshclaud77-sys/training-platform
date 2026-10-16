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
