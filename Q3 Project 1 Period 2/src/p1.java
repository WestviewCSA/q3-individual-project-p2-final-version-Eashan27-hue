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
 *   The filename is always the LAST argument: args[args.length - 1]
 *   All switches come before it.
 *
 * REQUIRED (exactly one):
 *   --Stack          Stack-based (DFS) path finder
 *   --Queue          Queue-based (BFS) path finder
 *   --Opt            Optimal (shortest) path finder
 *
 * OPTIONAL:
 *   --Incoordinate   Input is coordinate format (default: text-map)
 *   --Outcoordinate  Output in coordinate format (default: text-map)
 *   --Time           Print search algorithm runtime
 *   --Help           Print usage info and exit
 *
 * EXAMPLES:
 *   java p1 --Stack easyMap1.txt
 *   java p1 --Queue --Incoordinate coordinate.txt
 *   java p1 --Opt --Time hardMap1.txt
 *   java p1 --Queue --Outcoordinate mediumMap2.txt
 */
public class p1 {

    public static void main(String[] args) {

        // ---------------------------------------------------------------
        // STEP 1: Parse command-line arguments
        // ---------------------------------------------------------------

        boolean useStack      = false;
        boolean useQueue      = false;
        boolean useOpt        = false;
        boolean inCoordinate  = false;
        boolean outCoordinate = false;
        boolean printTime     = false;
        boolean printHelp     = false;
        String  filename      = null;

        // Loop over every argument
        // The filename is the last arg (does not start with "--")
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--Stack":         useStack      = true; break;
                case "--Queue":         useQueue      = true; break;
                case "--Opt":           useOpt        = true; break;
                case "--Incoordinate":  inCoordinate  = true; break;
                case "--Outcoordinate": outCoordinate = true; break;
                case "--Time":          printTime     = true; break;
                case "--Help":          printHelp     = true; break;
                default:
                    // Anything that is not a "--" switch is the filename
                    filename = args[i];
            }
        }

        // ---------------------------------------------------------------
        // STEP 2: Handle --Help (print and exit immediately)
        // ---------------------------------------------------------------
        if (printHelp) {
            printHelp();
            System.exit(0);
        }

        // ---------------------------------------------------------------
        // STEP 3: Validate command line inputs — throw
        //         IllegalCommandLineInputsException for bad arguments
        // ---------------------------------------------------------------

        // Must have exactly one routing mode
        int modeCount = (useStack ? 1 : 0) + (useQueue ? 1 : 0) + (useOpt ? 1 : 0);
        if (modeCount != 1) {
            try {
                throw new IllegalCommandLineInputsException(
                    "Exactly one of --Stack, --Queue, or --Opt is required. "
                    + "You provided " + modeCount + ".");
            } catch (IllegalCommandLineInputsException e) {
                System.err.println("Error: " + e.getMessage());
                System.err.println("Usage: java p1 [--Stack|--Queue|--Opt] [--Incoordinate]"
                    + " [--Outcoordinate] [--Time] <inputfile>");
                System.exit(-1);
            }
        }

        // Must have a filename
        if (filename == null) {
            try {
                throw new IllegalCommandLineInputsException("No input file specified.");
            } catch (IllegalCommandLineInputsException e) {
                System.err.println("Error: " + e.getMessage());
                System.err.println();
                System.err.println("HOW TO FIX IN ECLIPSE:");
                System.err.println("  1. Click Run -> Run Configurations");
                System.err.println("  2. Select p1 on the left");
                System.err.println("  3. Click the Arguments tab");
                System.err.println("  4. In Program arguments type one of:");
                System.err.println("       --Queue easyMap1.txt");
                System.err.println("       --Stack mediumMap1.txt");
                System.err.println("       --Opt hardMap1.txt");
                System.err.println("       --Queue --Incoordinate coordinate.txt");
                System.err.println("  5. Make sure the .txt file is in your project root folder");
                System.err.println("  6. Click Apply then Run");
                System.exit(-1);
            }
        }

        // ---------------------------------------------------------------
        // STEP 4: Read the input file
        //   --Incoordinate  → MapReader.readCoordinateFile()  (coordinate.txt)
        //   (default)       → MapReader.readMapFile()          (easyMap1.txt etc.)
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
        // STEP 5: Run the solver
        //   Time ONLY the search — not file reading or output (per spec)
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
        // STEP 6: Print output
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

        if (printTime) {
            System.out.printf("Total Runtime: %.9f seconds%n", elapsedSeconds);
        }
    }

    // -----------------------------------------------------------------------
    // Output helpers
    // -----------------------------------------------------------------------

    /**
     * Prints the maze with '+' marking the path.
     * 'W' (start) and '$' (goal) keep their characters.
     * No dimension line is printed (per spec).
     */
    private static void printMapOutput(char[][][] mazes, int[][] path) {
        int R = mazes.length;
        int M = mazes[0].length;
        int N = mazes[0][0].length;

        // Deep copy so original maze is not changed
        char[][][] display = new char[R][M][N];
        for (int r = 0; r < R; r++)
            for (int row = 0; row < M; row++)
                for (int col = 0; col < N; col++)
                    display[r][row][col] = mazes[r][row][col];

        // Mark intermediate steps with '+' (skip index 0 = W, skip last = $)
        for (int i = 1; i < path.length - 1; i++) {
            int maze = path[i][0];
            int row  = path[i][1];
            int col  = path[i][2];
            display[maze][row][col] = '+';
        }

        // Print all mazes top-to-bottom
        for (int r = 0; r < R; r++)
            for (int row = 0; row < M; row++)
                System.out.println(new String(display[r][row]));
    }

    /**
     * Prints path in coordinate format: +ROW COL MAZE_LEVEL
     * Skips the starting 'W' position (index 0).
     */
    private static void printCoordinateOutput(int[][] path) {
        for (int i = 1; i < path.length; i++) {
            int maze = path[i][0];
            int row  = path[i][1];
            int col  = path[i][2];
            System.out.println("+" + row + " " + col + " " + maze);
        }
    }

    private static void printHelp() {
        System.out.println("====================================================");
        System.out.println("  Wolverine's Quest for the Diamond Wolverine Coin  ");
        System.out.println("====================================================");
        System.out.println("Usage: java p1 [--Stack|--Queue|--Opt] [options] <inputfile>");
        System.out.println();
        System.out.println("REQUIRED (exactly one routing mode):");
        System.out.println("  --Stack          Stack-based (DFS) path search");
        System.out.println("  --Queue          Queue-based (BFS) path search");
        System.out.println("  --Opt            Shortest (optimal) path search");
        System.out.println();
        System.out.println("OPTIONAL:");
        System.out.println("  --Incoordinate   Input is coordinate format (default: text-map)");
        System.out.println("  --Outcoordinate  Output in coordinate format (default: text-map)");
        System.out.println("  --Time           Print search algorithm runtime");
        System.out.println("  --Help           Print this message and exit");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  java p1 --Stack easyMap1.txt");
        System.out.println("  java p1 --Queue --Incoordinate coordinate.txt");
        System.out.println("  java p1 --Opt --Time hardMap1.txt");
        System.out.println("  java p1 --Stack --Outcoordinate mediumMap2.txt");
    }
}
