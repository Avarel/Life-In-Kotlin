package com.github.hexavalon

import javafx.scene.control.Cell
import java.io.Serializable

/**
 * Class for the processing of Conway's Game of Life.
 *
 * NOTE: Accessing to the arrays in the style of y then x because
 *       multi-dimensional arrays are represented like this.
 *
 *         x -->
 *     y  [1,0,0]
 *     |  [0,1,0]
 *     v  [0,0,1]
 *
 * @param sizeX Width of board.
 * @param sizeY Height of board.
 * @param circular Will the board loop over on the other side?
 */
open class Board(val sizeX : Int, val sizeY : Int, var circular : Boolean = true)
{
    companion object
    {
        /**
         * Neighbor relative locations.
         */
        @JvmStatic
        private val NEIGHBOURS = arrayOf(
                intArrayOf(-1, -1), intArrayOf(-1, 0), intArrayOf(-1, +1),
                intArrayOf(0, -1), intArrayOf(0, +1),
                intArrayOf(+1, -1), intArrayOf(+1, 0), intArrayOf(+1, +1))
    }
    
    val DEAD_CELL = object : Cell(-1, -1, false)
    {
        override var alive : Boolean
            get() = false
            set(b) { alive = false }
    }
    
    /**
     * 2D array of the cells on the board.
     */
    var array = Array(sizeY) { x -> Array(sizeX) { y -> Cell(x, y) } }
        private set
    
    /**
     * @see Cell.alive
     * Set the state of the cell at location [x],[y].
     */
    operator fun set(x : Int, y : Int, state : Boolean)
    {
        if (circular)
        {
            array(y)(x).alive = state
        }
        else
        {
            try
            {
                array[y][x].alive = state
            }
            catch (ignore : ArrayIndexOutOfBoundsException) { }
        }
    }
    
    /**
     * @see Cell
     * @return Cell at location [x], [y].
     */
    operator fun get(x : Int, y : Int) : Cell
    {
        if (circular)
        {
            return array(y)(x)
        }
        else
        {
            try
            {
                return array[y][x]
            }
            catch (ignore : ArrayIndexOutOfBoundsException)
            {
                return DEAD_CELL
            }
        }
    }
    
    /**
     * Computes the next generation's board
     * based on current generation's.
     *
     * @return Differences between previous
     *         generation and new generation.
     */
    fun nextGeneration() : Int
    {
        var changes = 0
        
        val newBoard = Array(sizeX) { x -> Array(sizeY) { y -> Cell(x, y) } }
        
        for (x in 0 .. sizeX - 1) for (y in 0 .. sizeY - 1)
        {
            val n = array[y][x].neighbors
            
            // Simplified rule since all new cells start as dead.
            if (!this[x, y].alive)
            {
                if (n == 3)
                {
                    newBoard[y][x].alive = true
                }
            }
            else if (n == 2 || n == 3)
            {
                newBoard[y][x].alive = true
            }
            
            if (newBoard[y][x].alive !== array[y][x].alive) changes++
        }
        
        array = newBoard
        
        return changes
    }
    
    /**
     * Set all of the cells on the board to dead.
     */
    fun clear()
    {
        for (x in 0 .. sizeX - 1) for (y in 0 .. sizeY - 1) array[x][y].alive = false
    }
    
    override fun toString() : String
    {
        return array
                .map { it.map { if (it.alive) 1 else 0 } }
                .toString()
                .replace("], [", "]\n[")
                .replace("[[","[")
                .replace("]]", "]")
    }
    
    // EXTENSIONS
    
    /** Circular getting. */
    private operator fun <T> Array<T>.invoke(index : Int) : T
    {
        return this[index mod this.size]
    }
    
    /** Real modulus implementation. */
    private infix fun Int.mod(i : Int) : Int = (((this % i) + i) % i)
    
    
    //CELL CLASS
    
    /**
     * Representation of each cell on the board.
     * Stores it's own location and it's state.
     */
    open inner class Cell(open var x : Int, open var y : Int, open var alive : Boolean = false ) : Serializable
    {
        /**
         * Get neighbors of the current cell.
         */
        open val neighbors : Int
            get()
            {
                var n = 0
                for (arr in NEIGHBOURS)
                {
                    if (this@Board[y + arr[0], x + arr[1]].alive)
                    {
                        n++
                    }
                }
                return n
            }
    
        /**
         * Flips the state of the cell.
         */
        open fun toggle()
        {
            alive = !alive
        }
    
        /**
         * Return string representation of cell.
         */
        override fun toString() = "($x, $y, $alive)"
    }
}

