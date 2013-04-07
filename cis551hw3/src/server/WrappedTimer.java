package server;

import java.util.Timer;

public class WrappedTimer {
	
		Timer timer;
		long timerinterval;
		public WrappedTimer(){
			timer = new Timer(true);
			timerinterval = 2000;
		}
		public Timer getTimer() {
			return timer;
		}
		public void setTimer(Timer timer) {
			this.timer = timer;
		}
		public long getTimerinterval() {
			return timerinterval;
		}
		public void setTimerinterval(long timerinterval) {
			this.timerinterval = timerinterval;
		}	
	
}
