/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.diff.rules;

import java.util.HashSet;
import java.util.Set;

import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.APIStabilityRule;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.models.ChangeType;
import org.apiwatch.models.Severity;

public class DependenciesChange implements APIStabilityRule {

    static String ID = "DEP001";
    static String NAME = "Dependencies Changed";
    static String MESSAGE = "Changed dependencies in scope %s ADDED %s REMOVED %s";

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
        return diff.changeType == ChangeType.CHANGED && diff.element() instanceof APIScope
                && "dependencies".equals(diff.attribute);
    }

    @SuppressWarnings("unchecked")
    @Override
    public APIStabilityViolation evaluate(APIDifference diff) {
        Set<String> depsA = (Set<String>) diff.valueA;
        Set<String> depsB = (Set<String>) diff.valueB;

        Set<String> added = new HashSet<String>(depsB);
        added.removeAll(depsA);

        Set<String> removed = new HashSet<String>(depsA);
        added.removeAll(depsB);

        Severity severity;
        if (added.size() > 0) {
            severity = Severity.MAJOR;
        } else {
            severity = Severity.INFO;
        }
        String message = String.format(MESSAGE, diff.name(), added, removed);

        return new APIStabilityViolation(diff, this, severity, message);
    }
}
