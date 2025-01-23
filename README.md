# PIT Mutation Testing for Java

## Overview
Mutation testing framework for Java to assess test suite quality.

## Key Features
- Introduces artificial defects (mutations) in source code
- Evaluates test suite's ability to detect code changes
- Provides mutation score as quality metric

## Configuration
```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <configuration>
        <targetClasses>
            <param>your.package.*</param>
        </targetClasses>
        <targetTests>
            <param>your.test.package.*</param>
        </targetTests>
    </configuration>
</plugin>
```

## Running Tests
  Test coverage
1. Run ``./gradlew test`` to execute the tests
1. Run ``./gradlew jacocoTestReport`` to check test coverage

## PIT Mutation Testing
1. Run ``./gradlew pitest`` to execute create mutants and execute the tests

## Mutation Types
- Arithmetic operator replacement
- Conditional boundary modifications
- Logical operator substitutions
- Return value alterations

## Metrics
- Mutation Score = (Detected Mutations / Total Mutations) * 100%

## Best Practices
- Aim for >70% mutation coverage
- Review and improve tests for undetected mutations



