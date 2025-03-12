package org.binaryhive.policyengine.evaluation;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.binaryhive.policyengine.model.Policy;

import java.util.*;

@Log4j2
@RequiredArgsConstructor
public class ContextEvaluator {

    private final PolicyEvaluator policyEvaluator;

    /**
     * Evaluates all policies in the given context and returns the final result.
     * This method evaluates policies considering all actions in the context.
     * 
     * @param context The evaluation context containing policies and attributes
     * @return The final policy evaluation result
     */
    public PolicyEvaluationResult evaluate(EvaluationContext context) {
        List<Policy> policies = context.policies();
        
        if (policies.isEmpty()) {
            log.debug("No policies found for actions: {}", context.getActions());
            return PolicyEvaluationResult.NOT_APPLICABLE;
        }
        
        log.debug("Evaluating {} policies for actions: {}", policies.size(), context.getActions());
        
        boolean anyAllow = false;
        for (Policy policy : policies) {
            PolicyEvaluationResult result = policyEvaluator.evaluate(policy, context.getContext());
            log.debug("Policy {} evaluated to {}", policy.getName(), result);
            
            if (result == PolicyEvaluationResult.DENY) {
                // An explicit DENY takes precedence over everything else
                return PolicyEvaluationResult.DENY;
            } else if (result == PolicyEvaluationResult.ALLOW) {
                anyAllow = true;
            }
        }
        
        // If at least one policy explicitly allowed and none denied
        return anyAllow ? PolicyEvaluationResult.ALLOW : PolicyEvaluationResult.NOT_APPLICABLE;
    }
    
    /**
     * Evaluates policies for a specific action in the context.
     * This is useful when context contains multiple actions but you need to evaluate just one.
     * 
     * @param context The evaluation context containing policies and attributes
     * @param action The specific action to evaluate
     * @return The policy evaluation result for the specific action
     */
    public PolicyEvaluationResult evaluateForAction(EvaluationContext context, String action) {
        if (!context.hasAction(action)) {
            log.debug("Action {} not present in context actions: {}", action, context.getActions());
            return PolicyEvaluationResult.NOT_APPLICABLE;
        }
        
        List<Policy> policies = context.policiesForAction(action);
        
        if (policies.isEmpty()) {
            log.debug("No policies found for action: {}", action);
            return PolicyEvaluationResult.NOT_APPLICABLE;
        }
        
        log.debug("Evaluating {} policies for action: {}", policies.size(), action);
        
        boolean anyAllow = false;
        for (Policy policy : policies) {
            PolicyEvaluationResult result = policyEvaluator.evaluate(policy, context.getContext());
            log.debug("Policy {} evaluated to {} for action {}", policy.getName(), result, action);
            
            if (result == PolicyEvaluationResult.DENY) {
                return PolicyEvaluationResult.DENY;
            } else if (result == PolicyEvaluationResult.ALLOW) {
                anyAllow = true;
            }
        }
        
        return anyAllow ? PolicyEvaluationResult.ALLOW : PolicyEvaluationResult.NOT_APPLICABLE;
    }
    
    /**
     * Evaluates each action in the context separately and returns the results.
     * This is useful for batch checking multiple actions at once.
     * 
     * @param context The evaluation context containing policies and attributes
     * @return Map of action to evaluation result
     */
    public Map<String, PolicyEvaluationResult> evaluateAllActions(EvaluationContext context) {
        Map<String, PolicyEvaluationResult> results = new HashMap<>();
        
        for (String action : context.getActions()) {
            results.put(action, evaluateForAction(context, action));
        }
        
        return results;
    }
}
