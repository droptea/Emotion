package priv.droptea.emotion.panel;

import java.awt.Color;
import java.util.concurrent.LinkedBlockingQueue;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import priv.droptea.emotion.AudioEvent;

public class WaveformChartPanel extends ChartPanel{
	private static final long serialVersionUID = 1L;

	XYSeriesCollection mXYSeriesCollection;
	
	private LinkedBlockingQueue<AudioEvent> mLinkedBlockingQueue;
	/**
	 * 波形图总共显示多少个音频块的数据
	 */
	private int showSoundBlockSizeSum = 2;
	/**
	 * 波形图已经显示了多少个音频块的数据
	 */
	private int curShowSoundBlockSize = 0;
	/**
	 * 记录显示的数据的X轴下标，每次显示一个数据就自增1，用来作为下一条数据的X轴下标
	 */
	private int indexX = 0;
	/**
	 * 是否忽略第一块音频数据
	 */
	private boolean isIgnoreFirstBlock = false;

	private WaveformChartPanel(JFreeChart chart) {
		super(chart);
		// TODO Auto-generated constructor stub
		
		mXYSeriesCollection = new XYSeriesCollection();
        JFreeChart mChart = ChartFactory.createXYLineChart(
                "Goals Scored Over Time", "Fixture Number", "Goals",
                mXYSeriesCollection, PlotOrientation.VERTICAL, true, true, false);
        mLinkedBlockingQueue = new LinkedBlockingQueue<AudioEvent>();
		
		this.setChart(mChart);
		startQueueHandler();
	}
	
	
	
	private void startQueueHandler() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					
					try {
						AudioEvent audioEvent = mLinkedBlockingQueue.take();
						if(isIgnoreFirstBlock) {
							isIgnoreFirstBlock = false;
							continue;
						}
						
						if(curShowSoundBlockSize>=showSoundBlockSizeSum)continue;
						curShowSoundBlockSize++;
						float[] audioFloatBuffer = audioEvent.getFloatBuffer();
						int overlap = audioEvent.getOverlap();
						int seekLength = audioEvent.getSeekLength();
						1
						System.out.println("bufferSize:"+audioFloatBuffer.length
								+"_queueSize:"+mLinkedBlockingQueue.size()
								+"_overlay:"+overlap
								+"_overlay:"+seekLength);
						if(overlap!=0){
							XYSeries mXYSeriesSeekLength = new XYSeries("seekLength"+curShowSoundBlockSize);
							for (int i = 0; i < seekLength; i++) {
								mXYSeriesSeekLength.add(indexX,audioFloatBuffer[i]);
								indexX++;
							}
							mXYSeriesCollection.addSeries(mXYSeriesSeekLength);
							((XYPlot)(getChart().getPlot())).getRenderer().setSeriesPaint(mXYSeriesCollection.getSeriesCount()-1, new Color(0xDD, 0xDD, 0xDD)); 
							
							XYSeries mXYSeriesOverlap = new XYSeries("overlap"+curShowSoundBlockSize);
							for (int i = seekLength; i < overlap; i++) {
								mXYSeriesOverlap.add(indexX,audioFloatBuffer[i]);
								indexX++;
								System.out.println("111");
							}
							mXYSeriesCollection.addSeries(mXYSeriesOverlap);
							((XYPlot)(getChart().getPlot())).getRenderer().setSeriesPaint(mXYSeriesCollection.getSeriesCount()-1, new Color(0xFF, 0xFF, 0xFF)); 
						}
						XYSeries mXYSeries = new XYSeries("soundData"+curShowSoundBlockSize);
						for (int i = overlap; i < audioFloatBuffer.length; i++) {
							mXYSeries.add(indexX,audioFloatBuffer[i]);
							indexX++;
						}
						mXYSeriesCollection.addSeries(mXYSeries);
						System.out.println("indexX:"+indexX);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	public static WaveformChartPanel getInstant() {
        return new WaveformChartPanel(null);
	}
	
	public void addAudioEvent(AudioEvent audioEvent) {
		mLinkedBlockingQueue.offer(audioEvent);
		
	}
}
