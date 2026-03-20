import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


public class MazeSolver {

    private final char[][][] mazes;
    private final int R; // number of mazes
    private final int M; // rows per maze
    private final int N; // cols per maze

    // North, South, East, West - checked in this order per spec
    private static final int[] DR = {-1, 1, 0, 0};
    private static final int[] DC = {0, 0, 1, -1};

    public MazeSolver(char[][][] mazes) {
        this.mazes = mazes;
        this.R     = mazes.length;
        this.M     = mazes[0].length;
        this.N     = mazes[0][0].length;
    }

   
    public int[][] solveWithQueue() {
        int[] start = findStart();
        if (start == null) return null;

        Map<Integer, Integer> parent = new HashMap<>();
        Queue<int[]> queue = new LinkedList<>();

        int startKey = encode(start[0], start[1], start[2]);
        parent.put(startKey, -1);
        queue.add(start);

        while (!queue.isEmpty()) {
            int[] cur  = queue.poll();
            int maze   = cur[0];
            int row    = cur[1];
            int col    = cur[2];

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
                    return reconstructPath(parent, nKey);
                }

                if (cell == '|') {
                    // Walkway - also visit same position in next maze
                    int nextMaze = maze + 1;
                    if (nextMaze < R) {
                        int wKey = encode(nextMaze, nr, nc);
                        if (!parent.containsKey(wKey)) {
                            parent.put(wKey, nKey);
                            queue.add(new int[]{nextMaze, nr, nc});
                        }
                    }
                }

                queue.add(new int[]{maze, nr, nc});
            }
        }

        return null; // no path found
    }

    public int[][] solveWithStack() {
        int[] start = findStart();
        if (start == null) return null;

        Map<Integer, Integer> parent = new HashMap<>();
        Deque<int[]> stack = new ArrayDeque<>();

        int startKey = encode(start[0], start[1], start[2]);
        parent.put(startKey, -1);
        stack.push(start);

        while (!stack.isEmpty()) {
            int[] cur  = stack.pop();
            int maze   = cur[0];
            int row    = cur[1];
            int col    = cur[2];

            // Push in reverse so North (d=0) is processed first (LIFO)
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
                    return reconstructPath(parent, nKey);
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
                }

                stack.push(new int[]{maze, nr, nc});
            }
        }

        return null;
    }

 
    public int[][] solveOptimal() {
        int[] start = findStart();
        if (start == null) return null;

        Map<Integer, Integer> parent = new HashMap<>();
        Queue<int[]> queue = new LinkedList<>();

        int startKey = encode(start[0], start[1], start[2]);
        parent.put(startKey, -1);
        queue.add(start);

        while (!queue.isEmpty()) {
            int[] cur  = queue.poll();
            int maze   = cur[0];
            int row    = cur[1];
            int col    = cur[2];

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
                    return reconstructPath(parent, nKey);
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
                }

                queue.add(new int[]{maze, nr, nc});
            }
        }

        return null;
    }

   
    private int[] findStart() {
        for (int r = 0; r < R; r++)
            for (int row = 0; row < M; row++)
                for (int col = 0; col < N; col++)
                    if (mazes[r][row][col] == 'W')
                        return new int[]{r, row, col};
        return null;
    }

    
    private int encode(int maze, int row, int col) {
        return maze * M * N + row * N + col;
    }

    
    private int[] decode(int key) {
        int col  = key % N;
        int row  = (key / N) % M;
        int maze = key / (M * N);
        return new int[]{maze, row, col};
    }

    /** Returns true if (row, col) is within the maze grid. */
    private boolean inBounds(int row, int col) {
        return row >= 0 && row < M && col >= 0 && col < N;
    }

   
    private int[][] reconstructPath(Map<Integer, Integer> parent, int goalKey) {
        List<int[]> path = new ArrayList<>();
        int cur = goalKey;

        while (cur != -1) {
            path.add(decode(cur));
            Integer p = parent.get(cur);
            cur = (p == null) ? -1 : p;
        }

        Collections.reverse(path);
        return path.toArray(new int[0][]);
    }
}
