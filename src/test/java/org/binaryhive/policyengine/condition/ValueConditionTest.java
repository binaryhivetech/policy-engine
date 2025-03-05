package org.binaryhive.policyengine.condition;

import static org.junit.jupiter.api.Assertions.*;

import org.binaryhive.policyengine.model.Attribute;
import org.binaryhive.policyengine.model.Operator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ValueConditionTest {

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create condition with valid operators")
    void shouldCreateConditionWithValidOperators() {
      Attribute<String> attribute = Attribute.string("test");

      assertDoesNotThrow(() -> new ValueCondition<>(attribute, Operator.EQUALS, "value"));
      assertDoesNotThrow(() -> new ValueCondition<>(attribute, Operator.NOT_EQUALS, "value"));
      assertDoesNotThrow(() -> new ValueCondition<>(attribute, Operator.GREATER_THAN, "value"));
      assertDoesNotThrow(() -> new ValueCondition<>(attribute, Operator.LESS_THAN, "value"));
    }

    @Test
    @DisplayName("Should throw exception for IN operator")
    void shouldThrowExceptionForInOperator() {
      Attribute<String> attribute = Attribute.string("test");

      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> new ValueCondition<>(attribute, Operator.IN, "value"));

      assertTrue(exception.getMessage().contains("Use ListCondition"));
    }

    @Test
    @DisplayName("Should throw exception for NOT_IN operator")
    void shouldThrowExceptionForNotInOperator() {
      Attribute<String> attribute = Attribute.string("test");

      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> new ValueCondition<>(attribute, Operator.NOT_IN, "value"));

      assertTrue(exception.getMessage().contains("Use ListCondition"));
    }
  }

  @Nested
  @DisplayName("EQUALS Operator Tests")
  class EqualsOperatorTests {

    @Test
    @DisplayName("Should return true when context value equals condition value")
    void shouldReturnTrueWhenContextValueEqualsConditionValue() {
      Attribute<String> attribute = Attribute.string("test");
      ValueCondition<String> condition = new ValueCondition<>(attribute, Operator.EQUALS, "value");

      assertTrue(condition.evaluate("value"));
    }

    @Test
    @DisplayName("Should return false when context value does not equal condition value")
    void shouldReturnFalseWhenContextValueDoesNotEqualConditionValue() {
      Attribute<String> attribute = Attribute.string("test");
      ValueCondition<String> condition = new ValueCondition<>(attribute, Operator.EQUALS, "value");

      assertFalse(condition.evaluate("different"));
    }

    @Test
    @DisplayName("Should return false when context value is null")
    void shouldReturnFalseWhenContextValueIsNull() {
      Attribute<String> attribute = Attribute.string("test");
      ValueCondition<String> condition = new ValueCondition<>(attribute, Operator.EQUALS, "value");

      assertFalse(condition.evaluate(null));
    }

    @Test
    @DisplayName("Should return true when both context and condition values are null")
    void shouldReturnTrueWhenBothContextAndConditionValuesAreNull() {
      Attribute<String> attribute = Attribute.string("test");
      ValueCondition<String> condition = new ValueCondition<>(attribute, Operator.EQUALS, null);

      assertTrue(condition.evaluate(null));
    }
  }

  @Nested
  @DisplayName("NOT_EQUALS Operator Tests")
  class NotEqualsOperatorTests {

    @Test
    @DisplayName("Should return false when context value equals condition value")
    void shouldReturnFalseWhenContextValueEqualsConditionValue() {
      Attribute<String> attribute = Attribute.string("test");
      ValueCondition<String> condition =
          new ValueCondition<>(attribute, Operator.NOT_EQUALS, "value");

      assertFalse(condition.evaluate("value"));
    }

    @Test
    @DisplayName("Should return true when context value does not equal condition value")
    void shouldReturnTrueWhenContextValueDoesNotEqualConditionValue() {
      Attribute<String> attribute = Attribute.string("test");
      ValueCondition<String> condition =
          new ValueCondition<>(attribute, Operator.NOT_EQUALS, "value");

      assertTrue(condition.evaluate("different"));
    }

    @Test
    @DisplayName("Should return true when context value is null but condition value is not")
    void shouldReturnTrueWhenContextValueIsNullButConditionValueIsNot() {
      Attribute<String> attribute = Attribute.string("test");
      ValueCondition<String> condition =
          new ValueCondition<>(attribute, Operator.NOT_EQUALS, "value");

      assertTrue(condition.evaluate(null));
    }

    @Test
    @DisplayName("Should return false when both context and condition values are null")
    void shouldReturnFalseWhenBothContextAndConditionValuesAreNull() {
      Attribute<String> attribute = Attribute.string("test");
      ValueCondition<String> condition = new ValueCondition<>(attribute, Operator.NOT_EQUALS, null);

      assertFalse(condition.evaluate(null));
    }
  }

  @Nested
  @DisplayName("GREATER_THAN Operator Tests")
  class GreaterThanOperatorTests {

    @Test
    @DisplayName("Should return true when context value is greater than condition value")
    void shouldReturnTrueWhenContextValueIsGreaterThanConditionValue() {
      Attribute<Integer> attribute = Attribute.integer("test");
      ValueCondition<Integer> condition = new ValueCondition<>(attribute, Operator.GREATER_THAN, 5);

      assertTrue(condition.evaluate(10));
    }

    @Test
    @DisplayName("Should return false when context value is equal to condition value")
    void shouldReturnFalseWhenContextValueIsEqualToConditionValue() {
      Attribute<Integer> attribute = Attribute.integer("test");
      ValueCondition<Integer> condition = new ValueCondition<>(attribute, Operator.GREATER_THAN, 5);

      assertFalse(condition.evaluate(5));
    }

    @Test
    @DisplayName("Should return false when context value is less than condition value")
    void shouldReturnFalseWhenContextValueIsLessThanConditionValue() {
      Attribute<Integer> attribute = Attribute.integer("test");
      ValueCondition<Integer> condition = new ValueCondition<>(attribute, Operator.GREATER_THAN, 5);

      assertFalse(condition.evaluate(3));
    }

    @Test
    @DisplayName("Should work with string comparisons")
    void shouldWorkWithStringComparisons() {
      Attribute<String> attribute = Attribute.string("test");
      ValueCondition<String> condition =
          new ValueCondition<>(attribute, Operator.GREATER_THAN, "apple");

      assertTrue(condition.evaluate("banana"));
      assertFalse(condition.evaluate("aardvark"));
    }

    @Test
    @DisplayName("Should throw exception when values are not comparable")
    void shouldThrowExceptionWhenValuesAreNotComparable() {
      // Using a mock non-comparable object
      Attribute<Object> attribute = new Attribute<>("test", Object.class, v -> v, v -> true);
      Object value1 = new Object();
      Object value2 = new Object();

      ValueCondition<Object> condition =
          new ValueCondition<>(attribute, Operator.GREATER_THAN, value1);

      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> condition.evaluate(value2));

      assertTrue(exception.getMessage().contains("Cannot compare"));
    }

    @Test
    @DisplayName("Should return false when context value is null")
    void shouldThrowExceptionWhenContextValueIsNull() {
      Attribute<Integer> attribute = Attribute.integer("test");
      ValueCondition<Integer> condition = new ValueCondition<>(attribute, Operator.GREATER_THAN, 5);

      assertFalse(condition.evaluate(null));
    }
  }

  @Nested
  @DisplayName("LESS_THAN Operator Tests")
  class LessThanOperatorTests {

    @Test
    @DisplayName("Should return true when context value is less than condition value")
    void shouldReturnTrueWhenContextValueIsLessThanConditionValue() {
      Attribute<Integer> attribute = Attribute.integer("test");
      ValueCondition<Integer> condition = new ValueCondition<>(attribute, Operator.LESS_THAN, 10);

      assertTrue(condition.evaluate(5));
    }

    @Test
    @DisplayName("Should return false when context value is equal to condition value")
    void shouldReturnFalseWhenContextValueIsEqualToConditionValue() {
      Attribute<Integer> attribute = Attribute.integer("test");
      ValueCondition<Integer> condition = new ValueCondition<>(attribute, Operator.LESS_THAN, 5);

      assertFalse(condition.evaluate(5));
    }

    @Test
    @DisplayName("Should return false when context value is greater than condition value")
    void shouldReturnFalseWhenContextValueIsGreaterThanConditionValue() {
      Attribute<Integer> attribute = Attribute.integer("test");
      ValueCondition<Integer> condition = new ValueCondition<>(attribute, Operator.LESS_THAN, 5);

      assertFalse(condition.evaluate(10));
    }

    @Test
    @DisplayName("Should work with string comparisons")
    void shouldWorkWithStringComparisons() {
      Attribute<String> attribute = Attribute.string("test");
      ValueCondition<String> condition =
          new ValueCondition<>(attribute, Operator.LESS_THAN, "zebra");

      assertTrue(condition.evaluate("apple"));
      assertFalse(condition.evaluate("zzzzzz"));
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
    @DisplayName("Should handle enum comparisons")
    void shouldHandleEnumComparisons() {
      Attribute<TestEnum> attribute = Attribute.enumType("priority", TestEnum.class);

      // EQUALS with enums
      ValueCondition<TestEnum> equalsCondition =
          new ValueCondition<>(attribute, Operator.EQUALS, TestEnum.MEDIUM);
      assertTrue(equalsCondition.evaluate(TestEnum.MEDIUM));
      assertFalse(equalsCondition.evaluate(TestEnum.HIGH));

      // GREATER_THAN with enums
      ValueCondition<TestEnum> greaterCondition =
          new ValueCondition<>(attribute, Operator.GREATER_THAN, TestEnum.LOW);
      assertTrue(greaterCondition.evaluate(TestEnum.MEDIUM));
      assertTrue(greaterCondition.evaluate(TestEnum.HIGH));
      assertFalse(greaterCondition.evaluate(TestEnum.LOW));
    }

    @Test
    @DisplayName("Should handle numeric conversions")
    void shouldHandleNumericConversions() {
      Attribute<Integer> attribute = Attribute.integer("test");

      // Using doubles in context, integers in condition
      ValueCondition<Integer> condition = new ValueCondition<>(attribute, Operator.GREATER_THAN, 5);

      Integer contextValue = attribute.convert(7.5); // Should convert to 7
      assertTrue(condition.evaluate(contextValue));
    }
  }

  @Nested
  @DisplayName("Getter Tests")
  class GetterTests {

    @Test
    @DisplayName("Should return correct attribute")
    void shouldReturnCorrectAttribute() {
      Attribute<String> attribute = Attribute.string("test");
      ValueCondition<String> condition = new ValueCondition<>(attribute, Operator.EQUALS, "value");

      assertSame(attribute, condition.getAttribute());
    }

    @Test
    @DisplayName("Should return correct operator")
    void shouldReturnCorrectOperator() {
      Attribute<String> attribute = Attribute.string("test");
      ValueCondition<String> condition = new ValueCondition<>(attribute, Operator.EQUALS, "value");

      assertEquals(Operator.EQUALS, condition.getOperator());
    }

    @Test
    @DisplayName("Should return correct value")
    void shouldReturnCorrectValue() {
      Attribute<String> attribute = Attribute.string("test");
      String conditionValue = "value";
      ValueCondition<String> condition =
          new ValueCondition<>(attribute, Operator.EQUALS, conditionValue);

      assertSame(conditionValue, condition.getValue());
    }
  }
}
