package com.github.hexavalon

import javafx.scene.Node
import javafx.scene.layout.GridPane

operator fun GridPane.set(x : Int, y : Int, node : Node) = this.add(node, x, y)

operator fun GridPane.get(x : Int, y : Int) : Node?
{
    return this.children.find { GridPane.getColumnIndex(it) === x && GridPane.getRowIndex(it) === y }
}

val GridPane.rows : Int
    get() {
        var numRows = this.rowConstraints.size
        for (i in 0 .. this.children.size - 1)
        {
            val child = this.children[i]
            if (child.isManaged)
            {
                val rowIndex = GridPane.getRowIndex(child)
                if (rowIndex != null)
                {
                    numRows = Math.max(numRows, rowIndex + 1)
                }
            }
        }
        return numRows
    }

val GridPane.columns : Int
    get() {
        var numColumns = this.rowConstraints.size
        for (i in 0 .. this.children.size - 1)
        {
            val child = this.children[i]
            if (child.isManaged)
            {
                val rowIndex = GridPane.getColumnIndex(child)
                if (rowIndex != null)
                {
                    numColumns = Math.max(numColumns, rowIndex + 1)
                }
            }
        }
        return numColumns
    }