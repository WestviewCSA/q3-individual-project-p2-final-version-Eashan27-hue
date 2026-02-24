
class IllegalCommandLineInputsException extends Exception {
    public IllegalCommandLineInputsException(String message) {
        super(message);
    }
}



class IllegalMapCharacterException extends Exception {
    public IllegalMapCharacterException(String message) {
        super(message);
    }
}


/**
 * Thrown when the map file does not have enough data.
 *
 * WHEN THROWN:
 *   - A row in the text-map has fewer than N characters
 *   - The file ends before all M rows of a maze have been read
 *   - A coordinate entry has ROW/COL/LEVEL outside the declared dimensions
 *
 * EXAMPLE:
 *   Header says 5 rows, but file only has 3 → IncompleteMapException
 */
class IncompleteMapException extends Exception {
    public IncompleteMapException(String message) {
        super(message);
    }
}


/**
 * Thrown when the map file's header line is missing or malformed.
 *
 * WHEN THROWN:
 *   - The file is completely empty
 *   - The first line has fewer than 3 values
 *   - Any of the three values is not a valid integer
 *   - Any of M, N, or R is zero or negative
 *
 * EXAMPLE:
 *   First line is "5 abc 1" → IncorrectMapFormatException (abc is not an int)
 *   First line is "5 4"     → IncorrectMapFormatException (missing R)
 */
class IncorrectMapFormatException extends Exception {
    public IncorrectMapFormatException(String message) {
        super(message);
    }
}
