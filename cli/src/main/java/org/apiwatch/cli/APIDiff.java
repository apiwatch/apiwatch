/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
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
import org.apiwatch.analyser.Analyser;
import org.apiwatch.diff.DifferencesCalculator;
import org.apiwatch.diff.RulesFinder;
import org.apiwatch.diff.ViolationsCalculator;
import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.models.Severity;
import org.apiwatch.serialization.Serializers;
import org.apiwatch.util.IO;
import org.apiwatch.util.Logging;

public class APIDiff {

    public static final String DESCRIPTION = Args.VERSION_NAME + "\n\n"
            + "Calculate the API differences between 2 versions of a software component."
            + "Then, process these differences through stability rules to check for API stability "
            + "violations.";

    public static void main(String[] argv) {
        try {
            Namespace args = parseArgs(argv);

            Logger log = Logger.getLogger(APIDiff.class.getName());

            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> rulesConfig = (Map<String, Map<String, String>>) args
                    .get(Args.RULES_CONFIG_OPTION);
            if (rulesConfig != null) {
                RulesFinder.configureRules(rulesConfig);
            }

            log.trace("Deserializing API data...");
            APIScope scopeA = IO.getAPIData(args.getString(COMPONENT_A),
                    args.getString(Args.INPUT_FORMAT_OPTION),
                    args.getString(Analyser.ENCODING_OPTION), args.getString(Args.USERNAME_OPTION),
                    args.getString(Args.PASSWORD_OPTION));
            APIScope scopeB = IO.getAPIData(args.getString(COMPONENT_B),
                    args.getString(Args.INPUT_FORMAT_OPTION),
                    args.getString(Analyser.ENCODING_OPTION), args.getString(Args.USERNAME_OPTION),
                    args.getString(Args.PASSWORD_OPTION));

            log.trace("Calculation of differences...");
            List<APIDifference> diffs = DifferencesCalculator.getDiffs(scopeA, scopeB);

            log.trace("Detection of API stability violations...");
            ViolationsCalculator violationsClac = new ViolationsCalculator(RulesFinder.rules()
                    .values());

            Severity threshold = (Severity) args.get(Args.SEVERITY_THRESHOLD_OPTION);
            List<APIStabilityViolation> violations = violationsClac.getViolations(diffs, threshold);

            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            Serializers.dumpViolations(violations, writer,
                    args.getString(Args.OUTPUT_FORMAT_OPTION));
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

    private static final String COMPONENT_B = "component_b";
    private static final String COMPONENT_A = "component_a";

    private static Namespace parseArgs(String[] argv) {
        Logging.configureLogging();

        String prog = APIDiff.class.getSimpleName().toLowerCase();
        ArgumentParser cli = ArgumentParsers.newArgumentParser(prog).description(DESCRIPTION)
                .version(Args.VERSION);

        cli.addArgument(COMPONENT_A)
                .help("API data of the first version of the component (file or URL)")
                .metavar("COMPONENT_A");
        cli.addArgument(COMPONENT_B)
                .help("API data of the second version of the component (file or URL)")
                .metavar("COMPONENT_B");
        Args.verbosity(cli);

        ArgumentGroup inputGroup = cli.addArgumentGroup("Input options");
        Args.inputFormat(inputGroup, APIScope.class, null);
        Args.encoding(inputGroup);
        Args.httpAuth(inputGroup);

        ArgumentGroup outputGroup = cli.addArgumentGroup("Output options");
        Args.outputFormat(outputGroup, APIStabilityViolation.class, "text");
        Args.rulesConfig(outputGroup);
        Args.severityThreshold(outputGroup);

        Namespace args = null;
        try {
            args = cli.parseArgs(argv);
        } catch (ArgumentParserException e) {
            Args.reportArgumentError(e, System.err);
            System.exit(1);
        }

        Logging.configureLogging((Level) args.get(Args.VERBOSITY_OPTION));

        return args;
    }

}
