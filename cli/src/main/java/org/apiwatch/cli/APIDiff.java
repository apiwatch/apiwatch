/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.cli;

import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.apache.http.HttpException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apiwatch.cli.ArgsUtil.AuthFileReader;
import org.apiwatch.diff.DifferencesCalculator;
import org.apiwatch.diff.RulesFinder;
import org.apiwatch.diff.ViolationsCalculator;
import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.models.Severity;
import org.apiwatch.serialization.Serializers;
import org.apiwatch.util.Logging;

public class APIDiff {

    public static final String DESCRIPTION = ArgsUtil.VERSION_NAME + "\n\n" +
            "Calculate the API differences between 2 versions of a software component." +
            "Then, process these differences through stability rules to check for API stability " +
            "violations.";

    public static void main(String[] argv) {
        try {
            Namespace args = parseArgs(argv);
            
            Logger log = Logger.getLogger(APIDiff.class.getName());
            
            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> rulesConfig = (Map<String, Map<String, String>>) args
                    .get("rules_config");
            if (rulesConfig != null) {
                RulesFinder.configureRules(rulesConfig);
            }
            
            log.trace("Deserializing API data...");
            APIScope scopeA = ArgsUtil.getAPIData(args.getString("component_a"),
                    args.getString("encoding"), args.getString("username"),
                    args.getString("password"));
            APIScope scopeB = ArgsUtil.getAPIData(args.getString("component_b"),
                    args.getString("encoding"), args.getString("username"),
                    args.getString("password"));

            log.trace("Calculation of differences...");
            List<APIDifference> diffs = DifferencesCalculator.getDiffs(scopeA, scopeB);

            log.trace("Detection of API stability violations...");
            ViolationsCalculator violationsClac = new ViolationsCalculator(RulesFinder.rules()
                    .values());

            Severity threshold = (Severity) args.get("severity_threshold");
            List<APIStabilityViolation> violations = violationsClac.getViolations(diffs, threshold);

            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            Serializers.dumpViolations(violations, writer, args.getString("format"));
            writer.flush();
            writer.close();
        } catch (HttpException e) {
            Logger.getLogger(APIDiff.class.getName()).error(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
    
    private static Namespace parseArgs(String[] argv) {
        Logging.configureLogging();
        
        String prog = APIDiff.class.getSimpleName().toLowerCase();
        ArgumentParser cli = ArgumentParsers.newArgumentParser(prog)
                .description(DESCRIPTION)
                .version(ArgsUtil.VERSION);

        cli.addArgument("component_a")
                .help("API data of the first version of the component (file or URL)")
                .metavar("COMPONENT_A");
        cli.addArgument("component_b")
                .help("API data of the second version of the component (file or URL)")
                .metavar("COMPONENT_B");
        
        ArgumentGroup inputGroup = cli.addArgumentGroup("Input options");
        inputGroup.addArgument("-e", "--encoding")
                .help("Source files encoding (default: UTF-8)")
                .dest("encoding")
                .setDefault("UTF-8");
        AuthFileReader auth = new AuthFileReader();
        inputGroup.addArgument("-u", "--server-user")
                .help("Username for HTTP authentication (by default read from ~/.apiwatchrc)")
                .setDefault(auth.username)
                .dest("username");
        inputGroup.addArgument("-p", "--server-password")
                .help("User password for HTTP authentication (by default read from ~/.apiwatchrc)")
                .setDefault(auth.password)
                .dest("password");
        
        ArgumentGroup outputGroup = cli.addArgumentGroup("Output options");
        outputGroup.addArgument("-v", "--verbosity")
                .help("Display logging events starting from this level (default: INFO)")
                .dest("verbosity")
                .setDefault(Level.INFO)
                .choices(ArgsUtil.LOG_LEVELS)
                .type(new ArgsUtil.LogLevelArgument());
        outputGroup.addArgument("-f", "--format")
                .help("Output format for API stability violations (default: text)")
                .dest("format")
                .setDefault("text")
                .choices(Serializers.availableFormats(APIStabilityViolation.class));
        outputGroup.addArgument("-r", "--rules-config")
                .help("API stability rules configuration file")
                .dest("rules_config")
                .type(new ArgsUtil.IniFileArgument());
        outputGroup.addArgument("-s", "--severity-threshold")
                .help("Exclude all API stablity violations below this severity level")
                .dest("severity_threshold")
                .setDefault(Severity.MINOR)
                .choices(Severity.values())
                .type(new ArgsUtil.SeverityArgument());

        Namespace args = null;
        try {
            args = cli.parseArgs(argv);
        } catch (ArgumentParserException e) {
            System.err.println("error: " + e.getMessage());
            System.err.println("use `-h/--help` for syntax");
            System.exit(1);
        }
        
        Logging.configureLogging((Level) args.get("verbosity"));
        
        return args;
    }
    
}
