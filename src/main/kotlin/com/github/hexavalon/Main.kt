package com.github.hexavalon

import javafx.application.Application
import tornadofx.App

fun main(args : Array<String>)
{
    Application.launch(Main::class.java, *args)
}

class Main() : App(BoardView::class)