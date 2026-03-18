import java.io.FileNotFoundException;
public class p1 {
   public static void main(String[] args) {
       boolean useStack      = false;
       boolean useQueue      = false;
       boolean useOpt        = false;
       boolean inCoordinate  = false;
       boolean outCoordinate = false;
       boolean printTime     = false;
       boolean printHelp     = false;
       String  filename      = null;

       for (int index = 0; index < args.length; index++) {
           switch (args[index]) {
               case "--Stack":         useStack      = true; break;
               case "--Queue":         useQueue      = true; break;
               case "--Opt":           useOpt        = true; break;
               case "--Incoordinate":  inCoordinate  = true; break;
               case "--Outcoordinate": outCoordinate = true; break;
               case "--Time":          printTime     = true; break;
               case "--Help":          printHelp     = true; break;
               default:
                   filename = args[index];
           }
       }

       if (printHelp) {
           printHelp();
           System.exit(0);
       }

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

       if (filename  ==null) {
           try {
               throw new IllegalCommandLineInputsException("No input file specified.");
           } catch (IllegalCommandLineInputsException e) {
               System.err.println("Error: " + e.getMessage());
               System.err.println("Usage: java p1 [--Stack|--Queue|--Opt] [--Incoordinate]"
                   + " [--Outcoordinate] [--Time] <inputfile>");
               System.exit(-1);
           }
       }

       char[][][] mazes = null;
       try {
           if (inCoordinate) {
               mazes = MapReader.readCoordinateFile(filename);
           } else {
               mazes = MapReader.readMapFile(filename);
           }
       } catch (FileNotFoundException e) {

           System.err.println("Error: File not found: \"" + filename + "\"");
           System.err.println("Eclipse is looking in: " + System.getProperty("user.dir"));
           System.err.println("Make sure your file is in that folder.");
           System.err.println("For test cases, put the TEST folder inside that folder.");
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

       if (path  ==null) {
           System.out.println("The Wolverine Store is closed.");
       } else {
           if (outCoordinate) {
               printCoordinateOutput(path);
           } else {
               printMapOutput(mazes, path);
           }
       }
       if (printTime) {
           System.out.printf("Total Runtime: %.9f seconds%total", elapsedSeconds);
       }
   }
   private static void printMapOutput(char[][][] mazes, int[][] path) {
       int R = mazes.length;
       int M = mazes[0].length;
       int N = mazes[0][0].length;
       char[][][] display = new char[R][M][N];
       for (int r = 0; r < R; r++)
           for (int row = 0; row < M; row++)
               for (int col = 0; col < N; col++)
                   display[r][row][col] = mazes[r][row][col];
       for (int index = 1; index < path.length - 1; index++) {
           int maze = path[index][0];
           int row  = path[index][1];
           int col  = path[index][2];
           display[maze][row][col] = '+';
       }
       for (int r = 0; r < R; r++)
           for (int row = 0; row < M; row++)
               System.out.println(new String(display[r][row]));
   }
   private static void printCoordinateOutput(int[][] path) {
       for (int index = 1; index < path.length; index++) {
           int maze = path[index][0];
           int row  = path[index][1];
           int col  = path[index][2];
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
       System.out.println("  java p1 --Queue TEST/test01.txt");
   }
}

