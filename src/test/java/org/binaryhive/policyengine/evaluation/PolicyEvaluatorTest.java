package org.binaryhive.policyengine.evaluation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.binaryhive.policyengine.condition.Condition;
import org.binaryhive.policyengine.condition.ListCondition;
import org.binaryhive.policyengine.condition.ValueCondition;
import org.binaryhive.policyengine.model.Attribute;
import org.binaryhive.policyengine.model.Effect;
import org.binaryhive.policyengine.model.Operator;
import org.binaryhive.policyengine.model.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyEvaluatorTest {

  private PolicyEvaluator evaluator;

  @Mock private Policy mockPolicy;

  @BeforeEach
  void setUp() {
    evaluator = new PolicyEvaluator();
  }

  @Nested
  @DisplayName("Basic Policy Tests")
  class BasicPolicyTests {

    @Test
    @DisplayName("Should return ALLOW for policy with no conditions and ALLOW effect")
    void shouldReturnAllowForPolicyWithNoConditionsAndAllowEffect() {
      // Setup
      List<Condition<?>> emptyConditions = Collections.emptyList();
      when(mockPolicy.getConditions()).thenReturn(emptyConditions);
      when(mockPolicy.getEffect()).thenReturn(Effect.ALLOW);

      // Execute
      PolicyEvaluationResult result = evaluator.evaluate(mockPolicy, Collections.emptyMap());

      // Verify
      assertEquals(PolicyEvaluationResult.ALLOW, result);
    }

    @Test
    @DisplayName("Should return DENY for policy with no conditions and DENY effect")
    void shouldReturnDenyForPolicyWithNoConditionsAndDenyEffect() {
      // Setup
      List<Condition<?>> emptyConditions = Collections.emptyList();
      when(mockPolicy.getConditions()).thenReturn(emptyConditions);
      when(mockPolicy.getEffect()).thenReturn(Effect.DENY);

      // Execute
      PolicyEvaluationResult result = evaluator.evaluate(mockPolicy, Collections.emptyMap());

      // Verify
      assertEquals(PolicyEvaluationResult.DENY, result);
    }

    @Test
    @DisplayName("Should return NOT_APPLICABLE when any condition is not met")
    void shouldReturnNotApplicableWhenAnyConditionIsNotMet() {
      // Setup
      Attribute<String> attribute = Attribute.string("testAttr");
      Condition<String> condition = new ValueCondition<>(attribute, Operator.EQUALS, "value");

      List<Condition<?>> conditions = Collections.singletonList(condition);
      when(mockPolicy.getConditions()).thenReturn(conditions);

      Map<String, Object> context = new HashMap<>();
      context.put("testAttr", "different-value");

      // Execute
      PolicyEvaluationResult result = evaluator.evaluate(mockPolicy, context);

      // Verify
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);
    }
  }

  @Nested
  @DisplayName("Complex Policy Tests")
  class ComplexPolicyTests {

    private Attribute<String> roleAttr;
    private Attribute<Integer> ageAttr;

    @BeforeEach
    void setUp() {
      roleAttr = Attribute.string("role");
      ageAttr = Attribute.integer("age");
    }

    @Test
    @DisplayName("Should evaluate complex policy with multiple conditions")
    void shouldEvaluateComplexPolicyWithMultipleConditions() {
      // Create a policy with multiple conditions
      Policy policy = new Policy();
      policy.setPolicyId("test-policy");
      policy.setDescription("Complex test policy");
      policy.setEffect(Effect.ALLOW);

      List<Condition<?>> conditions = new ArrayList<>();

      // User must be admin
      conditions.add(new ValueCondition<>(roleAttr, Operator.EQUALS, "admin"));

      // User must be at least 18
      conditions.add(new ValueCondition<>(ageAttr, Operator.GREATER_THAN, 17));

      // User must have either 'read' or 'write' permission
      Attribute<String> permissionAttr = Attribute.string("permission");
      conditions.add(
          new ListCondition<>(permissionAttr, Operator.IN, Arrays.asList("read", "write")));

      policy.setConditions(conditions);

      // Test with a context that meets all conditions
      Map<String, Object> context = new HashMap<>();
      context.put("userId", "user123");
      context.put("role", "admin");
      context.put("age", 25);
      context.put("permission", "read");

      PolicyEvaluationResult result = evaluator.evaluate(policy, context);
      assertEquals(PolicyEvaluationResult.ALLOW, result);

      // Test with a context that doesn't meet the age condition
      context.put("age", 16);
      result = evaluator.evaluate(policy, context);
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);

      // Test with a context that doesn't meet the role condition
      context.put("age", 25);
      context.put("role", "user");
      result = evaluator.evaluate(policy, context);
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);

      // Test with a context that doesn't meet the permissions condition
      context.put("role", "admin");
      context.put("permission", "delete");
      result = evaluator.evaluate(policy, context);
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);
    }
  }

  @Nested
  @DisplayName("Type Conversion Tests")
  class TypeConversionTests {

    @Test
    @DisplayName("Should convert string to integer for numeric comparison")
    void shouldConvertStringToIntegerForNumericComparison() {
      // Setup
      Attribute<Integer> ageAttr = Attribute.integer("age");
      Condition<Integer> condition = new ValueCondition<>(ageAttr, Operator.GREATER_THAN, 18);

      Policy policy = new Policy();
      policy.setPolicyId("age-check");
      policy.setDescription("Check if user is an adult");
      policy.setEffect(Effect.ALLOW);
      policy.setConditions(Collections.singletonList(condition));

      // Context with string value for age
      Map<String, Object> context = new HashMap<>();
      context.put("age", "25");

      // Execute
      PolicyEvaluationResult result = evaluator.evaluate(policy, context);

      // Verify
      assertEquals(PolicyEvaluationResult.ALLOW, result);

      // Test with a value that fails conversion
      context.put("age", "not-a-number");
      result = evaluator.evaluate(policy, context);
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);
    }

    @Test
    @DisplayName("Should handle conversion of comma-separated string to list")
    void shouldHandleConversionOfCommaSeparatedStringToList() {
      // Setup
      Attribute<String> roleAttr = Attribute.string("role");

      // We're checking if any specific role is in the allowed list
      var condition = new ListCondition<>(roleAttr, Operator.IN, Arrays.asList("admin", "manager"));

      Policy policy = new Policy();
      policy.setPolicyId("role-check");
      policy.setDescription("Check if user has admin or manager role");
      policy.setEffect(Effect.ALLOW);
      policy.setConditions(Collections.singletonList(condition));

      // Context with a single role
      Map<String, Object> context = new HashMap<>();
      context.put("role", "admin");

      // Execute
      PolicyEvaluationResult result = evaluator.evaluate(policy, context);

      // Verify - should match because "admin" is in the allowed list
      assertEquals(PolicyEvaluationResult.ALLOW, result);

      // Test with a different role
      context.put("role", "user");
      result = evaluator.evaluate(policy, context);
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);
    }
  }

  @Nested
  @DisplayName("Null and Error Handling Tests")
  class NullAndErrorHandlingTests {

    @Test
    @DisplayName("Should handle missing attributes in context")
    void shouldHandleMissingAttributesInContext() {
      // Setup
      Attribute<String> userIdAttr = Attribute.string("userId");
      Condition<String> condition =
          new ValueCondition<>(userIdAttr, Operator.NOT_EQUALS, "blocked-user");

      Policy policy = new Policy();
      policy.setPolicyId("user-check");
      policy.setDescription("Check if user is not blocked");
      policy.setEffect(Effect.ALLOW);
      policy.setConditions(Collections.singletonList(condition));

      // Empty context - userId is missing
      Map<String, Object> context = new HashMap<>();

      // Execute
      PolicyEvaluationResult result = evaluator.evaluate(policy, context);

      // Verify - NOT_EQUALS null should return true
      assertEquals(PolicyEvaluationResult.ALLOW, result);

      // Now test with EQUALS - this should fail
      condition = new ValueCondition<>(userIdAttr, Operator.EQUALS, "valid-user");
      policy.setConditions(Collections.singletonList(condition));

      result = evaluator.evaluate(policy, context);
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);
    }

    @Test
    @DisplayName("Should handle null values in context")
    void shouldHandleNullValuesInContext() {
      // Setup
      Attribute<String> statusAttr = Attribute.string("status");
      Condition<String> condition = new ValueCondition<>(statusAttr, Operator.EQUALS, "active");

      Policy policy = new Policy();
      policy.setPolicyId("status-check");
      policy.setDescription("Check if status is active");
      policy.setEffect(Effect.ALLOW);
      policy.setConditions(Collections.singletonList(condition));

      // Context with explicit null value
      Map<String, Object> context = new HashMap<>();
      context.put("status", null);

      // Execute
      PolicyEvaluationResult result = evaluator.evaluate(policy, context);

      // Verify - EQUALS with null contextValue should return false
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);
    }
  }

  @Nested
  @DisplayName("Real-world Scenario Tests")
  class RealWorldScenarioTests {

    private Policy createContentAccessPolicy() {
      // Attributes
      Attribute<String> userRoleAttr = Attribute.string("userRole");
      Attribute<Integer> contentLevelAttr = Attribute.integer("contentLevel");
      Attribute<String> contentOwnerAttr = Attribute.string("contentOwner");
      Attribute<String> userIdAttr = Attribute.string("userId");
      Attribute<String> contentTypeAttr = Attribute.string("contentType");
      Attribute<List<String>> allowedTypesAttr =
          Attribute.list("allowedTypes", Attribute.string("type"));

      // Create policy
      Policy policy = new Policy();
      policy.setPolicyId("content-access");
      policy.setDescription("Determines if a user can access content");
      policy.setEffect(Effect.ALLOW);

      List<Condition<?>> conditions = new ArrayList<>();

      // Condition 1: User must be admin, editor, or the content owner
      Attribute<String> roleAttr = Attribute.string("userRole");
      conditions.add(new ListCondition<>(roleAttr, Operator.IN, Arrays.asList("admin", "editor")));

      // Condition 2: If not admin, content level must be <= 3
      ValueCondition<String> adminRoleCondition =
          new ValueCondition<>(userRoleAttr, Operator.EQUALS, "admin");

      ValueCondition<Integer> contentLevelCondition =
          new ValueCondition<>(contentLevelAttr, Operator.LESS_THAN, 4);

      // Condition 3: If not admin or editor, user must be content owner
      ValueCondition<String> contentOwnerCondition =
          new ValueCondition<>(
              contentOwnerAttr, Operator.EQUALS, ""); // Placeholder, will use context

      // Condition 4: Content type must be in allowed types for the user
      ListCondition<String> contentTypeCondition =
          new ListCondition<>(
              contentTypeAttr, Operator.IN, Arrays.asList("document", "image", "video"));

      policy.setConditions(conditions);

      return policy;
    }

    @Test
    @DisplayName("Should handle complex content access control scenario")
    void shouldHandleComplexContentAccessControlScenario() {
      // Create a complex policy for content access
      Attribute<String> userRoleAttr = Attribute.string("userRole");
      Attribute<Integer> contentLevelAttr = Attribute.integer("contentLevel");
      Attribute<String> userIdAttr = Attribute.string("userId");
      Attribute<String> contentOwnerAttr = Attribute.string("contentOwner");
      Attribute<String> contentTypeAttr = Attribute.string("contentType");

      Policy policy = new Policy();
      policy.setPolicyId("content-access");
      policy.setDescription("Determines if a user can access content");
      policy.setEffect(Effect.ALLOW);

      List<Condition<?>> conditions = new ArrayList<>();

      // Content level must be less than or equal to 3 for non-admins
      conditions.add(new ValueCondition<>(contentLevelAttr, Operator.LESS_THAN, 4));

      // User must be admin, editor, or the content owner
      conditions.add(
          new ListCondition<>(userRoleAttr, Operator.IN, Arrays.asList("admin", "editor")));

      // Content type must be document, image, or video
      conditions.add(
          new ListCondition<>(
              contentTypeAttr, Operator.IN, Arrays.asList("document", "image", "video")));

      policy.setConditions(conditions);

      // Test case 1: Admin accessing level 5 document
      Map<String, Object> context = new HashMap<>();
      context.put("userId", "user123");
      context.put("userRole", "admin");
      context.put("contentLevel", 5);
      context.put("contentOwner", "user456");
      context.put("contentType", "document");

      PolicyEvaluationResult result = evaluator.evaluate(policy, context);
      // Admin should have access, but content level is too high
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);

      // Test case 2: Editor accessing level 2 document
      context.put("userRole", "editor");
      context.put("contentLevel", 2);

      result = evaluator.evaluate(policy, context);
      assertEquals(PolicyEvaluationResult.ALLOW, result);

      // Test case 3: Regular user trying to access content
      context.put("userRole", "user");

      result = evaluator.evaluate(policy, context);
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);

      // Test case 4: Editor trying to access unsupported content type
      context.put("userRole", "editor");
      context.put("contentType", "audio");

      result = evaluator.evaluate(policy, context);
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);
    }

    @Test
    @DisplayName("Should evaluate multiple policies in order")
    void shouldEvaluateMultiplePoliciesInOrder() {
      // Define some common attributes
      Attribute<String> actionAttr = Attribute.string("action");
      Attribute<String> resourceAttr = Attribute.string("resource");
      Attribute<String> userRoleAttr = Attribute.string("userRole");

      // Policy 1: Deny admins from deleting system resources
      Policy denyPolicy = new Policy();
      denyPolicy.setPolicyId("deny-system-deletion");
      denyPolicy.setDescription("Deny admins from deleting system resources");
      denyPolicy.setEffect(Effect.DENY);

      List<Condition<?>> denyConditions = new ArrayList<>();
      denyConditions.add(new ValueCondition<>(userRoleAttr, Operator.EQUALS, "admin"));
      denyConditions.add(new ValueCondition<>(actionAttr, Operator.EQUALS, "delete"));
      denyConditions.add(new ValueCondition<>(resourceAttr, Operator.EQUALS, "system"));

      denyPolicy.setConditions(denyConditions);

      // Policy 2: Allow admins to perform any action
      Policy adminPolicy = new Policy();
      adminPolicy.setPolicyId("admin-permissions");
      adminPolicy.setDescription("Allow admins to perform any action");
      adminPolicy.setEffect(Effect.ALLOW);

      List<Condition<?>> adminConditions = new ArrayList<>();
      adminConditions.add(new ValueCondition<>(userRoleAttr, Operator.EQUALS, "admin"));

      adminPolicy.setConditions(adminConditions);

      // Policy 3: Allow users to view resources
      Policy viewPolicy = new Policy();
      viewPolicy.setPolicyId("view-permissions");
      viewPolicy.setDescription("Allow users to view resources");
      viewPolicy.setEffect(Effect.ALLOW);

      List<Condition<?>> viewConditions = new ArrayList<>();
      viewConditions.add(new ValueCondition<>(actionAttr, Operator.EQUALS, "view"));

      viewPolicy.setConditions(viewConditions);

      // Create a list of policies in evaluation order
      List<Policy> policies = Arrays.asList(denyPolicy, adminPolicy, viewPolicy);

      // Test case 1: Admin trying to delete system resource
      Map<String, Object> context = new HashMap<>();
      context.put("userRole", "admin");
      context.put("action", "delete");
      context.put("resource", "system");

      // DENY should take precedence
      PolicyEvaluationResult result = evaluator.evaluate(denyPolicy, context);
      assertEquals(PolicyEvaluationResult.DENY, result);

      // Test case 2: Admin viewing a resource
      context.put("action", "view");

      // Admin should be allowed to view
      result = evaluator.evaluate(adminPolicy, context);
      assertEquals(PolicyEvaluationResult.ALLOW, result);

      // Test case 3: Regular user viewing a resource
      context.put("userRole", "user");

      // Admin policy shouldn't apply
      result = evaluator.evaluate(adminPolicy, context);
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);

      // But view policy should apply
      result = evaluator.evaluate(viewPolicy, context);
      assertEquals(PolicyEvaluationResult.ALLOW, result);

      // Test case 4: Regular user deleting a resource
      context.put("action", "delete");

      // No policy applies
      result = evaluator.evaluate(denyPolicy, context);
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);
      result = evaluator.evaluate(adminPolicy, context);
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);
      result = evaluator.evaluate(viewPolicy, context);
      assertEquals(PolicyEvaluationResult.NOT_APPLICABLE, result);
    }
  }
}
