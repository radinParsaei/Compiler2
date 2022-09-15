package com.example;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.commons.cli.*;

/**
 * Main class
 */
public class Main {
    private static String readFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            Utils.printError(fileName, ": No such file or directory");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

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
        if (cmd.hasOption(help.getOpt())) {
            formatter.printHelp("compiler [OPTIONS] <file>", options);
            System.exit(0);
        }

        if (cmd.getArgList().size() == 0) {
            Compiler compiler = new Compiler();
            VMWrapper vm = new VMWrapper();
            if (System.console() == null) {
                VMByteCodeGenerator vmByteCodeGenerator = new VMByteCodeGenerator();
                StringBuilder sb = new StringBuilder();
                final Scanner scanner = new Scanner(System.in);
                for (String line; true;) {
                    try {
                        line = scanner.nextLine();
                    } catch (NoSuchElementException exit) {
                        break;
                    }
                    sb.append(line).append("\n");
                }
                String code = sb.toString();
                if (!code.trim().equals("")) {
                    compiler.setCode(code);
                    SyntaxTree.Block program = CompilerMain.compile(compiler);
                    Object[] bytes = (Object[]) vmByteCodeGenerator.generate(program);
                    vm.run(bytes);
                }
            } else {
                VMByteCodeGenerator vmByteCodeGenerator = new VMByteCodeGenerator(new Object[] { VMWrapper.CALLFUNC, false });
                while (true) {
                    try {
                        String code = Shell.readLine(vmByteCodeGenerator.getScopeTool().getGlobals());
                        if (!code.trim().equals("")) {
                            compiler.setCode(code);
                            SyntaxTree.Block program = CompilerMain.compile(compiler);
                            Object[] bytes = (Object[]) vmByteCodeGenerator.generate(program);
                            vm.run(bytes);
                            VMWrapper.flush();
                        }
                    } catch (NoSuchElementException exit) {
                        break;
                    }
                }
            }
            Utils.exit(0);
        }

        String fileName = cmd.getArgList().get(0);

        if (cmd.getArgList().size() != 1) {
            if (cmd.getArgList().get(0).equals("highlight")) {
                System.out.println(new Highlighter()
                        .highlight(null, readFile(cmd.getArgList().get(1))).toAnsi());
                Utils.exit(0);
            } else if (cmd.getArgList().get(0).equals("run")) {
                fileName = cmd.getArgList().get(1);
            } else {
                formatter.printHelp("compiler [run/highlight] <file> [OPTIONS]", options);
                System.exit(1);
            }
        }

        String fileContent = readFile(fileName);

        if (fileContent == null) {
            Utils.exit(1);
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