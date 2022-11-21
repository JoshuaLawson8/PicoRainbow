package edu.sjsu.cs166group2.cli;
import picocli.CommandLine;
import edu.sjsu.cs166group2.util.HashUtil;

import java.io.*;

@CommandLine.Command(
        name = "PicoCLI",
        subcommands = {
                PicoCLICreateHash.class,
                PicoCLILookupHash.class,
                PicoCLIPostHash.class
        }
)
public class PicoCLI implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new PicoCLI()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.out.println("Running PicoCLI");
    }
}

/**
 * Creates hash for either a string or for all strings (plaintext) within a file
 * then uploads hash (or hashes in the case of a file) onto rainbow table
 */
@CommandLine.Command(
        name = "createHash"
)
class PicoCLICreateHash implements Runnable {

    /**
     * Either a file or string parameter must be specified.
     * Both can't be specified at the same time
     */
    @CommandLine.ArgGroup(multiplicity = "1")
    Exclusive exclusive;
    static class Exclusive {
        @CommandLine.Option(names = "-f", required = true)  File file;
        @CommandLine.Option(names = "-s", required = true)  String string;
    }
    @CommandLine.Parameters(arity = "1", description = "Type of hash to create and upload to rainbow table")
    String hashType;

    @Override
    public void run() {
        // String has been specified. So create and upload hash for the string
        if (exclusive.string != null) {
            HashUtil util = new HashUtil();
            // IMPLEMENT some kind of mapper which takes a string and calls the
            // appropriate hash function
            String hashedPassword = util.hash256(exclusive.string);

            // IMPLEMENT logic for uploading hashed password to the rainbow table here
            System.out.println("Password: " + exclusive.string + " becomes " + hashedPassword + " after " + hashType + " hash");

        }
        // File has been specified. So create and upload hash for all strings
        // within the file
        else if (exclusive.file != null) {
            System.out.println("Processing file: " + exclusive.file);
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(exclusive.file));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            // Declaring a string variable
            String st;

            while (true)
            {
                try {
                    if ((st = br.readLine()) == null) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(st);
            }
        }
        else {
            // Should never get here - picoCLI must require at least file or a
            // string
            throw new IllegalStateException("Must specify either string or file name");
        }
    }
}


/**
 * Class to be invoked when looking up a hash within rainbow table. Invoked with
 * the find parameter.
 */
@CommandLine.Command(
        name = "find"
)
class PicoCLILookupHash implements Runnable {
    // Parameter of hash up to look up onto database
    @CommandLine.Parameters(arity = "1", description = "Hash to look up on rainbow table")
    String hash;

    // Hash type to look up
    @CommandLine.Option(names = {"-h", "--hash"}, defaultValue = "sha256", description = "Type of input hash")
    String hashType;

    // Run Command that's run when something's run
    @Override
    public void run() {
        System.out.println("Look up " + hash + " with type " + hashType + " on database");
    }
}


@CommandLine.Command(
        name = "postHash"
)
class PicoCLIPostHash implements Runnable {
    @Override
    public void run() {
        System.out.println("Post a hash and its corresponding password to database");
    }
}