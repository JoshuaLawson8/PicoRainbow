package edu.sjsu.cs166group2.cli;

import edu.sjsu.cs166group2.util.DatabaseConnector;
import edu.sjsu.cs166group2.util.HashDao;
import edu.sjsu.cs166group2.util.HashUtil;
import org.fusesource.jansi.AnsiConsole;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.Builtins;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.widget.TailTipWidgets;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.shell.jline3.PicocliCommands;
import picocli.shell.jline3.PicocliCommands.PicocliCommandsFactory;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Example that demonstrates how to build an interactive shell with JLine3 and picocli.
 * This example requires JLine 3.16+ and picocli 4.4+.
 * <p>
 * The built-in {@code PicocliCommands.ClearScreen} command was introduced in picocli 4.6.
 * </p>
 */
public class PicoRainbow {

    private static HashDao dbc;
    /**
     * Top-level command that just prints help.
     * Add commands here as we develop
     */
    @Command(name = "",
            description = {
                    "Example interactive shell with completion and autosuggestions. " +
                            "Hit @|magenta <TAB>|@ to see available commands.",
                    "Hit @|magenta ALT-S|@ to toggle tailtips.",
                    ""},
            footer = {"", "Press Ctrl-D to exit."},
            //add commands here as {command}.class
            subcommands = {
                    insert.class, dbConnect.class, PicocliCommands.ClearScreen.class, CommandLine.HelpCommand.class})
    static class CliCommands implements Runnable {
        PrintWriter out;

        CliCommands() {}

        public void setReader(LineReader reader){
            out = reader.getTerminal().writer();
        }

        public void run() {
            out.println(new CommandLine(this).getUsageMessage());
        }
    }

    /**
     * A command with some options to demonstrate completion.
     */
    @Command(name = "connect", mixinStandardHelpOptions = true, version = "1.0",
            description = {"Command to establish a database connection.",
                    "Format: TBD"},
            subcommands = {Nested.class, CommandLine.HelpCommand.class})
    static class dbConnect implements Runnable {

        @ParentCommand CliCommands parent;

        public void run() {
            try{
                dbc = new HashDao(new DatabaseConnector().initiateConnection());
                System.out.println("Connection established.");
            } catch (Exception e){
                System.out.println(e.getMessage());
                System.out.println("failed to establish connection");
            }
        }
    }

    @Command(name = "insert", mixinStandardHelpOptions = true, version = "1.0",
            description = {"Write hash/password pair.",
                    "Format: TBD"},
            subcommands = {Nested.class, CommandLine.HelpCommand.class})
    static class insert implements Runnable {

        @ArgGroup(exclusive = false)
        private MyDuration myDuration = new MyDuration();

        static class MyDuration {
            @Option(names = {"-d", "--duration"},
                    description = "The duration quantity.",
                    required = true)
            private int amount;

            @Option(names = {"-u", "--timeUnit"},
                    description = "The duration time unit.",
                    required = true)
            private TimeUnit unit;
        }

        @ParentCommand CliCommands parent;

        public void run() {
            try{
                dbc.insertHashPair("ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb",
                        "a","sha256");
                System.out.println("inserted.");
            } catch (Exception e){
                System.out.println(e.getMessage());
                System.out.println("failed to insert.");
            }
        }
    }

    @Command(name = "nested", mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Hosts more sub-subcommands")
    static class Nested implements Runnable {
        public void run() {
            System.out.println("I'm a nested subcommand. I don't do much, but I have sub-subcommands!");
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Multiplies two numbers.")
        public void multiply(@Option(names = {"-l", "--left"}, required = true) int left,
                             @Option(names = {"-r", "--right"}, required = true) int right) {
            System.out.printf("%d * %d = %d%n", left, right, left * right);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Adds two numbers.")
        public void add(@Option(names = {"-l", "--left"}, required = true) int left,
                        @Option(names = {"-r", "--right"}, required = true) int right) {
            System.out.printf("%d + %d = %d%n", left, right, left + right);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Subtracts two numbers.")
        public void subtract(@Option(names = {"-l", "--left"}, required = true) int left,
                             @Option(names = {"-r", "--right"}, required = true) int right) {
            System.out.printf("%d - %d = %d%n", left, right, left - right);
        }
    }

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        try {
            Supplier<Path> workDir = () -> Paths.get(System.getProperty("user.dir"));
            // set up picocli commands
            CliCommands commands = new CliCommands();

            PicocliCommandsFactory factory = new PicocliCommandsFactory();
            // Or, if you have your own factory, you can chain them like this:
            // MyCustomFactory customFactory = createCustomFactory(); // your application custom factory
            // PicocliCommandsFactory factory = new PicocliCommandsFactory(customFactory); // chain the factories

            CommandLine cmd = new CommandLine(commands, factory);
            PicocliCommands picocliCommands = new PicocliCommands(cmd);

            Parser parser = new DefaultParser();
            try (Terminal terminal = TerminalBuilder.builder().build()) {
                SystemRegistry systemRegistry = new SystemRegistryImpl(parser, terminal, workDir, null);
                systemRegistry.setCommandRegistries(picocliCommands);
                systemRegistry.register("help", picocliCommands);

                LineReader reader = LineReaderBuilder.builder()
                        .terminal(terminal)
                        .completer(systemRegistry.completer())
                        .parser(parser)
                        .variable(LineReader.LIST_MAX, 50)   // max tab completion candidates
                        .build();
                commands.setReader(reader);
                factory.setTerminal(terminal);
                String prompt = "PicoRainbow> ";
                String rightPrompt = null;

                // start the shell and process input until the user quits with Ctrl-D
                String line;
                while (true) {
                    try {
                        systemRegistry.cleanUp();
                        line = reader.readLine(prompt, rightPrompt, (MaskingCallback) null, null);
                        systemRegistry.execute(line);
                    } catch (UserInterruptException e) {
                        // Ignore
                    } catch (EndOfFileException e) {
                        return;
                    } catch (Exception e) {
                        systemRegistry.trace(e);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            AnsiConsole.systemUninstall();
        }
    }
}