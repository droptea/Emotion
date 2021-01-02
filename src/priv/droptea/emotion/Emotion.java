package priv.droptea.emotion;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import priv.droptea.emotion.WaveformSimilarityBasedOverlapAdd.Parameters;
import priv.droptea.emotion.io.jvm.AudioPlayer;
import priv.droptea.emotion.io.jvm.JVMAudioInputStream;
import priv.droptea.emotion.resample.RateTransposer;


public class Emotion extends JFrame{
	
	private static final long serialVersionUID = 8652286524039596482L;
	
	public Emotion() {
		JPanel mMicChoosePanel = new MicChoosePanel();
		mMicChoosePanel.addPropertyChangeListener(MicChoosePanel.KEY_CHOOSE_MIC,
			new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent arg0) {
					changeMic((Mixer) arg0.getNewValue());
				}
			});
		JSlider slider = new JSlider(20, 250);
		slider.setValue(100);
		slider.addChangeListener(sliderChangedListener);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(slider, GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
				.addComponent(mMicChoosePanel, GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(mMicChoosePanel, GroupLayout.PREFERRED_SIZE, 178, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(slider, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(66, Short.MAX_VALUE))
		);
		getContentPane().setLayout(groupLayout);
		
	}
	private WaveformSimilarityBasedOverlapAdd wsola;
	private RateTransposer rateTransposer;
	private double currentFactor = 1;// pitch shift factor
	private double sampleRate;
	private AudioDispatcher dispatcher;
	private AudioPlayer audioPlayer;
	private Mixer curMixer;
	
	public static void main(String[] args) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (Exception e) {
						//ignore failure to set default look en feel;
					}
					JFrame frame = new Emotion();
					frame.pack();
					frame.setSize(800,550);
					
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					double width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
					double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
					frame.setLocation( (int) (width - frame.getWidth()) / 2,
				                  (int) (height - frame.getHeight()) / 2);
					frame.setVisible(true);
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private ChangeListener sliderChangedListener = new ChangeListener(){
		@Override
		public void stateChanged(ChangeEvent arg0) {
			if (arg0.getSource() instanceof JSlider) {
				JSlider mJSlider = (JSlider)arg0.getSource();
				currentFactor = mJSlider.getValue() / 100.0;
			}
			
			if (wsola!= null&&rateTransposer!=null) {	
				System.out.println(currentFactor);
				wsola.setParameters(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(currentFactor, sampleRate));
				rateTransposer.setFactor(currentFactor);
			}
		}}; 
	
	
	public  void changeMic(Mixer mixer) {
		try {
			if(curMixer!=null&&curMixer.isOpen()) {
				curMixer.close();
			}
			curMixer = mixer;
			AudioFormat mFormat = new AudioFormat(44100, 16, 1, true,false);
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, mFormat);
			if(audioPlayer==null) {
				audioPlayer = new AudioPlayer(mFormat);
			}
			rateTransposer = new RateTransposer(currentFactor);
			sampleRate =  mFormat.getSampleRate();
			wsola = new WaveformSimilarityBasedOverlapAdd(Parameters.musicDefaults(currentFactor,sampleRate));
			TargetDataLine line;
			line = (TargetDataLine) curMixer.getLine(dataLineInfo);
			line.open(mFormat, wsola.getInputBufferSize());
			line.start();
			final AudioInputStream stream = new AudioInputStream(line);
			JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
			dispatcher = new AudioDispatcher(audioStream, wsola.getInputBufferSize(),wsola.getOverlap()); 
			wsola.setDispatcher(dispatcher);
			dispatcher.addAudioProcessor(wsola);
			dispatcher.addAudioProcessor(rateTransposer);
			dispatcher.addAudioProcessor(audioPlayer);
			
			Thread t = new Thread(dispatcher);
			t.start();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
