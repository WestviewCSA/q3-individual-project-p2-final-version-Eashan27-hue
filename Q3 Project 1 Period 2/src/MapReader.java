import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


    public static char[][][] readMapFile(String filename)
            throws FileNotFoundException,
                   IncorrectMapFormatException,
                   IllegalMapCharacterException,
                   IncompleteMapException {

        // Open the file using File + Scanner (required by lab spec)
        Scanner scanner = new Scanner(new File(filename));

        // --- Read and validate the "M N R" header line ---
        int[] dims = readHeader(scanner, filename);
        int M = dims[0]; // rows per maze
        int N = dims[1]; // cols per maze
        int R = dims[2]; // number of mazes

        // Allocate the 3D array: [maze index][row][col]
        char[][][] mazes = new char[R][M][N];

        // --- Read R mazes, each M rows tall ---
        for (int r = 0; r < R; r++) {
            for (int row = 0; row < M; row++) {

                // Check that another line exists before trying to read it
                if (!scanner.hasNextLine()) {
                    scanner.close();
                    throw new IncompleteMapException(
                        "Ran out of input inside maze " + r + " at row " + row
                        + ". Expected " + M + " rows per maze.");
                }

                String line = scanner.nextLine();

                // Each line must have at least N characters
                if (line.length() < N) {
                    scanner.close();
                    throw new IncompleteMapException(
                        "Maze " + r + ", row " + row + " has " + line.length()
                        + " character(s) but needs " + N + ".");
                }

                // Read exactly N characters from this line
                for (int col = 0; col < N; col++) {
                    char ch = line.charAt(col);

                    if (!isValidChar(ch)) {
                        scanner.close();
                        throw new IllegalMapCharacterException(
                            "Illegal character '" + ch + "' found at maze " + r
                            + ", row " + row + ", col " + col + ".");
                    }

                    mazes[r][row][col] = ch;
                }
                // Any characters after column N on the same line are ignored (per spec)
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

        // Open the file using File + Scanner (required by lab spec)
        Scanner scanner = new Scanner(new File(filename));

        // --- Read and validate the "M N R" header line ---
        int[] dims = readHeader(scanner, filename);
        int M = dims[0]; // rows per maze
        int N = dims[1]; // cols per maze
        int R = dims[2]; // number of mazes

        // Initialize the entire grid to '.' — all unspecified cells are open (per spec)
        char[][][] mazes = new char[R][M][N];
        for (int r = 0; r < R; r++)
            for (int row = 0; row < M; row++)
                for (int col = 0; col < N; col++)
                    mazes[r][row][col] = '.';

        // --- Read coordinate entries one line at a time ---
        // Each line format:  CHAR ROW COL MAZE_LEVEL
        // Example line:      @ 3 2 0   means maze 0, row 3, col 2 is a wall '@'
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            // Skip blank lines
            if (line.isEmpty()) continue;

            // Split on whitespace to get individual tokens
            String[] tokens = line.split("\\s+");

            // Need exactly 4 tokens: CHAR, ROW, COL, MAZE_LEVEL
            // Per spec: format is assumed correct, so skip incomplete lines
            if (tokens.length < 4) continue;

            // Token 0: the maze character (must be exactly one character)
            char ch = tokens[0].charAt(0);

            // Validate the character — must be one of: . @ W $ |
            if (!isValidChar(ch)) {
                scanner.close();
                throw new IllegalMapCharacterException(
                    "Illegal character '" + ch + "' in coordinate entry: \"" + line + "\"");
            }

            // Tokens 1-3: integer coordinates
            int row, col, level;
            try {
                row   = Integer.parseInt(tokens[1]);
                col   = Integer.parseInt(tokens[2]);
                level = Integer.parseInt(tokens[3]);
            } catch (NumberFormatException e) {
                scanner.close();
                throw new IncorrectMapFormatException(
                    "Expected integers for ROW, COL, MAZE_LEVEL in: \"" + line + "\"");
            }

            // Validate that the coordinate falls within the declared maze dimensions
            if (level < 0 || level >= R ||
                row   < 0 || row   >= M ||
                col   < 0 || col   >= N) {
                scanner.close();
                throw new IncompleteMapException(
                    "Coordinate out of bounds in entry: \"" + line + "\". "
                    + "Valid range: row 0-" + (M-1) + ", col 0-" + (N-1)
                    + ", maze 0-" + (R-1) + ".");
            }

            // Place the character into the correct cell
            mazes[level][row][col] = ch;
        }

        scanner.close();
        return mazes;
    }


    private static int[] readHeader(Scanner scanner, String filename)
            throws IncorrectMapFormatException {

        // File must not be empty
        if (!scanner.hasNextLine()) {
            throw new IncorrectMapFormatException(
                "File \"" + filename + "\" is empty. "
                + "First line must be: M N R (three positive integers).");
        }

        String headerLine = scanner.nextLine().trim();
        String[] parts    = headerLine.split("\\s+");

        // Must have at least 3 space-separated values
        if (parts.length < 3) {
            throw new IncorrectMapFormatException(
                "First line must contain three positive integers (M N R). "
                + "Found: \"" + headerLine + "\"");
        }

        int M, N, R;
        try {
            M = Integer.parseInt(parts[0]);
            N = Integer.parseInt(parts[1]);
            R = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new IncorrectMapFormatException(
                "First line must contain three positive integers (M N R). "
                + "Found non-integer value in: \"" + headerLine + "\"");
        }

        // All three values must be positive and non-zero
        if (M <= 0 || N <= 0 || R <= 0) {
            throw new IncorrectMapFormatException(
                "M, N, and R must all be positive non-zero integers. "
                + "Found: M=" + M + ", N=" + N + ", R=" + R);
        }

        return new int[]{M, N, R};
    }

   
    private static boolean isValidChar(char ch) {
        return ch == '.' || ch == '@' || ch == 'W' || ch == '$' || ch == '|';
    }
