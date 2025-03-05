package org.binaryhive.policyengine.condition;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.binaryhive.policyengine.model.Attribute;
import org.binaryhive.policyengine.model.Operator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ListConditionTest {

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create condition with IN operator")
    void shouldCreateConditionWithInOperator() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", "value2");

      assertDoesNotThrow(() -> new ListCondition<>(attribute, Operator.IN, values));
    }

    @Test
    @DisplayName("Should create condition with NOT_IN operator")
    void shouldCreateConditionWithNotInOperator() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", "value2");

      assertDoesNotThrow(() -> new ListCondition<>(attribute, Operator.NOT_IN, values));
    }

    @Test
    @DisplayName("Should throw exception for EQUALS operator")
    void shouldThrowExceptionForEqualsOperator() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", "value2");

      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> new ListCondition<>(attribute, Operator.EQUALS, values));

      assertTrue(exception.getMessage().contains("only supports IN or NOT_IN"));
    }

    @Test
    @DisplayName("Should throw exception for other operators")
    void shouldThrowExceptionForOtherOperators() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", "value2");

      assertThrows(
          IllegalArgumentException.class,
          () -> new ListCondition<>(attribute, Operator.GREATER_THAN, values));
      assertThrows(
          IllegalArgumentException.class,
          () -> new ListCondition<>(attribute, Operator.LESS_THAN, values));
    }
  }

  @Nested
  @DisplayName("IN Operator Tests")
  class InOperatorTests {

    @Test
    @DisplayName("Should return true when context value is in the list")
    void shouldReturnTrueWhenContextValueIsInTheList() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", "value2", "value3");
      ListCondition<String> condition = new ListCondition<>(attribute, Operator.IN, values);

      assertTrue(condition.evaluate("value1"));
      assertTrue(condition.evaluate("value2"));
      assertTrue(condition.evaluate("value3"));
    }

    @Test
    @DisplayName("Should return false when context value is not in the list")
    void shouldReturnFalseWhenContextValueIsNotInTheList() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", "value2", "value3");
      ListCondition<String> condition = new ListCondition<>(attribute, Operator.IN, values);

      assertFalse(condition.evaluate("value4"));
      assertFalse(condition.evaluate("something else"));
    }

    @Test
    @DisplayName("Should return false when context value is null")
    void shouldReturnFalseWhenContextValueIsNull() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", "value2", "value3");
      ListCondition<String> condition = new ListCondition<>(attribute, Operator.IN, values);

      assertFalse(condition.evaluate(null));
    }

    @Test
    @DisplayName("Should return false when list is empty")
    void shouldReturnFalseWhenListIsEmpty() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Collections.emptyList();
      ListCondition<String> condition = new ListCondition<>(attribute, Operator.IN, values);

      assertFalse(condition.evaluate("value1"));
    }

    @Test
    @DisplayName("Should handle null values in the list")
    void shouldHandleNullValuesInTheList() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", null, "value3");
      ListCondition<String> condition = new ListCondition<>(attribute, Operator.IN, values);

      assertTrue(condition.evaluate("value1"));
      // A null in the list doesn't match a null context value
      // due to the early return in the evaluate method
      assertFalse(condition.evaluate(null));
    }
  }

  @Nested
  @DisplayName("NOT_IN Operator Tests")
  class NotInOperatorTests {

    @Test
    @DisplayName("Should return false when context value is in the list")
    void shouldReturnFalseWhenContextValueIsInTheList() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", "value2", "value3");
      ListCondition<String> condition = new ListCondition<>(attribute, Operator.NOT_IN, values);

      assertFalse(condition.evaluate("value1"));
      assertFalse(condition.evaluate("value2"));
      assertFalse(condition.evaluate("value3"));
    }

    @Test
    @DisplayName("Should return true when context value is not in the list")
    void shouldReturnTrueWhenContextValueIsNotInTheList() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", "value2", "value3");
      ListCondition<String> condition = new ListCondition<>(attribute, Operator.NOT_IN, values);

      assertTrue(condition.evaluate("value4"));
      assertTrue(condition.evaluate("something else"));
    }

    @Test
    @DisplayName("Should return true when context value is null")
    void shouldReturnTrueWhenContextValueIsNull() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", "value2", "value3");
      ListCondition<String> condition = new ListCondition<>(attribute, Operator.NOT_IN, values);

      assertTrue(condition.evaluate(null));
    }

    @Test
    @DisplayName("Should return true when list is empty")
    void shouldReturnTrueWhenListIsEmpty() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Collections.emptyList();
      ListCondition<String> condition = new ListCondition<>(attribute, Operator.NOT_IN, values);

      assertTrue(condition.evaluate("value1"));
    }
  }

  @Nested
  @DisplayName("Type-specific Tests")
  class TypeSpecificTests {

    enum TestEnum {
      LOW,
      MEDIUM,
      HIGH
    }

    @Test
    @DisplayName("Should work with enum values")
    void shouldWorkWithEnumValues() {
      Attribute<TestEnum> attribute = Attribute.enumType("priority", TestEnum.class);
      List<TestEnum> values = Arrays.asList(TestEnum.LOW, TestEnum.HIGH);

      ListCondition<TestEnum> condition = new ListCondition<>(attribute, Operator.IN, values);

      assertTrue(condition.evaluate(TestEnum.LOW));
      assertFalse(condition.evaluate(TestEnum.MEDIUM));
      assertTrue(condition.evaluate(TestEnum.HIGH));
    }

    @Test
    @DisplayName("Should work with numeric values")
    void shouldWorkWithNumericValues() {
      Attribute<Integer> attribute = Attribute.integer("test");
      List<Integer> values = Arrays.asList(1, 3, 5, 7, 9);

      ListCondition<Integer> condition = new ListCondition<>(attribute, Operator.IN, values);

      assertTrue(condition.evaluate(5));
      assertFalse(condition.evaluate(6));

      // Test converted values
      Integer converted = attribute.convert("7");
      assertTrue(condition.evaluate(converted));
    }
  }

  @Nested
  @DisplayName("Getter Tests")
  class GetterTests {

    @Test
    @DisplayName("Should return correct attribute")
    void shouldReturnCorrectAttribute() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", "value2");
      ListCondition<String> condition = new ListCondition<>(attribute, Operator.IN, values);

      assertSame(attribute, condition.getAttribute());
    }

    @Test
    @DisplayName("Should return correct operator")
    void shouldReturnCorrectOperator() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", "value2");
      ListCondition<String> condition = new ListCondition<>(attribute, Operator.IN, values);

      assertEquals(Operator.IN, condition.getOperator());
    }

    @Test
    @DisplayName("Should return correct values list")
    void shouldReturnCorrectValuesList() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("value1", "value2");
      ListCondition<String> condition = new ListCondition<>(attribute, Operator.IN, values);

      assertSame(values, condition.getValues());
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCases {

    @Test
    @DisplayName("Should handle case sensitivity")
    void shouldHandleCaseSensitivity() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("Value1", "Value2");

      ListCondition<String> condition = new ListCondition<>(attribute, Operator.IN, values);

      assertTrue(condition.evaluate("Value1"));
      assertFalse(condition.evaluate("value1")); // Case-sensitive comparison
    }

    @Test
    @DisplayName("Should handle empty values")
    void shouldHandleEmptyValues() {
      Attribute<String> attribute = Attribute.string("test");
      List<String> values = Arrays.asList("", "value2");

      ListCondition<String> condition = new ListCondition<>(attribute, Operator.IN, values);

      assertTrue(condition.evaluate(""));
      assertFalse(condition.evaluate("something else"));
    }
  }
}
