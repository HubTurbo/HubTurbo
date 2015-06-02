package util;

import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

@SuppressWarnings("unused")
public class TextAnimation {
	
	private static final int INTERVAL = 100;
	private Timer timer;
	private Function<String, Void> callback;
	private String[] frames;
	private TimerTask animation;
	private int currentFrame;
	
	public TextAnimation(String[] frames, Function<String, Void> callback) {
		this.timer = new Timer();
		this.callback = callback;
		this.frames = frames;
		
		animation = new TimerTask() {
		    public void run() {
		         Platform.runLater(() -> callback.apply(frames[(currentFrame++) % frames.length]));
		    }
		};
	}
	
	public void start() {
		currentFrame = 0;
		timer.scheduleAtFixedRate(animation, INTERVAL, INTERVAL);
	}

	public void stop(Runnable callback) {
		timer.cancel();
		timer.purge();
		Platform.runLater(callback);
	}
}
