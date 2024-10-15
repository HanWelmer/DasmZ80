package com.github.hanwelmer.dasmZ80;

import java.io.IOException;
import java.util.HashMap;

public class Decoder {

  private HashMap<Integer, BinaryCode> hashMap = new HashMap<Integer, BinaryCode>(2);

  public AssemblyCode get(int address, Byte nextByte, ByteReader reader) throws IOException {
	Integer key = new Integer(nextByte);
	if (key < 0) {
	  key += 256;
	}
	BinaryCode binCode = hashMap.get(key);
	if (binCode == null) {
	  String errorMessage = String.format("Unsupported code 0x%02X at address 0x%04X", nextByte, address);
	  System.out.print(errorMessage);
	  throw new RuntimeException(errorMessage);
	}
	// TODO
	if ("<skip,next>".equals(binCode.getMnemonic())) {
	  String errorMessage = "<skip,next> not yet implemeneted.";
	  System.out.print(errorMessage);
	  throw new RuntimeException(errorMessage);
	}

	AssemblyCode asmCode = new AssemblyCode(address, binCode.getMnemonic());
	asmCode.addByte(nextByte);

	// Process prefix (0xCB, )
	if ("<next>".equals(binCode.getMnemonic())) {
	  nextByte = reader.getNextByte();
	  key = key * 256 + nextByte;
	  if (nextByte < 0) {
		key += 256;
	  }
	  binCode = hashMap.get(key);
	  if (binCode == null) {
		String errorMessage = String.format("Unsupported code 0x%04X at address 0x%04X", key, address);
		System.out.print(errorMessage);
		throw new RuntimeException(errorMessage);
	  }
	  asmCode.setMnemonic(binCode.getMnemonic());
	  asmCode.addByte(nextByte);
	}

	// Process word constant / address.
	if (binCode.getMnemonic().contains("@")) {
	  Byte byte2 = reader.getNextByte();
	  Byte byte3 = reader.getNextByte();
	  asmCode.addByte(byte2);
	  asmCode.addByte(byte3);
	  asmCode.updateMnemonic("@", String.format("0x%02X%02X", byte3, byte2));
	}
	// Process byte constant.
	if (binCode.getMnemonic().contains("#")) {
	  Byte byte2 = reader.getNextByte();
	  asmCode.addByte(byte2);
	  asmCode.updateMnemonic("#", String.format("0x%02X", byte2));
	}
	// Process relative address.
	if (binCode.getMnemonic().contains("%")) {
	  Byte byte2 = reader.getNextByte();
	  asmCode.addByte(byte2);
	  int targetAddress = address + binCode.getNrOfBytes();
	  targetAddress += (byte2 < 128) ? byte2 : (-byte2);
	  asmCode.updateMnemonic("%", String.format("lbl%04X", targetAddress));
	}
	// Process IO port.
	if (binCode.getMnemonic().contains("&")) {
	  Byte byte2 = reader.getNextByte();
	  asmCode.addByte(byte2);
	  asmCode.updateMnemonic("&", String.format("0x%02X", byte2));
	}

	return asmCode;
  }

  // Constructor
  public Decoder() {
	hashMap.put(new Integer(0x00), new BinaryCode(0x00, 1, "00", "NOP"));
	hashMap.put(new Integer(0x01), new BinaryCode(0x01, 3, "01@@@@", "LD   BC,@"));
	hashMap.put(new Integer(0x02), new BinaryCode(0x02, 1, "02", "LD   (BC),A"));
	hashMap.put(new Integer(0x06), new BinaryCode(0x06, 2, "06##", "LD   B,#"));
	hashMap.put(new Integer(0x10), new BinaryCode(0x10, 2, "10%%", "DJNZ %"));
	hashMap.put(new Integer(0x76), new BinaryCode(0x76, 1, "76", "HALT"));
	hashMap.put(new Integer(0xD3), new BinaryCode(0xD3, 2, "D3&&", "OUT  (&),A"));
	hashMap.put(new Integer(0xCB), new BinaryCode(0xCB, 2, "CB", "<next>"));
	hashMap.put(new Integer(0xCB00), new BinaryCode(0xCB00, 2, "CB00", "RLC  B"));

	// CB00 CB00 2 RLC B
  }

}
