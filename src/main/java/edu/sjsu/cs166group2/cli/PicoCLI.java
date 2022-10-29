package edu.sjsu.cs166group2.cli;
import picocli.CommandLine;

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

@CommandLine.Command(
        name = "createHash"
)
class PicoCLICreateHash implements Runnable {
    @Override
    public void run() {
        System.out.println("Create Hash for a given parameter");
    }
}

@CommandLine.Command(
        name = "hashLookup"
)
class PicoCLILookupHash implements Runnable {
    @Override
    public void run() {
        System.out.println("Look up hash on database");
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