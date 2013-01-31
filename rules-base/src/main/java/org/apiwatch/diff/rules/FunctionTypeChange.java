/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.diff.rules;

import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIStabilityRule;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.models.ChangeType;
import org.apiwatch.models.Function;
import org.apiwatch.models.Severity;
import org.apiwatch.util.APIWatchExtension;

@APIWatchExtension
public class FunctionTypeChange extends RuleBase implements APIStabilityRule {

    static final String ID = "TYP002";
    static final String NAME = "Function Return Type Change";
    static final String MESSAGE = "Changed return type of %s function '%s' (%s -> %s)";

    public FunctionTypeChange() {
        super();
        privateSeverity = Severity.INFO;
        scopeSeverity = Severity.CRITICAL;
        protectedSeverity = Severity.CRITICAL;
        publicSeverity = Severity.BLOCKER;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return MESSAGE;
    }

    @Override
    public boolean isApplicable(APIDifference diff) {
        return diff.changeType == ChangeType.CHANGED && diff.element() instanceof Function
                && "returnType".equals(diff.attribute);
    }

    @Override
    public APIStabilityViolation evaluate(APIDifference diff) {
        String message = String.format(MESSAGE, diff.visibility(), diff.name(),
                diff.valueA, diff.valueB);
        
        Severity severity;
        switch (diff.visibility()) {
        case PRIVATE:
            severity = privateSeverity;
            break;
        case SCOPE:
            severity = scopeSeverity;
            break;
        case PROTECTED:
            severity = protectedSeverity;
            break;
        case PUBLIC:
        default:
            severity = publicSeverity;
        }
        
        return new APIStabilityViolation(diff, this, severity, message);
    }
    
}
