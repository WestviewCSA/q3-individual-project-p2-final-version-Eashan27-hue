import java.io.FileNotFoundException;

/**
 * cmdLine.java
 * ------------
 * Demonstrates command line arguments using the maze project.
 *
 * HOW TO RUN IN ECLIPSE:
 *   Run -> Run Configurations -> Arguments tab
 *   Enter something like:  --Queue easyMap1.txt
 *
 * args[0] = "--Queue"  (the routing switch)
 * args[1] = "easyMap1.txt"  (the map file — always last)
 *
 * Just like the video example:
 *   int num1 = Integer.parseInt(args[0]);   <- reads first argument
 *   int num2 = Integer.parseInt(args[1]);   <- reads second argument
 *
 * We do the same thing, but for maze switches and filenames:
 *   String switch1  = args[0];              <- reads first argument  e.g. "--Queue"
 *   String filename = args[args.length-1];  <- reads last argument   e.g. "easyMap1.txt"
 */
public class cmdLine {

    public static void main(String[] args) {

        // Must have at least 2 args: one switch + one filename
        if (args.length < 2) {
            System.out.println("Usage: java cmdLine [--Queue|--Stack|--Opt] [--Incoordinate] <mapfile>");
            System.exit(-1);
        }

        // The filename is ALWAYS the last argument (args[args.length - 1])
        // This matches how p1.java reads it
        String filename = args[args.length - 1];

        // Boolean flags — same as p1.java
        boolean useStack     = false;
        boolean useQueue     = false;
        boolean useOpt       = false;
        boolean inCoordinate = false;

        // Loop over all args EXCEPT the last one (which is the filename)
        // args[0], args[1], ... args[args.length-2] are the switches
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "--Stack":        useStack     = true; break;
                case "--Queue":        useQueue     = true; break;
                case "--Opt":          useOpt       = true; break;
                case "--Incoordinate": inCoordinate = true; break;
                default:
                    System.out.println("Unknown switch: " + args[i]);
            }
        }

        // Print back what we parsed — so you can see args[] working
        System.out.println("Filename : " + filename);
        System.out.println("--Stack  : " + useStack);
        System.out.println("--Queue  : " + useQueue);
        System.out.println("--Opt    : " + useOpt);
        System.out.println("--Incoordinate: " + inCoordinate);

        // Now read the maze file using MapReader — same as p1.java does
        char[][][] mazes = null;
        try {
            if (inCoordinate) {
                mazes = MapReader.readCoordinateFile(filename);
            } else {
                mazes = MapReader.readMapFile(filename);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found: " + filename);
            System.exit(-1);
        } catch (IncorrectMapFormatException e) {
            System.out.println("Error (IncorrectMapFormat): " + e.getMessage());
            System.exit(-1);
        } catch (IllegalMapCharacterException e) {
            System.out.println("Error (IllegalMapCharacter): " + e.getMessage());
            System.exit(-1);
        } catch (IncompleteMapException e) {
            System.out.println("Error (IncompleteMap): " + e.getMessage());
            System.exit(-1);
        }

        // Print the maze dimensions to confirm it was read correctly
        int R = mazes.length;
        int M = mazes[0].length;
        int N = mazes[0][0].length;
        System.out.println("Successfully read maze: " + R + " maze(s), " + M + " rows, " + N + " cols.");

        // Print the raw maze so we can see it loaded correctly
        System.out.println("Maze contents:");
        for (int r = 0; r < R; r++) {
            System.out.println("--- Maze " + r + " ---");
            for (int row = 0; row < M; row++) {
                System.out.println(new String(mazes[r][row]));
            }
        }
    }
}
