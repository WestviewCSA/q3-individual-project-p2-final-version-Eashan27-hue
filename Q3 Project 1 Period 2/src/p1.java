import java.io.FileNotFoundException;

/**
 * p1.java
 * -------
 * Main driver for "Wolverine's Quest for the Diamond Wolverine Coin."
 *
 * HOW COMMAND LINE ARGUMENTS WORK:
 *   When you run: java p1 --Queue --Incoordinate myMaze.txt
 *   Java puts each space-separated token into the args[] array:
 *       args[0] = "--Queue"
 *       args[1] = "--Incoordinate"
 *       args[2] = "myMaze.txt"
 *   We loop over args[] and set boolean flags for each switch we find.
 *   The input filename is always the LAST argument (args[args.length - 1]).
 *
 * REQUIRED SWITCHES (exactly one must be present):
 *   --Stack          Use the stack-based (DFS) path finder
 *   --Queue          Use the queue-based (BFS) path finder
 *   --Opt            Use the optimal (shortest) path finder
 *
 * OPTIONAL SWITCHES:
 *   --Incoordinate   Input file uses coordinate format  (default: text-map)
 *   --Outcoordinate  Output uses coordinate format       (default: text-map)
 *   --Time           Print the search algorithm runtime
 *   --Help           Print usage info and exit
 *
 * USAGE EXAMPLES:
 *   java p1 --Stack easyMap1.txt
 *   java p1 --Queue --Incoordinate coordinate.txt
 *   java p1 --Opt --Time hardMap1.txt
 *   java p1 --Queue --Outcoordinate mediumMap2.txt
 */
public class p1 {

