package com.sz.charts

data class GraphXBoundaries(val lowerXBoundary: Int, val upperXBoundary: Int)
data class GraphYBoundaries(val lowerYBoundary: Double, val upperYBoundary: Double)

class GraphSeries(private val graphData: List<IGraphData>, val title: String,
                  xBoundariesIn: GraphXBoundaries?, yBoundariesIn: GraphYBoundaries?) {
    companion object {
        private fun calculateXBoundaries(graphData: List<IGraphData>): GraphXBoundaries {
            var lowerXBoundary = Int.MAX_VALUE
            var upperXBoundary = Int.MIN_VALUE
            for (data in graphData) {
                for (d in data.data) {
                    if (d.xPos < lowerXBoundary) {
                        lowerXBoundary = d.xPos
                    }
                    if (d.xPos > upperXBoundary) {
                        upperXBoundary = d.xPos
                    }
                }
            }
            return GraphXBoundaries(lowerXBoundary, upperXBoundary)
        }

        private fun calculateYBoundaries(graphData: List<IGraphData>): GraphYBoundaries {
            var lowerYBoundary = Double.MAX_VALUE
            var upperYBoundary = Double.MIN_VALUE
            for (data in graphData) {
                for (d in data.data) {
                    if (d.yPos < lowerYBoundary) {
                        lowerYBoundary = d.yPos
                    }
                    if (d.yPos > upperYBoundary) {
                        upperYBoundary = d.yPos
                    }
                }
            }
            if (lowerYBoundary == upperYBoundary) {
                lowerYBoundary -= 0.1
                upperYBoundary += 0.1
            }
            return GraphYBoundaries(lowerYBoundary, upperYBoundary)
        }
    }

    val xBoundaries: GraphXBoundaries = xBoundariesIn ?: calculateXBoundaries(graphData)
    val yBoundaries: GraphYBoundaries = yBoundariesIn ?: calculateYBoundaries(graphData)

    fun getGraphTitles(): List<String> {
        return graphData.map { it.title }
    }

    fun getGraphData(): List<IGraphData> {
        return graphData
    }
}