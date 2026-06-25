import java.util.concurrent.atomic.AtomicInteger;
public class ThreadSafetyDemo{
	static int i = 0;
	static int si = 0;
	static int usi = 0;
	static AtomicInteger ai;
	public static void main(String[]args){
		ai = new AtomicInteger(0);

		for(int i = 0; i < 200; i++){
			new MyThread().start();
		}
		try{
			Thread.sleep(50);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Primitive value (i) after program execution : " + i);
		System.out.println("Atomic value (ai) after program execution : " + ai);
		System.out.println("Primitive value (si) from a synchronized method : " + si);
		System.out.println("Primitive value (ui) from a unsynchronized method : " + usi);
	}
	public static synchronized void synchronizedAddMethod(int unit){
		si += unit;
	}
	public static void unsynchronizedAddMethod(int unit){
		usi += unit;
	}
	public static class MyThread extends Thread{
		public void run(){
			try{
					Thread.sleep(5);
			}catch(Exception e){
				e.printStackTrace();
			}
			i++;
			ai.getAndAdd(1);
			unsynchronizedAddMethod(1);
			synchronizedAddMethod(1);
		}
	}
}