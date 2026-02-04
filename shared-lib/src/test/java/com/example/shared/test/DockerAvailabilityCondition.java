package com.example.shared.test;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Test-only JUnit 5 condition that disables Testcontainers-based tests when Docker is not available.
 *
 * This implementation avoids compile-time dependency on Testcontainers by using reflection and
 * by checking the presence of the Docker CLI (running `docker info`). If Docker is not usable
 * on the host, tests that appear to rely on Testcontainers are skipped.
 */
public class DockerAvailabilityCondition implements ExecutionCondition {
    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
            "Docker available or test does not require Testcontainers");
    private static final ConditionEvaluationResult DISABLED = ConditionEvaluationResult.disabled(
            "Docker not available; skipping Testcontainers-based test");

    private static Boolean dockerAvailable;

    private static boolean checkDockerAvailable() {
        if (dockerAvailable != null) return dockerAvailable;
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "info");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int rc = p.waitFor();
            dockerAvailable = (rc == 0);
        } catch (Throwable t) {
            dockerAvailable = false;
        }
        return dockerAvailable;
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (checkDockerAvailable()) return ENABLED;

        Optional<Class<?>> testClass = context.getTestClass();
        if (testClass.isEmpty()) return ENABLED;

        Class<?> clazz = testClass.get();

        try {
            Class<?> tcRaw = Class.forName("org.testcontainers.junit.jupiter.Testcontainers");
            Class<? extends Annotation> tc = tcRaw.asSubclass(Annotation.class);
            if (clazz.isAnnotationPresent(tc)) return DISABLED;
        } catch (ClassNotFoundException ignored) {
        }

        for (Field f : clazz.getDeclaredFields()) {
            try {
                Class<?> t = f.getType();
                if (t.getName().startsWith("org.testcontainers.")) {
                    return DISABLED;
                }
                try {
                    Class<?> containerAnnRaw = Class.forName("org.testcontainers.junit.jupiter.Container");
                    Class<? extends Annotation> containerAnn = containerAnnRaw.asSubclass(Annotation.class);
                    if (f.isAnnotationPresent(containerAnn)) return DISABLED;
                } catch (ClassNotFoundException ignored) {
                }
            } catch (Throwable ignored) {
            }
        }

        return ENABLED;
    }
}
