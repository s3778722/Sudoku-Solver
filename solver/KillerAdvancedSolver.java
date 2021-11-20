/*
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */

package solver;

import java.util.ArrayList;

import grid.KillerSudokuGrid;
import grid.SudokuGrid;


/**
 * Your advanced solver for Killer Sudoku.
 */
public class KillerAdvancedSolver extends KillerSudokuSolver
{
    public static class Placement {
        public int r;
        public int c;
        public int v;
        public Placement(int r, int c, int v) {
            this.r = r;
            this.c = c;
            this.v = v;
        }
    }

    //A row in the binary matrix - contains list of placements
    public static class Row {
        public ArrayList<Placement> placements;
        public KillerSudokuGrid.Cage cage;
        public boolean deleted;
        public Row(ArrayList<Placement> placements, KillerSudokuGrid.Cage cage) {
            this.placements = placements;
            this.cage = cage;
            deleted = false;
        }
    }

    //A column in the binary matrix - contains list of rows with a one in this column
    public static class Column {
        public ArrayList<Row> rows;
        public boolean deleted;
        public Column() {
            rows = new ArrayList<>();
            deleted = false;
        }
        public void add(Row row) {
            rows.add(row);
        }
        public int size() {
            int size = 0;
            for (Row row : rows) {
                if (!row.deleted)
                    size++;
            }
            return size;
        }
    }

    //Utility for accessing specific columns
    public static class ColumnList {
        Column[][] row_col;
        Column[][] row_val;
        Column[][] col_val;
        Column[][] box_val;
        Column[] cage_total;
        public ColumnList(int grid_dim, int num_cages) {
            row_col = new Column[grid_dim][grid_dim];
            row_val = new Column[grid_dim][grid_dim];
            col_val = new Column[grid_dim][grid_dim];
            box_val = new Column[grid_dim][grid_dim];
            cage_total = new Column[num_cages];

            for (int i = 0; i < grid_dim; i++) {
                for (int j = 0; j < grid_dim; j++) {
                    row_col[i][j] = new Column();
                    row_val[i][j] = new Column();
                    col_val[i][j] = new Column();
                    box_val[i][j] = new Column();
                }
                if (i < num_cages) {
                    cage_total[i] = new Column();
                }
            }
            for (int i = grid_dim; i < num_cages; i++) {
                cage_total[i] = new Column();
            }

        }
    }

    //Representation of binary matrix
    private static class Matrix {

        private ColumnList columns;
        KillerSudokuGrid grid;
        int grid_dim;
        int num_cages;

        public Matrix(KillerSudokuGrid grid) {

            this.grid = grid;
            this.grid_dim = grid.getGrid().length;
            this.num_cages = grid.getCages().size();
            columns = new ColumnList(grid_dim, grid.getCages().size());

            //For each cage
            for (KillerSudokuGrid.Cage cage : grid.getCages()) {
                for (int[] combination : getSumCombinations(cage.cells.size(), cage.total)) {

                    int[] index_combination = new int[cage.cells.size()];
                    for (int i = 0; i < cage.cells.size(); i++) {
                        index_combination[i] = grid.symbolIndex(combination[i]);
                    }

                    for (int[] ordering : getAllOrderings(index_combination)) {

                        //For every possible ordering of every possible combination of valid symbols that add to this cage's total

                        //Add this ordering of symbols as placements in a row
                        ArrayList<Placement> placements = new ArrayList<>();
                        for (int i = 0; i < cage.cells.size(); i++) {
                            KillerSudokuGrid.Cell cell = cage.cells.get(i);
                            placements.add(new Placement(cell.row, cell.col, ordering[i]));
                        }

                        Row row = new Row(placements, cage);

                        //Add this row to its satisfied columns
                        for (Placement placement: placements) {
                            columns.row_col[placement.r][placement.c].add(row);
                            columns.row_val[placement.r][placement.v].add(row);
                            columns.col_val[placement.c][placement.v].add(row);

                            int box_size = (int)Math.sqrt(grid_dim);
                            int boxCornerR = placement.r - placement.r % box_size;
                            int boxCornerC = placement.c - placement.c % box_size;
                            int box_num = (boxCornerR * box_size + boxCornerC) / box_size;
                            columns.box_val[box_num][placement.v].add(row);
                        }

                        columns.cage_total[grid.getCages().indexOf(cage)].add(row);

                    }
                }
            }

        }

        //Get all possible orderings of an array
        public ArrayList<int[]> getAllOrderings(int[] values) {
            return getAllOrderings(new ArrayList<>(), values, 0, values.length - 1);
        }

        public ArrayList<int[]> getAllOrderings(ArrayList<int[]> orderings, int[] values, int l, int r) {
            if (l == r) {
                orderings.add(values);
                return orderings;
            }
            for (int i = l; i <= r; i++) {
                int[] newValues = values.clone();

                int tmp = newValues[l];
                newValues[l] = newValues[i];
                newValues[i] = tmp;

                orderings = getAllOrderings(orderings, newValues, l + 1, r);

                tmp = values[l];
                values[l] = values[i];
                values[i] = tmp;
            }
            return orderings;
        }

        //Get all possible length-num_cells combinations of the valid symbols that add to a cage's total
        public ArrayList<int[]> getSumCombinations(int num_cells, int total) {
            ArrayList<int[]> combinations = getCombinations(new ArrayList<>(), grid.getValidSymbols(), num_cells, new int[num_cells], 0, grid.getValidSymbols().length - 1, 0);
            for (int i = 0; i < combinations.size(); i++) {
                int sum = 0;
                for (int j = 0; j < num_cells; j++) {
                    sum += combinations.get(i)[j];
                }
                if (sum != total) {
                    combinations.remove(i--);
                }
            }
            return combinations;
        }

