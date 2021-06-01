package lab02;

public class Worker extends Thread{

	private int workState;
	private Data data;
	
	public Worker(int workState, Data d){
		super("Worker - " + workState);
		this.workState = workState;
		data = d;
		this.start();
	}
	
	@Override
	public void run() {
		try {
			synchronized (data) {
				while (data.getState() != workState) {
					data.wait();
				}
				switch (workState) {
					case 1:
						data.Tic();
					case 2:
						data.Tak();
					default:
						data.Toy();
					}
				data.notifyAll();
			}
		} catch (Exception e) {
		}
	}
}
