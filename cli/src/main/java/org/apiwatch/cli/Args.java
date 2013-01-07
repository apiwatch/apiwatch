package org.apiwatch.cli;

import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;

import org.apache.log4j.Level;
import org.apiwatch.analyser.Analyser;
import org.apiwatch.analyser.LanguageAnalyser;
import org.apiwatch.analyser.Option;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.models.Severity;
import org.apiwatch.serialization.Serializers;
import org.apiwatch.util.ArgActions;
import org.apiwatch.util.ArgTypes;
import org.apiwatch.util.AuthFileReader;

public class Args {


    public static final String EPILOG = "You may have to add -- before the positional arguments "
            + "to separate them from the options that take more than one value (e.g. -i or -x).";
    public static final Level[] LOG_LEVELS = new Level[] { Level.TRACE, Level.DEBUG, Level.INFO,
            Level.WARN, Level.ERROR };
    public static final String VERSION = Args.class.getPackage().getImplementationVersion();
    public static final String VERSION_NAME = "APIWATCH version " + VERSION;
    
    public static void listLanguages(ArgumentParser parser) {
        parser.addArgument("--list-languages")
                .help("List supported languages and their default associated file extensions")
                .action(new ArgActions.ListLanguagesAction());
    }

    public static final String VERBOSITY_OPTION = "verbosity";

    public static void verbosity(ArgumentParser parser) {
        parser.addArgument("-v", "--verbosity")
                .help("Display logging events starting from this level (default: INFO)")
                .dest(VERBOSITY_OPTION)
                .setDefault(Level.INFO)
                .choices(LOG_LEVELS)
                .type(new ArgTypes.LogLevelArgument());
    }

    public static void jobs(ArgumentParser parser) {
        parser.addArgument("-j", "--jobs")
                .help("Number of parallel jobs (default: nbCPU)")
                .dest(Analyser.JOBS_OPTION)
                .setDefault(Runtime.getRuntime().availableProcessors())
                .type(new ArgTypes.IntegerArgument());
    }

    public static void encoding(ArgumentGroup group) {
        group.addArgument("-e", "--encoding")
                .help("Source files encoding (default: " + Analyser.DEFAULT_ENCODING + ")")
                .dest(Analyser.ENCODING_OPTION)
                .setDefault(Analyser.DEFAULT_ENCODING);
    }

    public static final String EXCLUDES_OPTION = "excludes";

    public static void excludes(ArgumentGroup group) {
        group.addArgument("-x", "--exclude")
                .help("Exclude from analysis")
                .dest(EXCLUDES_OPTION)
                .metavar("PATTERN")
                .nargs("+");
    }

    public static final String INCLUDES_OPTION = "includes";

    public static void includes(ArgumentGroup group) {
        group.addArgument("-i", "--include")
                .help("Only include in analysis")
                .dest(INCLUDES_OPTION)
                .metavar("PATTERN")
                .nargs("+");
    }

    public static void languageExtensions(ArgumentGroup group) {
        group.addArgument("-l", "--language-extensions")
                .help("Override default language extensions. Use the following syntax: "
                        + "LANG1=ext1,ext2,ext3;LANG2=ext4,ext5,ext6;...")
                .dest(Analyser.EXTENSIONS_OPTION)
                .type(new ArgTypes.ExtensionsArgument());
    }

    public static final String USERNAME_OPTION = "username";
    public static final String PASSWORD_OPTION = "password";

    public static void httpAuth(ArgumentGroup group) {
        AuthFileReader auth = new AuthFileReader();
        group.addArgument("-u", "--server-user")
                .help("Username for HTTP authentication (by default read from ~/.apiwatchrc)")
                .setDefault(auth.username)
                .dest(USERNAME_OPTION);
        group.addArgument("-p", "--server-password")
                .help("User password for HTTP authentication (by default read from ~/.apiwatchrc)")
                .setDefault(auth.password)
                .dest(PASSWORD_OPTION);
    }

    public static final String FORMAT_OPTION = "format";

    public static void format(ArgumentGroup group, String fmt) {
        group.addArgument("-f", "--format")
                .help("Output format for API stability violations (default: " + fmt + ")")
                .dest(FORMAT_OPTION)
                .setDefault(fmt)
                .choices(Serializers.availableFormats(APIStabilityViolation.class));
    }

    public static final String RULES_CONFIG_OPTION = "rules_config";

    public static void rulesConfig(ArgumentGroup group) {
        group.addArgument("-r", "--rules-config")
                .help("API stability rules configuration file")
                .dest(RULES_CONFIG_OPTION)
                .type(new ArgTypes.IniFileArgument());
    }

    public static final String SEVERITY_THRESHOLD_OPTION = "severity_threshold";

    public static void severityThreshold(ArgumentGroup group) {
        group.addArgument("-s", "--severity-threshold")
                .help("Exclude all API stablity violations below this severity level")
                .dest(SEVERITY_THRESHOLD_OPTION)
                .setDefault(Severity.INFO)
                .choices(Severity.values())
                .type(new ArgTypes.SeverityArgument());
    }

    public static void analysersOptions(ArgumentParser parser) {
        for (LanguageAnalyser a : Analyser.getAllAnalysers().values()) {
            if (a.options() != null) {
                ArgumentGroup group = parser.addArgumentGroup(a.language() + " Language Options");
                for (Option option : a.options()) {
                    addArgument(group, option);
                }
            }
        }
    }

    private static void addArgument(ArgumentGroup group, Option option) {
        Argument argument = group.addArgument("--" + option.name.replace("_", "-"));
        argument.dest(option.name);
        if (option.description != null) {
            argument.help(option.description);
        }
        if (option.meta != null) {
            argument.metavar(option.meta);
        }
        if (option.nargs != null) {
            try {
                argument.nargs(Integer.parseInt(option.nargs));
            } catch (NumberFormatException e) {
                argument.nargs(option.nargs);
            }
        }
    }


}