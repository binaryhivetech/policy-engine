package org.binaryhive.policyengine.condition;

import org.binaryhive.policyengine.model.Attribute;
import org.binaryhive.policyengine.model.Operator;

import java.util.List;

public class Conditions {
    public static <T> Condition<T> equals(Attribute<T> attribute, T value) {
        return new ValueCondition<>(attribute, Operator.EQUALS, value);
    }

    public static <T> Condition<T> notEquals(Attribute<T> attribute, T value) {
        return new ValueCondition<>(attribute, Operator.NOT_EQUALS, value);
    }

    public static <T> Condition<T> in(Attribute<T> attribute, List<T> values) {
        return new ListCondition<>(attribute, Operator.IN, values);
    }

    public static <T> Condition<T> notIn(Attribute<T> attribute, List<T> values) {
        return new ListCondition<>(attribute, Operator.NOT_IN, values);
    }

}
