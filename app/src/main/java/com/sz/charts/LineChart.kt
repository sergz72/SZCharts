package com.sz.charts

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.getTextArrayOrThrow
import java.text.Format
import kotlin.math.PI
import kotlin.math.sin

class LineChart(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    data class LinePaints(val linePaint: Paint, val pointPaint: Paint)

    private val margin = 20
    private val sinpi4 = sin(PI/4)

    private val mTitle: String
    private val mRangeTitle: String
    private val mDomainTitle: String
    private val mTitlePaint: Paint
    private val mLegendPaint: Paint
    private val mLabelPaint: Paint
    private val mBlackPaint: Paint
    private val mGrayPaint: Paint
    private val mLinePaintList: List<LinePaints>
    private val mLegendTableColumns: Int

    private var mSeries: GraphSeries? = null
    private var mDomainStepValue: Int = 1
    private var mDomainLabelFormatter: Format? = null
    private var mRangeStepValue: Float = 1.0F
    private var mRangeLabelFormatter: Format? = null

    init {
        val typedArray = context?.theme?.obtainStyledAttributes(attrs, R.styleable.LineChart,
            0, 0)
        mDomainTitle = typedArray?.getString(R.styleable.LineChart_domainTitle) ?: "domain"
        mRangeTitle = typedArray?.getString(R.styleable.LineChart_rangeTitle) ?: "range"
        mTitle = typedArray?.getString(R.styleable.LineChart_title) ?: "title"

        mTitlePaint = createPaint(typedArray, R.styleable.LineChart_titleColor, Color.BLACK,
                                  R.styleable.LineChart_titleSize, 50.0F)
        mLabelPaint = createPaint(typedArray, R.styleable.LineChart_labelColor, Color.BLACK,
                                  R.styleable.LineChart_labelSize, 50.0F)
        mLegendPaint = createPaint(typedArray, R.styleable.LineChart_legendColor, Color.BLACK,
                                   R.styleable.LineChart_legendSize, 50.0F)

        mLinePaintList = loadLinePaints(typedArray)

        mBlackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            strokeWidth = 3.0F
        }

        mGrayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            style = Paint.Style.FILL
            strokeWidth = 2.0F
        }

        mLegendTableColumns = typedArray?.getInt(R.styleable.LineChart_legendTableColumns, 2) ?: 2

        typedArray?.recycle()
    }

    private fun loadLinePaints(typedArray: TypedArray?): List<LinePaints> {
        val lineColors = typedArray?.getTextArrayOrThrow(R.styleable.LineChart_lineColors)
        val paintList: MutableList<LinePaints> = mutableListOf()
        for (lineColor in lineColors!!) {
            val colors = lineColor.toString().split(',')
            if (colors.size != 2) {
                throw IllegalArgumentException()
            }
            val linec  = Color.parseColor(colors[0])
            val pointc = Color.parseColor(colors[1])
            paintList.add(LinePaints(
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = linec
                    strokeWidth = 3.0F
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = pointc
                    style = Paint.Style.FILL
                }
            ))
        }
        return paintList.toList()
    }

    private fun createPaint(typedArray: TypedArray?, colorStyleable: Int, defaultColor: Int,
                            sizeStyleable: Int, defaultSize: Float): Paint {
        var colorValue = typedArray?.getResourceId(colorStyleable, 0) ?: 0
        colorValue = if (colorValue == 0) {
            defaultColor
        } else {
            context?.getColor(colorValue) ?: defaultColor
        }
        val size = typedArray?.getFloat(sizeStyleable, defaultSize) ?: defaultSize
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorValue
            textSize = size
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val y1 = drawTitle(canvas) + margin
        if (mSeries != null) {
            val y2 = drawLegend(canvas)
            val yLabelWidth = getYLabelMaxWidth() + margin * 2
            val xLabelHeight = getXLabelMaxHeight() + margin
            val yAxis = y2 - xLabelHeight
            val dy = (((yAxis - y1) * mRangeStepValue).toDouble() / (mSeries!!.mUpperYBoundary - mSeries!!.mLowerYBoundary)).toFloat()
            val dx = (((width - yLabelWidth) * mDomainStepValue).toDouble() / (mSeries!!.mUpperXBoundary - mSeries!!.mLowerXBoundary)).toFloat()
            val offsetY = drawYLabels(canvas, y1, yAxis, dy, yLabelWidth)
            drawXLabels(canvas, yAxis, dx, yLabelWidth)
            drawAxis(canvas, y1, yAxis, yLabelWidth, dy, dx, offsetY)
            drawSeries(canvas, y1, yAxis, yLabelWidth)
        }
    }

    private fun drawXLabels(canvas: Canvas, y: Float, dx: Float, x: Float) {
        var xx = x
        var v = mSeries!!.mLowerXBoundary
        while (v <= mSeries!!.mUpperXBoundary) {
            val text = mDomainLabelFormatter!!.format(v)
            val r = Rect()
            mLabelPaint.getTextBounds(text, 0, text.length, r)
            val (h1, h2) = getRectHeights45(r)
            val x1 = (xx - h2 + h1).toFloat()
            val y1 = (y + h1 + h2).toFloat()
            canvas.rotate(-45.0F, x1, y1)
            canvas.drawText(text, x1, y1, mLabelPaint)
            canvas.rotate(45.0F, x1, y1)
            xx += dx
            v += mDomainStepValue
        }
    }

    private fun drawSeries(canvas: Canvas, yStart: Float, yEnd: Float, x: Float) {
        var idx = 0
        for (gd in mSeries!!.getGraphData()) {
            val s = gd.size
            if (s > 1) {
                var prevX = getGraphX(gd, 0, x)
                var prevY = getGraphY(gd, 0, yStart, yEnd)
                for (i in 1 until s) {
                    val currX = getGraphX(gd, i, x)
                    val currY = getGraphY(gd, i, yStart, yEnd)
                    canvas.drawLine(prevX, prevY, currX, currY, mLinePaintList[idx].linePaint)
                    prevX = currX
                    prevY = currY
                }
            }
            idx++
        }
    }

    private fun getGraphX(gd: IGraphData, i: Int, x: Float): Float {
        val v = gd.getX(i)
        return x + ((width - x) * (v - mSeries!!.mLowerXBoundary)) / (mSeries!!.mUpperXBoundary - mSeries!!.mLowerXBoundary)
    }

    private fun getGraphY(gd: IGraphData, i: Int, yStart: Float, yEnd: Float): Float {
        val v = gd.getY(i)
        return yStart + (((yEnd - yStart) * (mSeries!!.mUpperYBoundary - v)) / (mSeries!!.mUpperYBoundary - mSeries!!.mLowerYBoundary)).toFloat()
    }

    private fun drawYLabelText(canvas: Canvas, v: Double, y: Float, w: Float): Int {
        val text = mRangeLabelFormatter!!.format(v)
        val r = Rect()
        mLabelPaint.getTextBounds(text, 0, text.length, r)
        val x = w - r.width() - margin
        canvas.drawText(text, x, y + r.height() / 2, mLabelPaint)
        return r.height()
    }

    private fun drawYLabels(canvas: Canvas, yStart: Float, yEnd: Float, dy: Float, w: Float): Float {
        var y = yStart
        var v = mSeries!!.mUpperYBoundary - (mSeries!!.mUpperYBoundary % mRangeStepValue)
        val offsetY = ((mSeries!!.mUpperYBoundary - v) / mRangeStepValue * dy).toFloat()
        y += offsetY
        var height = drawYLabelText(canvas, mSeries!!.mUpperYBoundary, yStart, w)
        if (offsetY >= height) {
            drawYLabelText(canvas, v, y, w)
        }
        y += dy
        v -= mRangeStepValue
        while (v >= mSeries!!.mLowerYBoundary) {
            if ((v - mRangeStepValue >= mSeries!!.mLowerYBoundary) || (yEnd - y >= height)) {
                height = drawYLabelText(canvas, v, y, w)
            }
            y += dy
            v -= mRangeStepValue
        }
        drawYLabelText(canvas, mSeries!!.mLowerYBoundary, yEnd, w)
        return offsetY
    }

    private fun drawAxis(
        canvas: Canvas,
        yStart: Float,
        yEnd: Float,
        x: Float,
        dy: Float,
        dx: Float,
        offsetY: Float
    ) {
        var y = yStart + offsetY
        while (y < yEnd) {
            canvas.drawLine(x, y, width.toFloat(), y, mGrayPaint)
            y += dy
        }
        var xx = x
        var vx = mSeries!!.mLowerXBoundary
        while (vx <= mSeries!!.mUpperXBoundary) {
            canvas.drawLine(xx, yStart, xx, yEnd, mGrayPaint)
            xx += dx
            vx += mDomainStepValue
        }
        canvas.drawLine(x, yStart, x, yEnd, mBlackPaint)
        canvas.drawLine(x, yEnd, width.toFloat(), yEnd, mBlackPaint)
    }

    private fun getRectHeights45(r: Rect): Pair<Double, Double> {
        return Pair(sinpi4 * r.height(),  sinpi4 * r.width())
    }

    private fun getXLabelMaxHeight(): Float {
        var h = 0.0
        var v = mSeries!!.mLowerXBoundary
        while (v <= mSeries!!.mUpperXBoundary) {
            val text = mDomainLabelFormatter!!.format(v)
            val r = Rect()
            mLabelPaint.getTextBounds(text, 0, text.length, r)
            val (h1, h2) = getRectHeights45(r)
            val hh = h1 + h2
            if (hh > h) {
                h = hh
            }
            v += mDomainStepValue
        }
        return h.toFloat()
    }

    private fun getYLabelMaxWidth(): Float {
        var w = 0.0F
        var y = mSeries!!.mLowerYBoundary
        while (y <= mSeries!!.mUpperYBoundary) {
            val text = mRangeLabelFormatter!!.format(y)
            val r = Rect()
            mLabelPaint.getTextBounds(text, 0, text.length, r)
            val rw = r.width().toFloat()
            if (w < rw) {
                w = rw
            }
            y += mRangeStepValue
        }
        return w
    }

    private fun drawTitle(canvas: Canvas): Float {
        val r = Rect()
        mTitlePaint.getTextBounds(mTitle, 0, mTitle.length, r)
        val w = r.width()
        val h = r.height().toFloat()
        val x = ((width - w) / 2).toFloat()
        canvas.drawText(mTitle, x, h, mTitlePaint)
        return h
    }

    private fun drawLegend(canvas: Canvas): Float {
        val titles = mSeries!!.getGraphTitles()
        var totalH = 0
        if (titles.isNotEmpty()) {
            val columns =
                if (titles.size < mLegendTableColumns) titles.size else mLegendTableColumns
            var rows = titles.size / columns
            if ((titles.size % columns) != 0) {
                rows++
            }

            val lengths = IntArray(titles.size) { _ -> 0 }
            val widths  = IntArray(columns) { _ -> 0 }
            var h = 0
            var idx = 0
            val r = Rect()
            val w = width / columns

            for (row in 1..rows) {
                for (column in 1..columns) {
                    if (idx >= titles.size) {
                        break
                    }
                    val title = titles[idx]
                    var l = title.length
                    while (l >= 1) {
                        mLegendPaint.getTextBounds(title, 0, l, r)
                        if (r.width() + h < w) {
                            break
                        }
                        l--
                    }
                    lengths[idx++] = l
                    if (widths[column - 1] < r.width()) {
                        widths[column - 1] = r.width()
                    }
                    if (r.height() > h) {
                        h = r.height()
                    }
                }
            }

            totalH = h * (rows + 1)

            var y = height - totalH
            idx = 0
            for (row in 1..rows) {
                var x = 0
                for (column in 1..columns) {
                    if (idx >= titles.size) {
                        break
                    }
                    val title = titles[idx]
                    val dx = (w - widths[column - 1] - h) / 2
                    r.left = x + dx
                    r.right = x + dx + h
                    r.top = y
                    r.bottom = y + h
                    canvas.drawRect(r, mBlackPaint)
                    canvas.drawLine(r.left.toFloat(), r.bottom.toFloat(), r.right.toFloat(),
                                    r.top.toFloat(), mLinePaintList[idx].linePaint)
                    //canvas.drawCircle(r.exactCenterX(), r.exactCenterY(), 5.0F,
                    //                  mLinePaintList[idx].pointPaint)
                    canvas.drawText(title.substring(0, lengths[idx++]), r.right.toFloat(), r.bottom.toFloat(),
                                    mLegendPaint)
                    x += w
                }
                y += h
            }
        }
        return (height - totalH).toFloat()
    }

    fun setSeries(series: GraphSeries, domainStepValue: Int, domainLabelFormatter: Format,
                  rangeStepValue: Float, rangeLabelFormatter: Format) {
        mSeries = series
        mDomainStepValue = domainStepValue
        mDomainLabelFormatter = domainLabelFormatter
        mRangeStepValue = rangeStepValue
        mRangeLabelFormatter = rangeLabelFormatter
        invalidate()
    }

    fun clearSeries() {
        mSeries = null
        invalidate()
    }
}