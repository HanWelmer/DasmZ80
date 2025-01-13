package com.github.hanwelmer.dasmZ80;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AssemblyCode {

  private static final List<Byte> CALL_OR_JUMP = Arrays.asList((byte) 0xC2, (byte) 0xC3, (byte) 0xC4, (byte) 0xCA,
      (byte) 0xCC, (byte) 0xCD, (byte) 0xD2, (byte) 0xD4, (byte) 0xDA, (byte) 0xDC, (byte) 0xE2, (byte) 0xE4,
      (byte) 0xEA, (byte) 0xEC, (byte) 0xF2, (byte) 0xF4, (byte) 0xFA, (byte) 0xFC);
  private static final List<Byte> RELATIVE_JUMP = Arrays.asList((byte) 0x10, (byte) 0x18, (byte) 0x20, (byte) 0x28,
      (byte) 0x30, (byte) 0x38);
  private static final List<Byte> RESET = Arrays.asList((byte) 0xC7, (byte) 0xCF, (byte) 0xD7, (byte) 0xDF, (byte) 0xE7,
      (byte) 0xEF, (byte) 0xF7, (byte) 0xFF);

  private int address = 0;
  private ArrayList<Byte> bytes;
  private String label;
  private String mnemonic;
  private String comment;

  public AssemblyCode(int address, ArrayList<Byte> bytes, String label, String mnemonic, String comment) {
	this.address = address;
	this.bytes = bytes;
	this.label = label;
	this.mnemonic = mnemonic;
	this.comment = comment;
  }

  public AssemblyCode(int address, ArrayList<Byte> bytes) {
	this.address = address;
	this.bytes = bytes;
  }

  public AssemblyCode(int address, String mnemonic, String comment) {
	this.address = address;
	this.label = "";
	this.mnemonic = mnemonic;
	this.comment = comment;
  }

  public AssemblyCode(int address, String mnemonic) {
	this.address = address;
	this.label = "";
	this.mnemonic = mnemonic;
	this.comment = "";
  }

  public int getAddress() {
	return address;
  }

  public void addByte(Byte nextByte) {
	if (bytes == null) {
	  bytes = new ArrayList<Byte>();
	}
	bytes.add(nextByte);
  }

  public void updateMnemonic(String oldValue, String newValue) {
	mnemonic = mnemonic.replace(oldValue, newValue);
  }

  public ArrayList<Byte> getBytes() {
	return bytes;
  }

  public String getMnemonic() {
	return mnemonic;
  }

  public void setLabel(String label) {
	this.label = label;
  }

  public void setMnemonic(String mnemonic) {
	this.mnemonic = mnemonic;
  }

  public String toString() {
	// address
	String result = String.format("%04X", address);

	if ((mnemonic == null || mnemonic.length() == 0) && (label == null || label.length() == 0) && bytes == null) {
	  // only a comment or nothing:
	  if (comment == null || comment.length() == 0) {
		result = "";
	  } else {
		result = String.format("%24s%s", "", comment);
	  }
	} else {
	  // binary code
	  if (bytes != null) {
		int i = 0;
		for (Byte byt : bytes) {
		  if (i == 0) {
			result += " ";
			i = 4;
		  }
		  i--;
		  result += String.format("%02X", byt);
		}
	  }

	  // label
	  if (label != null && label.length() > 0) {
		if (result.length() < 14) {
		  result += String.format("%14s", "");
		  result = result.substring(0, 14);
		}
		result += label + ":";
	  }

	  // mnemonic
	  if (mnemonic != null && mnemonic.length() > 0) {
		if (result.length() < 24) {
		  result += String.format("%24s", "");
		  ;
		  result = result.substring(0, 24);
		}
		result += mnemonic;
	  }

	  // comment
	  if (comment != null && comment.length() > 0) {
		if (result.length() < 44) {
		  result += String.format("%44s", "");
		  result = result.substring(0, 44);
		}
		result += comment;
	  }
	}

	return result + "\n";
  }

  public boolean isExit() {
	// TODO add flag to code definition, so that this part becomes
	// microprocessor independent.
	return bytes.get(0) == (byte) 0xC9 // RET
	    || bytes.get(0) == (byte) 0xC3 // JP nnnn
	    || bytes.get(0) == (byte) 0x18 // JR dd
	    || bytes.get(0) == (byte) 0xE9 // JP (HL), JP (IX), JP (IY)
	    || "JP   (IX)".equals(mnemonic) || "JP   (IY)".equals(mnemonic);
  }

  public Integer getCallOrJumpAddress() {
	// TODO Auto-generated method stub
	Integer result = null;
	// private int address = 0;
	// private ArrayList<Byte> bytes;
	// private String mnemonic;

	// call or jump to absolute address
	if (CALL_OR_JUMP.contains(bytes.get(0))) {
	  result = bytes.get(1) + bytes.get(2) * 256;
	} else if (RELATIVE_JUMP.contains(bytes.get(0))) {
	  result = address + 2 + bytes.get(1);
	} else if (RESET.contains(bytes.get(0))) {
	  // C7 11.000.111 00
	  // CF 11.001.111 08
	  // D7 11.010.111 10
	  // DF 11.011.111 18
	  // E7 11.100.111 20
	  // EF 11.101.111 28
	  // F7 11.110.111 30
	  // FF 11.111.111 38
	  result = bytes.get(0) & 0x38;
	}
	// jump relative
	return result;
  }
}
