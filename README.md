# HistogramView
Android自定义柱形图

![image](https://github.com/JeremyLeeL/HistogramView/blob/master/histogramView.png)

[ ![Download](https://api.bintray.com/packages/lym6437/AndroidRepository/HistogramView/images/download.svg) ](https://bintray.com/lym6437/AndroidRepository/HistogramView/_latestVersion)

## 属性及使用说明

* 自定义属性
> 
        <!--纵坐标单位-->
        <attr name="unit" format="string|reference"/>
        <!--坐标轴颜色-->
        <attr name="axisColor" format="color"/>
        <!--坐标轴文字颜色-->
        <attr name="axisTextColor" format="color"/>
        <!--坐标轴粗细-->
        <attr name="axisWidth" format="dimension"/>
        <!--柱子颜色-->
        <attr name="pillarsColor" format="color"/>
        <!--纵坐标单位间隔量（默认5）-->
        <attr name="verticalUnitCount" format="integer"/>
        <!--纵坐标总量（默认100）-->
        <attr name="verticalCount" format="integer"/>
        <!--柱子之间的间隔距离-->
        <attr name="horizontalSpacing" format="dimension"/>
* 设置数据
> 
        histogramView.setXAxisStrings(strArray)
        histogramView.setPillarsNumbers(pillarsNumArray)
