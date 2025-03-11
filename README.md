# Policy Engine

A flexible, attribute-based policy evaluation library for Java applications that need to implement access control and authorization.

## Overview

This policy engine allows you to define and evaluate access control policies based on attributes of the subject (user), resource, action, and environment. The library provides a flexible way to implement complex authorization rules in your Java applications.

Key features:
- Attribute-based access control (ABAC)
- Type-safe attribute handling with automatic conversion
- Flexible condition operators (equals, not equals, in, not in, greater than, less than)
- Support for both single-value and list-based conditions
- Clean, expressive API for building and evaluating policies

## Installation

### Gradle

```kotlin
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/binaryhivetech/policy-engine")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
    mavenCentral()
}

dependencies {
    implementation("org.binaryhive:policy-engine:2025.1.0")
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/binaryhivetech/policy-engine</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.binaryhive</groupId>
        <artifactId>policy-engine</artifactId>
        <version>2025.1.0</version>
    </dependency>
</dependencies>
```

## Authentication for GitHub Packages

GitHub Packages requires authentication. You need to add your GitHub credentials:

1. Create a Personal Access Token (PAT) with `read:packages` scope
2. Add credentials to `~/.gradle/gradle.properties`:
```
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_PAT
```

## Quick Start

Here's a simple example to get you started:

```java
import org.binaryhive.policyengine.condition.*;
import org.binaryhive.policyengine.evaluation.*;
import org.binaryhive.policyengine.model.*;

import java.util.*;

// 1. Create a simple policy
Policy policy = new Policy();
policy.setPolicyId("document-access-policy");
policy.setDescription("Controls access to documents");
policy.setEffect(Effect.ALLOW);

// 2. Define the conditions
List<Condition<?>> conditions = new ArrayList<>();

// User must be admin or editor
Attribute<String> roleAttr = Attribute.string("role");
conditions.add(new ListCondition<>(roleAttr, Operator.IN, Arrays.asList("admin", "editor")));

// Document security level must be less than 3
Attribute<Integer> securityLevelAttr = Attribute.integer("securityLevel");
conditions.add(new ValueCondition<>(securityLevelAttr, Operator.LESS_THAN, 3));

policy.setConditions(conditions);

// 3. Create evaluation context
Map<String, Object> context = new HashMap<>();
context.put("role", "editor");
context.put("securityLevel", 2);

// 4. Evaluate the policy
PolicyEvaluator evaluator = new PolicyEvaluator();
PolicyEvaluationResult result = evaluator.evaluate(policy, context);

// 5. Check the result
if (result == PolicyEvaluationResult.ALLOW) {
    // Grant access
    System.out.println("Access granted!");
} else {
    // Deny access
    System.out.println("Access denied!");
}
```

## Core Concepts

### Policies

A `Policy` is the core entity that defines an access control rule. Each policy has:
- A unique ID
- A description
- An effect (ALLOW or DENY)
- A list of conditions that must all be satisfied for the policy to apply

### Attributes

`Attribute` is a typed metadata definition for values being evaluated. The library supports:
- String attributes
- Integer attributes
- Enum attributes
- List attributes

Attributes handle type conversion and validation automatically.

### Conditions

Conditions define rules that must be satisfied for a policy to apply:
- `ValueCondition` - For comparing single values (equals, not equals, greater than, less than)
- `ListCondition` - For comparing against multiple values (in, not in)

### Operators

Supported operators:
- `EQUALS` - Value equals the condition value
- `NOT_EQUALS` - Value does not equal the condition value
- `IN` - Value is in a list of allowed values
- `NOT_IN` - Value is not in a list of disallowed values
- `GREATER_THAN` - Value is greater than the condition value
- `LESS_THAN` - Value is less than the condition value

### Evaluation

The `PolicyEvaluator` evaluates policies against a context (a map of attribute names to values). The result can be:
- `ALLOW` - The policy applies and allows the action
- `DENY` - The policy applies and denies the action
- `NOT_APPLICABLE` - The policy conditions are not met, so the policy doesn't apply

## Advanced Usage

### Combining Multiple Policies

In real applications, you'll typically have multiple policies. You can evaluate them in sequence:

```java
List<Policy> policies = getPoliciesFromDatabase();
Map<String, Object> context = buildContext(user, resource, action);

// Find the first applicable policy
for (Policy policy : policies) {
    PolicyEvaluationResult result = evaluator.evaluate(policy, context);
    if (result != PolicyEvaluationResult.NOT_APPLICABLE) {
        return result; // Return first ALLOW or DENY
    }
}

// Default decision if no policy applies
return PolicyEvaluationResult.DENY;
```

### Using Custom Evaluation Context

You can extend the `EvaluationContext` abstract class to create a custom context:

```java
public class MyEvaluationContext extends EvaluationContext {
    private final User user;
    private final Resource resource;
    private final String action;
    private final List<Policy> policies;

    public MyEvaluationContext(User user, Resource resource, String action) {
        this.user = user;
        this.resource = resource;
        this.action = action;
        this.policies = fetchPoliciesFromDatabase();
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public List<Policy> getApplicablePolicies() {
        return policies;
    }

    @Override
    public Map<String, Object> getContextData() {
        Map<String, Object> context = new HashMap<>();
        // Build context from user, resource, and environment
        context.put("role", user.getRole());
        context.put("resourceType", resource.getType());
        context.put("resourceOwner", resource.getOwnerId());
        context.put("timeOfDay", LocalTime.now().getHour());
        return context;
    }
}
```

## License

Apache License 2.0