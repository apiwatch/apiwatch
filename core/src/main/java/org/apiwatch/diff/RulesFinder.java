/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.diff;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;
import org.apiwatch.models.APIStabilityRule;

public class RulesFinder {

    static Logger LOGGER = Logger.getLogger(RulesFinder.class.getName());
    static Map<String, APIStabilityRule> RULES;
    
    private static void discoverRules() {
        ServiceLoader<APIStabilityRule> loader = ServiceLoader.load(APIStabilityRule.class);
        LOGGER.trace("Discovering APIStabilityRule implementations in class path...");
        for (APIStabilityRule impl : loader) {
            LOGGER.debug(String.format("Found %s. ID: %s", impl.getClass(), impl.id()));
            RULES.put(impl.id(), impl);
        }
    }
    
    public static Map<String, APIStabilityRule> rules() {
        if (RULES == null) {
            RULES = new HashMap<String, APIStabilityRule>();
            discoverRules();
        }
        return RULES;
    }
    
    public static void configureRules(Map<String, Map<String, String>> rulesProperties) {
        // to discover rules first, if necessary
        rules();
        
        for (Map.Entry<String, Map<String, String>> ruleConf : rulesProperties.entrySet()) {
            
            String ruleId = ruleConf.getKey();
            Map<String, String> ruleProps = ruleConf.getValue();
            
            if (ruleProps != null) {
                if ("false".equals(ruleProps.get("enabled"))) {
                    RULES.remove(ruleId);
                } else {
                    APIStabilityRule rule = RULES.get(ruleId);
                    if (rule != null) {
                        try {
                            rule.configure(ruleProps);
                        } catch (IllegalArgumentException e) {
                            LOGGER.error("Error while configuring rule " + ruleId, e);
                        }
                    }
                }
            }
        }
    }
    
}
