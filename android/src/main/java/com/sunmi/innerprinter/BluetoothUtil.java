package com.sunmi.innerprinter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothUtil {

	private static final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private static final String Innerprinter_Address = "00:11:22:33:44:55";

	public static BluetoothAdapter getBTAdapter() {
		return BluetoothAdapter.getDefaultAdapter();
	}

	public static BluetoothDevice getDevice(BluetoothAdapter bluetoothAdapter) {
		BluetoothDevice innerprinter_device = null;
		Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
		for (BluetoothDevice device : devices) {
			if (device.getAddress().equals(Innerprinter_Address)) {
				innerprinter_device = device;
				break;
			}
		}
		return innerprinter_device;
	}

	public static BluetoothSocket getSocket(BluetoothDevice device) throws IOException {
		BluetoothSocket socket = device.createRfcommSocketToServiceRecord(PRINTER_UUID);
		socket.connect();
		return socket;
	}

	public static void sendData(byte[] bytes, BluetoothSocket socket) throws IOException {
		OutputStream out = socket.getOutputStream();
		out.write(bytes, 0, bytes.length);
		out.flush();
		out.close();
	}

}
