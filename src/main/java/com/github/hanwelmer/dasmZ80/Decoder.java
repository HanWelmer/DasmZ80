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

	  if ("<skip,next>".equals(binCode.getMnemonic())) {
		Byte byte3 = reader.getNextByte();
		key = key * 256;
		Byte byte4 = reader.getNextByte();
		key = key * 256 + byte4;
		if (byte4 < 0) {
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
		asmCode.addByte(byte3);
		asmCode.addByte(byte4);

		// Process IX/IY displacement.
		if (!asmCode.getMnemonic().contains("$")) {
		  String errorMessage = String.format("Unsupported code 0x%08X at address 0x%04X", key, address);
		  System.out.print(errorMessage);
		  throw new RuntimeException(errorMessage);
		}
		applyDisplacement(asmCode, byte3);
	  } else {
		asmCode.setMnemonic(binCode.getMnemonic());
		asmCode.addByte(nextByte);
	  }
	}

	// Process IX/IY displacement.
	if (asmCode.getMnemonic().contains("$")) {
	  Byte byte2 = reader.getNextByte();
	  asmCode.addByte(byte2);
	  applyDisplacement(asmCode, byte2);
	}
	// Process byte constant.
	if (asmCode.getMnemonic().contains("#")) {
	  Byte byte2 = reader.getNextByte();
	  asmCode.addByte(byte2);
	  asmCode.updateMnemonic("#", String.format("0x%02X", byte2));
	}
	// Process word constant / address.
	if (asmCode.getMnemonic().contains("@")) {
	  Byte byte2 = reader.getNextByte();
	  Byte byte3 = reader.getNextByte();
	  asmCode.addByte(byte2);
	  asmCode.addByte(byte3);
	  asmCode.updateMnemonic("@", String.format("0x%02X%02X", byte3, byte2));
	}
	// Process relative address.
	if (asmCode.getMnemonic().contains("%")) {
	  Byte byte2 = reader.getNextByte();
	  asmCode.addByte(byte2);
	  int targetAddress = address + asmCode.getBytes().size();
	  targetAddress += (byte2 < 128) ? byte2 : (-byte2);
	  asmCode.updateMnemonic("%", String.format("lbl%04X", targetAddress));
	}
	// Process IO port.
	if (asmCode.getMnemonic().contains("&")) {
	  Byte byte2 = reader.getNextByte();
	  asmCode.addByte(byte2);
	  asmCode.updateMnemonic("&", String.format("0x%02X", byte2));
	}

	return asmCode;
  }

  private void applyDisplacement(AssemblyCode asmCode, Byte value) {
	if (value >= 0) {
	  asmCode.updateMnemonic("$", String.format("%d", value));
	} else {
	  asmCode.updateMnemonic("+", "-");
	  asmCode.updateMnemonic("$", String.format("%d", -value));
	}
  }

  // Constructor
  public Decoder() {
	hashMap.put(new Integer(0x00), new BinaryCode(0x00, "00", "NOP"));
	hashMap.put(new Integer(0x01), new BinaryCode(0x01, "01@@@@", "LD   BC,@"));
	hashMap.put(new Integer(0x02), new BinaryCode(0x02, "02", "LD   (BC),A"));
	hashMap.put(new Integer(0x06), new BinaryCode(0x06, "06##", "LD   B,#"));
	hashMap.put(new Integer(0x10), new BinaryCode(0x10, "10%%", "DJNZ %"));
	hashMap.put(new Integer(0x76), new BinaryCode(0x76, "76", "HALT"));
	hashMap.put(new Integer(0xD3), new BinaryCode(0xD3, "D3&&", "OUT  (&),A"));
	hashMap.put(new Integer(0xCB), new BinaryCode(0xCB, "CB", "<next>"));
	hashMap.put(new Integer(0xDD), new BinaryCode(0xDD, "DD", "<next>"));
	hashMap.put(new Integer(0xED), new BinaryCode(0xED, "ED", "<next>"));
	hashMap.put(new Integer(0xFD), new BinaryCode(0xFD, "FD", "<next>"));
	hashMap.put(new Integer(0xCB00), new BinaryCode(0xCB00, "CB00", "RLC  B"));
	hashMap.put(new Integer(0xDD19), new BinaryCode(0xDD19, "DD19", "ADD  IX,DE"));
	hashMap.put(new Integer(0xDD21), new BinaryCode(0xDD21, "DD21@@@@", "LD   IX,@"));
	hashMap.put(new Integer(0xDD26), new BinaryCode(0xDD26, "DD26##", "LD   IXH,#"));
	hashMap.put(new Integer(0xDD34), new BinaryCode(0xDD34, "DD34$$", "INC  (IX+$)"));
	hashMap.put(new Integer(0xDD36), new BinaryCode(0xDD36, "DD36$$##", "LD   (IX+$),#"));
	hashMap.put(new Integer(0xED40), new BinaryCode(0xED40, "ED40", "IN   B,(C)"));
	hashMap.put(new Integer(0xED43), new BinaryCode(0xED43, "ED43@@@@", "LD   (@),BC"));
	hashMap.put(new Integer(0xFD26), new BinaryCode(0xFD26, "FD26##", "LD   IYH,#"));
	hashMap.put(new Integer(0xFD35), new BinaryCode(0xFD35, "FD35$$", "DEC  (IY+$)"));
	hashMap.put(new Integer(0xFD36), new BinaryCode(0xFD36, "FD36$$##", "LD   (IY+$),#"));
	hashMap.put(new Integer(0xDDCB), new BinaryCode(0xDDCB, "DDCB", "<skip,next>"));
	hashMap.put(new Integer(0xFDCB), new BinaryCode(0xFDCB, "FDCB", "<skip,next>"));
	hashMap.put(new Integer(0xDDCB0000), new BinaryCode(0xDDCB0000, "DDCB$$00", "LD   B,RLC (IX+$)"));
	hashMap.put(new Integer(0xFDCB0001), new BinaryCode(0xFDCB0001, "FDCB$$01", "LD   C,RLC (IY+$)"));
	hashMap.put(new Integer(0xFDCB0080), new BinaryCode(0xFDCB0080, "FDCB$$80", "LD   B,RES 0,(IY+$)"));
  }

}
