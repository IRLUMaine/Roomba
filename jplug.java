// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   jplug.java

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

public class jplug extends Applet implements Runnable, ActionListener {
	class TS extends Thread {

		public void run() {
			while (mRun) {
				if (sendFlag)
					PostData();
				try {
					Thread.currentThread();
					Thread.sleep(30L);
				} catch (Exception exception) {
				}
			}
		}

		public void PostData() {
			byte abyte2[] = new byte[512];
			try {
				if (port.intValue() == 8481 || port.intValue() == 80)
					socket = new Socket(hostname, 80);
				else
					socket = new Socket(hostname, port.intValue());
				socket.setSoTimeout(timeout.intValue());
				socketInput = new DataInputStream(socket.getInputStream());
				socketOutput = new DataOutputStream(socket.getOutputStream());
			} catch (Exception exception) {
				System.err
						.println("<err>: Establish realtime data connection.");
				sendFlag = false;
				setTriggerRunning = false;
				setPanRunning = false;
				return;
			}
			try {
				if (setTriggerRunning) {
					byte abyte0[] = triggerPortString.getBytes("8859_1");
					System.out.println("Trigger String " + port + "\n\n\n" + triggerPortString + "\n\n\n");
					socketOutput.write(abyte0);
					setTriggerRunning = false;
				} else if (setPanRunning) {
					byte abyte1[] = postString.getBytes("8859_1");
					System.out.println("Trigger String " + port + "\n\n\n" + postString + "\n\n\n");
					for (int i = 0; i < abyte1.length; i++) {
						System.out.print((char)abyte1[i]);
					}
					System.out.println("\n\n\n");
					socketOutput.write(abyte1);
					setPanRunning = false;
				}
				int i = 0;
				int j = 0;
				do {
					if (i > 0)
						break;
					i = socketInput.read(abyte2, 0, 512);
					if (i > 0 || ++j > 20)
						break;
					Thread.currentThread();
					Thread.sleep(10L);
				} while (true);
			} catch (Exception exception1) {
				System.err.println(exception1);
			}
			try {
				socketInput.close();
				socketOutput.close();
				socket.close();
			} catch (Exception exception2) {
				System.err.println(exception2);
			}
			if (!setTriggerRunning && !setPanRunning)
				sendFlag = false;
		}
	}

	class SI extends Thread {

		public void run() {
			Thread thread = Thread.currentThread();
			thread.setPriority(10);
			try {
				while (mRun) {
					if (needImage) {
						toolkitImage = Toolkit.getDefaultToolkit().createImage(
								imageData, 0, imageLength);
						if (toolkitImage != null) {
							int i = 0;
							do {
								if (Toolkit.getDefaultToolkit().prepareImage(
										toolkitImage,
										toolkitImage.getWidth(null),
										toolkitImage.getHeight(null), null))
									break;
								Thread.sleep(3L);
							} while (++i <= 150);
							int width = toolkitImage.getWidth(null);
							int height = toolkitImage.getHeight(null);
							if (width != currentWidth
									|| height != currentHeight) {
								currentWidth = width;
								currentHeight = height;
								jpeg_label.setSize(width, height);
							}
							if (i <= 150) {
								if (zoomlevel > 1) {
									CropImageFilter cropimagefilter = new CropImageFilter(
											(width * (zoomlevel - 1))
													/ zoomlevel / 2,
											(height * (zoomlevel - 1))
													/ zoomlevel / 2, width
													/ zoomlevel, height
													/ zoomlevel);
									FilteredImageSource filteredimagesource = new FilteredImageSource(
											toolkitImage.getSource(),
											cropimagefilter);
									Image image = Toolkit.getDefaultToolkit()
											.createImage(filteredimagesource);
									int j = 0;
									do {
										if (Toolkit.getDefaultToolkit()
												.prepareImage(image,
														image.getWidth(null),
														image.getHeight(null),
														null))
											break;
										Thread.sleep(3L);
									} while (++j <= 150);
									if (j <= 150) {
										scaledImage = image.getScaledInstance(
												width, height, 1);
										j = 0;
										do {
											if (Toolkit
													.getDefaultToolkit()
													.prepareImage(
															scaledImage,
															scaledImage
																	.getWidth(null),
															scaledImage
																	.getHeight(null),
															null))
												break;
											Thread.sleep(3L);
										} while (++j <= 150);
									}
									if (j <= 150)
										jpeg_label.getGraphics().drawImage(
												scaledImage, 0, 0, width,
												height, jpeg_label);
								} else {
									jpeg_label.getGraphics().drawImage(
											toolkitImage, 0, 0, width, height,
											jpeg_label);
								}
								frameCount = frameCount + (long) 1;
								CalculateFrameRate();
							}
						}
						needImage = false;
					}
					Thread.sleep(7L);
				}
			} catch (Exception exception) {
			}
		}

		// private final jplug _$97862; /* synthetic field */

		SI() {
		}
	}

