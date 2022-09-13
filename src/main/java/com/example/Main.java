package com.example;

import java.io.*;
import org.apache.commons.cli.*;

/**
 * Main class
 */
public class Main {
    public static void main(String[] args) throws ParseException, IOException {
        Options options = new Options();

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(false);
        options.addOption(output);

        Option help = new Option("h", "help", false, "print help message");
        help.setRequired(false);
        options.addOption(help);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption(help.getOpt()) || cmd.getArgList().size() == 0) {
            formatter.printHelp("compiler [OPTIONS] <file>", options);
            System.exit(0);
        }

        String fileName = cmd.getArgList().get(0);
        String fileContent = null;

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            fileContent = sb.toString();
        } catch (FileNotFoundException e) {
            Utils.printError(fileName, ": No such file or directory");
            Utils.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            Utils.exit(0);
        }
        SyntaxTree.Block program = CompilerMain.compile(new Compiler(fileContent));
        VMByteCodeGenerator vmByteCodeGenerator = new VMByteCodeGenerator();
        Object[] bytes = (Object[]) vmByteCodeGenerator.generate(program);
        if (cmd.hasOption(output.getOpt())) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue(output.getOpt())));
            writer.write(VMWrapper.disassemble(bytes));
            writer.close();
        } else {
            VMWrapper vm = new VMWrapper();
            vm.run(bytes);
        }
    }
}