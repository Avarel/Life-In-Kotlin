package com.github.hexavalon

import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.control.Button
import javafx.scene.effect.Glow
import javafx.scene.input.MouseButton
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.RowConstraints
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.StageStyle
import tornadofx.View
import tornadofx.tooltip
import java.awt.GraphicsEnvironment
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.properties.Delegates
import kotlin.system.exitProcess


class BoardView : View("Game of Life")
{
    companion object
    {
        @JvmStatic
        val DEFAULT_SIZE = 13
        
        @JvmStatic
        val MIN_SIZE = 5
        
        @JvmStatic
        val MAX_SIZE = 30
        
        @JvmStatic
        val DELAY_MS = 250L // pls never go to 0
    }
    
    // FXML objects.
    override val root : GridPane by fxml()
    val grid : GridPane by fxid()
    
    val titleBar : AnchorPane by fxid()
    
    val sizeIndicator : Text by fxid()
    val muteIndicator : Text by fxid()
    val playButton  : Button by fxid()
    val nextButton  : Button by fxid()
    val resetButton : Button by fxid()
    val closeButton : Button by fxid()
    
    var board : Board by Delegates.notNull()
    
    // Timer objects the play feature.
    private val timer = Timer()
    private var task : TimerTask? = null
    
    // Colors of each displayed cell.
    private var offColor = Color.valueOf("#37474F")
    private var onColor = Color.valueOf("#607D8B")
    
    // Size of the grid.
    // NOTE: The grid doesnt like non-square values.
    var size = DEFAULT_SIZE
    
    // Is muted
    var muted = false
    
    // For dragging the title bar.
    private var xOffset = 0.0
    private var yOffset = 0.0
    
    init
    {
        primaryStage.resizableProperty().set(false)
        primaryStage.initStyle(StageStyle.TRANSPARENT)
        
        titleBar.setOnMousePressed { event ->
            xOffset = event.sceneX
            yOffset = event.sceneY
        }
    
        titleBar.setOnMouseDragged { event ->
            primaryStage.x = event.screenX - xOffset
            primaryStage.y = event.screenY - yOffset
        }
        resetStagePosition()
        
        // MAIN SETUP
        
        gridSetup(size)
    
        sizeIndicator.tooltip("Left-Click to increase board size.\nRight-Click to decrease board size.").apply { font = Font.font(12.0) }
        muteIndicator.tooltip("Mute the clicking-sound.")
        playButton.tooltip("Auto-Play")
        nextButton.tooltip("Next Generation")
        resetButton.tooltip("Reset Board")
        closeButton.tooltip("Close")
    
        sizeIndicator.setOnMouseClicked() //->
        {
            if (task !== null) return@setOnMouseClicked // no change while running
        
            var change = false
        
            when(it.button)
            {
                MouseButton.PRIMARY ->
                {
                    val newNum = size + 1
                    if (newNum <= MAX_SIZE)
                    {
                        size = newNum
                        change = true
                    }
                }
                MouseButton.SECONDARY ->
                {
                    val newNum = size - 1
                    if (newNum >= MIN_SIZE)
                    {
                        size = newNum
                        change = true
                    }
                }
                else -> {}
            }
        
            if (change)
            {
                sizeIndicator.text = "$size * $size"
            
                gridReset()
                gridSetup(size)
            }
        }
        
        muteIndicator.setOnMouseClicked {
            if (muted)
            {
                muteIndicator.underlineProperty().set(false)
                muted = false
            }
            else
            {
                muteIndicator.underlineProperty().set(true)
                muted = true
            }
        }
        
        playButton.setOnMouseClicked() //->
        {
            if (task !== null)
            {
                playButton.text = "▶"
                task?.cancel()
                task = null
                
                nextButton.disableProperty().set(false)
                resetButton.disableProperty().set(false)
                sizeIndicator.disableProperty().set(false)
                sizeIndicator.text = "$size * $size ↕"
            }
            else
            {
                playButton.text = "⏸"
                
                task = timer.scheduleAtFixedRate(0L, DELAY_MS)
                {
                    val changes = board.nextGeneration()
                    updateGrid()
                    if (changes > 0) clickSound(0.25)
                }
    
                nextButton.disableProperty().set(true)
                resetButton.disableProperty().set(true)
                sizeIndicator.disableProperty().set(true)
                sizeIndicator.text = "$size * $size"
            }
        }
        
        nextButton.setOnMouseClicked() //->
        {
            val changes = board.nextGeneration()
            updateGrid()
            if (changes > 0) clickSound()
        }
        
        resetButton.setOnMouseClicked() // ->
        {
            board.clear()
            updateGrid()
        }
    
        closeButton.setOnMouseClicked() //->
        {
            if (it.button == MouseButton.PRIMARY)
            {
                closeModal()
                exitProcess(0)
            }
        }
    }
    
    // Setup the grid.
    private fun gridSetup(size : Int)
    {
        sizeIndicator.text = "$size * $size ↕"
        
        for (i in 1 .. size)
        {
            grid.columnConstraints.add(ColumnConstraints().apply {
                halignment = HPos.CENTER
                prefWidth = 100.0
            })
    
            grid.rowConstraints.add(RowConstraints().apply {
                valignment = VPos.CENTER
                prefHeight = 100.0
            })
        }
    
        board = Board(grid.columns, grid.rows)
    
        val width = (300.0 - 10) / grid.columns - 2
        val height = (300.0 - 10) / grid.rows - 2
    
        for (x1 in 0 .. grid.columns - 1) for (y1 in 0 .. grid.rows - 1)
        {
            val cell = Rectangle(width, height, offColor).apply { arcHeight = 5.0; arcWidth = 5.0 }
        
            grid[x1, y1] = cell.apply {
            
                setOnMouseEntered {
                    cell.effect = Glow(1.0)
                }
            
                setOnMouseExited {
                    if (board[x1, y1].alive)
                    {
                        fill = onColor
                    }
                    else
                    {
                        fill = offColor
                    }
                    effect = null
                }
            
                setOnMouseClicked {
                    board[x1, y1].toggle()
                
                    if (board[x1, y1].alive)
                    {
                        fill = onColor
                    }
                    else
                    {
                        fill = offColor
                    }
                    clickSound()
                }
            }
        }
    }
    
    // Clear everything inside the grid.
    private fun gridReset()
    {
        grid.columnConstraints.clear()
        grid.rowConstraints.clear()
        grid.children.clear()
    }
    
    // Update the displayed board base on
    // the [Board] instance.
    fun updateGrid()
    {
        for (x in 0 .. grid.columns - 1) for (y in 0 .. grid.rows - 1)
        {
            if (board[x, y].alive)
            {
                (grid[x, y] as Rectangle).fill = onColor
            }
            else
            {
                (grid[x, y] as Rectangle).fill = offColor
            }
        }
    }
    
    // Set position of window to top-right.
    private fun resetStagePosition()
    {
        val gd = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
        val desktopWidth = gd.displayMode.width
    
        primaryStage.x = desktopWidth - 320.0
        primaryStage.y = 20.0
    }
    
    // Sound stuff.
    private fun clickSound(volume : Double = 1.0)
    {
        if (muted) return
        
        val file = javaClass.classLoader.getResource("click.mp3")
        val mediaPlayer = MediaPlayer(Media(file.toString())).apply { this.volume = volume }
        mediaPlayer.play()
    }
}

