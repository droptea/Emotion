package priv.droptea.emotion.module.pitch;

import priv.droptea.emotion.module.fft.FFT;
import priv.droptea.emotion.module.fft.HammingWindow;
import priv.droptea.emotion.util.ChartTool;

public class CepstrumPitchMethod implements PitchDetector {
	
	private final PitchDetectionResult result;
	private float sampleRate;
	public CepstrumPitchMethod(float sampleRate,int bufferSize){
		this.sampleRate = sampleRate;
		result = new PitchDetectionResult();
	}
	private int showCepstrumNum = 3;
	@Override
	public PitchDetectionResult getPitch(float[] audioBuffer) {
		float pitchF = -1.0f;
		//傅立叶变换之前后一半都是0，傅立叶变换之后2位表示一个复数
		float[] fftData = new float[audioBuffer.length*2];
		float[] amplitudes = new float[audioBuffer.length];
		System.arraycopy(audioBuffer, 0, fftData, 0, audioBuffer.length); 
		FFT fft = new FFT(fftData.length,new HammingWindow());
		fft.forwardTransform(fftData);
		fft.modulus(fftData, amplitudes);
		for (int i = 0; i < amplitudes.length; i++) {
			amplitudes[i] = (float) Math.log(amplitudes[i]);
		}
		float[] IfftData = new float[audioBuffer.length*2];
		System.arraycopy(amplitudes, 0, IfftData, 0, amplitudes.length); 
		fft.backwardsTransform(IfftData);
		fft.modulus(fftData, amplitudes);
		int start = (int)Math.floor(sampleRate*0.001);
		int end = (int)Math.floor(sampleRate*0.02);
		int index = 0;
		
		float max = amplitudes[start];//假设第一个元素是最大值
		for (int i = start; i < end; i++) {
			if(max<amplitudes[i]) {
				index = i;
				max = amplitudes[i];
				
			}
		}
		if(showCepstrumNum>0) {
			new ChartTool("倒频谱",amplitudes);
			showCepstrumNum--;
		}
		pitchF = sampleRate/(index-1);
		result.setPitch(pitchF);
		result.setPitched(-1!=pitchF&&max>10);
		result.setProbability(-1);
		System.out.println("start:"+audioBuffer.length);
		System.out.println("max:"+max);
		System.out.println("pitchF:"+pitchF);
		return result;
	}
	

}