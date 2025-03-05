package org.binaryhive.policyengine.model;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.binaryhive.policyengine.exception.AttributeConversionException;

@RequiredArgsConstructor
public class Attribute<T> {
  @Getter private final String name;
  @Getter private final Class<T> type;
  private final Function<Object, T> converter;
  private final Function<T, Boolean> validator;

  public T convert(Object value) {
    if (value == null) {
      return null;
    }
    try {
      T converted = converter.apply(value);
      if (converted != null && !validator.apply(converted)) {
        throw new AttributeConversionException(
            "Value " + value + " is not valid for attribute " + name);
      }
      return converted;

    } catch (Exception e) {
      if (e instanceof AttributeConversionException) {
        throw e;
      }
      throw new AttributeConversionException(
          "Cannot convert " + value + " to " + type.getSimpleName(), e);
    }
  }

  public boolean isValid(Object value) {
    try {
      T converted = convert(value);
      return converted != null && validator.apply(converted);
    } catch (Exception e) {
      return false;
    }
  }

  public static Attribute<String> string(String name) {
    return string(name, s -> true);
  }

  public static Attribute<String> string(String name, Function<String, Boolean> validator) {
    return new Attribute<>(
        name, String.class, v -> v instanceof String ? (String) v : String.valueOf(v), validator);
  }

  public static Attribute<Integer> integer(String name) {
    return integer(name, i -> true);
  }

  public static Attribute<Integer> integer(String name, Function<Integer, Boolean> validator) {
    return new Attribute<>(
        name,
        Integer.class,
        v -> {
          if (v instanceof Integer) return (Integer) v;
          if (v instanceof Number) return ((Number) v).intValue();
          if (v instanceof String) return Integer.parseInt((String) v);
          throw new AttributeConversionException("Cannot convert to Integer: " + v);
        },
        validator);
  }

  public static <E extends Enum<E>> Attribute<E> enumType(String name, Class<E> enumClass) {
    return new Attribute<>(
        name,
        enumClass,
        v -> {
          if (enumClass.isInstance(v)) return enumClass.cast(v);
          if (v instanceof String) {
            String strValue = (String) v;
            try {
              return Enum.valueOf(enumClass, strValue);
            } catch (IllegalArgumentException e) {
              // Try case-insensitive match
              for (E enumConstant : enumClass.getEnumConstants()) {
                if (enumConstant.name().equalsIgnoreCase(strValue)) {
                  return enumConstant;
                }
              }
              throw new AttributeConversionException(
                  "No enum constant " + enumClass.getSimpleName() + "." + strValue);
            }
          }
          throw new AttributeConversionException(
              "Cannot convert to " + enumClass.getSimpleName() + ": " + v);
        },
        e -> true);
  }

  public static <T> Attribute<List<T>> list(String name, Attribute<T> elementType) {
    return new Attribute<>(
        name,
        (Class<List<T>>) (Class<?>) List.class,
        v -> {
          if (v instanceof List) {
            // Convert each element to the expected type
            List<?> list = (List<?>) v;
            return list.stream().map(elementType::convert).toList();
          }
          if (v instanceof String && ((String) v).contains(",")) {
            // Convert comma-separated string to list
            String[] parts = ((String) v).split("\\s*,\\s*");
            return Arrays.stream(parts).map(elementType::convert).toList();
          }
          // Single value to list
          return List.of(elementType.convert(v));
        },
        list -> list.stream().allMatch(elementType::isValid));
  }
}
