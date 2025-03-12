package org.binaryhive.policyengine.evaluation;

import org.binaryhive.policyengine.condition.Condition;
import org.binaryhive.policyengine.condition.ValueCondition;
import org.binaryhive.policyengine.model.Attribute;
import org.binaryhive.policyengine.model.Effect;
import org.binaryhive.policyengine.model.Operator;
import org.binaryhive.policyengine.model.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContextEvaluatorTest {

    private PolicyEvaluator policyEvaluator;
    private ContextEvaluator contextEvaluator;
    private Attribute<String> resourceAttr;
    private Attribute<String> actionAttr;
    private Attribute<Integer> ageAttr;

    @BeforeEach
    void setUp() {
        policyEvaluator = new PolicyEvaluator();
        contextEvaluator = new ContextEvaluator(policyEvaluator);
        resourceAttr = Attribute.string("resource");
        actionAttr = Attribute.string("action");
        ageAttr = Attribute.integer("age");
    }

    @Test
    void testEvaluateWithSingleAction() {
        // Create a test policy
        Condition<String> condition = new ValueCondition<>(resourceAttr, Operator.EQUALS, "document1");
        Policy policy = new Policy("policy1", "Test Policy", List.of(condition), Effect.ALLOW);

        // Create a test context with single action
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("resource", "document1");

        TestEvaluationContext context = new TestEvaluationContext("read", contextMap, List.of(policy));

        // Evaluate the context
        PolicyEvaluationResult result = contextEvaluator.evaluate(context);

        // Assert
        assertEquals(PolicyEvaluationResult.ALLOW, result);
    }

    @Test
    void testEvaluateWithNoPolicies() {
        // Create a test context with no policies
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("resource", "document1");

        TestEvaluationContext context = new TestEvaluationContext("read", contextMap, Collections.emptyList());

        // Evaluate the context
        PolicyEvaluationResult result = contextEvaluator.evaluate(context);

        // Assert - should return NOT_APPLICABLE when no policies exist
        assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);
    }

    @Test
    void testEvaluateWithDenyPolicy() {
        // Create a deny policy
        Condition<String> condition = new ValueCondition<>(resourceAttr, Operator.EQUALS, "sensitive-doc");
        Policy policy = new Policy("policy-deny", "Deny Policy", List.of(condition), Effect.DENY);

        // Create a test context that matches the deny condition
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("resource", "sensitive-doc");

        TestEvaluationContext context = new TestEvaluationContext("read", contextMap, List.of(policy));

        // Evaluate the context
        PolicyEvaluationResult result = contextEvaluator.evaluate(context);

        // Assert - deny should be returned
        assertEquals(PolicyEvaluationResult.DENY, result);
    }

    @Test
    void testEvaluateWithDenyTakesPrecedence() {
        // Create one allow policy and one deny policy
        Condition<String> resourceCondition = new ValueCondition<>(resourceAttr, Operator.EQUALS, "document1");
        Policy allowPolicy = new Policy("policy-allow", "Allow Policy", List.of(resourceCondition), Effect.ALLOW);

        Condition<Integer> ageCondition = new ValueCondition<>(ageAttr, Operator.LESS_THAN, 18);
        Policy denyPolicy = new Policy("policy-deny", "Deny Policy", List.of(ageCondition), Effect.DENY);

        // Create a test context that matches both policies
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("resource", "document1");
        contextMap.put("age", 17);

        TestEvaluationContext context = new TestEvaluationContext(
                "read",
                contextMap,
                Arrays.asList(allowPolicy, denyPolicy)
        );

        // Evaluate the context
        PolicyEvaluationResult result = contextEvaluator.evaluate(context);

        // Assert - deny should take precedence over allow
        assertEquals(PolicyEvaluationResult.DENY, result);
    }

    @Test
    void testEvaluateWithAllowAndNotApplicable() {
        // Create an allow policy and a non-applicable policy
        Condition<String> resourceCondition = new ValueCondition<>(resourceAttr, Operator.EQUALS, "document1");
        Policy allowPolicy = new Policy("policy-allow", "Allow Policy", List.of(resourceCondition), Effect.ALLOW);

        Condition<String> wrongResourceCondition = new ValueCondition<>(resourceAttr, Operator.EQUALS, "document2");
        Policy notApplicablePolicy = new Policy("policy-na", "NA Policy", List.of(wrongResourceCondition), Effect.ALLOW);

        // Create a test context that matches only the allow policy
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("resource", "document1");

        TestEvaluationContext context = new TestEvaluationContext(
                "read",
                contextMap,
                Arrays.asList(allowPolicy, notApplicablePolicy)
        );

        // Evaluate the context
        PolicyEvaluationResult result = contextEvaluator.evaluate(context);

        // Assert - if at least one allows and none deny, should allow
        assertEquals(PolicyEvaluationResult.ALLOW, result);
    }

    @Test
    void testEvaluateForSpecificAction() {
        // Create policies
        Condition<String> resourceCondition = new ValueCondition<>(resourceAttr, Operator.EQUALS, "document1");

        Policy readPolicy = new Policy(
                "policy-read",
                "Read Policy",
                List.of(resourceCondition),
                Effect.ALLOW
        );

        Policy writePolicy = new Policy(
                "policy-write",
                "Write Policy",
                List.of(resourceCondition),
                Effect.DENY
        );

        // Create action-specific test context
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("resource", "document1");

        TestEvaluationContext context = new TestEvaluationContext(
                Arrays.asList("read", "write"),
                contextMap
        ) {
            @Override
            public List<Policy> policiesForAction(String action) {
                if ("read".equals(action)) {
                    return Collections.singletonList(readPolicy);
                } else if ("write".equals(action)) {
                    return Collections.singletonList(writePolicy);
                } else {
                    return Collections.emptyList();
                }
            }

            @Override
            public List<Policy> policies() {
                return Arrays.asList(readPolicy, writePolicy);
            }
        };

        // Evaluate for the read action
        PolicyEvaluationResult readResult = contextEvaluator.evaluateForAction(context, "read");

        // Evaluate for the write action
        PolicyEvaluationResult writeResult = contextEvaluator.evaluateForAction(context, "write");

        // Evaluate for non-existent action
        PolicyEvaluationResult unknownResult = contextEvaluator.evaluateForAction(context, "delete");

        // Assert
        assertEquals(PolicyEvaluationResult.ALLOW, readResult);
        assertEquals(PolicyEvaluationResult.DENY, writeResult);
        assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, unknownResult);
    }

    @Test
    void testEvaluateForActionWithNoPolicies() {
        // Create a test context with no policies for a specific action
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("resource", "document1");

        TestEvaluationContext context = new TestEvaluationContext(
                Arrays.asList("read", "write"),
                contextMap
        ) {
            @Override
            public List<Policy> policiesForAction(String action) {
                return Collections.emptyList();
            }

            @Override
            public List<Policy> policies() {
                return Collections.emptyList();
            }
        };

        // Evaluate for the read action
        PolicyEvaluationResult result = contextEvaluator.evaluateForAction(context, "read");

        // Assert - should return NOT_APPLICABLE when no policies exist for the action
        assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);
    }

    @Test
    void testEvaluateAllActions() {
        // Create policies
        Condition<String> resourceCondition = new ValueCondition<>(resourceAttr, Operator.EQUALS, "document1");
        Condition<String> readCondition = new ValueCondition<>(actionAttr, Operator.EQUALS, "read");
        Condition<String> writeCondition = new ValueCondition<>(actionAttr, Operator.EQUALS, "write");

        Policy readPolicy = new Policy(
                "policy-read",
                "Read Policy",
                List.of(resourceCondition, readCondition),
                Effect.ALLOW
        );

        Policy writePolicy = new Policy(
                "policy-write",
                "Write Policy",
                List.of(resourceCondition, writeCondition),
                Effect.DENY
        );

        // Create a test context with multiple actions
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("resource", "document1");

        TestEvaluationContext context = new TestEvaluationContext(
                Arrays.asList("read", "write", "delete"),
                contextMap
        ) {
            @Override
            public List<Policy> policiesForAction(String action) {
                if ("read".equals(action)) {
                    contextMap.put("action", "read");
                    return Arrays.asList(readPolicy, writePolicy);
                } else if ("write".equals(action)) {
                    contextMap.put("action", "write");
                    return Arrays.asList(readPolicy, writePolicy);
                } else if ("delete".equals(action)) {
                    contextMap.put("action", "delete");
                    return Arrays.asList(readPolicy, writePolicy);
                } else {
                    return Arrays.asList(readPolicy, writePolicy);
                }
            }

            @Override
            public List<Policy> policies() {
                return Arrays.asList(readPolicy, writePolicy);
            }
        };

        // Evaluate all actions
        Map<String, PolicyEvaluationResult> results = contextEvaluator.evaluateAllActions(context);

        // Assert
        assertEquals(3, results.size());
        assertEquals(PolicyEvaluationResult.ALLOW, results.get("read"));
        assertEquals(PolicyEvaluationResult.DENY, results.get("write"));
        assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, results.get("delete"));
    }

    @Test
    void testEvaluateWithMultipleConditions() {
        // Create a policy with multiple conditions that must all be met
        Condition<String> resourceCondition = new ValueCondition<>(resourceAttr, Operator.EQUALS, "document1");
        Condition<Integer> ageCondition = new ValueCondition<>(ageAttr, Operator.GREATER_THAN, 17);

        Policy policy = new Policy(
                "policy-multi",
                "Multi-condition Policy",
                Arrays.asList(resourceCondition, ageCondition),
                Effect.ALLOW
        );

        // Create test contexts
        Map<String, Object> matchingContext = new HashMap<>();
        matchingContext.put("resource", "document1");
        matchingContext.put("age", 21);

        Map<String, Object> partialMatchContext = new HashMap<>();
        partialMatchContext.put("resource", "document1");
        partialMatchContext.put("age", 17);

        TestEvaluationContext matchingTestContext = new TestEvaluationContext(
                "read",
                matchingContext,
                Collections.singletonList(policy)
        );

        TestEvaluationContext partialMatchTestContext = new TestEvaluationContext(
                "read",
                partialMatchContext,
                Collections.singletonList(policy)
        );

        // Evaluate contexts
        PolicyEvaluationResult matchingResult = contextEvaluator.evaluate(matchingTestContext);
        PolicyEvaluationResult partialMatchResult = contextEvaluator.evaluate(partialMatchTestContext);

        // Assert
        assertEquals(PolicyEvaluationResult.ALLOW, matchingResult);
        assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, partialMatchResult);
    }

    /**
     * Simple implementation of EvaluationContext for testing.
     */
    private static class TestEvaluationContext extends EvaluationContext {
        private List<Policy> policyList;

        public TestEvaluationContext(String action, Map<String, Object> context, List<Policy> policies) {
            super(action, context);
            this.policyList = policies;
        }

        public TestEvaluationContext(Collection<String> actions, Map<String, Object> context, List<Policy> policies) {
            super(actions, context);
            this.policyList = policies;
        }

        public TestEvaluationContext(Collection<String> actions, Map<String, Object> context) {
            super(actions, context);
            this.policyList = new ArrayList<>();
        }

        @Override
        public List<Policy> policies() {
            return policyList;
        }
    }
}