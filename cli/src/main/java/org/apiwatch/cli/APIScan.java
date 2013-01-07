/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.cli;

import java.io.OutputStreamWriter;
import java.util.Set;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.apache.http.HttpException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apiwatch.analyser.Analyser;
import org.apiwatch.models.APIScope;
import org.apiwatch.serialization.Serializers;
import org.apiwatch.util.DirectoryWalker;
import org.apiwatch.util.IO;
import org.apiwatch.util.Logging;

public class APIScan {

    private static final String INPUT_PATHS = "input_paths";
    private static final String OUTPUT_LOCATION = "output_location";
    public static final String DESCRIPTION = Args.VERSION_NAME + "\n\n"
            + "Analyse source code and extract API information from it. The API data can then "
            + "be exported to a file or directly uploaded to an APIWATCH server instance.";

    public static void main(String[] argv) {
        try {
            Namespace args = parseArgs(argv);

            Logger log = Logger.getLogger(APIDiff.class.getName());

            log.trace("Finding files to analyse...");
            DirectoryWalker walker = new DirectoryWalker(
                    args.<String> getList(Args.EXCLUDES_OPTION),
                    args.<String> getList(Args.INCLUDES_OPTION));

            Set<String> files = walker.walk(args.<String> getList(INPUT_PATHS));
            APIScope scope = Analyser.analyse(files, args.getAttrs());

            if (args.get(OUTPUT_LOCATION) != null) {
                IO.putAPIData(scope, args.getString(Args.FORMAT_OPTION),
                        args.getString(Analyser.ENCODING_OPTION), args.getString(OUTPUT_LOCATION),
                        args.getString(Args.USERNAME_OPTION), args.getString(Args.PASSWORD_OPTION));
            } else {
                OutputStreamWriter writer = new OutputStreamWriter(System.out);
                Serializers.dumpAPIScope(scope, writer, args.getString(Args.FORMAT_OPTION));
                writer.flush();
                writer.close();
            }
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

        String prog = APIScan.class.getSimpleName().toLowerCase();
        ArgumentParser cli = ArgumentParsers.newArgumentParser(prog).description(DESCRIPTION)
                .epilog(Args.EPILOG).version(Args.VERSION);

        cli.addArgument(INPUT_PATHS).help("Input file/directory to be analysed")
                .metavar("FILE_OR_DIR").nargs("+");

        Args.listLanguages(cli);
        Args.jobs(cli);

        ArgumentGroup inputGroup = cli.addArgumentGroup("Input options");
        Args.encoding(inputGroup);
        Args.excludes(inputGroup);
        Args.includes(inputGroup);
        Args.languageExtensions(inputGroup);

        ArgumentGroup outputGroup = cli.addArgumentGroup("Output options");
        Args.format(outputGroup, "json");
        outputGroup
                .addArgument("-o", "--output-location")
                .dest(OUTPUT_LOCATION)
                .help("Write analysed API data to this location. Local file paths and URLs are "
                        + "accepted (If the location is a directory, it will be created if necessary "
                        + "and the data will be written to a 'api.<format>' file). By default, the "
                        + "API data is displayed on the standard output.");
        Args.httpAuth(outputGroup);

        Args.analysersOptions(cli);

        Namespace args = null;
        try {
            args = cli.parseArgs(argv);
        } catch (ArgumentParserException e) {
            System.err.println("error: " + e.getMessage());
            System.err.println("use `-h/--help` for syntax");
            System.exit(1);
        }

        Logging.configureLogging((Level) args.get(Args.VERBOSITY_OPTION));

        return args;
    }

}