	public void init() {
		try {
			url = new URL("http://roomba1.eece.maine.edu:80");// getCodeBase();
			hostname = url.getHost();
			port = 80;// Integer.valueOf(getParameter("RemotePort"));
			timeout = 5000;// Integer.valueOf(getParameter("Timeout"));
			frameRate = (long) 2;// Long.valueOf(getParameter("PreviewFrameRate"));
			rotateAngle = (double) 0;// Double.valueOf(getParameter("RotateAngle"));
			deviceSerial = "";// getParameter("DeviceSerialNo");
			deviceSerialEncoded = deviceSerial.getBytes("8859_1");
			for (int i = 0; i < 25; i++) {
				String s = i + " ";// getParameter("Preset Position ".concat(String
				// .valueOf(String.valueOf(Integer.toString(i)))));
				if (s.length() > 0)
					preset_combo.addItem(s);
				else
					preset_combo.addItem(Integer.toString(i));
			}

			String s1 = getParameter("ServerName");
			servername_label.setText(s1);
			s1 = getParameter("Location");
			location_label.setText(s1);
			s1 = new String(
					"GET /MJPEG.CGI HTTP/1.0\r\nUser-Agent: user\r\nAuthorization: Basic ");
			s1 = s1.concat(deviceSerial);
			s1 = s1.concat("\r\n\r\n");
			transmitBuffer = s1.getBytes("8859_1");
			s1 = new String(
					"GET /IOCONTROL.CGI HTTP/1.0\r\nUser-Agent: user\r\nAuthorization: Basic ");
			s1 = s1.concat(deviceSerial);
			s1 = s1.concat("\r\n\r\n");
			_$1737 = s1.getBytes("8859_1");
			_$1077[0] = 0;
			s1 = new String("Content-type: image/jpeg\r\n\r\n");
			_$1084[0] = s1.length();
			_$1077[1] = s1.length();
			s1 = s1.concat("\r\n\r\n--video boundary--\r\n");
			_$1084[1] = s1.length() - _$1077[1];
			_$1077[2] = s1.length();
			s1 = s1.concat("Content-length: ");
			_$1084[2] = s1.length() - _$1077[2];
			_$1077[3] = s1.length();
			s1 = s1.concat("Trigger1=");
			_$1084[3] = s1.length() - _$1077[3];
			_$1077[4] = s1.length();
			s1 = s1.concat("Trigger2=");
			_$1084[4] = s1.length() - _$1077[4];
			_$1077[5] = s1.length();
			s1 = s1.concat("ImageUpload=");
			_$1084[5] = s1.length() - _$1077[5];
			_$1077[6] = s1.length();
			s1 = s1.concat("Date: ");
			_$1084[6] = s1.length() - _$1077[6];
			_$1077[7] = s1.length();
			s1 = s1.concat("ImageEmail=");
			_$1084[7] = s1.length() - _$1077[7];
			_$1094 = s1.getBytes();
			frameCount = 0L;
			_$1553 = System.currentTimeMillis();
			_$1565 = System.currentTimeMillis();
			_$1576 = 0.0F;
			mRun = false;
			_$1428 = false;
			//_$1452 = false;
			sendFlag = false;
			setTriggerRunning = false;
			setPanRunning = false;
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent actionevent) {
		PanSingleMoveDegree(pandegree_combo.getSelectedIndex() + 1);
		TiltSingleMoveDegree(tiltdegree_combo.getSelectedIndex() + 1);
		int i = preset_combo.getSelectedIndex();
		if ((currentSelection != i) && (i != -1)) {
			String s = new String((String) preset_combo.getItemAt(i));
			int l = s.indexOf(' ');
			int i1 = s.indexOf('(');
			if (l < 0 || i1 < 0)
				preset_field.setText("");
			else
				preset_field.setText(s.substring(l + 1, i1 - 1));
			if (i > 0)
				preset_field.setEnabled(true);
			else
				preset_field.setEnabled(false);
			currentSelection = i;
		}
		if (actionevent.getSource() == _$1915) {
			time_label.setText(GetTimeString());
			position_label.setText(String.valueOf(String
					.valueOf((new StringBuffer("("))
							.append(Integer.toString(GetHorizontalPosition()))
							.append(", ")
							.append(Integer.toString(GetVerticalPosition()))
							.append(")"))));
			if (GetImageUpload() == 1)
				upload_status_label.setText("[ON]");
			else
				upload_status_label.setText("[OFF]");
			if (GetImageEmail() == 1)
				email_status_label.setText("[ON]");
			else
				email_status_label.setText("[OFF]");
			showStatus(String.valueOf(String.valueOf((new StringBuffer(
					"Frame: ")).append(Float.toString(GetFrameRate())).append(
					" fps"))));
		} else if (actionevent.getActionCommand() == "Up")
			MovePanTiltDegree(1);
		else if (actionevent.getActionCommand() == "Down")
			MovePanTiltDegree(7);
		else if (actionevent.getActionCommand() == "Left") {
			System.out.println("Left");
			MovePanTiltDegree(3);
		} else if (actionevent.getActionCommand() == "Right")
			MovePanTiltDegree(5);
		else if (actionevent.getActionCommand() == "Home")
			MovePanTiltDegree(4);
		else if (actionevent.getActionCommand() == "Zoom1")
			Zoom(1);
		else if (actionevent.getActionCommand() == "Zoom2")
			Zoom(2);
		else if (actionevent.getActionCommand() == "Zoom3")
			Zoom(3);
		else if (actionevent.getActionCommand() == "Zoom4")
			Zoom(4);
		else if (actionevent.getActionCommand() == "Set") {
			int j = preset_combo.getSelectedIndex();
			String s1 = new String(preset_field.getText());
			if (j > 0) {
				SetName(s1);
				SetPosition(j);
				preset_combo.removeItemAt(j);
				preset_combo
						.insertItemAt(
								String.valueOf(String.valueOf((new StringBuffer(
										String.valueOf(String.valueOf(Integer
												.toString(j)))))
										.append(" ")
										.append(s1)
										.append(" (")
										.append(Integer
												.toString(GetHorizontalPosition()))
										.append(", ")
										.append(Integer
												.toString(GetVerticalPosition()))
										.append(")"))), j);
				preset_combo.setSelectedIndex(j);
			}
		} else if (actionevent.getActionCommand() == "Clear") {
			int k = preset_combo.getSelectedIndex();
			if (k > 0) {
				ClearPosition(k);
				preset_combo.removeItemAt(k);
				preset_combo.insertItemAt(Integer.toString(k), k);
				preset_combo.setSelectedIndex(k);
			}
		} else if (actionevent.getActionCommand() == "Goto")
			MovePanTiltPosition(preset_combo.getSelectedIndex());
		else if (actionevent.getActionCommand() == "Swing")
			SwingMode(2);
		else if (actionevent.getActionCommand() == "Stop")
			SwingMode(0);
		else if (actionevent.getActionCommand() == "UploadOn")
			SetImageUpload(1);
		else if (actionevent.getActionCommand() == "UploadOff")
			SetImageUpload(0);
		else if (actionevent.getActionCommand() == "EmailOn")
			SetImageEmail(1);
		else if (actionevent.getActionCommand() == "EmailOff")
			SetImageEmail(0);
	}

	public void destroy() {
		mRun = false;
		try {
			mainInputStream.close();
			mainOutputStream.close();
			mainSocket.close();
		} catch (Exception exception) {
			System.err.println(exception);
		}
	}

	public void start() {
		Play();
	}

	public void stop() {
		Stop();
	}

	public void paint(Graphics g) {
		if (toolkitImage != null)
			if (zoomlevel > 1)
				jpeg_label.setIcon(new ImageIcon(scaledImage));
			else
				jpeg_label.setIcon(new ImageIcon(toolkitImage));
		printComponents(g);
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void run() {
		System.out.println("Run");
		boolean flag;
		Thread thread;
		flag = true;
		mRun = true;
		isConnected = false;
		thread = Thread.currentThread();
		// _L1:
		while ((thread == _$1109) && flag && mRun) {
			System.out.println("Outter...");
			int totalRead;
			int j2;
			int i3;
			int j3;
			try {
				if (port.intValue() == 8481 || port.intValue() == 80)
					mainSocket = new Socket(hostname, 80);
				else
					mainSocket = new Socket(hostname, port.intValue());
				mainSocket.setSoTimeout(timeout.intValue());
				mainInputStream = new DataInputStream(mainSocket.getInputStream());
				mainOutputStream = new DataOutputStream(mainSocket.getOutputStream());
				isConnected = true;
				mainOutputStream.write(transmitBuffer);
			} catch (IOException ioexception) {
				ioexception.printStackTrace();
				System.err.println("<err>: Establish image connection.");
				isConnected = false;
				continue; /* Loop/switch isn't completed */
			}
			boolean flag1 = false;
			boolean flag2 = false;
			totalRead = 0;
			j2 = -1;
			i3 = 0;
			j3 = 0;
			needImage = false;
			// _L4:
			while (mRun) {
				try {
					//if (!mRun)
					//	break;// MISSING_BLOCK_LABEL_999;
					int i2;
					while (needImage) {
						if (totalRead > 0x13000) {
							totalRead = 0;
							j2 = -1;
							i3 = 0;
							j3 = 0;
						}
						int k1;
						try {
							k1 = mainInputStream.read(otherData, totalRead, 4096);
						} catch (IOException e1) {
							k1 = -1;
						}
						if (k1 > 0)
							totalRead += k1;
						try {
							Thread.sleep(3L);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
						i2 = 0;
						// _L3:
					while (true) {
						if (isConnected) {
							try {
								//if (!isConnected)
								//	break;// MISSING_BLOCK_LABEL_951;
									// label0:
								if (j3 < 0 || totalRead > 0x13000) {
									totalRead = 0;
									j2 = -1;
									i3 = 0;
									j3 = 0;
								}
								int amountRead;
								if (j2 == -1)
									amountRead = mainInputStream.read(otherData, totalRead, 16384);
								else
									amountRead = mainInputStream.read(imageData, totalRead, j3);
								if (amountRead > 0) {
									//break;
									
									totalRead += amountRead;
									i2 = 0;
									if (j2 == -1) {
										int index = SearchString(i3, totalRead, 6);
										if (index > 0) {
											timeString = new String(otherData, index + 6, 19);
											if (!setTriggerRunning) {
												_$1629 = otherData[index + 36] - 48;
												_$1638 = otherData[index + 35] - 48;
												_$1588 = otherData[index + 34] - 48;
												_$1601 = otherData[index + 33] - 48;
											}
											int k3 = 0;
											for (int i = 0; i < 3; i++)
												k3 = k3 * 10 + (otherData[index + 41 + i] - 48);
			
											horizontalPosition = k3;
											k3 = 0;
											for (int j = 0; j < 3; j++)
												k3 = k3 * 10 + (otherData[index + 45 + j] - 48);
			
											verticlePosition = k3;
										}
										index = SearchString(i3, totalRead, 2);
										if (index >= 0) {
											index += _$1084[2];
											j3 = 0;
											while (otherData[index] >= 48 && otherData[index] <= 57) {
												j3 = j3 * 10 + (otherData[index] - 48);
												index++;
											}
			
											imageLength = j3;
											j3 += _$1084[1];
											i3 = 0;
											j2 = SearchString(index, totalRead, 0);
											j2 += _$1084[0];
											totalRead -= j2;
											if (j3 > totalRead) {
												for (int k = 0; k < totalRead; k++)
													imageData[k] = otherData[k + j2];
			
												j3 -= totalRead;
												Thread.yield();
												continue;
											}
											for (int l = 0; l < j3; l++)
												imageData[l] = otherData[l + j2];
		
											int l2;
											if (totalRead > j3 * 2) {
												l2 = SearchString(j2 + j3 * 2, totalRead - j3
														* 2, 2);
												if (l2 >= 0) {
													totalRead = (totalRead + j2) - l2;
												} else {
													totalRead -= j3;
													l2 = j2 + j3;
												}
											} else {
												totalRead -= j3;
												l2 = j2 + j3;
											}
											for (int i1 = 0; i1 < totalRead; i1++) {
												otherData[i1] = otherData[i1 + l2];
											}
			
												//break MISSING_BLOCK_LABEL_951;
										}
										i3 = totalRead - _$1084[2];
											//break MISSING_BLOCK_LABEL_932;
									} else {
										j3 -= amountRead;
										if (j3 <= 0) {
											totalRead = mainInputStream.read(otherData, 0, 16384);
											//break MISSING_BLOCK_LABEL_951;
											break;
										}
										//break MISSING_BLOCK_LABEL_932;
									}
								} else {
									if (++i2 > 1500) {
										System.out.println("Breaking");
										break;// MISSING_BLOCK_LABEL_951;
									}
								}
								Thread.sleep(8L);
							} catch (IOException e) {
								e.printStackTrace();
								i2 = 20000;
							}
						} else {
							System.out.println("Not Connected");
						}
					}
					if (i2 < 1500) {
						needImage = true;
						j2 = -1;
						i3 = 0;
						continue;
					}

					isConnected = false;
				} catch (Exception e2) {
					System.err.println("<err>: Getting image connection broken.");
					isConnected = false;
				}
			}
			try {
				mainInputStream.close();
				mainOutputStream.close();
				mainSocket.close();
				isConnected = false;
			} catch (Exception exception1) {
				System.err.println(exception1);
			}
			if (!mRun)
				flag = false;
		}
		System.out.println("Done");
		// _L2:
	}

	public int SearchString(int i, int j, int k) {
		if (i < 0)
			i = 0;
		int l = 0;
		l = _$1084[k];
		int k1 = _$1077[k];
		for (int i1 = i; i1 < (j - l) + 1; i1++) {
			int j1;
			for (j1 = 0; j1 < l && otherData[i1 + j1] == _$1094[j1 + k1]; j1++)
				;
			if (j1 == l)
				return i1;
		}

		return -1;
	}

	public int GetRealTimeData() {
		return realTimeData;
	}

	public float GetFrameRate() {
		return _$1576;
	}

	public void CalculateFrameRate() {
		try {
			_$1565 = System.currentTimeMillis();
			float f = _$1565 - _$1553;
			if (f >= (float) 1000) {
				_$1576 = ((float) frameCount / f) * (float) 1000;
				frameCount = 0L;
				_$1553 = _$1565;
			}
		} catch (Exception exception) {
			System.err.println(exception);
		}
	}

	public int Play() {
		if (!mRun) {
			mRun = true;
			_$1127 = new SI();
			_$1127.start();
			_$1149 = new TS();
			_$1149.start();
			_$1109 = new Thread(this);
			_$1109.start();
			_$1915 = new Timer(1000, this);
			_$1915.start();
		}
		return realTimeData;
	}

	public void Stop() {
		mRun = false;
	}

	public String GetRemoteHost() {
		return hostname;
	}

	public int GetRemotePort() {
		return port.intValue();
	}

	public int SetRemotePort(int i) {
		port = new Integer(i);
		return realTimeData;
	}

	public int GetTimeout() {
		return timeout.intValue();
	}

	public int SetTimeout(int i) {
		timeout = new Integer(i);
		return realTimeData;
	}

	public long GetPreviewFrameRate() {
		return frameRate.longValue();
	}

	public int SetPreviewFrameRate(long l) {
		frameRate = new Long(l);
		return realTimeData;
	}

	public double GetRotateAngle() {
		return rotateAngle.doubleValue();
	}

	public int SetRotateAngle(double d) {
		rotateAngle = new Double(d);
		return realTimeData;
	}

	private boolean _$3391(byte abyte0[], byte abyte1[], int i) {
		for (int j = 0; j < i; j++)
			if (abyte0[j] != abyte1[j])
				return false;

		return true;
	}

	public int GetIOIn1() {
		return _$1613;
	}

	public int GetIOIn2() {
		return _$1621;
	}

	public int GetImageUpload() {
		return _$1588;
	}

	public int GetImageEmail() {
		return _$1601;
	}

	public int GetIOOut1() {
		return _$1629;
	}

	public int GetIOOut2() {
		return _$1638;
	}

	public int SetTriggerState(int i, int j, int k, int l) {
		int i1 = 0;
		if (!isConnected)
			return zeroRet;
		while (setTriggerRunning || setPanRunning) {
			if (++i1 > 15)
				return zeroRet;
			try {
				Thread.currentThread();
				Thread.sleep(20L);
			} catch (Exception exception) {
			}
		}
		setTriggerRunning = true;
		//_$1452 = false;
		triggerPortString = new String("POST /IOCONTROL.CGI HTTP/1.0\r\n");
		triggerPortString = triggerPortString.concat("Content-length: 52\r\n");
		triggerPortString = triggerPortString.concat("User-Agent: user\r\nAuthorization: Basic ");
		triggerPortString = triggerPortString.concat(deviceSerial);
		triggerPortString = triggerPortString.concat("\r\n\r\n");
		if (i == 0)
			triggerPortString = triggerPortString.concat("Trigger1=0&");
		else
			triggerPortString = triggerPortString.concat("Trigger1=1&");
		if (j == 0)
			triggerPortString = triggerPortString.concat("Trigger2=0&");
		else
			triggerPortString = triggerPortString.concat("Trigger2=1&");
		if (k == 0)
			triggerPortString = triggerPortString.concat("ImageUpload=0&");
		else
			triggerPortString = triggerPortString.concat("ImageUpload=1&");
		if (l == 0)
			triggerPortString = triggerPortString.concat("ImageEmail=0\r\n\r\n");
		else
			triggerPortString = triggerPortString.concat("ImageEmail=1\r\n\r\n");
		_$1629 = i;
		_$1638 = j;
		_$1588 = k;
		_$1601 = l;
		sendFlag = true;
		return realTimeData;
	}

	public int SetIOOut1(int i) {
		return SetTriggerState(i, _$1638, _$1588, _$1601);
	}

	public int SetIOOut2(int i) {
		return SetTriggerState(_$1629, i, _$1588, _$1601);
	}

	public int SetImageUpload(int i) {
		return SetTriggerState(_$1629, _$1638, i, _$1601);
	}

	public int SetImageEmail(int i) {
		return SetTriggerState(_$1629, _$1638, _$1588, i);
	}

	public int GetYear() {
		return year;
	}

	public int GetMonth() {
		return month;
	}

	public int GetDay() {
		return day;
	}

	public int GetDate() {
		return date;
	}

	public int GetHour() {
		return hour;
	}

	public int GetMinute() {
		return minute;
	}

	public int GetSecond() {
		return second;
	}

	public String GetTimeString() {
		return timeString;
	}

	public String GetVersion() {
		String s = "20030123";
		return s;
	}

	public boolean GetIsConnect() {
		return isConnected;
	}

	public int GetHorizontalPosition() {
		return horizontalPosition;
	}

	public int GetVerticalPosition() {
		return verticlePosition;
	}

	public int SetPanTilt(int i, int j) {
		int k = 0;
		if (!isConnected)
			return zeroRet;
		while (setTriggerRunning || setPanRunning) {
			if (++k > 15)
				return zeroRet;
			try {
				Thread.currentThread();
				Thread.sleep(20L);
			} catch (Exception exception) {
			}
		}
		setPanRunning = true;
		//_$1452 = false;
		postString = new String("POST /PANTILTCONTROL.CGI HTTP/1.0\r\n");
		String transmitString = new String("");
		switch (i) {
		case 1: // '\001'
			transmitString = transmitString.concat("PanSingleMoveDegree=");
			String s = Integer.toString(_$1808, 10);
			transmitString = transmitString.concat(s);
			transmitString = transmitString.concat("&TiltSingleMoveDegree=");
			s = Integer.toString(_$1829, 10);
			transmitString = transmitString.concat(s);
			transmitString = transmitString.concat("&PanTiltSingleMove=");
			s = Integer.toString(j, 10);
			transmitString = transmitString.concat(s);
			break;

		case 2: // '\002'
			transmitString = transmitString.concat("PanTiltPresetPositionMove=");
			String s1 = Integer.toString(j, 10);
			transmitString = transmitString.concat(s1);
			break;

		case 3: // '\003'
			transmitString = transmitString.concat("PanTiltHorizontal=");
			String s2 = Integer.toString(horizontalPosition, 10);
			transmitString = transmitString.concat(s2);
			transmitString = transmitString.concat("&PanTiltVertical=");
			s2 = Integer.toString(verticlePosition, 10);
			transmitString = transmitString.concat(s2);
			transmitString = transmitString.concat("&SetName=");
			transmitString = transmitString.concat(_$1796);
			transmitString = transmitString.concat("&SetPosition=");
			s2 = Integer.toString(j, 10);
			transmitString = transmitString.concat(s2);
			break;

		case 4: // '\004'
			transmitString = transmitString.concat("ClearPosition=");
			String s3 = Integer.toString(j, 10);
			transmitString = transmitString.concat(s3);
			break;

		case 5: // '\005'
			transmitString = transmitString.concat("PanTiltSwingMode=");
			String s4 = Integer.toString(j, 10);
			transmitString = transmitString.concat(s4);
			break;
		}
		transmitString = transmitString.concat("\r\n\r\n");
		String s5 = Integer.toString(transmitString.length(), 10);
		postString = postString.concat("Content-length: ");
		postString = postString.concat(s5);
		postString = postString.concat("\r\n");
		postString = postString.concat("User-Agent: user\r\nAuthorization: Basic ");
		postString = postString.concat(deviceSerial);
		postString = postString.concat("\r\n\r\n");
		postString = postString.concat(transmitString);
		sendFlag = true;
		return realTimeData;
	}

	public int MovePanTiltDegree(int i) {
		return SetPanTilt(1, i);
	}

	public int MovePanTiltPosition(int i) {
		return SetPanTilt(2, i);
	}

	public int SetPosition(int i) {
		return SetPanTilt(3, i);
	}

	public int ClearPosition(int i) {
		return SetPanTilt(4, i);
	}

	public int SwingMode(int i) {
		return SetPanTilt(5, i);
	}

	public int PanSingleMoveDegree(int i) {
		_$1808 = i;
		return 0;
	}

	public int TiltSingleMoveDegree(int i) {
		_$1829 = i;
		return 0;
	}

	public int SetName(String s) {
		_$1796 = new String(s);
		return 0;
	}

	public int Zoom(int i) {
		zoomlevel = i;
		return 0;
	}

	public void ComputePasswordEncryption20(byte abyte0[], byte abyte1[],
			byte abyte2[]) {
		for (int i = 0; i < 20; i++)
			abyte0[i] = abyte1[i];

		for (int j = 0; j < 2; j++) {
			for (int k = 0; k < 20; k++) {
				abyte0[k] ^= abyte2[k + j * 20];
				byte byte1 = (byte) (abyte0[k] & 0xf);
				byte byte0 = (byte) (abyte0[k] & 0xfffffff0);
				if ((abyte0[k] & 1) != 0) {
					byte1 ^= abyte2[k + j * 20 + 40];
					byte1 &= 0xf;
				}
				if ((abyte0[k] & 0x10) != 0) {
					byte0 ^= abyte2[k + j * 20 + 40];
					byte0 &= 0xf0;
				}
				abyte0[k] = (byte) (byte0 | byte1);
			}

		}

	}

	public jplug() {
		_$1077 = new int[8];
		_$1084 = new int[8];
		toolkitImage = null;
		scaledImage = null;
		needImage = false;
		imageData = new byte[0x19000];
		otherData = new byte[0x19000];
		_$1242 = new byte[1024];
		timeString = new String("2004-01-01 00:00:00");
		horizontalPosition = 0;
		verticlePosition = 0;
		_$1796 = new String("test");
		_$1808 = 0;
		_$1829 = 0;
		realTimeData = -1;
		zeroRet = 0;
		zoomlevel = 1;
		currentWidth = 0;
		currentHeight = 0;
		currentSelection = 0;
		b_home = new JButton();
		b_up = new JButton();
		b_down = new JButton();
		b_right = new JButton();
		b_left = new JButton();
		zoom_label = new JLabel();
		pandegree_label = new JLabel();
		tiltdegree_label = new JLabel();
		b_zoom1 = new JButton();
		b_zoom2 = new JButton();
		b_zoom3 = new JButton();
		b_zoom4 = new JButton();
		pandegree_combo = new JComboBox(_$1930);
		tiltdegree_combo = new JComboBox(_$1930);
		upload_label = new JLabel();
		email_label = new JLabel();
		b_upload_off = new JButton();
		b_upload_on = new JButton();
		b_email_on = new JButton();
		b_email_off = new JButton();
		upload_status_label = new Label();
		email_status_label = new Label();
		preset_label = new JLabel();
		preset_combo = new JComboBox();
		preset_field = new JTextField();
		b_clear = new JButton();
		b_set = new JButton();
		b_swing = new JButton();
		b_goto = new JButton();
		b_stop = new JButton();
		jpeg_label = new JLabel();
		time_label = new Label();
		position_label = new Label();
		location_label = new Label();
		servername_label = new Label();
		separator = new JSeparator();
		try {
			setupWindow();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void setupWindow() throws Exception {
		titledBorder1 = new TitledBorder("");
		setBackground(SystemColor.window);
		setLayout(null);
		b_home.setActionCommand("Home");
		b_home.setText("Home");
		b_home.setBounds(new Rectangle(86, 220, 69, 24));
		b_up.setActionCommand("Up");
		b_up.setText("Up");
		b_up.setBounds(new Rectangle(86, 191, 69, 24));
		b_down.setActionCommand("Down");
		b_down.setText("Down");
		b_down.setBounds(new Rectangle(86, 249, 69, 24));
		b_right.setActionCommand("Right");
		b_right.setText("Right");
		b_right.setBounds(new Rectangle(161, 220, 69, 24));
		b_left.setActionCommand("Left");
		b_left.setText("Left");
		b_left.setBounds(new Rectangle(12, 220, 69, 24));
		zoom_label.setText("Zoom :");
		zoom_label.setBounds(new Rectangle(10, 105, 57, 17));
		pandegree_label.setText("Pan degree");
		pandegree_label.setBounds(new Rectangle(10, 134, 80, 17));
		tiltdegree_label.setText("Tilt degree");
		tiltdegree_label.setBounds(new Rectangle(10, 163, 80, 17));
		b_zoom1.setText("x1");
		b_zoom1.setBounds(new Rectangle(57, 95, 55, 24));
		b_zoom1.setActionCommand("Zoom1");
		b_zoom2.setText("x2");
		b_zoom2.setBounds(new Rectangle(115, 95, 55, 24));
		b_zoom2.setActionCommand("Zoom2");
		b_zoom3.setText("x3");
		b_zoom3.setBounds(new Rectangle(173, 95, 55, 24));
		b_zoom3.setActionCommand("Zoom3");
		b_zoom4.setText("x4");
		b_zoom4.setBounds(new Rectangle(231, 95, 55, 24));
		b_zoom4.setActionCommand("Zoom4");
		pandegree_combo.setMaximumRowCount(10);
		pandegree_combo.setBounds(new Rectangle(225, 125, 62, 24));
		tiltdegree_combo.setMaximumRowCount(10);
		tiltdegree_combo.setBounds(new Rectangle(225, 154, 62, 24));
		upload_label.setFont(new Font("Dialog", 1, 12));
		upload_label.setForeground(Color.red);
		upload_label.setText("Upload Video");
		upload_label.setBounds(new Rectangle(318, 13, 80, 17));
		email_label.setFont(new Font("Dialog", 1, 12));
		email_label.setForeground(Color.red);
		email_label.setText("E-mail Video");
		email_label.setBounds(new Rectangle(318, 39, 80, 17));
		b_upload_off.setText("OFF");
		b_upload_off.setBounds(new Rectangle(475, 8, 58, 24));
		b_upload_off.setActionCommand("UploadOff");
		b_upload_on.setText("ON");
		b_upload_on.setBounds(new Rectangle(412, 8, 58, 24));
		b_upload_on.setActionCommand("UploadOn");
		b_email_on.setText("ON");
		b_email_on.setBounds(new Rectangle(412, 36, 58, 24));
		b_email_on.setActionCommand("EmailOn");
		b_email_off.setText("OFF");
		b_email_off.setBounds(new Rectangle(475, 36, 58, 24));
		b_email_off.setActionCommand("EmailOff");
		upload_status_label.setFont(new Font("Dialog", 1, 12));
		upload_status_label.setText("[OFF]");
		upload_status_label.setBounds(new Rectangle(540, 13, 45, 17));
		email_status_label.setFont(new Font("Dialog", 1, 12));
		email_status_label.setText("[OFF]");
		email_status_label.setBounds(new Rectangle(540, 39, 45, 17));
		preset_label.setText("Preset Position");
		preset_label.setBounds(new Rectangle(12, 289, 144, 18));
		preset_combo.setMaximumRowCount(10);
		preset_combo.setBounds(new Rectangle(12, 310, 185, 22));
		preset_field.setText("Home");
		preset_field.setBounds(new Rectangle(12, 337, 185, 22));
		preset_field.setEnabled(false);
		b_clear.setText("Clear");
		b_clear.setBounds(new Rectangle(87, 366, 69, 24));
		b_clear.setActionCommand("Clear");
		b_set.setText("Set");
		b_set.setBounds(new Rectangle(12, 366, 69, 24));
		b_set.setActionCommand("Set");
		b_swing.setText("Swing");
		b_swing.setBounds(new Rectangle(12, 425, 69, 24));
		b_swing.setActionCommand("Swing");
		b_goto.setText("Go To");
		b_goto.setBounds(new Rectangle(12, 396, 69, 24));
		b_goto.setActionCommand("Goto");
		b_stop.setText("Stop");
		b_stop.setBounds(new Rectangle(87, 425, 69, 24));
		b_stop.setActionCommand("Stop");
		jpeg_label.setDoubleBuffered(true);
		jpeg_label.setHorizontalAlignment(2);
		jpeg_label.setBounds(new Rectangle(318, 94, 640, 480));
		jpeg_label.setVerticalAlignment(1);
		time_label.setFont(new Font("Dialog", 1, 12));
		time_label.setText("2005-01-01 00:00:00");
		time_label.setBounds(new Rectangle(10, 46, 200, 17));
		position_label.setBounds(new Rectangle(232, 222, 83, 20));
		position_label.setFont(new Font("Dialog", 1, 12));
		position_label.setText("(0, 0)");
		location_label.setBounds(new Rectangle(10, 27, 200, 17));
		location_label.setText("Location");
		location_label.setFont(new Font("Dialog", 1, 12));
		servername_label.setBounds(new Rectangle(10, 8, 200, 17));
		servername_label.setText("Device");
		servername_label.setFont(new Font("Dialog", 1, 12));
		separator.setBounds(new Rectangle(10, 75, 660, 3));
		add(b_zoom1, null);
		add(zoom_label, null);
		add(b_zoom2, null);
		add(b_zoom3, null);
		add(b_zoom4, null);
		add(pandegree_combo, null);
		add(tiltdegree_combo, null);
		add(b_up, null);
		add(b_home, null);
		add(b_left, null);
		add(b_down, null);
		add(b_right, null);
		add(preset_label, null);
		add(preset_combo, null);
		add(preset_field, null);
		add(b_stop, null);
		add(b_clear, null);
		add(b_set, null);
		add(b_swing, null);
		add(b_goto, null);
		add(tiltdegree_label, null);
		add(pandegree_label, null);
		add(position_label, null);
		add(jpeg_label, null);
		add(b_upload_on, null);
		add(b_upload_off, null);
		add(upload_label, null);
		add(email_label, null);
		add(b_email_on, null);
		add(b_email_off, null);
		add(upload_status_label, null);
		add(email_status_label, null);
		add(time_label, null);
		add(servername_label, null);
		add(location_label, null);
		add(separator, null);
		b_zoom1.addActionListener(this);
		b_zoom2.addActionListener(this);
		b_zoom3.addActionListener(this);
		b_zoom4.addActionListener(this);
		b_upload_on.addActionListener(this);
		b_upload_off.addActionListener(this);
		b_email_on.addActionListener(this);
		b_email_off.addActionListener(this);
		b_home.addActionListener(this);
		b_up.addActionListener(this);
		b_down.addActionListener(this);
		b_left.addActionListener(this);
		b_right.addActionListener(this);
		b_set.addActionListener(this);
		b_clear.addActionListener(this);
		b_goto.addActionListener(this);
		b_swing.addActionListener(this);
		b_stop.addActionListener(this);
	}

	private String hostname;
	private URL url;
	private Integer port;
	private Integer timeout;
	private Long frameRate;
	private Long _$983;
	private Double rotateAngle;
	private String deviceSerial;
	private byte deviceSerialEncoded[];
	private String triggerPortString;
	private String postString;
	private int _$1077[];
	private int _$1084[];
	private byte _$1094[];
	private Thread _$1109;
	private SI _$1127;
	private TS _$1149;
	private Image toolkitImage;
	private Image scaledImage;
	private int imageLength;
	private boolean needImage;
	private byte imageData[];
	private byte otherData[];
	private byte _$1242[];
	private Socket mainSocket;
	private DataInputStream mainInputStream;
	private DataOutputStream mainOutputStream;
	private Socket _$1311;
	private DataInputStream _$1328;
	private DataOutputStream _$1347;
	private Socket socket;
	private DataInputStream socketInput;
	private DataOutputStream socketOutput;
	private boolean mRun;
	private boolean _$1428;
	//private boolean _$1452;
	private boolean isConnected;
	private boolean sendFlag;
	private boolean setTriggerRunning;
	private boolean setPanRunning;
	private long frameCount;
	private long _$1553;
	private long _$1565;
	private float _$1576;
	private int _$1588;
	private int _$1601;
	private int _$1613;
	private int _$1621;
	private int _$1629;
	private int _$1638;
	private int second;
	private int minute;
	private int hour;
	private int date;
	private int month;
	private int day;
	private int year;
	private String timeString;
	private byte transmitBuffer[];
	private byte _$1737[];
	private int horizontalPosition;
	private int verticlePosition;
	private String _$1796;
	private int _$1808;
	private int _$1829;
	private int realTimeData;
	private int zeroRet;
	private int zoomlevel;
	private int currentWidth;
	private int currentHeight;
	private jplug _$1886;
	private Graphics _$1902;
	private Timer _$1915;
	private int currentSelection;
	private String _$1930[] = { "1", "2", "3", "4", "5", "6", "7", "8", "9",
			"10" };
	JButton b_home;
	JButton b_up;
	JButton b_down;
	JButton b_right;
	JButton b_left;
	JLabel zoom_label;
	JLabel pandegree_label;
	JLabel tiltdegree_label;
	JButton b_zoom1;
	JButton b_zoom2;
	JButton b_zoom3;
	JButton b_zoom4;
	JComboBox pandegree_combo;
	JComboBox tiltdegree_combo;
	JLabel upload_label;
	JLabel email_label;
	JButton b_upload_off;
	JButton b_upload_on;
	JButton b_email_on;
	JButton b_email_off;
	Label upload_status_label;
	Label email_status_label;
	JLabel preset_label;
	JComboBox preset_combo;
	JTextField preset_field;
	JButton b_clear;
	JButton b_set;
	JButton b_swing;
	JButton b_goto;
	JButton b_stop;
	TitledBorder titledBorder1;
	JLabel jpeg_label;
	Label time_label;
	Label position_label;
	Label location_label;
	Label servername_label;
	JSeparator separator;

}
