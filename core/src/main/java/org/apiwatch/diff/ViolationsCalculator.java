/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIStabilityRule;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.models.Severity;

public class ViolationsCalculator {

    private Collection<APIStabilityRule> rules;

    public ViolationsCalculator(Collection<APIStabilityRule> rules) {
        if (rules == null) {
            throw new IllegalArgumentException("`rules` cannot be null");
        }
        this.rules = rules;
    }

    public List<APIStabilityViolation> getViolations(List<APIDifference> diffs, Severity threshold)
    {
        List<APIStabilityViolation> violations = new ArrayList<APIStabilityViolation>();

        for (APIDifference diff : diffs) {
            for (APIStabilityRule rule : rules) {
                if (rule.isApplicable(diff)) {
                    APIStabilityViolation violation = rule.evaluate(diff);
                    if (violation != null && violation.severity.compareTo(threshold) >= 0) {
                        violations.add(violation);
                    }
                }
            }
        }

        return violations;
    }

}
