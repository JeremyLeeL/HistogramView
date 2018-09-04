package com.jeremy.lym.histogramview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import java.text.DecimalFormat

/**
 * Created by lym on 2018/8/30.
 * GitHub：https://github.com/JeremyLeeL
 */
class HistogramView @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attributeSet, defStyleAttr) {

    /**画线*/
    private val linePaint = Paint()
    /**画坐标轴文字*/
    private val axisTextPaint = TextPaint()
    /**画柱子*/
    private val pillarsPaint = Paint()
    /**画柱子上标注文字*/
    private val pillarsTextPaint by lazy { Paint() }

    /**柱子的宽度*/
    private var pillarsWidth = 0f

    private var staticLayout: StaticLayout? = null

    /**Y坐标单位*/
    private var unit = ""

    /**纵坐标到左边界的距离*/
    private var leftSpacing = 100f

    /**横坐标到下边界的距离*/
    private var bottomSpacing = 100f

    /**纵坐标总量*/
    private var verticalCount = 100

    /**纵坐标单位间隔量*/
    private var verticalUnitCount = 5

    /**纵坐标长横线间隔量*/
    private var verticalBigUnitCount = 2 * verticalUnitCount

    /**纵坐标短横线长度*/
    private val verticalNormalLineLength = 10f

    /**纵坐标长横线长度*/
    private val verticalLongLineLength = 20f

    /**纵坐标间隔*/
    private var verticalSpacing = 0f

    /**柱子之间的间距*/
    private var horizontalSpacing = 20f

    /**坐标轴的粗细*/
    private var axisWidth = 2f

    /**横坐标底部标注文字集合*/
    private var xAxisStrings: ArrayList<String>? = null

    /**单个柱子代表的数量集合*/
    private var pillarsNumbers: ArrayList<Float>? = null

    /**是否显示柱子上标注文字*/
    private var showPillarsText = true

    //保留两位小数
    private val df = DecimalFormat("#.00")
    init {
        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.HistogramView, 0, 0)
        unit = typeArray.getString(R.styleable.HistogramView_unit) ?: ""
        val axisColor = typeArray.getColor(R.styleable.HistogramView_axisColor, Color.DKGRAY)
        val axisTextColor = typeArray.getColor(R.styleable.HistogramView_axisTextColor, Color.DKGRAY)
        val pillarsTextColor = typeArray.getColor(R.styleable.HistogramView_pillarsTextColor, Color.DKGRAY)
        showPillarsText = typeArray.getBoolean(R.styleable.HistogramView_showPillarsText, true)
        axisWidth = typeArray.getDimension(R.styleable.HistogramView_axisWidth, 2f)
        val pillarsColor = typeArray.getColor(R.styleable.HistogramView_pillarsColor, Color.GREEN)
        verticalUnitCount = typeArray.getInteger(R.styleable.HistogramView_verticalUnitCount, 5)
        verticalCount = typeArray.getInteger(R.styleable.HistogramView_verticalCount, 100)
        horizontalSpacing = typeArray.getDimension(R.styleable.HistogramView_horizontalSpacing, 20f)
        typeArray.recycle()

        verticalBigUnitCount = 2 * verticalUnitCount

        linePaint.style = Paint.Style.STROKE
        linePaint.color = axisColor
        linePaint.isAntiAlias = true
        linePaint.strokeWidth = axisWidth

        axisTextPaint.color = axisTextColor
        axisTextPaint.textAlign = Paint.Align.CENTER    //设置align为center之后根据text的width确定startX绘制文字width需要 /2

        if (showPillarsText) {
            pillarsTextPaint.color = pillarsTextColor
            pillarsTextPaint.textAlign = Paint.Align.CENTER
        }

        pillarsPaint.color = pillarsColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (xAxisStrings == null || xAxisStrings!!.isEmpty() || pillarsNumbers == null || pillarsNumbers!!.isEmpty())
            return

        setParam()
        //将坐标原点从左上角移动到左下角
        canvas.translate(leftSpacing, height - bottomSpacing)

