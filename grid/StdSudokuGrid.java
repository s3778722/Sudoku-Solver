/**
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */
package grid;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * Class implementing the grid for standard Sudoku.
 * Extends SudokuGrid (hence implements all abstract methods in that abstract
 * class).
 * You will need to complete the implementation for this for task A and
 * subsequently use it to complete the other classes.
 * See the comments in SudokuGrid to understand what each overriden method is
 * aiming to do (and hence what you should aim for in your implementation).
 */
public class StdSudokuGrid extends SudokuGrid
{
    private int[][] grid;
    private int[] valid_symbols;

    public StdSudokuGrid() {
        super();
    } // end of StdSudokuGrid()


    /* ********************************************************* */
    
    //Get the index of the symbol in valid symbols
    public int symbolIndex(int symbol){
        for (int i = 0; i < valid_symbols.length; i ++){
            if (valid_symbols[i] == symbol){
                return i;
            }
        }
        return -1;
    }

    @Override
    public void initGrid(String filename)
        throws FileNotFoundException, IOException
    {
        File inFile = new File(filename);
        Scanner in = new Scanner(inFile);

        //Get the dimension of the sudoku grid
        int grid_dim = in.nextInt();

        //store the list of integers from input file to valid symbols array
        valid_symbols = new int[grid_dim];
        for (int i = 0; i < grid_dim; i++) {
            valid_symbols[i] = in.nextInt();
        }

        //fill the empty grid with -1
        grid = new int[grid_dim][grid_dim];
        for (int i = 0; i < grid_dim; i++){
            for (int j = 0; j < grid_dim; j++){
                grid[i][j] = -1;
            }
        }

        //fill the grid with the initial numbers from input file
        while(in.hasNext()){
            String tuple = in.next();
            int comma_loc = tuple.indexOf(',');
            String rowStr = tuple.substring(0, comma_loc);
            String colStr = tuple.substring(comma_loc+1);
            int row = Integer.parseInt(rowStr);
            int col = Integer.parseInt(colStr);

            grid[row][col] = in.nextInt();
        }
        in.close();
    } // end of initBoard()


    @Override
    public void outputGrid(String filename)
        throws FileNotFoundException, IOException
    {
        FileOutputStream outFile = new FileOutputStream(filename);
        outFile.write(this.toString().getBytes());
        outFile.close();
    } // end of outputBoard()


    @Override
    public String toString() {
        StringBuilder outStr = new StringBuilder();
        for(int i = 0; i < grid.length; i++){
            for (int j = 0; j < grid.length; j++ ){
                //loop through the grid on each row by iterating through each of the columns
                //Not equal -1 means the location is occupied
                if (grid[i][j] != -1){
                    outStr.append(grid[i][j]);
                }
                //else when equal -1 means the location is empty
                else{
                    outStr.append('x');
                }
                //if the end of each line is not reached, put a comma
                if (j != grid.length-1){
                    outStr.append(',');
                }
            }
            //make new line for each row till the end of the grid's row
            if (i != grid.length-1){
                outStr.append('\n');
            }
        }
        return outStr.toString();
    } // end of toString()


    @Override
    public boolean validate() {
        //Create an array list
        ArrayList<Integer> remaining_symbols = new ArrayList<>();

        //Check rows
        for (int i = 0; i < grid.length; i++) {
            //add all the values (e.g. list of integers) in valid symbols to the arraylist
            for (int e : valid_symbols)
                remaining_symbols.add(e);

            //iterate through the columns
            for (int j = 0; j < grid.length; j++) {
                //if still contains empty location, return false
                if (grid[i][j] == -1)
                    return false;

                //if the remaining symbols contains the symbols/integer in the grid, remove the object
                if (remaining_symbols.contains(grid[i][j]))
                    remaining_symbols.remove((Object)grid[i][j]);
                    
                //if the remaining symbols does not contain in the grid, return false
                else
                    return false;
            }

            remaining_symbols.clear();
        }


        //Check cols
        for (int i = 0; i < grid.length; i++) {
            //add all the values (e.g. list of integers) in valid symbols to the arraylist
            for (int e : valid_symbols)
                remaining_symbols.add(e);
            //iterate through the rows
            for (int j = 0; j < grid.length; j++) {

                //if the remaining symbols contains the symbols/integer in the grid, remove the object
                if (remaining_symbols.contains(grid[j][i]))
                    remaining_symbols.remove((Object)grid[j][i]);
                    
                //if the remaining symbols does not contain in the grid, return false
                else
                    return false;
            }

            remaining_symbols.clear();
        }


        //Check boxes
        int box_size = (int)Math.sqrt(grid.length); //square root the grid length to get box size
        //iterate through each box row
        for (int i = 0; i < grid.length; i += box_size) {
            //iterate through each box column
            for (int j = 0; j < grid.length; j += box_size) {
                //add all the values (e.g. list of integers) in valid symbols to the arraylist
                for (int e : valid_symbols)
                    remaining_symbols.add(e);
                //iterate through the row in the box
                for (int k = 0; k < box_size; k++) {
                    //iterate through the column in the box
                    for (int m = 0; m < box_size; m++) {
                        //if the remaining symbols contains the symbols/integer in the grid, remove the object
                        if (remaining_symbols.contains(grid[i + k][j + m]))
                            remaining_symbols.remove((Object)grid[i + k][j + m]);
                        //if the remaining symbols does not contain in the grid, return false
                        else
                            return false;

                    }
                }

                remaining_symbols.clear();
            }
        }

        return true;
    } // end of validate()

    //getter for grid
    public int[][] getGrid() {
        return grid;
    }
    //getter for valid symbols
    public int[] getValidSymbols() {
        return valid_symbols;
    }
    
} // end of class StdSudokuGrid
