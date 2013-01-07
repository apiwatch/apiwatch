package org.apiwatch.util;

import java.util.Arrays;
import java.util.Map;

import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

import org.apiwatch.analyser.Analyser;
import org.apiwatch.analyser.LanguageAnalyser;
import org.apiwatch.util.StringUtils;

public class ArgActions {
    
    public static class ListLanguagesAction implements ArgumentAction {

        @Override
        public void run(ArgumentParser parser, Argument arg, Map<String, Object> attrs,
                String flag, Object value) throws ArgumentParserException
        {
            System.out.println("=============== ==============================");
            System.out.println("Language        Default File Extensions       ");
            System.out.println("=============== ==============================");
            for (Map.Entry<String, LanguageAnalyser> e : Analyser.getAllAnalysers().entrySet()) {
                String ext = StringUtils.join(", .", Arrays.asList(e.getValue().fileExtensions()));
                System.out.println(String.format("%-15s %-30s", e.getKey(), "." + ext));
            }
            System.out.println("=============== ==============================");
            System.exit(0);
        }

        @Override
        public void onAttach(Argument arg) {

        }

        @Override
        public boolean consumeArgument() {
            return false;
        }

    }
}
