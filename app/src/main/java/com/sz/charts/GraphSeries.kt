package com.sz.charts

class GraphSeries {
    private val graphData: MutableList<IGraphData> = mutableListOf()
    var mLowerYBoundary: Double = Double.MAX_VALUE
        private set
    var mUpperYBoundary: Double = Double.MIN_VALUE
        private set
    var mLowerXBoundary: Int = Int.MAX_VALUE
        private set
    var mUpperXBoundary: Int = Int.MIN_VALUE
        private set
    val size: Int
        get() = graphData.size
    private var hasFixedXBoundaries: Boolean = false
    private var hasFixedYBoundaries: Boolean = false

    fun setXBoundaries(lowerXBoundary: Int, upperXBoundary: Int) {
        hasFixedXBoundaries = true
        mLowerXBoundary = lowerXBoundary
        mUpperXBoundary = upperXBoundary
    }

    fun setYBoundaries(lowerYBoundary: Double, upperYBoundary: Double) {
        hasFixedYBoundaries = true
        mLowerYBoundary = lowerYBoundary
        mUpperYBoundary = upperYBoundary
    }

    fun addGraph(data: IGraphData) {
        graphData.add(data)
        if (!hasFixedXBoundaries || !hasFixedYBoundaries) {
            for (i in 0 until data.size) {
                if (!hasFixedXBoundaries) {
                    val x = data.getX(i)
                    if (x < mLowerXBoundary) {
                        mLowerXBoundary = x
                    }
                    if (x > mUpperXBoundary) {
                        mUpperXBoundary = x
                    }
                }
                if (!hasFixedYBoundaries) {
                    val y = data.getY(i)
                    if (y < mLowerYBoundary) {
                        mLowerYBoundary = y
                    }
                    if (y > mUpperYBoundary) {
                        mUpperYBoundary = y
                    }
                }
            }
            if (mLowerYBoundary == mUpperYBoundary) {
                mLowerYBoundary -= 0.1
                mUpperYBoundary += 0.1
            }
        }
    }

    fun getGraphTitles(): List<String> {
        return graphData.map { it.title }
    }

    fun getGraphData(): List<IGraphData> {
        return graphData
    }
}