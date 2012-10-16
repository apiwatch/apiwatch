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
import org.apiwatch.models.Symbol;

public class ModifiersChange extends RuleBase  implements APIStabilityRule {

    static final String ID = "MOD001";
    static final String NAME = "Modifiers Changed";
    static final String MESSAGE = "Changed modifiers of %s %s '%s' (%s -> %s)";

    public ModifiersChange() {
        super();
        privateSeverity = Severity.INFO;
        scopeSeverity = Severity.MAJOR;
        protectedSeverity = Severity.MAJOR;
        publicSeverity = Severity.MAJOR;
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
        return diff.changeType == ChangeType.CHANGED && diff.element() instanceof Symbol
                && "modifiers".equals(diff.attribute);
    }

    @Override
    public APIStabilityViolation evaluate(APIDifference diff) {
        String message = String.format(MESSAGE, diff.visibility(), diff.element().getClass()
                .getSimpleName(), diff.element().name(), diff.valueA, diff.valueB);
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
