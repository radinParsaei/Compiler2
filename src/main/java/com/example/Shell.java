package com.example;

import org.jline.builtins.Completers;
import org.jline.reader.*;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.widget.AutosuggestionWidgets;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Shell {
    /**
     * stores a list of environment variables (used in suggestions later)
     */
    private static String[] shellVariables = null;
    private static Terminal terminal = null;
    private static String prompt1 = "> ";
    private static String prompt2 = "";
    private static BufferedReader bufferedReader;

    public static String colorParse(String input) {
        for (int i = 256; i >= 0; i--) {
            input = input.replace("@" + i, "\u001b[38;5;" + i + "m");
            input = input.replace("#" + i, "\u001b[48;5;" + i + "m");
        }
        return input.replace("@-", "\u001b[0m");
    }

    public static String variablesParse(String input) {
        for (Map.Entry<String, String> variable : System.getenv().entrySet()) {
            input = input.replace("$" + variable.getKey(), variable.getValue());
        }
        return input;
    }

    public static String leftTrim(String s) {
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return s.substring(i);
    }

    public static void init() {
        if (shellVariables == null) {
            try {
                bufferedReader = new BufferedReader(new FileReader("./.rc"));
            } catch (FileNotFoundException ignored) {

            }
            terminal = null;
            try {
                terminal = TerminalBuilder.terminal();
            } catch (IOException ignore) {}
            shellVariables = new String[System.getenv().size() + 3];
            int varCounter = 0;
            for (Map.Entry<String, String> shellvar : System.getenv().entrySet()) {
                shellVariables[varCounter++] = "$" + shellvar.getKey();
            }
            shellVariables[varCounter++] = "$DATE";
            shellVariables[varCounter++] = "$TIME";
            shellVariables[varCounter] = "$FULL_DATE";
        }
    }

    public static String readLine(HashSet<String> variables) {
        init();
        variables = (HashSet<String>) variables.clone();
        int openBracketCounter = 0;
        boolean repeat = false;
        Map<String, Completer> completion = new HashMap<>();
        StringBuilder res = new StringBuilder();
        String line = "";
        completion.put("COMMANDS", new StringsCompleter("/hist", "/help", "/colorhelp", "/prompt1", "/prompt2", "/removeHist"));
        completion.put("COMMANDS_WITH_ARGS", new StringsCompleter("/prompt1", "/prompt2"));
        completion.put("ARGS", new StringsCompleter(shellVariables));
        do {
            Completers.RegexCompleter commandCompleter =
                new Completers.RegexCompleter("COMMANDS|COMMANDS_WITH_ARGS (ARGS ?)*", completion::get);
            AggregateCompleter completer = new AggregateCompleter(commandCompleter, new StringsCompleter(variables));
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .variable(LineReader.HISTORY_FILE, Paths.get("./.history"))
                    .appName("Compiler")
                    .highlighter(new Highlighter())
                    .build();
            AutosuggestionWidgets autosuggestionWidgets = new AutosuggestionWidgets(lineReader);
            autosuggestionWidgets.enable();
            try {
                if (bufferedReader == null || (line = bufferedReader.readLine()) == null) {
                    bufferedReader = null;
                    if (openBracketCounter > 0 || repeat) {
                        line = lineReader.readLine("... ", replaceTimedVariables(prompt2),
                                (Character) null, new String(new char[openBracketCounter]).replace("\0", "  "));
                    } else {
                        line = lineReader.readLine(replaceTimedVariables(prompt1), replaceTimedVariables(prompt2),
                                (Character) null, "");
                    }
                    Lexer lexer = new Lexer();
                    lexer.ignoreErrors(true);
                    Compiler.initLexer(lexer);
                    Parser parser = new Parser(lexer.lex(line));
                    openBracketCounter += parser.countStartsWith("LEFT_");
                    openBracketCounter -= parser.countStartsWith("RIGHT_");
                    if (parser.getTokens().size() > 0) {
                        String name = parser.getTokens().get(parser.getTokens().size() - 1).getName();
                        repeat = name.startsWith("OP") || name.equals("EXPONENTIATION") || name.equals("COMP") ||
                                name.equals("SET");
                    } else repeat = false;

                    int i = parser.findFirst("VAR");
                    while (i != -1) {
                        if (parser.getTokens().get(i + 1).getName().equals("ID"))
                            variables.add(parser.getTokens().get(i + 1).getText());
                        i = parser.findAfter(i + 1, "VAR");
                    }
                }
            } catch (EndOfFileException e) {
                throw new NoSuchElementException();
            } catch (UserInterruptException e) {
                return "";
            } catch (IOException ignore) {
            }
            if (line.startsWith("/")) {
                if (line.startsWith("/p1") || line.startsWith("/prompt1")) {
                    line = leftTrim(line.replace("/p1", "").replace("/prompt1", ""));
                    prompt1 = variablesParse(colorParse(line));
                } else if (line.startsWith("/p2") || line.startsWith("/prompt2")) {
                    line = leftTrim(line.replace("/p2", "").replace("/prompt2", ""));
                    prompt2 = variablesParse(colorParse(line));
                } else if (line.startsWith("/colorhelp")) {
                    for (byte i = 0; i < 16; i++) {
                        for (byte j = 0; j < 16; j++) {
                            System.out.print("\u001b[38;5;" + (j + i * 16) + "m" + (j + i * 16) + "\t");
                            if ((j + 1) % 8 == 0) System.out.println();
                        }
                    }
                } else if (line.startsWith("/hist")) {
                    History history = lineReader.getHistory();
                    Iterator<History.Entry> iter = history.iterator();
                    Highlighter highlighter = new Highlighter();
                    while (iter.hasNext()) {
                        History.Entry entry = iter.next();
                        highlighter.highlight(lineReader, entry.line()).println(lineReader.getTerminal());
                    }
                } else if (line.startsWith("/removeHist")) {
                    History history = lineReader.getHistory();
                    try {
                        history.purge();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (line.startsWith("/help")) {
                    System.out.println("/hist -> shows history");
                    System.out.println("/removeHist -> clears history");
                    System.out.println("/colorhelp -> prints a color/number table");
                    System.out.println("/prompt1 | /p1 -> set prompt 1 (left side) (@<color code> for foreground color and #<color code> for background color)");
                    System.out.println("/prompt2 | /p2 -> set prompt 2 (right side)");
                    System.out.println("/help -> prints this help message");
                } else {
                    res.append(line).append('\n');
                }
            } else {
                res.append(line).append('\n');
            }
        } while (openBracketCounter > 0 || repeat);
        return res.toString();
    }

    private static String replaceTimedVariables(String prompt) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E, MMM dd HH:mm:ss");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return prompt.replace("$FULL_DATE", new Date().toString())
                .replace("$DATE", LocalDateTime.now().format(dateFormatter))
                .replace("$TIME", LocalDateTime.now().format(timeFormatter));
    }
}