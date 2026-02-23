import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * MazeSolver.java
 * Provides three routing approaches for navigating Wolverine through the maze:
 *   1) Queue-based (BFS) - finds a valid path
 *   2) Stack-based (DFS) - finds a valid path
 *   3) Optimal (BFS shortest path)
 *
 * The maze is stored as char[maze][row][col].
 * Valid chars: '.' open, '@' wall, 'W' start, '$' goal, '|' walkway to next maze.
 *
 * Path is returned as int[][3] where each entry is {mazeIndex, row, col}.
 * Returns null if no path exists.
 */
public class MazeSolver {

    private final char[][][] mazes;
    private final int R; // number of mazes
    private final int M; // rows per maze
    private final int N; // cols per maze

    // Movement deltas: North, South, East, West
    private static final int[] DR = {-1, 1, 0, 0};
    private static final int[] DC = {0, 0, 1, -1};

    public MazeSolver(char[][][] mazes) {
        this.mazes = mazes;
        this.R = mazes.length;
        this.M = mazes[0].length;
        this.N = mazes[0][0].length;
    }

    // -----------------------------------------------------------------------
    // Queue-based BFS approach
    // -----------------------------------------------------------------------

    /**
     * Finds a valid path using a queue (BFS).
     * Explores North, South, East, West in that order.
     * Handles multi-maze traversal via '|' walkway.
     *
     * @return path as int[][3] {maze, row, col}, or null if no path found
     */
    public int[][] solveWithQueue() {
        int[] start = findStart();
        if (start == null) return null;

        // parent map: encodes each visited cell -> the cell that discovered it
        // key/value: encoded as mazeIndex * M * N + row * N + col
        Map<Integer, Integer> parent = new HashMap<>();

        Queue<int[]> queue = new LinkedList<>();
        int startKey = encode(start[0], start[1], start[2]);
        parent.put(startKey, -1); // start has no parent
        queue.add(start);

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int maze = cur[0], row = cur[1], col = cur[2];

            // Try all 4 neighbors
            for (int d = 0; d < 4; d++) {
                int nr = row + DR[d];
                int nc = col + DC[d];

                if (!inBounds(nr, nc)) continue;

                char cell = mazes[maze][nr][nc];

                if (cell == '@') continue; // wall

                int nKey = encode(maze, nr, nc);
                if (parent.containsKey(nKey)) continue; // already visited

                parent.put(nKey, encode(maze, row, col));

                if (cell == '$') {
                    // Found the goal — reconstruct path
                    return reconstructPath(parent, encode(maze, nr, nc));
                }

                if (cell == '|') {
                    // Walkway: move to same position in next maze
                    int nextMaze = maze + 1;
                    if (nextMaze < R) {
                        int wKey = encode(nextMaze, nr, nc);
                        if (!parent.containsKey(wKey)) {
                            parent.put(wKey, nKey);
                            queue.add(new int[]{nextMaze, nr, nc});
                        }
                    }
                    // Also enqueue the walkway cell itself in current maze for path marking
                    queue.add(new int[]{maze, nr, nc});
                    continue;
                }

                queue.add(new int[]{maze, nr, nc});
            }
        }

