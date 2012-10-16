/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
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
import org.apiwatch.models.Severity;
import org.apiwatch.models.Variable;

public class VariableTypeChange extends RuleBase implements APIStabilityRule {

    static final String ID = "TYP001";
    static final String NAME = "Variable Type Change";
    static final String MESSAGE = "Changed type of %s variable '%s' (%s -> %s)";

    public VariableTypeChange() {
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
        return diff.changeType == ChangeType.CHANGED && diff.element() instanceof Variable
                && "type".equals(diff.attribute);
    }

    @Override
    public APIStabilityViolation evaluate(APIDifference diff) {

        String typeA = ((Variable) diff.elementA).type;
        String typeB = ((Variable) diff.elementB).type;

        String message = String
                .format(MESSAGE, diff.elementB.visibility, diff.name(), typeA, typeB);
        
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
