package edu.sjsu.cs166group2.cli;

import edu.sjsu.cs166group2.model.PassHash;
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

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Example that demonstrates how to build an interactive shell with JLine3 and picocli.
 * This example requires JLine 3.16+ and picocli 4.4+.
 * <p>
 * The built-in {@code PicocliCommands.ClearScreen} command was introduced in picocli 4.6.
 * </p>
 */
public class PicoRainbow {

    // DB Connection persists
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
                    insert.class,
                    dbConnect.class,
                    dbDisconnect.class,
                    dbLookUp.class,
                    hashDelete.class,
                    PicocliCommands.ClearScreen.class,
                    CommandLine.HelpCommand.class
            })
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

        @CommandLine.Option(names = {"-h", "--hash"}, defaultValue = "sha256",
                description = "Specify which rainbow table hash type to connect to. Defaults to sha256")
        String hashType;

        public void run() {
            try {
                dbc = new HashDao(new DatabaseConnector().initiateConnection(), hashType.toLowerCase());
                System.out.println("Connection established to table: " + hashType);
            } catch (Exception e){
                System.out.println(e.getMessage());
                System.out.println("failed to establish connection");
            }
        }
    }

    /**
     * This command inserts either a single plaintext password or a file full of plaintext passwords
     * into the database. Must use one of the options: "-f" or "-s" to specify whether you wish
     * to insert a single plaintext password or a file of plaintext passwords
     */
    @Command(name = "insert", mixinStandardHelpOptions = true, version = "1.0",
            description = {"Write hash/password pair.",
                    "Format: TBD"},
            subcommands = {Nested.class, CommandLine.HelpCommand.class})
    static class insert implements Runnable {

        @CommandLine.ArgGroup(multiplicity = "1")
        Exclusive exclusive;
        static class Exclusive {
            @CommandLine.Option(names = "-f", required = true)  File file;
            @CommandLine.Option(names = "-s", required = true)  String string;
        }

        @ParentCommand CliCommands parent;

        public void run() {
            try{
                // Ensure connection is established
                if (dbc == null) {
                    System.out.println("Connection is required to database prior to insertion. Please use connect " +
                            "command to create connection first.");
                    return;
                }
                // String has been specified. So create and upload hash for the string
                if (exclusive.string != null) {
                    HashUtil util = new HashUtil();
                    // IMPLEMENT some kind of mapper which takes a string and calls the
                    // appropriate hash function
                    PassHash hashObj = new PassHash(util.hash(exclusive.string, dbc.getHashType()), exclusive.string);

                    // Upload hashed password to the rainbow table here
                    if (!dbc.insertHashPair(hashObj))
                        throw new Exception("Failure to insert - HashDao insert returned false");
                    System.out.println("Inserted");

                }
                // File has been specified. So create and upload hash for all strings
                // within the file
                else if (exclusive.file != null) {
                    System.out.println("Processing file: " + exclusive.file);
                    BufferedReader br = new BufferedReader(new FileReader(exclusive.file));
                    // String for storing line by line input
                    String input;
                    HashUtil util = new HashUtil();
                    List<PassHash> listOfHashes = new ArrayList<>();

                    // Read file line by line
                    while ((input = br.readLine()) != null)
                        listOfHashes.add(new PassHash(util.hash(input, dbc.getHashType()), input));

                    // Upload all these hashes to DB
                    for (PassHash obj : listOfHashes) {
                        if (!dbc.insertHashPair(obj)) {
                            System.out.println("Couldn't insert: " + obj.getPassword() + " because insert returned" +
                                    "false");
                            continue;
                        }
                        System.out.println("Inserted " + obj.getPassword());
                    }
                }
                else {
                    // Should never get here - picoCLI must require at least file or a string
                    throw new IllegalStateException("Must specify either string or file name");
                }
            } catch (Exception e){
                System.out.println(e.getMessage());
                System.out.println("failed to insert");
            }
        }
    }

    /**
     * Command tries to look up corresponding plaintext password for a given hash
     */
    @Command(name = "find", mixinStandardHelpOptions = true, version = "1.0",
            description = {"Find password for a given hash within the table. Connection required to a table prior to " +
                    "invoking this command",
                    "Format: TBD"},
            subcommands = {Nested.class, CommandLine.HelpCommand.class})
    static class dbLookUp implements Runnable {

        @ParentCommand CliCommands parent;

        // Parameter of hash up to look up onto database
        @CommandLine.Parameters(arity = "1", description = "Hash to look up on rainbow table")
        String hash;

        public void run() {
            try{
                // Ensure connection is established prior to look up
                if (dbc == null) {
                    System.out.println("Connection is required to database prior to lookup. Please use connect command" +
                            " to create connection first.");
                    return;
                }
                String password = dbc.decryptHash(hash);
                // Decrypt hash function returns null for any hash issues (hash.len != 64, hashType Null || is Empty,
                // or hash is null or empty
                if (password == null) {
                    System.out.println("Invalid hash to look up. Please try again with a valid hash");
                    return;
                }
                // Lookup hash
                System.out.println("The password for hash '" + hash + "' of type '" + dbc.getHashType() + "' is: " +
                        password);
            } catch (Exception e){
                System.out.println(e.getMessage());
                System.out.println("Failed to find password for given hash");
            }
        }
    }

    /**
     * Command deletes a given hash if hash exists within db & table
     */
    @Command(name = "delete", mixinStandardHelpOptions = true, version = "1.0",
            description = {"Deletes a hash from db if hash exists within db",
                    "Format: TBD"},
            subcommands = {Nested.class, CommandLine.HelpCommand.class})
    static class hashDelete implements Runnable {

        @ParentCommand CliCommands parent;

        // Parameter of hash up to look up onto database
        @CommandLine.Parameters(arity = "1", description = "Hash to delete in rainbow table")
        String hash;

        public void run() {
            try{
                // Ensure connection is established prior to look up
                if (dbc == null) {
                    System.out.println("Connection is required to database prior to delete. Please use connect command" +
                            " to create connection first.");
                    return;
                }
                dbc.deleteHashPair(hash);
                // Delete hash. If delete fails, throw error
                if (!dbc.deleteHashPair(hash))
                    throw new Exception("Unable to delete hash");
                System.out.println("Hash deleted");
            } catch (Exception e){
                System.out.println(e.getMessage());
                System.out.println("Failed to find password for given hash");
            }
        }
    }

    /**
     * Command disconnects from database if connection exists
     */
    @Command(name = "disconnect", mixinStandardHelpOptions = true, version = "1.0",
            description = {"Disconnect from database table",
                    "Format: TBD"},
            subcommands = {Nested.class, CommandLine.HelpCommand.class})
    static class dbDisconnect implements Runnable {

        @ParentCommand CliCommands parent;

        public void run() {
            try{
                // Ensure connection exists
                if (dbc == null){
                    System.out.println("No connection exists. Nothing to disconnect from");
                    return;
                }
                // Disconnect
                System.out.println("Disconnected from " + dbc.getHashType() + " table");
                dbc = null;
            } catch (Exception e){
                System.out.println(e.getMessage());
                System.out.println("Failed to disconnect");
            }
        }
    }

    /**
     * Note from Saad: IDK what this command can be used for, but I left it in
     */
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

    /**
     * Runnable for PicoRainbow interpreter
     * @param args
     */
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