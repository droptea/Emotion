package priv.droptea.emotion;

import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
public class TimeChart {
    public static void main(String[] args) {
    	
       
        JFreeChart mChart = ChartFactory.createXYLineChart(
        		"11折线",//图名字
                "年份",//横坐标
                "数量",//纵坐标
                GetDataset1(),
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        CategoryDataset mDataset = GetDataset();
		/*
		 * JFreeChart mChart = ChartFactory.createLineChart( "11折线",//图名字 "年份",//横坐标
		 * "数量",//纵坐标 mDataset,//数据集 PlotOrientation.VERTICAL, true, // 显示图例 true, //
		 * 采用标准生成器 false);// 是否生成超链接
		 */        
        
        //解决中文变方块问题
        mChart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        Font titleFont=new Font("隶书", Font.ITALIC, 18);
		Font font=new Font("宋体",Font.BOLD,12);
		Font legendFont=new Font("宋体", Font.BOLD, 15);
		mChart.getTitle().setFont(titleFont);
		mChart.getLegend().setItemFont(legendFont);
		/*
		 * CategoryPlot plot=mChart.getCategoryPlot();
		 * plot.getDomainAxis().setLabelFont(font);
		 * plot.getDomainAxis().setTickLabelFont(font);
		 * plot.getRangeAxis().setLabelFont(font);
		 */
		//end
		/*
		 * CategoryPlot mPlot = (CategoryPlot)mChart.getPlot();
		 * mPlot.setBackgroundPaint(Color.LIGHT_GRAY);
		 * mPlot.setRangeGridlinePaint(Color.BLUE);//背景底部横虚线
		 * mPlot.setOutlinePaint(Color.RED);//边界线
		 */ 
        ChartFrame mChartFrame = new ChartFrame("折线图", mChart);
        mChartFrame.pack();
        mChartFrame.setVisible(true);
 
    }
    public static CategoryDataset GetDataset()
    {
        DefaultCategoryDataset mDataset = new DefaultCategoryDataset();
        mDataset.addValue(1, "First", "2012");
        mDataset.addValue(3, "First", "2014");
        mDataset.addValue(2, "First", "2015");
        mDataset.addValue(6, "First", "2016");
        mDataset.addValue(5, "First", "2017");
        mDataset.addValue(12, "First", "2018");
        mDataset.addValue(14, "Second", "2011");
        mDataset.addValue(13, "Second", "2014");
        mDataset.addValue(12, "Second", "2015");
        mDataset.addValue(9, "Second", "2016");
        mDataset.addValue(5, "Second", "2017");
        mDataset.addValue(7, "Second", "2018");
        return mDataset;
    }
    public static XYSeriesCollection GetDataset1()
    {
    	XYSeries series = new XYSeries("Random Data");
    	
        series.add(1.0, 500.2);
        series.add(5.0, 694.1);
        series.add(3.0, 100.0);
        series.add(12.5, 734.4);
        series.add(17.3, 453.2);
        series.add(21.2, 500.2);
        series.add(21.9, 1);
        series.add(25.6, 734.4);
        series.add(30.0, 453.2);
        return new XYSeriesCollection(series);
    }
}