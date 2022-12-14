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
            "  _____ _           _____       _       _                   \n" +
                    " |  __ (_)         |  __ \\     (_)     | |                  \n" +
                    " | |__) |  ___ ___ | |__) |__ _ _ _ __ | |__   _____      __\n" +
                    " |  ___/ |/ __/ _ \\|  _  // _` | | '_ \\| '_ \\ / _ \\ \\ /\\ / /\n" +
                    " | |   | | (_| (_) | | \\ \\ (_| | | | | | |_) | (_) \\ V  V / \n" +
                    " |_|   |_|\\___\\___/|_|  \\_\\__,_|_|_| |_|_.__/ \\___/ \\_/\\_/ ",
                    ""},
            footer = {"", "Press Ctrl-D to exit."},
            //add commands here as {command}.class
            subcommands = {
                    insert.class,
                    dbConnect.class,
                    dbDisconnect.class,
                    dbLookUp.class,
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

    @Command(name = "connect", mixinStandardHelpOptions = true, version = "1.0",
            description = {"Establish a database connection. Only specify -w to connect to localhost."},
            subcommands = {CommandLine.HelpCommand.class})
    static class dbConnect implements Runnable {

        @ParentCommand CliCommands parent;

        @Option(names = {"-h", "--host"}, defaultValue = "localhost",
                description = "host of db to connect to")
        private String host;

        @Option(names = {"-p", "--port"}, defaultValue = "3306",
                description = "host port")
        private int port;

        @Option(names = {"-u", "--username"}, defaultValue = "root",
                description = "username of user connecting to db")
        private String username;

        @Option(names = {"-w", "--password"}, description = "Passphrase", interactive = true, required = true)
        private char[] password;
        @CommandLine.Option(names = {"-t", "--hash"}, defaultValue = "sha256",
                description = "Specify which rainbow table hash type to connect to. Defaults to sha256")
        private String hashType;

        public void run() {
            HashUtil hashUtil = new HashUtil();
            if(hashUtil.validHash(hashType)) {
                try {
                    dbc = new HashDao(new DatabaseConnector().initiateConnection(host, port, username, String.valueOf(password)), hashType.toLowerCase());
                    System.out.println("Connection established to table: " + hashType);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("failed to establish connection");
                }
            }
            else{
                System.out.println("Could not establish connection: Invalid hash type");
            }
        }
    }

    /**
     * This command inserts either a single plaintext password or a file full of plaintext passwords
     * into the database. Must use one of the options: "-f" or "-s" to specify whether you wish
     * to insert a single plaintext password or a file of plaintext passwords
     */
    @Command(name = "insert", mixinStandardHelpOptions = true, version = "1.0",
            description = {"Write single password/file of passwords to hashes.",
                    "Format: insert -f=<filename> or insert -s=<password>"},
            subcommands = {CommandLine.HelpCommand.class})
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

                    System.out.println("computed all hashes");
                    // Upload all these hashes to DB
                    dbc.insert(listOfHashes);
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
            description = {"Find password based on given hash"},
            subcommands = {CommandLine.HelpCommand.class})
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
     * Command disconnects from database if connection exists
     */
    @Command(name = "disconnect", mixinStandardHelpOptions = true, version = "1.0",
            description = {"Disconnect from database table.",
                    "Format: TBD"},
            subcommands = {CommandLine.HelpCommand.class})
    static class dbDisconnect implements Runnable {

        @ParentCommand
        CliCommands parent;

        public void run() {
            try {
                // Ensure connection exists
                if (dbc == null) {
                    System.out.println("No connection exists. Nothing to disconnect from");
                    return;
                }
                // Disconnect
                System.out.println("Disconnected from " + dbc.getHashType() + " table");
                dbc = null;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Failed to disconnect");
            }
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