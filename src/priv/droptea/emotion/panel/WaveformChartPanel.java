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
import priv.droptea.emotion.AudioEvent.DataForAnalysisInWaveformChart;

public class WaveformChartPanel extends ChartPanel {
	private static final long serialVersionUID = 1L;
	/**
	 * 通过这个字段来指定将要显示的波形图
	 */
	private String what;

	/**
	 * 显示未经处理的音频波形图
	 */
	public static final String what_inputWaveformChart = "what_inputWaveformChart";
	/**
	 * 显示经过wsola处理的音频波形图
	 */
	public static final String what_outputWaveformChartWsola = "what_outputWaveformChartWsola";
	/**
	 * 显示经过rateTransposer处理的音频波形图
	 */
	public static final String what_outputWaveformChartRt = "what_outputWaveformChartRt";

	XYSeriesCollection mXYSeriesCollection;

	private LinkedBlockingQueue<AudioEvent> mLinkedBlockingQueue;
	/**
	 * 波形图总共显示多少个音频块的数据
	 */
	private int showSoundBlockSizeSum = 1;
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
	private boolean isIgnoreFirstBlock = true;

	private WaveformChartPanel(String what) {
		super(null);
		// TODO Auto-generated constructor stub
		this.what = what;
		mXYSeriesCollection = new XYSeriesCollection();
		JFreeChart mChart = ChartFactory.createXYLineChart("Goals Scored Over Time", "Fixture Number", "Goals",
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
						/**
						 * 不分析第一块音频数据
						 */
						if (isIgnoreFirstBlock) {
							isIgnoreFirstBlock = false;
							continue;
						}
						if (curShowSoundBlockSize >= showSoundBlockSizeSum)
							continue;

						curShowSoundBlockSize++;

						DataForAnalysisInWaveformChart mDataForAnalysisInWaveformChart = audioEvent
								.getDataForAnalysisInWaveformChart();
						if (what_inputWaveformChart.equals(what)) {
							float[] audioFloatBuffer = mDataForAnalysisInWaveformChart.getFloatBufferOriginal();
							int seekWinOffsetWsola = mDataForAnalysisInWaveformChart.getSeekWinOffsetWsola();
							int seekWinLenghtWsola = mDataForAnalysisInWaveformChart.getSeekWinLengthWsola();
							int overlapWsola = mDataForAnalysisInWaveformChart.getOverlapWsola();
							int dataNotOverlapWsola = mDataForAnalysisInWaveformChart.getDataNotOverlapWsola();
							System.out.println("seekWinOffsetWsola:" + seekWinOffsetWsola);
							System.out.println("seekWinLenghtWsola:" + seekWinLenghtWsola);
							System.out.println("overlapWsola:" + overlapWsola);
							System.out.println("overlap:" + (overlapWsola+seekWinLenghtWsola));
							System.out.println("overlap:" + mDataForAnalysisInWaveformChart.getOverlapOriginal());
							//seekWinOffsetWsola
							XYSeries mXYseekWinOffsetWsola = new XYSeries("seekWinOffsetWsola" + curShowSoundBlockSize);
							for (int i = 0; i < seekWinOffsetWsola; i++) {
								mXYseekWinOffsetWsola.add(indexX, audioFloatBuffer[i]);
								indexX++;
							}
							mXYSeriesCollection.addSeries(mXYseekWinOffsetWsola);
							((XYPlot) (getChart().getPlot())).getRenderer().setSeriesPaint(
									mXYSeriesCollection.getSeriesCount() - 1, new Color(0xDD, 0xDD, 0xDD));
							//overlapWsola
							XYSeries mXYOverlapWsola = new XYSeries("overlapWsola" + curShowSoundBlockSize);
							for (int i = seekWinOffsetWsola; i < overlapWsola + seekWinOffsetWsola; i++) {
								mXYOverlapWsola.add(indexX, audioFloatBuffer[i]);
								indexX++;
							}
							mXYSeriesCollection.addSeries(mXYOverlapWsola);
							((XYPlot) (getChart().getPlot())).getRenderer().setSeriesPaint(
									mXYSeriesCollection.getSeriesCount() - 1, new Color(0xFF, 0xFF, 0xFF));
							//dataNotOverlapWsola
							XYSeries mXYDataNotOverlapWsola = new XYSeries("dataNotOverlapWsola" + curShowSoundBlockSize);
							for (int i = overlapWsola + seekWinOffsetWsola; i <  overlapWsola + seekWinOffsetWsola+dataNotOverlapWsola; i++) {
								mXYDataNotOverlapWsola.add(indexX, audioFloatBuffer[i]);
								indexX++;
							}
							mXYSeriesCollection.addSeries(mXYDataNotOverlapWsola);
							//none
							XYSeries mXYNone = new XYSeries("none" + curShowSoundBlockSize);
							for (int i = overlapWsola + seekWinOffsetWsola+dataNotOverlapWsola; i < audioFloatBuffer.length; i++) {
								mXYNone.add(indexX, audioFloatBuffer[i]);
								indexX++;
							}
							mXYSeriesCollection.addSeries(mXYNone);
							((XYPlot) (getChart().getPlot())).getRenderer().setSeriesPaint(
									mXYSeriesCollection.getSeriesCount() - 1, new Color(0x00, 0x00, 0x00));
							//valideData表示音频实际的数据长度，allData中包括了重叠区域
							System.out.println(what+"_valideData:" + (indexX-mDataForAnalysisInWaveformChart.getOverlapOriginal()));
						} else if (what_outputWaveformChartWsola.equals(what)) {
							float[] audioFloatBuffer = mDataForAnalysisInWaveformChart.getFloatBufferWsola();
							int overlapWsola = mDataForAnalysisInWaveformChart.getOverlapWsola();
							//overlapWsola
							XYSeries mXYOverlapWsola = new XYSeries("overlapWsola" + curShowSoundBlockSize);
							for (int i = 0; i < overlapWsola; i++) {
								mXYOverlapWsola.add(indexX, audioFloatBuffer[i]);
								indexX++;
							}
							mXYSeriesCollection.addSeries(mXYOverlapWsola);
							((XYPlot) (getChart().getPlot())).getRenderer().setSeriesPaint(
									mXYSeriesCollection.getSeriesCount() - 1, new Color(0xFF, 0xFF, 0xFF));
							//dataNotOverlapWsola
							XYSeries mXYDataNotOverlapWsola = new XYSeries("dataNotOverlapWsola" + curShowSoundBlockSize);
							for (int i = overlapWsola; i < audioFloatBuffer.length; i++) {
								mXYDataNotOverlapWsola.add(indexX, audioFloatBuffer[i]);
								indexX++;
							}
							mXYSeriesCollection.addSeries(mXYDataNotOverlapWsola);
						} else if(what_outputWaveformChartRt.equals(what)) {
							float[] audioFloatBuffer = mDataForAnalysisInWaveformChart.getFloatBufferCur();
							XYSeries mXYDataNotOverlapWsola = new XYSeries("soundData" + curShowSoundBlockSize);
							for (int i = 0; i < audioFloatBuffer.length; i++) {
								mXYDataNotOverlapWsola.add(indexX, audioFloatBuffer[i]);
								indexX++;
							}
							mXYSeriesCollection.addSeries(mXYDataNotOverlapWsola);
						}
						System.out.println(what+"_allDataLength:" + indexX);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public static WaveformChartPanel getInstant(String what) {
		return new WaveformChartPanel(what);
	}

	public void addAudioEvent(AudioEvent audioEvent) {
		mLinkedBlockingQueue.offer(audioEvent);

	}
}