        //Get all possible length-num_cells combinations of the valid symbols
        public ArrayList<int[]> getCombinations(ArrayList<int[]> combinations, int[] valid_symbols, int num_cells, int[] current_combination, int start, int end, int current_index) {
            current_combination = current_combination.clone();
            if (current_index == num_cells)
            {
                combinations.add(current_combination);
                return combinations;
            }

            for (int i = start; i <= end && end - i + 1 >= num_cells - current_index; i++)
            {
                current_combination[current_index] = valid_symbols[i];
                combinations = getCombinations(combinations, valid_symbols, num_cells, current_combination,  i+1, end, current_index+1);
            }
            return combinations;
        }


        public Column getLeastColumn() {
            Column leastColumn = null;
            int leastVal = grid_dim * grid_dim * grid_dim;

            for (int i = 0; i < grid_dim; i++) {
                for (int j = 0; j < grid_dim; j++) {
                    if (!columns.row_col[i][j].deleted && columns.row_col[i][j].size() < leastVal) {
                        leastColumn = columns.row_col[i][j];
                        leastVal = leastColumn.size();
                    }
                    if (!columns.row_val[i][j].deleted && columns.row_val[i][j].size() < leastVal) {
                        leastColumn = columns.row_val[i][j];
                        leastVal = leastColumn.size();
                    }
                    if (!columns.col_val[i][j].deleted && columns.col_val[i][j].size() < leastVal) {
                        leastColumn = columns.col_val[i][j];
                        leastVal = leastColumn.size();
                    }
                    if (!columns.box_val[i][j].deleted && columns.box_val[i][j].size() < leastVal) {
                        leastColumn = columns.box_val[i][j];
                        leastVal = leastColumn.size();
                    }
                    if (i < num_cages) {
                        if (!columns.cage_total[i].deleted && columns.cage_total[i].size() < leastVal) {
                            leastColumn = columns.cage_total[i];
                            leastVal = leastColumn.size();
                        }
                    }
                }
            }

            for (int i = grid_dim; i < num_cages; i++) {
                if (!columns.cage_total[i].deleted && columns.cage_total[i].size() < leastVal) {
                    leastColumn = columns.cage_total[i];
                    leastVal = leastColumn.size();
                }
            }

            return leastColumn;
        }

        //Include this row in the solution
        public void chooseRow(Row row, boolean delete) {

            for (Placement p : row.placements) {

                //For each column that this row satisfies, delete all rows that also satisfy this column, then delete column
                for (Row delRow : columns.row_col[p.r][p.c].rows) {
                    delRow.deleted = delete;
                }
                columns.row_col[p.r][p.c].deleted = delete;

                for (Row delRow : columns.row_val[p.r][p.v].rows) {
                    delRow.deleted = delete;
                }
                columns.row_val[p.r][p.v].deleted = delete;

                for (Row delRow : columns.col_val[p.c][p.v].rows) {
                    delRow.deleted = delete;
                }
                columns.col_val[p.c][p.v].deleted = delete;


                int box_size = (int) Math.sqrt(grid_dim);
                int boxCornerR = p.r - p.r % box_size;
                int boxCornerC = p.c - p.c % box_size;
                int box_num = (boxCornerR * box_size + boxCornerC) / box_size;

                for (Row delRow : columns.box_val[box_num][p.v].rows) {
                    delRow.deleted = delete;
                }
                columns.box_val[box_num][p.v].deleted = delete;

            }

            int cage_index = grid.getCages().indexOf(row.cage);
            for (Row delRow : columns.cage_total[cage_index].rows) {
                delRow.deleted = delete;
            }
            columns.cage_total[cage_index].deleted = delete;

            row.deleted = delete;

        }


    }


    public KillerAdvancedSolver() {
    } // end of KillerAdvancedSolver()

    public boolean solve(int[][] g, Matrix matrix) {

        Column leastColumn = matrix.getLeastColumn();

        //No more columns, grid is complete
        if (leastColumn == null)
            return true;

        //Choose a row in the least column
        for (Row row : leastColumn.rows) {
            if (!row.deleted) {

                //Apply the placements of this row and recurse
                for (Placement p : row.placements) {
                    g[p.r][p.c] = p.v;
                }
                matrix.chooseRow(row,true);

                if (solve(g, matrix))
                    return true;
                else {

                    //This branch failed, undo placements and undelete rows and columns
                    for (Placement p : row.placements) {
                        g[p.r][p.c] = -1;
                    }
                    matrix.chooseRow(row, false);
                }

            }
        }

        return false;
    }

    @Override
    public boolean solve(SudokuGrid grid) {

        KillerSudokuGrid killer_grid = (KillerSudokuGrid) grid;
        int[][] g = killer_grid.getGrid();

        //Make matrix from grid
        Matrix matrix = new Matrix(killer_grid);

        boolean result = solve(g, matrix);
        if (!result) {
            System.out.println("Initial Values Error");
        }
        else {
            for (int i = 0; i < g.length; i++) {
                for (int j = 0; j < g.length; j++) {
                    //Reset grid values from their indexes to their proper symbols
                    g[i][j] = killer_grid.getValidSymbols()[g[i][j]];
                }
            }
        }

        return result;
    } // end of solve()

} // end of class KillerAdvancedSolver