        return null; // no path found
    }

    // -----------------------------------------------------------------------
    // Stack-based DFS approach
    // -----------------------------------------------------------------------

    /**
     * Finds a valid path using a stack (DFS).
     * Pushes North, South, East, West neighbors in that order.
     * Handles multi-maze traversal via '|' walkway.
     *
     * @return path as int[][3] {maze, row, col}, or null if no path found
     */
    public int[][] solveWithStack() {
        int[] start = findStart();
        if (start == null) return null;

        Map<Integer, Integer> parent = new HashMap<>();

        Deque<int[]> stack = new ArrayDeque<>();
        int startKey = encode(start[0], start[1], start[2]);
        parent.put(startKey, -1);
        stack.push(start);

        while (!stack.isEmpty()) {
            int[] cur = stack.pop();
            int maze = cur[0], row = cur[1], col = cur[2];

            // Try all 4 neighbors (push in reverse so North is processed first)
            for (int d = 3; d >= 0; d--) {
                int nr = row + DR[d];
                int nc = col + DC[d];

                if (!inBounds(nr, nc)) continue;

                char cell = mazes[maze][nr][nc];

                if (cell == '@') continue;

                int nKey = encode(maze, nr, nc);
                if (parent.containsKey(nKey)) continue;

                parent.put(nKey, encode(maze, row, col));

                if (cell == '$') {
                    return reconstructPath(parent, encode(maze, nr, nc));
                }

                if (cell == '|') {
                    int nextMaze = maze + 1;
                    if (nextMaze < R) {
                        int wKey = encode(nextMaze, nr, nc);
                        if (!parent.containsKey(wKey)) {
                            parent.put(wKey, nKey);
                            stack.push(new int[]{nextMaze, nr, nc});
                        }
                    }
                    stack.push(new int[]{maze, nr, nc});
                    continue;
                }

                stack.push(new int[]{maze, nr, nc});
            }
        }

        return null;
    }

    // -----------------------------------------------------------------------
    // Optimal BFS approach (shortest path)
    // -----------------------------------------------------------------------

    /**
     * Finds the shortest path using BFS (guaranteed optimal for unweighted graphs).
     * Handles multi-maze traversal via '|' walkway.
     *
     * @return path as int[][3] {maze, row, col}, or null if no path found
     */
    public int[][] solveOptimal() {
        int[] start = findStart();
        if (start == null) return null;

        Map<Integer, Integer> parent = new HashMap<>();

        Queue<int[]> queue = new LinkedList<>();
        int startKey = encode(start[0], start[1], start[2]);
        parent.put(startKey, -1);
        queue.add(start);

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int maze = cur[0], row = cur[1], col = cur[2];

            for (int d = 0; d < 4; d++) {
                int nr = row + DR[d];
                int nc = col + DC[d];

                if (!inBounds(nr, nc)) continue;

                char cell = mazes[maze][nr][nc];

                if (cell == '@') continue;

                int nKey = encode(maze, nr, nc);
                if (parent.containsKey(nKey)) continue;

                parent.put(nKey, encode(maze, row, col));

                if (cell == '$') {
                    return reconstructPath(parent, encode(maze, nr, nc));
                }

                if (cell == '|') {
                    int nextMaze = maze + 1;
                    if (nextMaze < R) {
                        int wKey = encode(nextMaze, nr, nc);
                        if (!parent.containsKey(wKey)) {
                            parent.put(wKey, nKey);
                            queue.add(new int[]{nextMaze, nr, nc});
                        }
                    }
                    queue.add(new int[]{maze, nr, nc});
                    continue;
                }

                queue.add(new int[]{maze, nr, nc});
            }
        }

        return null;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Find Wolverine's starting position 'W' across all mazes. */
    private int[] findStart() {
        for (int r = 0; r < R; r++)
            for (int row = 0; row < M; row++)
                for (int col = 0; col < N; col++)
                    if (mazes[r][row][col] == 'W')
                        return new int[]{r, row, col};
        return null;
    }

    /** Encode a {maze, row, col} triple into a single integer key. */
    private int encode(int maze, int row, int col) {
        return maze * M * N + row * N + col;
    }

    /** Decode an integer key back into {maze, row, col}. */
    private int[] decode(int key) {
        int col  = key % N;
        int row  = (key / N) % M;
        int maze = key / (M * N);
        return new int[]{maze, row, col};
    }

    /** Check if (row, col) is within maze bounds. */
    private boolean inBounds(int row, int col) {
        return row >= 0 && row < M && col >= 0 && col < N;
    }

    /**
     * Reconstructs the path from start to goal using the parent map.
     * Returns the path as int[][3] from start to goal.
     */
    private int[][] reconstructPath(Map<Integer, Integer> parent, int goalKey) {
        List<int[]> path = new ArrayList<>();
        int cur = goalKey;

        while (cur != -1) {
            path.add(decode(cur));
            Integer parentKey = parent.get(cur);
            cur = (parentKey == null) ? -1 : parentKey;
        }

        Collections.reverse(path);
        return path.toArray(new int[0][]);
    }
}
