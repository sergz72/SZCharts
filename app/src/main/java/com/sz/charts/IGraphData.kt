package com.sz.charts

data class GraphDataItem(val xPos: Int, val yPos: Double)

interface IGraphData {
    val title: String
    val data: List<GraphDataItem>
}
