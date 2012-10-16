/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.diff.rules;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.APIStabilityRule;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.models.ChangeType;
import org.apiwatch.models.Severity;

public class DependenciesChange implements APIStabilityRule {

    static final String ID = "DEP001";
    static final String NAME = "Dependencies Change";
    static final String MESSAGE = "Changed dependencies in scope %s ADDED %s REMOVED %s";

    private Severity addPrivateSeverity = Severity.INFO;
    private Severity addProtectedSeverity = Severity.MAJOR;
    private Severity addScopeSeverity = Severity.MAJOR;
    private Severity addPublicSeverity = Severity.MAJOR;

    private Severity remPrivateSeverity = Severity.INFO;
    private Severity remProtectedSeverity = Severity.INFO;
    private Severity remScopeSeverity = Severity.INFO;
    private Severity remPublicSeverity = Severity.INFO;

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
    public void configure(Map<String, String> properties) throws IllegalArgumentException {
        String addPrivateSeverity = properties.get("addPrivateSeverity");
        if (addPrivateSeverity != null) {
            this.addPrivateSeverity = Severity.valueOf(addPrivateSeverity);
        }
        String addProtectedSeverity = properties.get("addProtectedSeverity");
        if (addProtectedSeverity != null) {
            this.addProtectedSeverity = Severity.valueOf(addProtectedSeverity);
        }
        String addScopeSeverity = properties.get("addScopeSeverity");
        if (addScopeSeverity != null) {
            this.addScopeSeverity = Severity.valueOf(addScopeSeverity);
        }
        String addPublicSeverity = properties.get("addPublicSeverity");
        if (addPublicSeverity != null) {
            this.addPublicSeverity = Severity.valueOf(addPublicSeverity);
        }
        String remPrivateSeverity = properties.get("remPrivateSeverity");
        if (remPrivateSeverity != null) {
            this.remPrivateSeverity = Severity.valueOf(remPrivateSeverity);
        }
        String remProtectedSeverity = properties.get("remProtectedSeverity");
        if (remProtectedSeverity != null) {
            this.remProtectedSeverity = Severity.valueOf(remProtectedSeverity);
        }
        String remScopeSeverity = properties.get("remScopeSeverity");
        if (remScopeSeverity != null) {
            this.remScopeSeverity = Severity.valueOf(remScopeSeverity);
        }
        String remPublicSeverity = properties.get("remPublicSeverity");
        if (remPublicSeverity != null) {
            this.remPublicSeverity = Severity.valueOf(remPublicSeverity);
        }
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
            switch (diff.element().visibility) {
            case PRIVATE:
                severity = addPrivateSeverity;
                break;
            case PROTECTED:
                severity = addProtectedSeverity;
                break;
            case SCOPE:
                severity = addScopeSeverity;
                break;
            case PUBLIC:
            default:
                severity = addPublicSeverity;
                break;
            }
        } else {
            switch (diff.element().visibility) {
            case PRIVATE:
                severity = remPrivateSeverity;
                break;
            case PROTECTED:
                severity = remProtectedSeverity;
                break;
            case SCOPE:
                severity = remScopeSeverity;
                break;
            case PUBLIC:
            default:
                severity = remPublicSeverity;
                break;
            }
        }
        String message = String.format(MESSAGE, diff.name(), added, removed);

        return new APIStabilityViolation(diff, this, severity, message);
    }
}
