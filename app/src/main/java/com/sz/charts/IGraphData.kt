package com.sz.charts

interface IGraphData {
    val title: String
    val size: Int
    fun getY(pos: Int): Double
    fun getX(pos: Int): Int
}
