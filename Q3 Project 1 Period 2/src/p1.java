import java.io.FileNotFoundException;

/**
 * p1.java
 * Main driver for Wolverine's Quest for the Diamond Wolverine Coin.
 *
 * Usage:
 *   java p1 [--Stack | --Queue | --Opt] [--Incoordinate] [--Outcoordinate] [--Time] [--Help] <mapfile>
 */
public class p1 {

    public static void main(String[] args) {

        // --- Parse command-line arguments ---
        boolean useStack       = false;
        boolean useQueue       = false;
        boolean useOpt         = false;
        boolean inCoordinate   = false;
        boolean outCoordinate  = false;
        boolean printTime      = false;
        boolean printHelp      = false;
        String  filename       = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--Stack":        useStack      = true; break;
                case "--Queue":        useQueue      = true; break;
                case "--Opt":          useOpt        = true; break;
                case "--Incoordinate": inCoordinate  = true; break;
                case "--Outcoordinate":outCoordinate = true; break;
                case "--Time":         printTime     = true; break;
                case "--Help":         printHelp     = true; break;
                default:
                    // Last non-flag argument is the filename
                    filename = args[i];
            }
        }

        // --Help: print usage and exit
        if (printHelp) {
            printHelp();
            System.exit(0);
        }

        // Validate exactly one routing mode is selected
        int modeCount = (useStack ? 1 : 0) + (useQueue ? 1 : 0) + (useOpt ? 1 : 0);
        if (modeCount != 1) {
            System.err.println("Error: You must specify exactly one of --Stack, --Queue, or --Opt.");
            System.exit(-1);
        }

        // Validate filename provided
        if (filename == null) {
            System.err.println("Error: No input file specified. The input file must be the last argument.");
            System.exit(-1);
        }

        // --- Read the maze ---
        char[][][] mazes = null;
        try {
            if (inCoordinate) {
                mazes = MapReader.readCoordinateFile(filename);
            } else {
                mazes = MapReader.readMapFile(filename);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: " + filename);
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

        // --- Run the solver ---
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

        // --- Output ---
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
     * Marks the solution path on the maze with '+' and prints all mazes top-to-bottom.
     * The start 'W' and end '$' are preserved; only intermediate steps get '+'.
     */
    private static void printMapOutput(char[][][] mazes, int[][] path) {
        // Deep copy so we don't mutate original
        int R = mazes.length;
        int M = mazes[0].length;
        int N = mazes[0][0].length;
        char[][][] display = new char[R][M][N];
        for (int r = 0; r < R; r++)
            for (int row = 0; row < M; row++)
                for (int col = 0; col < N; col++)
                    display[r][row][col] = mazes[r][row][col];

        // Mark path steps (skip first = W, skip last = $)
        for (int i = 1; i < path.length - 1; i++) {
            int maze = path[i][0];
            int row  = path[i][1];
            int col  = path[i][2];
            display[maze][row][col] = '+';
        }

        // Print: no dimension line in output per spec
        for (int r = 0; r < R; r++) {
            for (int row = 0; row < M; row++) {
                System.out.println(new String(display[r][row]));
            }
        }
    }

    /**
     * Prints the solution path in coordinate format.
     * Each line: +ROW COL MAZE_LEVEL
     * Skips the first step (Wolverine's start 'W').
     */
    private static void printCoordinateOutput(int[][] path) {
        // Skip the starting position (index 0 = W)
        for (int i = 1; i < path.length; i++) {
            System.out.println("+" + path[i][1] + " " + path[i][2] + " " + path[i][0]);
        }
    }

    private static void printHelp() {
        System.out.println("Wolverine's Quest for the Diamond Wolverine Coin");
        System.out.println("Navigates Wolverine through a maze to find the Diamond Wolverine Buck ($).");
        System.out.println();
        System.out.println("Usage: java p1 [switches] <inputfile>");
        System.out.println();
        System.out.println("Switches (exactly one routing mode required):");
        System.out.println("  --Stack          Find a path using a stack-based (DFS) approach");
        System.out.println("  --Queue          Find a path using a queue-based (BFS) approach");
        System.out.println("  --Opt            Find the shortest (optimal) path");
        System.out.println("  --Incoordinate   Input file is in coordinate format (default: text-map)");
        System.out.println("  --Outcoordinate  Output in coordinate format (default: text-map)");
        System.out.println("  --Time           Print the total search runtime in seconds");
        System.out.println("  --Help           Print this help message and exit");
    }
}
