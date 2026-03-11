import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * MapReader.java
 * --------------
 * Reads maze input files in two formats using File and Scanner.
 *
 * FORMAT 1 - Text-map:
 *   Used by: easyMap1.txt, easyMap2.txt, mediumMap1.txt,
 *            mediumMap2.txt, hardMap1.txt, hardMap2.txt
 *
 * FORMAT 2 - Coordinate:
 *   Used by: coordinate.txt
 *
 * Both start with header line: M N R
 *   M = rows per maze
 *   N = cols per maze
 *   R = number of mazes
 *
 * Returns: char[R][M][N] indexed as [maze][row][col]
 *
 * Valid characters: '.' open  '@' wall  'W' start  '$' goal  '|' walkway
 */
public class MapReader {

    // =======================================================================
    // FORMAT 1: Text-map reader
    // =======================================================================

    /**
     * readMapFile() - reads a text-map input file.
     *
     * Layout:
     *   Line 1:   M N R
     *   Lines 2+: R mazes tiled top-to-bottom.
     *             Each maze is M rows of exactly N characters.
     *
     * Extra characters past column N on any line are ignored (per spec).
     *
     * @param filename  path to the input file
     * @return          char[R][M][N]
     * @throws FileNotFoundException        file not found
     * @throws IncorrectMapFormatException  bad or missing header
     * @throws IllegalMapCharacterException invalid character in grid
     * @throws IncompleteMapException       row or maze too short
     */
    public static char[][][] readMapFile(String filename)
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

        char[][][] mazes = new char[R][M][N];

        for (int r = 0; r < R; r++) {
            for (int row = 0; row < M; row++) {

                if (!scanner.hasNextLine()) {
                    scanner.close();
                    throw new IncompleteMapException(
                        "Ran out of input in maze " + r + " at row " + row
                        + ". Expected " + M + " rows per maze.");
                }

                String line = scanner.nextLine();

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

    // =======================================================================
    // FORMAT 2: Coordinate-based reader
    // =======================================================================

    /**
     * readCoordinateFile() - reads a coordinate-based input file.
     *
     * Layout:
     *   Line 1:   M N R
     *   Lines 2+: CHAR ROW COL MAZE_LEVEL  (one entry per line)
     *   All unlisted cells default to '.' (open).
     *
     * @param filename  path to the input file
     * @return          char[R][M][N]
     * @throws FileNotFoundException        file not found
     * @throws IncorrectMapFormatException  bad or missing header
     * @throws IllegalMapCharacterException invalid CHAR in entry
     * @throws IncompleteMapException       coordinate out of bounds
     */
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
            if (line.isEmpty()) continue;

            String[] tokens = line.split("\\s+");
            if (tokens.length < 4) continue; // skip incomplete lines per spec

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

    // =======================================================================
    // Shared helpers
    // =======================================================================

    /**
     * Reads and validates the "M N R" header line.
     * Called by both readMapFile() and readCoordinateFile().
     */
    private static int[] readHeader(Scanner scanner, String filename)
            throws IncorrectMapFormatException {

        if (!scanner.hasNextLine()) {
            throw new IncorrectMapFormatException(
                "File \"" + filename + "\" is empty. Expected header: M N R");
        }

        String headerLine = scanner.nextLine().trim();
        String[] parts    = headerLine.split("\\s+");

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

    /**
     * Returns true if ch is a valid maze character.
     * Valid: '.' open  '@' wall  'W' start  '$' goal  '|' walkway
     */
    private static boolean isValidChar(char ch) {
        return ch == '.' || ch == '@' || ch == 'W' || ch == '$' || ch == '|';
    }
}
