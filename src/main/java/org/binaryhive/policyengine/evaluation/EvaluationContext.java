package org.binaryhive.policyengine.evaluation;

import lombok.Getter;
import org.binaryhive.policyengine.model.Policy;

import java.util.*;

@Getter
public abstract class EvaluationContext {

    protected final Set<String> actions;
    protected final Map<String, Object> context;

    /**
     * Constructor for a context with a single action.
     *
     * @param action  The single action to evaluate
     * @param context The context attributes map
     */
    public EvaluationContext(String action, Map<String, Object> context) {
        this.actions = Collections.singleton(action);
        this.context = context != null ? context : new HashMap<>();
    }

    /**
     * Constructor for a context with multiple actions.
     *
     * @param actions Collection of actions to evaluate
     * @param context The context attributes map
     */
    public EvaluationContext(Collection<String> actions, Map<String, Object> context) {
        this.actions = actions != null ? new HashSet<>(actions) : new HashSet<>();
        this.context = context != null ? context : new HashMap<>();
    }

    /**
     * Gets the primary action if only one exists.
     * This is for backward compatibility with code expecting a single action.
     *
     * @return The single action or the first action if multiple exist
     */
    public String getAction() {
        if (actions.isEmpty()) {
            return "";
        }
        return actions.iterator().next();
    }

    /**
     * Retrieves policies applicable to this evaluation context.
     *
     * @return List of policy objects
     */
    public abstract List<Policy> policies();

    /**
     * Retrieves policies applicable to a specific action.
     * Implementations may filter policies by action if supported.
     *
     * @param action The action to get policies for
     * @return List of policy objects applicable to the action
     */
    public List<Policy> policiesForAction(String action) {
        // Default implementation returns all policies
        // Subclasses can override to provide action-specific filtering
        return policies();
    }

    /**
     * Gets an attribute value from the context.
     *
     * @param key The attribute key to lookup
     * @return Optional containing the value if present
     */
    public Optional<Object> getAttribute(String key) {
        return Optional.ofNullable(context.get(key));
    }

    /**
     * Gets an attribute value from the context and casts to the expected type.
     *
     * @param key   The attribute key to lookup
     * @param clazz The expected class of the value
     * @return Optional containing the typed value if present and of correct type
     */
    public <T> Optional<T> getAttribute(String key, Class<T> clazz) {
        Object value = context.get(key);
        if (clazz.isInstance(value)) {
            return Optional.of(clazz.cast(value));
        }
        return Optional.empty();
    }

    /**
     * Checks if the given action is included in this context.
     *
     * @param actionToMatch The action to check against
     * @return true if the action is included in this context, false otherwise
     */
    public boolean hasAction(String actionToMatch) {
        return actions.contains(actionToMatch);
    }

    /**
     * Checks if this context contains any of the given actions.
     *
     * @param actionsToMatch Collection of actions to check against
     * @return true if any of the actions are included in this context, false otherwise
     */
    public boolean hasAnyAction(Collection<String> actionsToMatch) {
        for (String action : actionsToMatch) {
            if (actions.contains(action)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this context contains all the given actions.
     *
     * @param actionsToMatch Collection of actions to check against
     * @return true if all the actions are included in this context, false otherwise
     */
    public boolean hasAllActions(Collection<String> actionsToMatch) {
        return actions.containsAll(actionsToMatch);
    }
}
