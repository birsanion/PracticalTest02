package ro.pub.cs.systems.pdsd.practicaltest02;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import ro.pub.cs.systems.pdsd.general.Constants;
import ro.pub.cs.systems.pdsd.general.Utilities;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class PracticalTest02MainActivity extends Activity {

	private EditText operator1EditText, operator2EditText;
	private TextView addView, mulView, portView;
	private Button addButton, mulButton;
	private ServerThread serverThread = null; 
	
private class ClientThread extends Thread {
		
		private Socket socket = null;
		private String operation = null;
		
		public ClientThread(String operation){
			this.operation = operation;
		}
		
		@Override
		public void run() {
			String operator1 = operator1EditText.getText().toString();
			String operator2 = operator2EditText.getText().toString();
			try {
				socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
				String message = operation + "," + operator1 + "," + operator2;
				PrintWriter printWriter = Utilities.getWriter(socket);
				printWriter.println(message);
				BufferedReader bufferReader = new BufferedReader(Utilities.getReader(socket));
				final String ret = bufferReader.readLine();
				switch(operation){
				case "add":
					addView.post(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							addView.setText(ret);
						}
					
					});
					break;
				case "mul":
					mulView.post(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mulView.setText(ret);
						}
					
					});
					break;
				}
				Log.v(Constants.TAG, "Send message " + message);
				socket.close();
			} catch (Exception exception) {
				Log.e(Constants.TAG, "An exception has occurred: "+exception.getMessage());
				if (Constants.DEBUG) {
					exception.printStackTrace();
				}
			}			
		} 
	}	
	
	private class CommunicationThread extends Thread {
		
		private Socket socket;
		
		public CommunicationThread(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			try {
				Log.v(Constants.TAG, "Connection opened with "+socket.getInetAddress()+":"+socket.getLocalPort());
				BufferedReader bufferReader = new BufferedReader(Utilities.getReader(socket));
				String message = bufferReader.readLine();
				String[] words = message.split(",");
				String operation = words[0];
				int operator1 = Integer.parseInt(words[1]);
				int operator2 = Integer.parseInt(words[2]);
				int rez = 0;
				Log.v(Constants.TAG, "Received message city " + message);
				switch(operation){
				case "add":
					rez = operator1 + operator2;
					break;
				case "mul":
					Thread.sleep(1000);
					rez = operator1 * operator2;
					break;
				default:
					break;
				}
				PrintWriter printWriter = Utilities.getWriter(socket);
				printWriter.println(rez);
				
				socket.close();

			} catch (Exception exception) {
				Log.e(Constants.TAG, "An exception has occurred: "+exception.getMessage());
				if (Constants.DEBUG) {
					exception.printStackTrace();
				}
			}
		}
	}
	
	private class ServerThread extends Thread {
		
		private boolean isRunning;
		
		private ServerSocket serverSocket;
		
		public void startServer() {
			isRunning = true;
			start();
			Log.v(Constants.TAG, "startServer() method invoked");
		}
		
		public void stopServer() {
			isRunning = false;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						if (serverSocket != null) {
							serverSocket.close();
						}
						Log.v(Constants.TAG, "stopServer() method invoked "+serverSocket);
					} catch(IOException ioException) {
						Log.e(Constants.TAG, "An exception has occurred: "+ioException.getMessage());
						if (Constants.DEBUG) {
							ioException.printStackTrace();
						}
					}
				}
			}).start();
		}
		
		@Override
		public void run() {
			//portView.setText(Constants.SERVER_PORT);
			try {
				serverSocket = new ServerSocket(Constants.SERVER_PORT);
				while (isRunning) {
					Socket socket = serverSocket.accept();
					new CommunicationThread(socket).start();
				}
			} catch (IOException ioException) {
				Log.e(Constants.TAG, "An exception has occurred: "+ioException.getMessage());
				if (Constants.DEBUG) {
					ioException.printStackTrace();
				}
			}
		}
	}	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_practical_test02_main);
		
		
		operator1EditText = (EditText)this.findViewById(R.id.operator1);
		operator2EditText = (EditText)this.findViewById(R.id.operator2);
		addView = (TextView)this.findViewById(R.id.add_rez);
		mulView = (TextView)this.findViewById(R.id.mul_rez);
		portView = (TextView)this.findViewById(R.id.port);
		addButton = (Button)this.findViewById(R.id.add);
		addButton.setOnClickListener(new OnClickListener(){
		
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new ClientThread("add").start();
			}
			
		});
		
		mulButton = (Button)this.findViewById(R.id.mul);
		mulButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new ClientThread("mul").start();
			}
			
		});
		serverThread = new ServerThread();
		serverThread.startServer();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.practical_test02_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void onDestroy() {
		  if (serverThread != null) {
		    serverThread.stopServer();
		  }
		  super.onDestroy();
		}
}