        //x坐标
        canvas.drawLine(0f - axisWidth / 2, 0f, width - leftSpacing, 0f, linePaint)
        //y坐标
        canvas.drawLine(0f, 0f - axisWidth / 2, 0f, -height - bottomSpacing, linePaint)

        //记录纵坐标标记线的Y坐标的值
        var startY = verticalSpacing * verticalUnitCount
        //画纵坐标的标记线和文字
        for (i in verticalUnitCount .. verticalCount + 1){
            if (i % verticalUnitCount == 0 && i % verticalBigUnitCount != 0){
                canvas.drawLine(0f - axisWidth / 2, -startY,
                        0 - verticalNormalLineLength - axisWidth / 2, -startY, linePaint)
                startY += verticalSpacing * verticalUnitCount
            }else if (i % verticalBigUnitCount == 0) {
                canvas.drawLine(0f - axisWidth / 2, -startY,
                        0 - verticalLongLineLength - axisWidth / 2, -startY, linePaint)

                val rect = getTextBounds(i.toString(), axisTextPaint)
                canvas.drawText(i.toString(),
                        0f - rect.width() / 2 - 10 - verticalLongLineLength - axisWidth / 2,
                        -startY + rect.height() / 2, axisTextPaint)

                startY += verticalSpacing * verticalUnitCount
            }else if(i == verticalCount + 1){
                val rect = getTextBounds(unit, axisTextPaint)
                canvas.drawText(unit,
                        0f - rect.width() / 2 - 10 - verticalLongLineLength - axisWidth / 2,
                        -startY, axisTextPaint)
            }else{
                continue
            }
        }
        //画柱子 和柱子上下的文字
        for (i in 1 .. xAxisStrings!!.size){
            if (pillarsNumbers!!.size < i)
                return
            //柱子的right点（X坐标）
            val endX = (pillarsWidth + horizontalSpacing) * i
            //柱子的center点（x坐标）
            val centerX = endX - pillarsWidth / 2
            //柱子的top点（Y坐标）
            val pillarsY = -pillarsNumbers!![i - 1] * verticalSpacing
            if (showPillarsText){
                //绘制柱子上标注文字
                var text = pillarsNumbers!![i - 1].toString()
                var needFormat = false
                if (text.contains(".")){
                    text = text.substring(text.indexOf(".") + 1, text.length)
                    needFormat = text.length > 2
                }
                text = if (needFormat) (df.format(pillarsNumbers!![i - 1])).toString() else (pillarsNumbers!![i - 1]).toString()
                canvas.drawText(text, centerX, pillarsY - rect.height() / 2, pillarsTextPaint)
            }
            //绘制柱子
            canvas.drawRect(endX - pillarsWidth, pillarsY, endX, -axisWidth / 2, pillarsPaint)
            val str = xAxisStrings!![i - 1]
            canvas.save()
            canvas.translate(centerX, 0f + axisWidth / 2)
            if (staticLayout == null)
                //绘制X轴标注文字
                staticLayout = StaticLayout(str, axisTextPaint, pillarsWidth.toInt() + horizontalSpacing.toInt(),
                        Layout.Alignment.ALIGN_NORMAL, 1f, 1f, false)
            staticLayout?.draw(canvas)
            staticLayout = null
            canvas.restore()
        }
    }

    private val rect by lazy {Rect()}
    private fun getTextBounds(str: String, paint: Paint): Rect{
        paint.getTextBounds(str, 0, str.length, rect)
        return rect
    }

    /**参数设置*/
    private fun setParam(){
        val textSize = width / 35f
        if (showPillarsText)
            pillarsTextPaint.textSize = textSize
        //设置完文字size之后测量文字的规格（包含宽高等信息的rect）
        axisTextPaint.textSize = textSize
        val unitWidth = getTextBounds(unit, axisTextPaint).width()
        val countWidth = getTextBounds(verticalCount.toString(), axisTextPaint).width()
        val leftTextWidth = Math.max(unitWidth, countWidth)
        //设置坐标轴到左侧边界间距
        leftSpacing = leftTextWidth + verticalLongLineLength + axisWidth / 2 + 30
        //设置坐标轴到下边界间距
        bottomSpacing = rect.height() * 3 + 20f
        //设置柱子的宽度(柱子的宽度计算依据是width，如果柱子数量太少（此处是少于7个），需要对柱子宽度设置一个默认值)
        val pillarsCount = if (xAxisStrings!!.size < 7) 7 else xAxisStrings!!.size
        pillarsWidth = (width - leftSpacing - horizontalSpacing) / pillarsCount - horizontalSpacing

        verticalSpacing = (height - bottomSpacing - rect.height() * 4) / verticalCount
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        var sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        val modeWith = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)

        //为当前控件设置默认宽高
        if (modeWith != MeasureSpec.EXACTLY)
            sizeWidth = 300
        if (modeHeight != MeasureSpec.EXACTLY)
            sizeHeight = 300
        setMeasuredDimension(sizeWidth, sizeHeight)
    }

    /**坐标轴颜色*/
    fun setAxisLineColor(@ColorRes colorRes: Int){
        val color = context.resources.getColor(colorRes)
        linePaint.color = color
        invalidate()
    }
    fun setAxisLineColor(colorStr: String){
        val color = Color.parseColor(colorStr)
        linePaint.color = color
        invalidate()
    }

    /**设置文字颜色*/
    fun setTextColor(@ColorRes colorRes: Int){
        val color = context.resources.getColor(colorRes)
        axisTextPaint.color = color
        invalidate()
    }
    fun setTextColor(colorStr: String){
        val color = Color.parseColor(colorStr)
        axisTextPaint.color = color
        invalidate()
    }

    /**设置坐标轴粗细*/
    fun setAsixWidth(axisWidth: Int){
        this.axisWidth = axisWidth.toFloat()
        invalidate()
    }

    /**设置柱子的颜色*/
    fun setPillarsColor(@ColorRes colorRes: Int){
        val color = context.resources.getColor(colorRes)
        pillarsPaint.color = color
        invalidate()
    }
    fun setPillarsColor(colorStr: String){
        val color = Color.parseColor(colorStr)
        pillarsPaint.color = color
        invalidate()
    }

    /**设置单位间隔量*/
    fun setVerticalUnitCount(count: Int){
        verticalUnitCount = count
        verticalBigUnitCount = 2 * verticalUnitCount
        invalidate()
    }

    /**设置总量*/
    fun setVerticalCount(verticalCount: Int){
        this.verticalCount = verticalCount
        invalidate()
    }

    /**设置柱子之间的间距*/
    fun setHorizontalSpacing(horizontalSpacing: Int){
        this.horizontalSpacing = horizontalSpacing.toFloat()
        invalidate()
    }

    /**设置横坐标标记文字集合*/
    fun setXAxisStrings(xAxisStrings: ArrayList<String>){
        if (this.xAxisStrings == null)
            this.xAxisStrings = xAxisStrings
        else{
            this.xAxisStrings?.clear()
            this.xAxisStrings?.addAll(xAxisStrings)
        }
        invalidate()
    }

    /**设置柱子的数量集合*/
    fun setPillarsNumbers(pillarsNumbers: ArrayList<Float>){
        if (this.pillarsNumbers == null)
            this.pillarsNumbers = pillarsNumbers
        else{
            this.pillarsNumbers?.clear()
            this.pillarsNumbers?.addAll(pillarsNumbers)
        }
        invalidate()
    }

    /**设置单位*/
    fun setUnit(unitStr: String?){
        this.unit = unitStr ?: ""
    }
    fun setUnit(@StringRes unitStrId: Int){
        this.unit = context.getString(unitStrId) ?: ""
    }

    /**设置是否显示柱子上标注文字*/
    fun setShowPillarsText(showPillarsText: Boolean){
        this.showPillarsText = showPillarsText
        invalidate()
    }

    /**设置柱子上标注文字颜色*/
    fun setPillarsTextColor(@ColorRes colorRes: Int){
        pillarsTextPaint.color = context.resources.getColor(colorRes)
        invalidate()
    }
    fun setPillarsTextColor(colorStr: String){
        pillarsTextPaint.color = Color.parseColor(colorStr)
        invalidate()
    }
}