package org.binaryhive.policyengine.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.binaryhive.policyengine.exception.AttributeConversionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AttributeTest {

  @Nested
  @DisplayName("String Attribute Tests")
  class StringAttributeTests {

    @Test
    @DisplayName("Should create string attribute")
    void shouldCreateStringAttribute() {
      Attribute<String> attr = Attribute.string("testAttr");
      assertEquals("testAttr", attr.getName());
      assertEquals(String.class, attr.getType());
    }

    @Test
    @DisplayName("Should convert String value")
    void shouldConvertStringValue() {
      Attribute<String> attr = Attribute.string("testAttr");
      assertEquals("test", attr.convert("test"));
    }

    @Test
    @DisplayName("Should convert non-String value to String")
    void shouldConvertNonStringValueToString() {
      Attribute<String> attr = Attribute.string("testAttr");
      assertEquals("123", attr.convert(123));
      assertEquals("true", attr.convert(true));
      assertEquals("3.14", attr.convert(3.14));
    }

    @Test
    @DisplayName("Should return null for null input")
    void shouldReturnNullForNullInput() {
      Attribute<String> attr = Attribute.string("testAttr");
      assertNull(attr.convert(null));
    }

    @Test
    @DisplayName("Should validate string with custom validator")
    void shouldValidateStringWithCustomValidator() {
      // Only accepts strings starting with "valid"
      Attribute<String> attr = Attribute.string("testAttr", s -> s.startsWith("valid"));

      assertTrue(attr.isValid("valid123"));
      assertFalse(attr.isValid("invalid"));
    }

    @Test
    @DisplayName("Should throw exception when validation fails")
    void shouldThrowExceptionWhenValidationFails() {
      Attribute<String> attr = Attribute.string("testAttr", s -> s.length() > 5);

      AttributeConversionException exception =
          assertThrows(AttributeConversionException.class, () -> attr.convert("short"));

      assertEquals("Value short is not valid for attribute testAttr", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("Integer Attribute Tests")
  class IntegerAttributeTests {

    @Test
    @DisplayName("Should create integer attribute")
    void shouldCreateIntegerAttribute() {
      Attribute<Integer> attr = Attribute.integer("testAttr");
      assertEquals("testAttr", attr.getName());
      assertEquals(Integer.class, attr.getType());
    }

    @Test
    @DisplayName("Should convert Integer value")
    void shouldConvertIntegerValue() {
      Attribute<Integer> attr = Attribute.integer("testAttr");
      assertEquals(Integer.valueOf(123), attr.convert(123));
    }

    @Test
    @DisplayName("Should convert String to Integer")
    void shouldConvertStringToInteger() {
      Attribute<Integer> attr = Attribute.integer("testAttr");
      assertEquals(Integer.valueOf(123), attr.convert("123"));
    }

    @Test
    @DisplayName("Should convert other Number types to Integer")
    void shouldConvertOtherNumberTypesToInteger() {
      Attribute<Integer> attr = Attribute.integer("testAttr");
      assertEquals(Integer.valueOf(123), attr.convert(123.0));
      assertEquals(Integer.valueOf(123), attr.convert(123L));
      assertEquals(Integer.valueOf(123), attr.convert(123.5f));
    }

    @Test
    @DisplayName("Should throw exception for non-numeric string")
    void shouldThrowExceptionForNonNumericString() {
      Attribute<Integer> attr = Attribute.integer("testAttr");

      AttributeConversionException exception =
          assertThrows(AttributeConversionException.class, () -> attr.convert("not a number"));

      assertTrue(exception.getMessage().contains("Cannot convert"));
    }

    @Test
    @DisplayName("Should throw exception for incompatible types")
    void shouldThrowExceptionForIncompatibleTypes() {
      Attribute<Integer> attr = Attribute.integer("testAttr");

      AttributeConversionException exception =
          assertThrows(AttributeConversionException.class, () -> attr.convert(new Object()));

      assertTrue(exception.getMessage().contains("Cannot convert"));
    }

    @Test
    @DisplayName("Should validate integer with custom validator")
    void shouldValidateIntegerWithCustomValidator() {
      // Only accepts positive integers
      Attribute<Integer> attr = Attribute.integer("testAttr", i -> i > 0);

      assertTrue(attr.isValid(123));
      assertFalse(attr.isValid(-5));
      assertFalse(attr.isValid(0));
    }
  }

  @Nested
  @DisplayName("Enum Attribute Tests")
  class EnumAttributeTests {

    enum TestEnum {
      FIRST,
      SECOND,
      THIRD
    }

    @Test
    @DisplayName("Should create enum attribute")
    void shouldCreateEnumAttribute() {
      Attribute<TestEnum> attr = Attribute.enumType("testAttr", TestEnum.class);
      assertEquals("testAttr", attr.getName());
      assertEquals(TestEnum.class, attr.getType());
    }

    @Test
    @DisplayName("Should convert enum value")
    void shouldConvertEnumValue() {
      Attribute<TestEnum> attr = Attribute.enumType("testAttr", TestEnum.class);
      assertEquals(TestEnum.FIRST, attr.convert(TestEnum.FIRST));
    }

    @Test
    @DisplayName("Should convert String to enum")
    void shouldConvertStringToEnum() {
      Attribute<TestEnum> attr = Attribute.enumType("testAttr", TestEnum.class);
      assertEquals(TestEnum.FIRST, attr.convert("FIRST"));
    }

    @Test
    @DisplayName("Should convert String to enum case-insensitively")
    void shouldConvertStringToEnumCaseInsensitively() {
      Attribute<TestEnum> attr = Attribute.enumType("testAttr", TestEnum.class);
      assertEquals(TestEnum.SECOND, attr.convert("second"));
      assertEquals(TestEnum.THIRD, attr.convert("Third"));
    }

    @Test
    @DisplayName("Should throw exception for invalid enum value")
    void shouldThrowExceptionForInvalidEnumValue() {
      Attribute<TestEnum> attr = Attribute.enumType("testAttr", TestEnum.class);

      AttributeConversionException exception =
          assertThrows(AttributeConversionException.class, () -> attr.convert("FOURTH"));

      assertTrue(exception.getMessage().contains("No enum constant"));
    }

    @Test
    @DisplayName("Should throw exception for incompatible types")
    void shouldThrowExceptionForIncompatibleTypes() {
      Attribute<TestEnum> attr = Attribute.enumType("testAttr", TestEnum.class);

      AttributeConversionException exception =
          assertThrows(AttributeConversionException.class, () -> attr.convert(123));

      assertTrue(exception.getMessage().contains("Cannot convert"));
    }
  }

  @Nested
  @DisplayName("List Attribute Tests")
  class ListAttributeTests {

    @Test
    @DisplayName("Should create list attribute")
    void shouldCreateListAttribute() {
      Attribute<String> stringAttr = Attribute.string("element");
      Attribute<List<String>> listAttr = Attribute.list("testAttr", stringAttr);

      assertEquals("testAttr", listAttr.getName());
      assertEquals(List.class, listAttr.getType());
    }

    @Test
    @DisplayName("Should convert List value")
    void shouldConvertListValue() {
      Attribute<String> stringAttr = Attribute.string("element");
      Attribute<List<String>> listAttr = Attribute.list("testAttr", stringAttr);

      List<String> input = Arrays.asList("one", "two", "three");
      List<String> result = listAttr.convert(input);

      assertEquals(3, result.size());
      assertEquals("one", result.get(0));
      assertEquals("two", result.get(1));
      assertEquals("three", result.get(2));
    }

    @Test
    @DisplayName("Should convert comma-separated String to List")
    void shouldConvertCommaSeparatedStringToList() {
      Attribute<String> stringAttr = Attribute.string("element");
      Attribute<List<String>> listAttr = Attribute.list("testAttr", stringAttr);

      List<String> result = listAttr.convert("one,two,three");

      assertEquals(3, result.size());
      assertEquals("one", result.get(0));
      assertEquals("two", result.get(1));
      assertEquals("three", result.get(2));
    }

    @Test
    @DisplayName("Should handle whitespace in comma-separated String")
    void shouldHandleWhitespaceInCommaSeparatedString() {
      Attribute<String> stringAttr = Attribute.string("element");
      Attribute<List<String>> listAttr = Attribute.list("testAttr", stringAttr);

      List<String> result = listAttr.convert("one, two , three");

      assertEquals(3, result.size());
      assertEquals("one", result.get(0));
      assertEquals("two", result.get(1));
      assertEquals("three", result.get(2));
    }

    @Test
    @DisplayName("Should convert single value to single-element List")
    void shouldConvertSingleValueToSingleElementList() {
      Attribute<String> stringAttr = Attribute.string("element");
      Attribute<List<String>> listAttr = Attribute.list("testAttr", stringAttr);

      List<String> result = listAttr.convert("single");

      assertEquals(1, result.size());
      assertEquals("single", result.get(0));
    }

    @Test
    @DisplayName("Should convert List with element type conversion")
    void shouldConvertListWithElementTypeConversion() {
      Attribute<Integer> intAttr = Attribute.integer("element");
      Attribute<List<Integer>> listAttr = Attribute.list("testAttr", intAttr);

      // Mixed input types
      List<Object> input = Arrays.asList(1, "2", 3.0);
      List<Integer> result = listAttr.convert(input);

      assertEquals(3, result.size());
      assertEquals(Integer.valueOf(1), result.get(0));
      assertEquals(Integer.valueOf(2), result.get(1));
      assertEquals(Integer.valueOf(3), result.get(2));
    }

    @Test
    @DisplayName("Should convert comma-separated String to typed List")
    void shouldConvertCommaSeparatedStringToTypedList() {
      Attribute<Integer> intAttr = Attribute.integer("element");
      Attribute<List<Integer>> listAttr = Attribute.list("testAttr", intAttr);

      List<Integer> result = listAttr.convert("1,2,3");

      assertEquals(3, result.size());
      assertEquals(Integer.valueOf(1), result.get(0));
      assertEquals(Integer.valueOf(2), result.get(1));
      assertEquals(Integer.valueOf(3), result.get(2));
    }

    @Test
    @DisplayName("Should throw exception when element conversion fails")
    void shouldThrowExceptionWhenElementConversionFails() {
      Attribute<Integer> intAttr = Attribute.integer("element");
      Attribute<List<Integer>> listAttr = Attribute.list("testAttr", intAttr);

      AttributeConversionException exception =
          assertThrows(AttributeConversionException.class, () -> listAttr.convert("1,two,3"));

      assertTrue(exception.getMessage().contains("Cannot convert"));
    }

    @Test
    @DisplayName("Should validate list with element validation")
    void shouldValidateListWithElementValidation() {
      // Only accepts positive integers
      Attribute<Integer> intAttr = Attribute.integer("element", i -> i > 0);
      Attribute<List<Integer>> listAttr = Attribute.list("testAttr", intAttr);

      assertTrue(listAttr.isValid("1,2,3"));
      assertFalse(listAttr.isValid("1,0,3"));
      assertFalse(listAttr.isValid("1,-2,3"));
    }
  }

  @Nested
  @DisplayName("Custom Attribute Tests")
  class CustomAttributeTests {

    @Test
    @DisplayName("Should create custom attribute")
    void shouldCreateCustomAttribute() {
      Function<Object, Double> converter =
          obj -> {
            if (obj instanceof Number) return ((Number) obj).doubleValue();
            if (obj instanceof String) return Double.parseDouble((String) obj);
            throw new AttributeConversionException("Cannot convert to Double");
          };

      Function<Double, Boolean> validator = d -> d >= 0.0 && d <= 1.0;

      Attribute<Double> attr = new Attribute<>("probability", Double.class, converter, validator);

      assertEquals("probability", attr.getName());
      assertEquals(Double.class, attr.getType());
      assertEquals(0.5, attr.convert(0.5));
      assertEquals(0.5, attr.convert("0.5"));
      assertTrue(attr.isValid(0.5));
      assertFalse(attr.isValid(1.5));
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should wrap non-AttributeConversionException in converter")
    void shouldWrapNonAttributeConversionExceptionInConverter() {
      Function<Object, String> converter =
          obj -> {
            throw new RuntimeException("Test exception");
          };

      Attribute<String> attr = new Attribute<>("testAttr", String.class, converter, s -> true);

      AttributeConversionException exception =
          assertThrows(AttributeConversionException.class, () -> attr.convert("test"));

      assertTrue(exception.getMessage().contains("Cannot convert"));
      assertEquals(RuntimeException.class, exception.getCause().getClass());
    }

    @Test
    @DisplayName("Should propagate AttributeConversionException in converter")
    void shouldPropagateAttributeConversionExceptionInConverter() {
      Function<Object, String> converter =
          obj -> {
            throw new AttributeConversionException("Original error");
          };

      Attribute<String> attr = new Attribute<>("testAttr", String.class, converter, s -> true);

      AttributeConversionException exception =
          assertThrows(AttributeConversionException.class, () -> attr.convert("test"));

      assertEquals("Original error", exception.getMessage());
    }

    @Test
    @DisplayName("Should return false in isValid when exception occurs")
    void shouldReturnFalseInIsValidWhenExceptionOccurs() {
      Function<Object, String> converter =
          obj -> {
            throw new RuntimeException("Test exception");
          };

      Attribute<String> attr = new Attribute<>("testAttr", String.class, converter, s -> true);

      assertFalse(attr.isValid("test"));
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    enum UserRole {
      ADMIN,
      USER,
      GUEST
    }

    @Test
    @DisplayName("Should handle complex nested conversions")
    void shouldHandleComplexNestedConversions() {
      // Create attributes
      Attribute<UserRole> roleAttr = Attribute.enumType("role", UserRole.class);
      Attribute<List<UserRole>> rolesAttr = Attribute.list("roles", roleAttr);

      // Test complex conversions
      List<UserRole> result = rolesAttr.convert("ADMIN,user,Guest");

      assertEquals(3, result.size());
      assertEquals(UserRole.ADMIN, result.get(0));
      assertEquals(UserRole.USER, result.get(1));
      assertEquals(UserRole.GUEST, result.get(2));

      // Test with mixed input
      List<Object> mixedInput = Arrays.asList(UserRole.ADMIN, "USER", "guest");
      result = rolesAttr.convert(mixedInput);

      assertEquals(3, result.size());
      assertEquals(UserRole.ADMIN, result.get(0));
      assertEquals(UserRole.USER, result.get(1));
      assertEquals(UserRole.GUEST, result.get(2));
    }

    @Test
    @DisplayName("Should enforce validation throughout conversion chain")
    void shouldEnforceValidationThroughoutConversionChain() {
      // Integer attribute that only accepts values from 1-10
      Attribute<Integer> intAttr = Attribute.integer("number", i -> i >= 1 && i <= 10);
      // List of those integers
      Attribute<List<Integer>> listAttr = Attribute.list("numbers", intAttr);

      // Valid input
      assertTrue(listAttr.isValid("1,5,10"));

      // Invalid input - contains value outside range
      assertFalse(listAttr.isValid("1,15,5"));

      // Invalid input - has non-numeric value
      assertFalse(listAttr.isValid("1,five,10"));
    }
  }
}
