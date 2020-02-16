package com.funnelback.publicui.search.model.collection.paramtransform;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.funnelback.publicui.search.model.collection.paramtransform.criteria.Criteria;
import com.funnelback.publicui.search.model.collection.paramtransform.operation.Operation;

/**
 * Parameter transformation rule.
 * Defines a criteria to match the rule, and a list of operations
 * to apply.
 * 
 */
@RequiredArgsConstructor
public class TransformRule {
    
    /** Criteria to match the rule */
    @Getter private final Criteria criteria;
    
    /** List of operations to apply */
    @Getter private final List<Operation> operations;

    /**
     * Returns an human-friendly version of the rule.
     */
    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("\n-> IF ").append(criteria.toString()).append("\n");
        out.append("APPLY \n");
        for (Operation o : operations) {
            out.append("\t").append(o.toString()).append("\n");
        }
        out.append("<-\n");
        return out.toString();
    }
}

