package com.greentree.example.telegram;

import com.greentree.commons.util.iterator.IteratorUtil;
import com.greentree.commons.util.iterator.SizedIterable;
import com.greentree.example.telegram.ai.CellState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Game {

    private final CellState[][] cells;
    private final int lineToWin;

    public Game() {
        this(3, 3, 3);
    }

    public Game(int width, int height, int lineToWin) {
        this.cells = new CellState[width][height];
        this.lineToWin = lineToWin;
        clear();
    }

    public void clear() {
        for (var arr : cells)
            for (int i = 0; i < arr.length; i++)
                arr[i] = CellState.Empty;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                builder.append(switch (get(x, y)) {
                    case Empty -> "-";
                    default -> get(x, y).toString();
                });
                builder.append(' ');
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    public int getWidth() {
        return cells.length;
    }

    public int getHeight() {
        return cells[0].length;
    }

    public CellState get(int x, int y) {
        return cells[x][y];
    }

    public CellState getWin() {
        A:
        {
            for (var x = 0; x < getWidth(); x++)
                for (var y = 0; y < getHeight(); y++)
                    if (!taken(x, y))
                        break A;
            return CellState.Empty;
        }
        for (var row : cells) {
            if (checkWinLine(row, CellState.X))
                return CellState.X;
            if (checkWinLine(row, CellState.O))
                return CellState.O;
        }
        for (var column : columns(cells)) {
            if (checkWinLine(column, CellState.X))
                return CellState.X;
            if (checkWinLine(column, CellState.O))
                return CellState.O;
        }
        for (var diagonal : diagonal(cells)) {
            if (diagonal.size() < lineToWin)
                continue;
            if (checkWinLine(diagonal, CellState.X))
                return CellState.X;
            if (checkWinLine(diagonal, CellState.O))
                return CellState.O;
        }
        return null;
    }

    public boolean taken(int x, int y) {
        return get(x, y) != CellState.Empty;
    }

    private boolean checkWinLine(CellState[] row, CellState win) {
        int count = 0;
        for (int i = 0; i < row.length - lineToWin + count + 1; i++) {
            var cell = row[i];
            if (cell == win) {
                count++;
                if (count >= lineToWin)
                    return true;
            } else {
                count = 0;
            }
        }
        return false;
    }

    private static <T> Iterable<Iterable<T>> columns(T[][] cells) {
        var result = new ArrayList<Iterable<T>>();
        for (int i = 0; i < cells[0].length; i++) {
            var column = new ArrayList<T>();
            result.add(column);
            for (int j = 0; j < cells.length; j++) {
                column.add(cells[j][i]);
            }
        }
        return result;
    }

    private boolean checkWinLine(Iterable<CellState> line, CellState win) {
        if (line instanceof SizedIterable<CellState> s)
            return checkWinLine(s, win);
        var size = IteratorUtil.size(line);
        return checkWinLine(new SizedIterable<>() {
            @Override
            public int size() {
                return size;
            }

            @NotNull
            @Override
            public Iterator<CellState> iterator() {
                return line.iterator();
            }
        }, win);
    }

    public static <T> Iterable<? extends Collection<T>> diagonal(T[][] cells) {
        var resultX = new ArrayList<List<T>>();
        int t = 0;
        t = cells.length + cells[0].length - 1;
        while (t-- > 0)
            resultX.add(new ArrayList<>());
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                resultX.get(j - i + cells.length - 1).add(cells[cells.length - i - 1][j]);
            }
        }
        var resultY = new ArrayList<List<T>>();
        t = cells.length + cells[0].length - 1;
        while (t-- > 0)
            resultY.add(new ArrayList<>());
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                resultY.get(j - i + cells.length - 1).add(cells[i][j]);
            }
        }
        var result = new HashSet<List<T>>(resultX.size() + resultY.size());
        result.addAll(resultX);
        result.addAll(resultY);
        return result;
    }

    private boolean checkWinLine(SizedIterable<CellState> line, CellState win) {
        int count = 0, i = 0;
        var iter = line.iterator();
        while (i++ < line.size() - lineToWin + count + 1) {
            var cell = iter.next();
            if (cell == win) {
                count++;
                if (count >= lineToWin)
                    return true;
            } else {
                count = 0;
            }
        }
        return false;
    }

    public Game inverse() {
        var game = new Game(getWidth(), getHeight(), getLineToWin());
        for (int x = 0; x < getWidth(); x++)
            for (int y = 0; y < getHeight(); y++)
                switch (get(x, y)) {
                    case X -> game.set(x, y, CellState.O);
                    case O -> game.set(x, y, CellState.X);
                    case Empty -> game.set(x, y, CellState.Empty);
                }
        return game;
    }

    public int getLineToWin() {
        return lineToWin;
    }

    public void set(int x, int y, CellState cell) {
        Objects.requireNonNull(cell);
        if (cell != CellState.Empty && taken(x, y))
            throw new IllegalArgumentException(x + " " + y + " already set " + get(x, y));
        cells[x][y] = cell;
    }

}