    public static void main(String[] args) {

        // ---------------------------------------------------------------
        // STEP 1: Parse command-line arguments
        //
        // The input file is always the last argument: args[args.length - 1]
        // All other arguments are switches starting with "--"
        // ---------------------------------------------------------------

        // Guard: must have at least one argument (the filename)
        if (args.length == 0) {
            System.err.println("Error: No arguments provided.");
            System.err.println("Usage: java p1 [--Stack|--Queue|--Opt] [options] <inputfile>");
            System.exit(-1);
        }

        // The filename is always the very last argument
        String filename = args[args.length - 1];

        // Boolean flags for each supported switch
        boolean useStack      = false;
        boolean useQueue      = false;
        boolean useOpt        = false;
        boolean inCoordinate  = false;
        boolean outCoordinate = false;
        boolean printTime     = false;
        boolean printHelp     = false;

        // Loop over all arguments EXCEPT the last one (which is the filename)
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "--Stack":         useStack      = true; break;
                case "--Queue":         useQueue      = true; break;
                case "--Opt":           useOpt        = true; break;
                case "--Incoordinate":  inCoordinate  = true; break;
                case "--Outcoordinate": outCoordinate = true; break;
                case "--Time":          printTime     = true; break;
                case "--Help":          printHelp     = true; break;
                default:
                    System.err.println("Warning: Unrecognized switch \"" + args[i] + "\" — ignoring.");
            }
        }

        // ---------------------------------------------------------------
        // STEP 2: Handle --Help first (print and exit before any validation)
        // ---------------------------------------------------------------
        if (printHelp) {
            printHelp();
            System.exit(0);
        }

        // ---------------------------------------------------------------
        // STEP 3: Validate that exactly one routing mode was specified.
        //         If not, throw IllegalCommandLineInputsException.
        // ---------------------------------------------------------------
        int modeCount = (useStack ? 1 : 0) + (useQueue ? 1 : 0) + (useOpt ? 1 : 0);
        if (modeCount != 1) {
            try {
                throw new IllegalCommandLineInputsException(
                    "You must specify exactly one of --Stack, --Queue, or --Opt. "
                    + "Found " + modeCount + " routing mode(s).");
            } catch (IllegalCommandLineInputsException e) {
                System.err.println("Error: " + e.getMessage());
                System.exit(-1);
            }
        }

        // ---------------------------------------------------------------
        // STEP 4: Read the input file using the appropriate reader.
        //         --Incoordinate → readCoordinateFile()
        //         (default)      → readMapFile()
        //
        //         readMapFile()        handles: easyMap1.txt, easyMap2.txt,
        //                                       mediumMap1.txt, mediumMap2.txt,
        //                                       hardMap1.txt, hardMap2.txt
        //         readCoordinateFile() handles: coordinate.txt
        // ---------------------------------------------------------------
        char[][][] mazes = null;
        try {
            if (inCoordinate) {
                mazes = MapReader.readCoordinateFile(filename);
            } else {
                mazes = MapReader.readMapFile(filename);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: \"" + filename + "\"");
            System.exit(-1);
        } catch (IncorrectMapFormatException e) {
            System.err.println("Error (IncorrectMapFormat): " + e.getMessage());
            System.exit(-1);
        } catch (IllegalMapCharacterException e) {
            System.err.println("Error (IllegalMapCharacter): " + e.getMessage());
            System.exit(-1);
        } catch (IncompleteMapException e) {
            System.err.println("Error (IncompleteMap): " + e.getMessage());
            System.exit(-1);
        }

        // ---------------------------------------------------------------
        // STEP 5: Run the solver — time ONLY the search algorithm,
        //         NOT the file reading or output printing (per spec).
        // ---------------------------------------------------------------
        MazeSolver solver = new MazeSolver(mazes);

        long startTime = System.nanoTime();

        int[][] path = null;
        if (useStack) {
            path = solver.solveWithStack();
        } else if (useQueue) {
            path = solver.solveWithQueue();
        } else {
            path = solver.solveOptimal();
        }

        long endTime = System.nanoTime();
        double elapsedSeconds = (endTime - startTime) / 1_000_000_000.0;

        // ---------------------------------------------------------------
        // STEP 6: Print output in the requested format.
        // ---------------------------------------------------------------
        if (path == null) {
            System.out.println("The Wolverine Store is closed.");
        } else {
            if (outCoordinate) {
                printCoordinateOutput(path);
            } else {
                printMapOutput(mazes, path);
            }
        }

        // Print runtime only if --Time was set
        if (printTime) {
            System.out.printf("Total Runtime: %.9f seconds%n", elapsedSeconds);
        }
    }

    // -----------------------------------------------------------------------
    // Output: Text-map format
    // -----------------------------------------------------------------------

    /**
     * Marks the solution path with '+' on a copy of the maze and prints it.
     * 'W' (start) and '$' (goal) keep their original characters.
     * Only the intermediate steps are marked '+'.
     * The dimension line is NOT printed in the output (per spec).
     */
    private static void printMapOutput(char[][][] mazes, int[][] path) {
        int R = mazes.length;
        int M = mazes[0].length;
        int N = mazes[0][0].length;

        // Deep copy so we don't alter the original maze data
        char[][][] display = new char[R][M][N];
        for (int r = 0; r < R; r++)
            for (int row = 0; row < M; row++)
                for (int col = 0; col < N; col++)
                    display[r][row][col] = mazes[r][row][col];

        // Mark each step in the path (skip index 0 = 'W', skip last = '$')
        for (int i = 1; i < path.length - 1; i++) {
            int maze = path[i][0];
            int row  = path[i][1];
            int col  = path[i][2];
            display[maze][row][col] = '+';
        }

        // Print all mazes tiled top-to-bottom
        for (int r = 0; r < R; r++)
            for (int row = 0; row < M; row++)
                System.out.println(new String(display[r][row]));
    }

    // -----------------------------------------------------------------------
    // Output: Coordinate format
    // -----------------------------------------------------------------------

    /**
     * Prints the path in coordinate format: +ROW COL MAZE_LEVEL
     * Skips the starting 'W' position (index 0).
     *
     * Example output:
     *   +1 1 0
     *   +2 1 0
     *   +2 2 0
     *   +2 3 0
     */
    private static void printCoordinateOutput(int[][] path) {
        for (int i = 1; i < path.length; i++) {
            int maze = path[i][0];
            int row  = path[i][1];
            int col  = path[i][2];
            System.out.println("+" + row + " " + col + " " + maze);
        }
    }

    // -----------------------------------------------------------------------
    // --Help message
    // -----------------------------------------------------------------------

    private static void printHelp() {
        System.out.println("====================================================");
        System.out.println("  Wolverine's Quest for the Diamond Wolverine Coin  ");
        System.out.println("====================================================");
        System.out.println("Guides Wolverine through a maze to find the Diamond");
        System.out.println("Wolverine Buck ($) using different path strategies.");
        System.out.println();
        System.out.println("Usage: java p1 [--Stack|--Queue|--Opt] [options] <inputfile>");
        System.out.println();
        System.out.println("REQUIRED (exactly one):");
        System.out.println("  --Stack          Stack-based (DFS) path search");
        System.out.println("  --Queue          Queue-based (BFS) path search");
        System.out.println("  --Opt            Shortest (optimal) path search");
        System.out.println();
        System.out.println("OPTIONAL:");
        System.out.println("  --Incoordinate   Input is in coordinate format (default: text-map)");
        System.out.println("  --Outcoordinate  Output in coordinate format   (default: text-map)");
        System.out.println("  --Time           Print search algorithm runtime");
        System.out.println("  --Help           Print this message and exit");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  java p1 --Stack easyMap1.txt");
        System.out.println("  java p1 --Queue --Incoordinate coordinate.txt");
        System.out.println("  java p1 --Opt --Time hardMap1.txt");
        System.out.println("  java p1 --Queue --Outcoordinate mediumMap2.txt");
    }
}
