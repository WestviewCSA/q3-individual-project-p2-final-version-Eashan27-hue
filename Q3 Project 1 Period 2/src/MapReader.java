import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class MapReader {

 
    public static char[][][] readMapFile(String filename)
            throws FileNotFoundException,
                   IncorrectMapFormatException,
                   IllegalMapCharacterException,
                   IncompleteMapException {

        Scanner scanner = new Scanner(new File(filename));

        // Read M N R header — skip any comment/blank lines before it
        int[] dims = readHeader(scanner, filename);
        int M = dims[0];
        int N = dims[1];
        int R = dims[2];

        char[][][] mazes = new char[R][M][N];

        for (int r = 0; r < R; r++) {
            for (int row = 0; row < M; row++) {

                // Get next meaningful line (skip blanks and // comments)
                String line = nextMeaningfulLine(scanner);

                if (line == null) {
                    scanner.close();
                    throw new IncompleteMapException(
                        "Ran out of input in maze " + r + " at row " + row
                        + ". Expected " + M + " rows per maze.");
                }

                if (line.length() < N) {
                    scanner.close();
                    throw new IncompleteMapException(
                        "Maze " + r + ", row " + row + " has " + line.length()
                        + " character(s) but needs " + N + ".");
                }

                for (int col = 0; col < N; col++) {
                    char ch = line.charAt(col);
                    if (!isValidChar(ch)) {
                        scanner.close();
                        throw new IllegalMapCharacterException(
                            "Illegal character '" + ch + "' at maze " + r
                            + ", row " + row + ", col " + col + ".");
                    }
                    mazes[r][row][col] = ch;
                }
                // Extra characters past col N are ignored per spec
            }
        }

        scanner.close();
        return mazes;
    }

   
    public static char[][][] readCoordinateFile(String filename)
            throws FileNotFoundException,
                   IncorrectMapFormatException,
                   IllegalMapCharacterException,
                   IncompleteMapException {

        Scanner scanner = new Scanner(new File(filename));

        // Read M N R header
        int[] dims = readHeader(scanner, filename);
        int M = dims[0];
        int N = dims[1];
        int R = dims[2];

        // Initialize all cells to '.' — unspecified cells are open (per spec)
        char[][][] mazes = new char[R][M][N];
        for (int r = 0; r < R; r++)
            for (int row = 0; row < M; row++)
                for (int col = 0; col < N; col++)
                    mazes[r][row][col] = '.';

        // Read each coordinate entry: CHAR ROW COL MAZE_LEVEL
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            // Skip blank lines and // comment lines
            if (line.isEmpty() || line.startsWith("//")) continue;

            String[] tokens = line.split("\\s+");
            if (tokens.length < 4) continue;

            char ch = tokens[0].charAt(0);

            if (!isValidChar(ch)) {
                scanner.close();
                throw new IllegalMapCharacterException(
                    "Illegal character '" + ch + "' in entry: \"" + line + "\"");
            }

            int row, col, level;
            try {
                row   = Integer.parseInt(tokens[1]);
                col   = Integer.parseInt(tokens[2]);
                level = Integer.parseInt(tokens[3]);
            } catch (NumberFormatException e) {
                scanner.close();
                throw new IncorrectMapFormatException(
                    "Expected integers for ROW COL LEVEL in: \"" + line + "\"");
            }

            if (level < 0 || level >= R || row < 0 || row >= M || col < 0 || col >= N) {
                scanner.close();
                throw new IncompleteMapException(
                    "Coordinate out of bounds in: \"" + line + "\". "
                    + "Valid: row 0-" + (M-1) + ", col 0-" + (N-1)
                    + ", maze 0-" + (R-1) + ".");
            }

            mazes[level][row][col] = ch;
        }

        scanner.close();
        return mazes;
    }

 
    private static String nextMeaningfulLine(Scanner scanner) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // Skip blank lines and comment lines
            if (!line.trim().isEmpty() && !line.trim().startsWith("//")) {
                return line;
            }
        }
        return null;
    }

    
    private static int[] readHeader(Scanner scanner, String filename)
            throws IncorrectMapFormatException {

        String headerLine = nextMeaningfulLine(scanner);

        if (headerLine == null) {
            throw new IncorrectMapFormatException(
                "File \"" + filename + "\" is empty or has no valid header. Expected: M N R");
        }

        headerLine = headerLine.trim();
        String[] parts = headerLine.split("\\s+");

        if (parts.length < 3) {
            throw new IncorrectMapFormatException(
                "First line must have three positive integers (M N R). "
                + "Found: \"" + headerLine + "\"");
        }

        int M, N, R;
        try {
            M = Integer.parseInt(parts[0]);
            N = Integer.parseInt(parts[1]);
            R = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new IncorrectMapFormatException(
                "First line must have three positive integers (M N R). "
                + "Non-integer found in: \"" + headerLine + "\"");
        }

        if (M <= 0 || N <= 0 || R <= 0) {
            throw new IncorrectMapFormatException(
                "M, N, and R must be positive non-zero integers. "
                + "Found: M=" + M + ", N=" + N + ", R=" + R);
        }

        return new int[]{M, N, R};
    }

    
    private static boolean isValidChar(char ch) {
        return ch == '.' || ch == '@' || ch == 'W' || ch == '$' || ch == '|';
    }
}
