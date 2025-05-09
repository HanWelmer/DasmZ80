package com.github.hanwelmer.dasmZ80;

import java.io.IOException;
import java.util.HashMap;

public class Decoder {

  private ByteReader reader;
  private HashMap<Integer, BinaryCode> hashMap = new HashMap<Integer, BinaryCode>(2);

  public void setReader(ByteReader reader) {
	this.reader = reader;
  }

  public AssemblyCode get(int address, Symbols symbols) throws IOException, IllegalOpcodeException {
	Byte nextByte = reader.getByte(address);
	if (nextByte == null)
	  return null;

	Integer key = new Integer(nextByte);
	if (key < 0) {
	  key += 256;
	}

	BinaryCode binCode = hashMap.get(key);
	if (binCode == null) {
	  String errorMessage = String.format("Unsupported code 0x%02X at address 0x%04X", nextByte, address);
	  throw new IllegalOpcodeException(errorMessage);
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
		String errorMessage = String.format("Unsupported code 0x%04X at address 0x%04X\n", key, address);
		throw new IllegalOpcodeException(errorMessage);
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
		  String errorMessage = String.format("Unsupported code 0x%04X at address 0x%04X\n", key, address);
		  throw new IllegalOpcodeException(errorMessage);
		}

		asmCode.setMnemonic(binCode.getMnemonic());
		asmCode.addByte(nextByte);
		asmCode.addByte(byte3);
		asmCode.addByte(byte4);

		// Process IX/IY displacement.
		if (!asmCode.getMnemonic().contains("$")) {
		  String errorMessage = String.format("Unsupported code 0x%08X at address 0x%04X\n", key, address);
		  throw new IllegalOpcodeException(errorMessage);
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

	// Process jump or call to absolute address.
	if (asmCode.getMnemonic().contains("!")) {
	  Byte byte2 = reader.getNextByte();
	  Byte byte3 = reader.getNextByte();
	  asmCode.addByte(byte2);
	  asmCode.addByte(byte3);

	  // Prepare value.
	  Integer value = new Integer(byte3);
	  if (byte3 < 0) {
		value += 256;
	  }
	  value *= 256;
	  value += byte2;
	  if (byte2 < 0) {
		value += 256;
	  }

	  // Get an existing or make a new symbol.
	  Symbol symbol = getOrMakeSymbol(value, address, symbols);

	  // Put label in assembly code instruction.
	  asmCode.updateMnemonic("!", symbol.getName());

	  // Add first line of symbol comment to the instruction.
	  if (symbol.getComments().size() > 0) {
		asmCode.setComment(symbol.getComments().get(0));
	  }
	}

	// Process jump to relative address.
	if (asmCode.getMnemonic().contains("%")) {
	  Byte byte2 = reader.getNextByte();
	  asmCode.addByte(byte2);

	  // Prepare value.
	  int value = address + asmCode.getBytes().size();
	  value += (byte2 < 128) ? byte2 : (-byte2);

	  // Get an existing or make a new symbol.
	  Symbol symbol = getOrMakeSymbol(value, address, symbols);

	  // Put label in assembly code instruction.
	  asmCode.updateMnemonic("%", symbol.getName() + "-$");
	}

	// Process word constant.
	if (asmCode.getMnemonic().contains("@")) {
	  Byte byte2 = reader.getNextByte();
	  Byte byte3 = reader.getNextByte();
	  asmCode.addByte(byte2);
	  asmCode.addByte(byte3);

	  // Prepare value.
	  Integer value = new Integer(byte3);
	  if (byte3 < 0) {
		value += 256;
	  }
	  value *= 256;
	  value += byte2;
	  if (byte2 < 0) {
		value += 256;
	  }

	  // Get an existing or make a new symbol.
	  String expression = String.format("%02X%02X", byte3, byte2);
	  String label = "lbl" + expression;
	  Symbol symbol = symbols.getConstants().get(value);
	  if (symbol == null) {
		symbol = symbols.getOrMakeSymbol(label, SymbolType.memoryAddress, value, "0x" + expression);
	  }

	  // Put label in assembly code instruction.
	  asmCode.updateMnemonic("@", symbol.getName());

	  // add address as reference to symbol.
	  symbol.addReference(new Integer(address));
	}

	// Process IO port.
	if (asmCode.getMnemonic().contains("&")) {
	  Byte byte2 = reader.getNextByte();
	  asmCode.addByte(byte2);

	  // Prepare value.
	  Integer value = new Integer(byte2);
	  if (value < 0) {
		value += 256;
	  }

	  // Get an existing or make a new symbol.
	  String expression = String.format("%02X", value);
	  String label = "port" + expression;
	  Symbol symbol = symbols.getOrMakeSymbol(label, SymbolType.portAddress, value, "0x" + expression);

	  // use port label in the assembly code.
	  asmCode.updateMnemonic("&", symbol.getName());

	  // add address as reference to symbol.
	  symbol.addReference(new Integer(address));
	}

	// Process reset address.
	if (asmCode.getMnemonic().contains("*")) {
	  // Prepare reset address.
	  // RST 00 = C7 = 11.000.111
	  // RST 08 = CF = 11.001.111
	  // RST 10 = D7 = 11.010.111
	  // RST 18 = DF = 11.011.111
	  // RST 20 = E7 = 11.100.111
	  // RST 28 = EF = 11.101.111
	  // RST 30 = F7 = 11.110.111
	  // RST 38 = FF = 11.111.111
	  Integer value = asmCode.getBytes().get(0) & 0x38;

	  // Get an existing or make a new symbol.
	  Symbol symbol = getOrMakeSymbol(value, address, symbols);

	  // Put label in assembly code instruction.
	  asmCode.updateMnemonic("*", symbol.getName());

	  // Add first line of symbol comment to the instruction.
	  if (symbol.getComments().size() > 0) {
		asmCode.setComment(symbol.getComments().get(0));
	  }
	}

	return asmCode;
  } // get()

  private Symbol getOrMakeSymbol(Integer value, Integer address, Symbols symbols) {
	// Get an existing or make a new symbol.
	String expression = String.format("%04X", value);
	String label = "lbl" + expression;
	Symbol symbol = symbols.getOrMakeSymbol(label, SymbolType.label, value, "0x" + expression);

	// add address as reference to symbol.
	symbol.addReference(new Integer(address));

	// add the symbol as an entry point.
	symbols.getEntryPoints().put(value, symbol);
	return symbol;
  }

  private void applyDisplacement(AssemblyCode asmCode, Byte value) {
	if (value >= 0) {
	  asmCode.updateMnemonic("$", String.format("%d", value));
	} else {
	  asmCode.updateMnemonic("+", "-");
	  asmCode.updateMnemonic("$", String.format("%d", -value));
	}
  } // applyDisplacement()

  // Constructor
  public Decoder() {
	hashMap.put(new Integer(0x00), new BinaryCode("00", "NOP"));
	hashMap.put(new Integer(0x01), new BinaryCode("01@@@@", "LD   BC,@"));
	hashMap.put(new Integer(0x02), new BinaryCode("02", "LD   (BC),A"));
	hashMap.put(new Integer(0x03), new BinaryCode("03", "INC  BC"));
	hashMap.put(new Integer(0x04), new BinaryCode("04", "INC  B"));
	hashMap.put(new Integer(0x05), new BinaryCode("05", "DEC  B"));
	hashMap.put(new Integer(0x06), new BinaryCode("06##", "LD   B,#"));
	hashMap.put(new Integer(0x07), new BinaryCode("07", "RLCA"));
	hashMap.put(new Integer(0x08), new BinaryCode("08", "EX   AF,AF'"));
	hashMap.put(new Integer(0x09), new BinaryCode("09", "ADD  HL,BC"));
	hashMap.put(new Integer(0x0A), new BinaryCode("0A", "LD   A,(BC)"));
	hashMap.put(new Integer(0x0B), new BinaryCode("0B", "DEC  BC"));
	hashMap.put(new Integer(0x0C), new BinaryCode("0C", "INC  C"));
	hashMap.put(new Integer(0x0D), new BinaryCode("0D", "DEC  C"));
	hashMap.put(new Integer(0x0E), new BinaryCode("0E##", "LD   C,#"));
	hashMap.put(new Integer(0x0F), new BinaryCode("0F", "RRCA"));
	hashMap.put(new Integer(0x10), new BinaryCode("10%%", "DJNZ %"));
	hashMap.put(new Integer(0x11), new BinaryCode("11@@@@", "LD   DE,@"));
	hashMap.put(new Integer(0x12), new BinaryCode("12", "LD   (DE),A"));
	hashMap.put(new Integer(0x13), new BinaryCode("13", "INC  DE"));
	hashMap.put(new Integer(0x14), new BinaryCode("14", "INC  D"));
	hashMap.put(new Integer(0x15), new BinaryCode("15", "DEC  D"));
	hashMap.put(new Integer(0x16), new BinaryCode("16##", "LD   D,#"));
	hashMap.put(new Integer(0x17), new BinaryCode("17", "RLA"));
	hashMap.put(new Integer(0x18), new BinaryCode("18%%", "JR   %"));
	hashMap.put(new Integer(0x19), new BinaryCode("19", "ADD  HL,DE"));
	hashMap.put(new Integer(0x1A), new BinaryCode("1A", "LD   A,(DE)"));
	hashMap.put(new Integer(0x1B), new BinaryCode("1B", "DEC  DE"));
	hashMap.put(new Integer(0x1C), new BinaryCode("1C", "INC  E"));
	hashMap.put(new Integer(0x1D), new BinaryCode("1D", "DEC  E"));
	hashMap.put(new Integer(0x1E), new BinaryCode("1E##", "LD   E,#"));
	hashMap.put(new Integer(0x1F), new BinaryCode("1F", "RRA"));
	hashMap.put(new Integer(0x20), new BinaryCode("20%%", "JR   NZ,%"));
	hashMap.put(new Integer(0x21), new BinaryCode("21@@@@", "LD   HL,@"));
	hashMap.put(new Integer(0x22), new BinaryCode("22@@@@", "LD   (@),HL"));
	hashMap.put(new Integer(0x23), new BinaryCode("23", "INC  HL"));
	hashMap.put(new Integer(0x24), new BinaryCode("24", "INC  H"));
	hashMap.put(new Integer(0x25), new BinaryCode("25", "DEC  H"));
	hashMap.put(new Integer(0x26), new BinaryCode("26##", "LD   H,#"));
	hashMap.put(new Integer(0x27), new BinaryCode("27", "DAA"));
	hashMap.put(new Integer(0x28), new BinaryCode("28%%", "JR   Z,%"));
	hashMap.put(new Integer(0x29), new BinaryCode("29", "ADD  HL,HL"));
	hashMap.put(new Integer(0x2A), new BinaryCode("2A@@@@", "LD   HL,(@)"));
	hashMap.put(new Integer(0x2B), new BinaryCode("2B", "DEC  HL"));
	hashMap.put(new Integer(0x2C), new BinaryCode("2C", "INC  L"));
	hashMap.put(new Integer(0x2D), new BinaryCode("2D", "DEC  L"));
	hashMap.put(new Integer(0x2E), new BinaryCode("2E##", "LD   L,#"));
	hashMap.put(new Integer(0x2F), new BinaryCode("2F", "CPL"));
	hashMap.put(new Integer(0x30), new BinaryCode("30%%", "JR   NC,%"));
	hashMap.put(new Integer(0x31), new BinaryCode("31@@@@", "LD   SP,@"));
	hashMap.put(new Integer(0x32), new BinaryCode("32@@@@", "LD   (@),A"));
	hashMap.put(new Integer(0x33), new BinaryCode("33", "INC  SP"));
	hashMap.put(new Integer(0x34), new BinaryCode("34", "INC  (HL)"));
	hashMap.put(new Integer(0x35), new BinaryCode("35", "DEC  (HL)"));
	hashMap.put(new Integer(0x36), new BinaryCode("36##", "LD   (HL),#"));
	hashMap.put(new Integer(0x37), new BinaryCode("37", "SCF"));
	hashMap.put(new Integer(0x38), new BinaryCode("38%%", "JR   C,%"));
	hashMap.put(new Integer(0x39), new BinaryCode("39", "ADD  HL,SP"));
	hashMap.put(new Integer(0x3A), new BinaryCode("3A@@@@", "LD   A,(@)"));
	hashMap.put(new Integer(0x3B), new BinaryCode("3B", "DEC  SP"));
	hashMap.put(new Integer(0x3C), new BinaryCode("3C", "INC  A"));
	hashMap.put(new Integer(0x3D), new BinaryCode("3D", "DEC  A"));
	hashMap.put(new Integer(0x3E), new BinaryCode("3E##", "LD   A,#"));
	hashMap.put(new Integer(0x3F), new BinaryCode("3F", "CCF"));
	hashMap.put(new Integer(0x40), new BinaryCode("40", "LD   B,B"));
	hashMap.put(new Integer(0x41), new BinaryCode("41", "LD   B,C"));
	hashMap.put(new Integer(0x42), new BinaryCode("42", "LD   B,D"));
	hashMap.put(new Integer(0x43), new BinaryCode("43", "LD   B,E"));
	hashMap.put(new Integer(0x44), new BinaryCode("44", "LD   B,H"));
	hashMap.put(new Integer(0x45), new BinaryCode("45", "LD   B,L"));
	hashMap.put(new Integer(0x46), new BinaryCode("46", "LD   B,(HL)"));
	hashMap.put(new Integer(0x47), new BinaryCode("47", "LD   B,A"));
	hashMap.put(new Integer(0x48), new BinaryCode("48", "LD   C,B"));
	hashMap.put(new Integer(0x49), new BinaryCode("49", "LD   C,C"));
	hashMap.put(new Integer(0x4A), new BinaryCode("4A", "LD   C,D"));
	hashMap.put(new Integer(0x4B), new BinaryCode("4B", "LD   C,E"));
	hashMap.put(new Integer(0x4C), new BinaryCode("4C", "LD   C,H"));
	hashMap.put(new Integer(0x4D), new BinaryCode("4D", "LD   C,L"));
	hashMap.put(new Integer(0x4E), new BinaryCode("4E", "LD   C,(HL)"));
	hashMap.put(new Integer(0x4F), new BinaryCode("4F", "LD   C,A"));
	hashMap.put(new Integer(0x50), new BinaryCode("50", "LD   D,B"));
	hashMap.put(new Integer(0x51), new BinaryCode("51", "LD   D,C"));
	hashMap.put(new Integer(0x52), new BinaryCode("52", "LD   D,D"));
	hashMap.put(new Integer(0x53), new BinaryCode("53", "LD   D,E"));
	hashMap.put(new Integer(0x54), new BinaryCode("54", "LD   D,H"));
	hashMap.put(new Integer(0x55), new BinaryCode("55", "LD   D,L"));
	hashMap.put(new Integer(0x56), new BinaryCode("56", "LD   D,(HL)"));
	hashMap.put(new Integer(0x57), new BinaryCode("57", "LD   D,A"));
	hashMap.put(new Integer(0x58), new BinaryCode("58", "LD   E,B"));
	hashMap.put(new Integer(0x59), new BinaryCode("59", "LD   E,C"));
	hashMap.put(new Integer(0x5A), new BinaryCode("5A", "LD   E,D"));
	hashMap.put(new Integer(0x5B), new BinaryCode("5B", "LD   E,E"));
	hashMap.put(new Integer(0x5C), new BinaryCode("5C", "LD   E,H"));
	hashMap.put(new Integer(0x5D), new BinaryCode("5D", "LD   E,L"));
	hashMap.put(new Integer(0x5E), new BinaryCode("5E", "LD   E,(HL)"));
	hashMap.put(new Integer(0x5F), new BinaryCode("5F", "LD   E,A"));
	hashMap.put(new Integer(0x60), new BinaryCode("60", "LD   H,B"));
	hashMap.put(new Integer(0x61), new BinaryCode("61", "LD   H,C"));
	hashMap.put(new Integer(0x62), new BinaryCode("62", "LD   H,D"));
	hashMap.put(new Integer(0x63), new BinaryCode("63", "LD   H,E"));
	hashMap.put(new Integer(0x64), new BinaryCode("64", "LD   H,H"));
	hashMap.put(new Integer(0x65), new BinaryCode("65", "LD   H,L"));
	hashMap.put(new Integer(0x66), new BinaryCode("66", "LD   H,(HL)"));
	hashMap.put(new Integer(0x67), new BinaryCode("67", "LD   H,A"));
	hashMap.put(new Integer(0x68), new BinaryCode("68", "LD   L,B"));
	hashMap.put(new Integer(0x69), new BinaryCode("69", "LD   L,C"));
	hashMap.put(new Integer(0x6A), new BinaryCode("6A", "LD   L,D"));
	hashMap.put(new Integer(0x6B), new BinaryCode("6B", "LD   L,E"));
	hashMap.put(new Integer(0x6C), new BinaryCode("6C", "LD   L,H"));
	hashMap.put(new Integer(0x6D), new BinaryCode("6D", "LD   L,L"));
	hashMap.put(new Integer(0x6E), new BinaryCode("6E", "LD   L,(HL)"));
	hashMap.put(new Integer(0x6F), new BinaryCode("6F", "LD   L,A"));
	hashMap.put(new Integer(0x70), new BinaryCode("70", "LD   (HL),B"));
	hashMap.put(new Integer(0x71), new BinaryCode("71", "LD   (HL),C"));
	hashMap.put(new Integer(0x72), new BinaryCode("72", "LD   (HL),D"));
	hashMap.put(new Integer(0x73), new BinaryCode("73", "LD   (HL),E"));
	hashMap.put(new Integer(0x74), new BinaryCode("74", "LD   (HL),H"));
	hashMap.put(new Integer(0x75), new BinaryCode("75", "LD   (HL),L"));
	hashMap.put(new Integer(0x76), new BinaryCode("76", "HALT"));
	hashMap.put(new Integer(0x77), new BinaryCode("77", "LD   (HL),A"));
	hashMap.put(new Integer(0x78), new BinaryCode("78", "LD   A,B"));
	hashMap.put(new Integer(0x79), new BinaryCode("79", "LD   A,C"));
	hashMap.put(new Integer(0x7A), new BinaryCode("7A", "LD   A,D"));
	hashMap.put(new Integer(0x7B), new BinaryCode("7B", "LD   A,E"));
	hashMap.put(new Integer(0x7C), new BinaryCode("7C", "LD   A,H"));
	hashMap.put(new Integer(0x7D), new BinaryCode("7D", "LD   A,L"));
	hashMap.put(new Integer(0x7E), new BinaryCode("7E", "LD   A,(HL)"));
	hashMap.put(new Integer(0x7F), new BinaryCode("7F", "LD   A,A"));
	hashMap.put(new Integer(0x80), new BinaryCode("80", "ADD  A,B"));
	hashMap.put(new Integer(0x81), new BinaryCode("81", "ADD  A,C"));
	hashMap.put(new Integer(0x82), new BinaryCode("82", "ADD  A,D"));
	hashMap.put(new Integer(0x83), new BinaryCode("83", "ADD  A,E"));
	hashMap.put(new Integer(0x84), new BinaryCode("84", "ADD  A,H"));
	hashMap.put(new Integer(0x85), new BinaryCode("85", "ADD  A,L"));
	hashMap.put(new Integer(0x86), new BinaryCode("86", "ADD  A,(HL)"));
	hashMap.put(new Integer(0x87), new BinaryCode("87", "ADD  A,A"));
	hashMap.put(new Integer(0x88), new BinaryCode("88", "ADC  A,B"));
	hashMap.put(new Integer(0x89), new BinaryCode("89", "ADC  A,C"));
	hashMap.put(new Integer(0x8A), new BinaryCode("8A", "ADC  A,D"));
	hashMap.put(new Integer(0x8B), new BinaryCode("8B", "ADC  A,E"));
	hashMap.put(new Integer(0x8C), new BinaryCode("8C", "ADC  A,H"));
	hashMap.put(new Integer(0x8D), new BinaryCode("8D", "ADC  A,L"));
	hashMap.put(new Integer(0x8E), new BinaryCode("8E", "ADC  A,(HL)"));
	hashMap.put(new Integer(0x8F), new BinaryCode("8F", "ADC  A,A"));
	hashMap.put(new Integer(0x90), new BinaryCode("90", "SUB  A,B"));
	hashMap.put(new Integer(0x91), new BinaryCode("91", "SUB  A,C"));
	hashMap.put(new Integer(0x92), new BinaryCode("92", "SUB  A,D"));
	hashMap.put(new Integer(0x93), new BinaryCode("93", "SUB  A,E"));
	hashMap.put(new Integer(0x94), new BinaryCode("94", "SUB  A,H"));
	hashMap.put(new Integer(0x95), new BinaryCode("95", "SUB  A,L"));
	hashMap.put(new Integer(0x96), new BinaryCode("96", "SUB  A,(HL)"));
	hashMap.put(new Integer(0x97), new BinaryCode("97", "SUB  A,A"));
	hashMap.put(new Integer(0x98), new BinaryCode("98", "SBC  A,B"));
	hashMap.put(new Integer(0x99), new BinaryCode("99", "SBC  A,C"));
	hashMap.put(new Integer(0x9A), new BinaryCode("9A", "SBC  A,D"));
	hashMap.put(new Integer(0x9B), new BinaryCode("9B", "SBC  A,E"));
	hashMap.put(new Integer(0x9C), new BinaryCode("9C", "SBC  A,H"));
	hashMap.put(new Integer(0x9D), new BinaryCode("9D", "SBC  A,L"));
	hashMap.put(new Integer(0x9E), new BinaryCode("9E", "SBC  A,(HL)"));
	hashMap.put(new Integer(0x9F), new BinaryCode("9F", "SBC  A,A"));
	hashMap.put(new Integer(0xA0), new BinaryCode("A0", "AND  B"));
	hashMap.put(new Integer(0xA1), new BinaryCode("A1", "AND  C"));
	hashMap.put(new Integer(0xA2), new BinaryCode("A2", "AND  D"));
	hashMap.put(new Integer(0xA3), new BinaryCode("A3", "AND  E"));
	hashMap.put(new Integer(0xA4), new BinaryCode("A4", "AND  H"));
	hashMap.put(new Integer(0xA5), new BinaryCode("A5", "AND  L"));
	hashMap.put(new Integer(0xA6), new BinaryCode("A6", "AND  (HL)"));
	hashMap.put(new Integer(0xA7), new BinaryCode("A7", "AND  A"));
	hashMap.put(new Integer(0xA8), new BinaryCode("A8", "XOR  B"));
	hashMap.put(new Integer(0xA9), new BinaryCode("A9", "XOR  C"));
	hashMap.put(new Integer(0xAA), new BinaryCode("AA", "XOR  D"));
	hashMap.put(new Integer(0xAB), new BinaryCode("AB", "XOR  E"));
	hashMap.put(new Integer(0xAC), new BinaryCode("AC", "XOR  H"));
	hashMap.put(new Integer(0xAD), new BinaryCode("AD", "XOR  L"));
	hashMap.put(new Integer(0xAE), new BinaryCode("AE", "XOR  (HL)"));
	hashMap.put(new Integer(0xAF), new BinaryCode("AF", "XOR  A"));
	hashMap.put(new Integer(0xB0), new BinaryCode("B0", "OR   B"));
	hashMap.put(new Integer(0xB1), new BinaryCode("B1", "OR   C"));
	hashMap.put(new Integer(0xB2), new BinaryCode("B2", "OR   D"));
	hashMap.put(new Integer(0xB3), new BinaryCode("B3", "OR   E"));
	hashMap.put(new Integer(0xB4), new BinaryCode("B4", "OR   H"));
	hashMap.put(new Integer(0xB5), new BinaryCode("B5", "OR   L"));
	hashMap.put(new Integer(0xB6), new BinaryCode("B6", "OR   (HL)"));
	hashMap.put(new Integer(0xB7), new BinaryCode("B7", "OR   A"));
	hashMap.put(new Integer(0xB8), new BinaryCode("B8", "CP   B"));
	hashMap.put(new Integer(0xB9), new BinaryCode("B9", "CP   C"));
	hashMap.put(new Integer(0xBA), new BinaryCode("BA", "CP   D"));
	hashMap.put(new Integer(0xBB), new BinaryCode("BB", "CP   E"));
	hashMap.put(new Integer(0xBC), new BinaryCode("BC", "CP   H"));
	hashMap.put(new Integer(0xBD), new BinaryCode("BD", "CP   L"));
	hashMap.put(new Integer(0xBE), new BinaryCode("BE", "CP   (HL)"));
	hashMap.put(new Integer(0xBF), new BinaryCode("BF", "CP   A"));
	hashMap.put(new Integer(0xC0), new BinaryCode("C0", "RET  NZ"));
	hashMap.put(new Integer(0xC1), new BinaryCode("C1", "POP  BC"));
	hashMap.put(new Integer(0xC2), new BinaryCode("C2!!!!", "JP   NZ,!"));
	hashMap.put(new Integer(0xC3), new BinaryCode("C3!!!!", "JP   !"));
	hashMap.put(new Integer(0xC4), new BinaryCode("C4!!!!", "CALL NZ,!"));
	hashMap.put(new Integer(0xC5), new BinaryCode("C5", "PUSH BC"));
	hashMap.put(new Integer(0xC6), new BinaryCode("C6##", "ADD  A,#"));
	hashMap.put(new Integer(0xC7), new BinaryCode("C7", "RST  *"));
	hashMap.put(new Integer(0xC8), new BinaryCode("C8", "RET  Z"));
	hashMap.put(new Integer(0xC9), new BinaryCode("C9", "RET"));
	hashMap.put(new Integer(0xCA), new BinaryCode("CA!!!!", "JP   Z,!"));
	hashMap.put(new Integer(0xCB), new BinaryCode("CB", "<next>"));
	hashMap.put(new Integer(0xCC), new BinaryCode("CC!!!!", "CALL Z,!"));
	hashMap.put(new Integer(0xCD), new BinaryCode("CD!!!!", "CALL !"));
	hashMap.put(new Integer(0xCE), new BinaryCode("CE##", "ADC  A,#"));
	hashMap.put(new Integer(0xCF), new BinaryCode("CF", "RST  *"));
	hashMap.put(new Integer(0xD0), new BinaryCode("D0", "RET  NC"));
	hashMap.put(new Integer(0xD1), new BinaryCode("D1", "POP  DE"));
	hashMap.put(new Integer(0xD2), new BinaryCode("D2!!!!", "JP   NC,!"));
	hashMap.put(new Integer(0xD3), new BinaryCode("D3&&", "OUT  (&),A"));
	hashMap.put(new Integer(0xD4), new BinaryCode("D4!!!!", "CALL NC,!"));
	hashMap.put(new Integer(0xD5), new BinaryCode("D5", "PUSH DE"));
	hashMap.put(new Integer(0xD6), new BinaryCode("D6##", "SUB  #"));
	hashMap.put(new Integer(0xD7), new BinaryCode("D7", "RST  *"));
	hashMap.put(new Integer(0xD8), new BinaryCode("D8", "RET  C"));
	hashMap.put(new Integer(0xD9), new BinaryCode("D9", "EXX"));
	hashMap.put(new Integer(0xDA), new BinaryCode("DA!!!!", "JP   C,!"));
	hashMap.put(new Integer(0xDB), new BinaryCode("DB&&", "IN   A,(&)"));
	hashMap.put(new Integer(0xDC), new BinaryCode("DC!!!!", "CALL C,!"));
	hashMap.put(new Integer(0xDD), new BinaryCode("DD", "<next>"));
	hashMap.put(new Integer(0xDE), new BinaryCode("DE##", "SBC  A,#"));
	hashMap.put(new Integer(0xDF), new BinaryCode("DF", "RST  *"));
	hashMap.put(new Integer(0xE0), new BinaryCode("E0", "RET  PO"));
	hashMap.put(new Integer(0xE1), new BinaryCode("E1", "POP  HL"));
	hashMap.put(new Integer(0xE2), new BinaryCode("E2!!!!", "JP   PO,!"));
	hashMap.put(new Integer(0xE3), new BinaryCode("E3", "EX   (SP),HL"));
	hashMap.put(new Integer(0xE4), new BinaryCode("E4!!!!", "CALL PO,!"));
	hashMap.put(new Integer(0xE5), new BinaryCode("E5", "PUSH HL"));
	hashMap.put(new Integer(0xE6), new BinaryCode("E6##", "AND  #"));
	hashMap.put(new Integer(0xE7), new BinaryCode("E7", "RST  *"));
	hashMap.put(new Integer(0xE8), new BinaryCode("E8", "RET  PE"));
	hashMap.put(new Integer(0xE9), new BinaryCode("E9", "JP   (HL)"));
	hashMap.put(new Integer(0xEA), new BinaryCode("EA!!!!", "JP   PE,!"));
	hashMap.put(new Integer(0xEB), new BinaryCode("EB", "EX   DE,HL"));
	hashMap.put(new Integer(0xEC), new BinaryCode("EC!!!!", "CALL PE,!"));
	hashMap.put(new Integer(0xED), new BinaryCode("ED", "<next>"));
	hashMap.put(new Integer(0xEE), new BinaryCode("EE##", "XOR  #"));
	hashMap.put(new Integer(0xEF), new BinaryCode("EF", "RST  *"));
	hashMap.put(new Integer(0xF0), new BinaryCode("F0", "RET  P"));
	hashMap.put(new Integer(0xF1), new BinaryCode("F1", "POP  AF"));
	hashMap.put(new Integer(0xF2), new BinaryCode("F2!!!!", "JP   P,!"));
	hashMap.put(new Integer(0xF3), new BinaryCode("F3", "DI"));
	hashMap.put(new Integer(0xF4), new BinaryCode("F4!!!!", "CALL P,!"));
	hashMap.put(new Integer(0xF5), new BinaryCode("F5", "PUSH AF"));
	hashMap.put(new Integer(0xF6), new BinaryCode("F6##", "OR   #"));
	hashMap.put(new Integer(0xF7), new BinaryCode("F7", "RST  *"));
	hashMap.put(new Integer(0xF8), new BinaryCode("F8", "RET  M"));
	hashMap.put(new Integer(0xF9), new BinaryCode("F9", "LD   SP,HL"));
	hashMap.put(new Integer(0xFA), new BinaryCode("FA!!!!", "JP   M,!"));
	hashMap.put(new Integer(0xFB), new BinaryCode("FB", "EI"));
	hashMap.put(new Integer(0xFC), new BinaryCode("FC!!!!", "CALL M,!"));
	hashMap.put(new Integer(0xFD), new BinaryCode("FD", "<next>"));
	hashMap.put(new Integer(0xFE), new BinaryCode("FE##", "CP   #"));
	hashMap.put(new Integer(0xFF), new BinaryCode("FF", "RST  *"));
	hashMap.put(new Integer(0xCB00), new BinaryCode("CB00", "RLC  B"));
	hashMap.put(new Integer(0xCB01), new BinaryCode("CB01", "RLC  C"));
	hashMap.put(new Integer(0xCB02), new BinaryCode("CB02", "RLC  D"));
	hashMap.put(new Integer(0xCB03), new BinaryCode("CB03", "RLC  E"));
	hashMap.put(new Integer(0xCB04), new BinaryCode("CB04", "RLC  H"));
	hashMap.put(new Integer(0xCB05), new BinaryCode("CB05", "RLC  L"));
	hashMap.put(new Integer(0xCB06), new BinaryCode("CB06", "RLC  (HL)"));
	hashMap.put(new Integer(0xCB07), new BinaryCode("CB07", "RLC  A"));
	hashMap.put(new Integer(0xCB08), new BinaryCode("CB08", "RRC  B"));
	hashMap.put(new Integer(0xCB09), new BinaryCode("CB09", "RRC  C"));
	hashMap.put(new Integer(0xCB0A), new BinaryCode("CB0A", "RRC  D"));
	hashMap.put(new Integer(0xCB0B), new BinaryCode("CB0B", "RRC  E"));
	hashMap.put(new Integer(0xCB0C), new BinaryCode("CB0C", "RRC  H"));
	hashMap.put(new Integer(0xCB0D), new BinaryCode("CB0D", "RRC  L"));
	hashMap.put(new Integer(0xCB0E), new BinaryCode("CB0E", "RRC  (HL)"));
	hashMap.put(new Integer(0xCB0F), new BinaryCode("CB0F", "RRC  A"));
	hashMap.put(new Integer(0xCB10), new BinaryCode("CB10", "RL   B"));
	hashMap.put(new Integer(0xCB11), new BinaryCode("CB11", "RL   C"));
	hashMap.put(new Integer(0xCB12), new BinaryCode("CB12", "RL   D"));
	hashMap.put(new Integer(0xCB13), new BinaryCode("CB13", "RL   E"));
	hashMap.put(new Integer(0xCB14), new BinaryCode("CB14", "RL   H"));
	hashMap.put(new Integer(0xCB15), new BinaryCode("CB15", "RL   L"));
	hashMap.put(new Integer(0xCB16), new BinaryCode("CB16", "RL   (HL)"));
	hashMap.put(new Integer(0xCB17), new BinaryCode("CB17", "RL   A"));
	hashMap.put(new Integer(0xCB18), new BinaryCode("CB18", "RR   B"));
	hashMap.put(new Integer(0xCB19), new BinaryCode("CB19", "RR   C"));
	hashMap.put(new Integer(0xCB1A), new BinaryCode("CB1A", "RR   D"));
	hashMap.put(new Integer(0xCB1B), new BinaryCode("CB1B", "RR   E"));
	hashMap.put(new Integer(0xCB1C), new BinaryCode("CB1C", "RR   H"));
	hashMap.put(new Integer(0xCB1D), new BinaryCode("CB1D", "RR   L"));
	hashMap.put(new Integer(0xCB1E), new BinaryCode("CB1E", "RR   (HL)"));
	hashMap.put(new Integer(0xCB1F), new BinaryCode("CB1F", "RR   A"));
	hashMap.put(new Integer(0xCB20), new BinaryCode("CB20", "SLA  B"));
	hashMap.put(new Integer(0xCB21), new BinaryCode("CB21", "SLA  C"));
	hashMap.put(new Integer(0xCB22), new BinaryCode("CB22", "SLA  D"));
	hashMap.put(new Integer(0xCB23), new BinaryCode("CB23", "SLA  E"));
	hashMap.put(new Integer(0xCB24), new BinaryCode("CB24", "SLA  H"));
	hashMap.put(new Integer(0xCB25), new BinaryCode("CB25", "SLA  L"));
	hashMap.put(new Integer(0xCB26), new BinaryCode("CB26", "SLA  (HL)"));
	hashMap.put(new Integer(0xCB27), new BinaryCode("CB27", "SLA  A"));
	hashMap.put(new Integer(0xCB28), new BinaryCode("CB28", "SRA  B"));
	hashMap.put(new Integer(0xCB29), new BinaryCode("CB29", "SRA  C"));
	hashMap.put(new Integer(0xCB2A), new BinaryCode("CB2A", "SRA  D"));
	hashMap.put(new Integer(0xCB2B), new BinaryCode("CB2B", "SRA  E"));
	hashMap.put(new Integer(0xCB2C), new BinaryCode("CB2C", "SRA  H"));
	hashMap.put(new Integer(0xCB2D), new BinaryCode("CB2D", "SRA  L"));
	hashMap.put(new Integer(0xCB2E), new BinaryCode("CB2E", "SRA  (HL)"));
	hashMap.put(new Integer(0xCB2F), new BinaryCode("CB2F", "SRA  A"));
	hashMap.put(new Integer(0xCB30), new BinaryCode("CB30", "SLL  B"));
	hashMap.put(new Integer(0xCB31), new BinaryCode("CB31", "SLL  C"));
	hashMap.put(new Integer(0xCB32), new BinaryCode("CB32", "SLL  D"));
	hashMap.put(new Integer(0xCB33), new BinaryCode("CB33", "SLL  E"));
	hashMap.put(new Integer(0xCB34), new BinaryCode("CB34", "SLL  H"));
	hashMap.put(new Integer(0xCB35), new BinaryCode("CB35", "SLL  L"));
	hashMap.put(new Integer(0xCB36), new BinaryCode("CB36", "SLL  (HL)"));
	hashMap.put(new Integer(0xCB37), new BinaryCode("CB37", "SLL  A"));
	hashMap.put(new Integer(0xCB38), new BinaryCode("CB38", "SRL  B"));
	hashMap.put(new Integer(0xCB39), new BinaryCode("CB39", "SRL  C"));
	hashMap.put(new Integer(0xCB3A), new BinaryCode("CB3A", "SRL  D"));
	hashMap.put(new Integer(0xCB3B), new BinaryCode("CB3B", "SRL  E"));
	hashMap.put(new Integer(0xCB3C), new BinaryCode("CB3C", "SRL  H"));
	hashMap.put(new Integer(0xCB3D), new BinaryCode("CB3D", "SRL  L"));
	hashMap.put(new Integer(0xCB3E), new BinaryCode("CB3E", "SRL  (HL)"));
	hashMap.put(new Integer(0xCB3F), new BinaryCode("CB3F", "SRL  A"));
	hashMap.put(new Integer(0xCB40), new BinaryCode("CB40", "BIT  0,B"));
	hashMap.put(new Integer(0xCB41), new BinaryCode("CB41", "BIT  0,C"));
	hashMap.put(new Integer(0xCB42), new BinaryCode("CB42", "BIT  0,D"));
	hashMap.put(new Integer(0xCB43), new BinaryCode("CB43", "BIT  0,E"));
	hashMap.put(new Integer(0xCB44), new BinaryCode("CB44", "BIT  0,H"));
	hashMap.put(new Integer(0xCB45), new BinaryCode("CB45", "BIT  0,L"));
	hashMap.put(new Integer(0xCB46), new BinaryCode("CB46", "BIT  0,(HL)"));
	hashMap.put(new Integer(0xCB47), new BinaryCode("CB47", "BIT  0,A"));
	hashMap.put(new Integer(0xCB48), new BinaryCode("CB48", "BIT  1,B"));
	hashMap.put(new Integer(0xCB49), new BinaryCode("CB49", "BIT  1,C"));
	hashMap.put(new Integer(0xCB4A), new BinaryCode("CB4A", "BIT  1,D"));
	hashMap.put(new Integer(0xCB4B), new BinaryCode("CB4B", "BIT  1,E"));
	hashMap.put(new Integer(0xCB4C), new BinaryCode("CB4C", "BIT  1,H"));
	hashMap.put(new Integer(0xCB4D), new BinaryCode("CB4D", "BIT  1,L"));
	hashMap.put(new Integer(0xCB4E), new BinaryCode("CB4E", "BIT  1,(HL)"));
	hashMap.put(new Integer(0xCB4F), new BinaryCode("CB4F", "BIT  1,A"));
	hashMap.put(new Integer(0xCB50), new BinaryCode("CB50", "BIT  2,B"));
	hashMap.put(new Integer(0xCB51), new BinaryCode("CB51", "BIT  2,C"));
	hashMap.put(new Integer(0xCB52), new BinaryCode("CB52", "BIT  2,D"));
	hashMap.put(new Integer(0xCB53), new BinaryCode("CB53", "BIT  2,E"));
	hashMap.put(new Integer(0xCB54), new BinaryCode("CB54", "BIT  2,H"));
	hashMap.put(new Integer(0xCB55), new BinaryCode("CB55", "BIT  2,L"));
	hashMap.put(new Integer(0xCB56), new BinaryCode("CB56", "BIT  2,(HL)"));
	hashMap.put(new Integer(0xCB57), new BinaryCode("CB57", "BIT  2,A"));
	hashMap.put(new Integer(0xCB58), new BinaryCode("CB58", "BIT  3,B"));
	hashMap.put(new Integer(0xCB59), new BinaryCode("CB59", "BIT  3,C"));
	hashMap.put(new Integer(0xCB5A), new BinaryCode("CB5A", "BIT  3,D"));
	hashMap.put(new Integer(0xCB5B), new BinaryCode("CB5B", "BIT  3,E"));
	hashMap.put(new Integer(0xCB5C), new BinaryCode("CB5C", "BIT  3,H"));
	hashMap.put(new Integer(0xCB5D), new BinaryCode("CB5D", "BIT  3,L"));
	hashMap.put(new Integer(0xCB5E), new BinaryCode("CB5E", "BIT  3,(HL)"));
	hashMap.put(new Integer(0xCB5F), new BinaryCode("CB5F", "BIT  3,A"));
	hashMap.put(new Integer(0xCB60), new BinaryCode("CB60", "BIT  4,B"));
	hashMap.put(new Integer(0xCB61), new BinaryCode("CB61", "BIT  4,C"));
	hashMap.put(new Integer(0xCB62), new BinaryCode("CB62", "BIT  4,D"));
	hashMap.put(new Integer(0xCB63), new BinaryCode("CB63", "BIT  4,E"));
	hashMap.put(new Integer(0xCB64), new BinaryCode("CB64", "BIT  4,H"));
	hashMap.put(new Integer(0xCB65), new BinaryCode("CB65", "BIT  4,L"));
	hashMap.put(new Integer(0xCB66), new BinaryCode("CB66", "BIT  4,(HL)"));
	hashMap.put(new Integer(0xCB67), new BinaryCode("CB67", "BIT  4,A"));
	hashMap.put(new Integer(0xCB68), new BinaryCode("CB68", "BIT  5,B"));
	hashMap.put(new Integer(0xCB69), new BinaryCode("CB69", "BIT  5,C"));
	hashMap.put(new Integer(0xCB6A), new BinaryCode("CB6A", "BIT  5,D"));
	hashMap.put(new Integer(0xCB6B), new BinaryCode("CB6B", "BIT  5,E"));
	hashMap.put(new Integer(0xCB6C), new BinaryCode("CB6C", "BIT  5,H"));
	hashMap.put(new Integer(0xCB6D), new BinaryCode("CB6D", "BIT  5,L"));
	hashMap.put(new Integer(0xCB6E), new BinaryCode("CB6E", "BIT  5,(HL)"));
	hashMap.put(new Integer(0xCB6F), new BinaryCode("CB6F", "BIT  5,A"));
	hashMap.put(new Integer(0xCB70), new BinaryCode("CB70", "BIT  6,B"));
	hashMap.put(new Integer(0xCB71), new BinaryCode("CB71", "BIT  6,C"));
	hashMap.put(new Integer(0xCB72), new BinaryCode("CB72", "BIT  6,D"));
	hashMap.put(new Integer(0xCB73), new BinaryCode("CB73", "BIT  6,E"));
	hashMap.put(new Integer(0xCB74), new BinaryCode("CB74", "BIT  6,H"));
	hashMap.put(new Integer(0xCB75), new BinaryCode("CB75", "BIT  6,L"));
	hashMap.put(new Integer(0xCB76), new BinaryCode("CB76", "BIT  6,(HL)"));
	hashMap.put(new Integer(0xCB77), new BinaryCode("CB77", "BIT  6,A"));
	hashMap.put(new Integer(0xCB78), new BinaryCode("CB78", "BIT  7,B"));
	hashMap.put(new Integer(0xCB79), new BinaryCode("CB79", "BIT  7,C"));
	hashMap.put(new Integer(0xCB7A), new BinaryCode("CB7A", "BIT  7,D"));
	hashMap.put(new Integer(0xCB7B), new BinaryCode("CB7B", "BIT  7,E"));
	hashMap.put(new Integer(0xCB7C), new BinaryCode("CB7C", "BIT  7,H"));
	hashMap.put(new Integer(0xCB7D), new BinaryCode("CB7D", "BIT  7,L"));
	hashMap.put(new Integer(0xCB7E), new BinaryCode("CB7E", "BIT  7,(HL)"));
	hashMap.put(new Integer(0xCB7F), new BinaryCode("CB7F", "BIT  7,A"));
	hashMap.put(new Integer(0xCB80), new BinaryCode("CB80", "RES  0,B"));
	hashMap.put(new Integer(0xCB81), new BinaryCode("CB81", "RES  0,C"));
	hashMap.put(new Integer(0xCB82), new BinaryCode("CB82", "RES  0,D"));
	hashMap.put(new Integer(0xCB83), new BinaryCode("CB83", "RES  0,E"));
	hashMap.put(new Integer(0xCB84), new BinaryCode("CB84", "RES  0,H"));
	hashMap.put(new Integer(0xCB85), new BinaryCode("CB85", "RES  0,L"));
	hashMap.put(new Integer(0xCB86), new BinaryCode("CB86", "RES  0,(HL)"));
	hashMap.put(new Integer(0xCB87), new BinaryCode("CB87", "RES  0,A"));
	hashMap.put(new Integer(0xCB88), new BinaryCode("CB88", "RES  1,B"));
	hashMap.put(new Integer(0xCB89), new BinaryCode("CB89", "RES  1,C"));
	hashMap.put(new Integer(0xCB8A), new BinaryCode("CB8A", "RES  1,D"));
	hashMap.put(new Integer(0xCB8B), new BinaryCode("CB8B", "RES  1,E"));
	hashMap.put(new Integer(0xCB8C), new BinaryCode("CB8C", "RES  1,H"));
	hashMap.put(new Integer(0xCB8D), new BinaryCode("CB8D", "RES  1,L"));
	hashMap.put(new Integer(0xCB8E), new BinaryCode("CB8E", "RES  1,(HL)"));
	hashMap.put(new Integer(0xCB8F), new BinaryCode("CB8F", "RES  1,A"));
	hashMap.put(new Integer(0xCB90), new BinaryCode("CB90", "RES  2,B"));
	hashMap.put(new Integer(0xCB91), new BinaryCode("CB91", "RES  2,C"));
	hashMap.put(new Integer(0xCB92), new BinaryCode("CB92", "RES  2,D"));
	hashMap.put(new Integer(0xCB93), new BinaryCode("CB93", "RES  2,E"));
	hashMap.put(new Integer(0xCB94), new BinaryCode("CB94", "RES  2,H"));
	hashMap.put(new Integer(0xCB95), new BinaryCode("CB95", "RES  2,L"));
	hashMap.put(new Integer(0xCB96), new BinaryCode("CB96", "RES  2,(HL)"));
	hashMap.put(new Integer(0xCB97), new BinaryCode("CB97", "RES  2,A"));
	hashMap.put(new Integer(0xCB98), new BinaryCode("CB98", "RES  3,B"));
	hashMap.put(new Integer(0xCB99), new BinaryCode("CB99", "RES  3,C"));
	hashMap.put(new Integer(0xCB9A), new BinaryCode("CB9A", "RES  3,D"));
	hashMap.put(new Integer(0xCB9B), new BinaryCode("CB9B", "RES  3,E"));
	hashMap.put(new Integer(0xCB9C), new BinaryCode("CB9C", "RES  3,H"));
	hashMap.put(new Integer(0xCB9D), new BinaryCode("CB9D", "RES  3,L"));
	hashMap.put(new Integer(0xCB9E), new BinaryCode("CB9E", "RES  3,(HL)"));
	hashMap.put(new Integer(0xCB9F), new BinaryCode("CB9F", "RES  3,A"));
	hashMap.put(new Integer(0xCBA0), new BinaryCode("CBA0", "RES  4,B"));
	hashMap.put(new Integer(0xCBA1), new BinaryCode("CBA1", "RES  4,C"));
	hashMap.put(new Integer(0xCBA2), new BinaryCode("CBA2", "RES  4,D"));
	hashMap.put(new Integer(0xCBA3), new BinaryCode("CBA3", "RES  4,E"));
	hashMap.put(new Integer(0xCBA4), new BinaryCode("CBA4", "RES  4,H"));
	hashMap.put(new Integer(0xCBA5), new BinaryCode("CBA5", "RES  4,L"));
	hashMap.put(new Integer(0xCBA6), new BinaryCode("CBA6", "RES  4,(HL)"));
	hashMap.put(new Integer(0xCBA7), new BinaryCode("CBA7", "RES  4,A"));
	hashMap.put(new Integer(0xCBA8), new BinaryCode("CBA8", "RES  5,B"));
	hashMap.put(new Integer(0xCBA9), new BinaryCode("CBA9", "RES  5,C"));
	hashMap.put(new Integer(0xCBAA), new BinaryCode("CBAA", "RES  5,D"));
	hashMap.put(new Integer(0xCBAB), new BinaryCode("CBAB", "RES  5,E"));
	hashMap.put(new Integer(0xCBAC), new BinaryCode("CBAC", "RES  5,H"));
	hashMap.put(new Integer(0xCBAD), new BinaryCode("CBAD", "RES  5,L"));
	hashMap.put(new Integer(0xCBAE), new BinaryCode("CBAE", "RES  5,(HL)"));
	hashMap.put(new Integer(0xCBAF), new BinaryCode("CBAF", "RES  5,A"));
	hashMap.put(new Integer(0xCBB0), new BinaryCode("CBB0", "RES  6,B"));
	hashMap.put(new Integer(0xCBB1), new BinaryCode("CBB1", "RES  6,C"));
	hashMap.put(new Integer(0xCBB2), new BinaryCode("CBB2", "RES  6,D"));
	hashMap.put(new Integer(0xCBB3), new BinaryCode("CBB3", "RES  6,E"));
	hashMap.put(new Integer(0xCBB4), new BinaryCode("CBB4", "RES  6,H"));
	hashMap.put(new Integer(0xCBB5), new BinaryCode("CBB5", "RES  6,L"));
	hashMap.put(new Integer(0xCBB6), new BinaryCode("CBB6", "RES  6,(HL)"));
	hashMap.put(new Integer(0xCBB7), new BinaryCode("CBB7", "RES  6,A"));
	hashMap.put(new Integer(0xCBB8), new BinaryCode("CBB8", "RES  7,B"));
	hashMap.put(new Integer(0xCBB9), new BinaryCode("CBB9", "RES  7,C"));
	hashMap.put(new Integer(0xCBBA), new BinaryCode("CBBA", "RES  7,D"));
	hashMap.put(new Integer(0xCBBB), new BinaryCode("CBBB", "RES  7,E"));
	hashMap.put(new Integer(0xCBBC), new BinaryCode("CBBC", "RES  7,H"));
	hashMap.put(new Integer(0xCBBD), new BinaryCode("CBBD", "RES  7,L"));
	hashMap.put(new Integer(0xCBBE), new BinaryCode("CBBE", "RES  7,(HL)"));
	hashMap.put(new Integer(0xCBBF), new BinaryCode("CBBF", "RES  7,A"));
	hashMap.put(new Integer(0xCBC0), new BinaryCode("CBC0", "SET  0,B"));
	hashMap.put(new Integer(0xCBC1), new BinaryCode("CBC1", "SET  0,C"));
	hashMap.put(new Integer(0xCBC2), new BinaryCode("CBC2", "SET  0,D"));
	hashMap.put(new Integer(0xCBC3), new BinaryCode("CBC3", "SET  0,E"));
	hashMap.put(new Integer(0xCBC4), new BinaryCode("CBC4", "SET  0,H"));
	hashMap.put(new Integer(0xCBC5), new BinaryCode("CBC5", "SET  0,L"));
	hashMap.put(new Integer(0xCBC6), new BinaryCode("CBC6", "SET  0,(HL)"));
	hashMap.put(new Integer(0xCBC7), new BinaryCode("CBC7", "SET  0,A"));
	hashMap.put(new Integer(0xCBC8), new BinaryCode("CBC8", "SET  1,B"));
	hashMap.put(new Integer(0xCBC9), new BinaryCode("CBC9", "SET  1,C"));
	hashMap.put(new Integer(0xCBCA), new BinaryCode("CBCA", "SET  1,D"));
	hashMap.put(new Integer(0xCBCB), new BinaryCode("CBCB", "SET  1,E"));
	hashMap.put(new Integer(0xCBCC), new BinaryCode("CBCC", "SET  1,H"));
	hashMap.put(new Integer(0xCBCD), new BinaryCode("CBCD", "SET  1,L"));
	hashMap.put(new Integer(0xCBCE), new BinaryCode("CBCE", "SET  1,(HL)"));
	hashMap.put(new Integer(0xCBCF), new BinaryCode("CBCF", "SET  1,A"));
	hashMap.put(new Integer(0xCBD0), new BinaryCode("CBD0", "SET  2,B"));
	hashMap.put(new Integer(0xCBD1), new BinaryCode("CBD1", "SET  2,C"));
	hashMap.put(new Integer(0xCBD2), new BinaryCode("CBD2", "SET  2,D"));
	hashMap.put(new Integer(0xCBD3), new BinaryCode("CBD3", "SET  2,E"));
	hashMap.put(new Integer(0xCBD4), new BinaryCode("CBD4", "SET  2,H"));
	hashMap.put(new Integer(0xCBD5), new BinaryCode("CBD5", "SET  2,L"));
	hashMap.put(new Integer(0xCBD6), new BinaryCode("CBD6", "SET  2,(HL)"));
	hashMap.put(new Integer(0xCBD7), new BinaryCode("CBD7", "SET  2,A"));
	hashMap.put(new Integer(0xCBD8), new BinaryCode("CBD8", "SET  3,B"));
	hashMap.put(new Integer(0xCBD9), new BinaryCode("CBD9", "SET  3,C"));
	hashMap.put(new Integer(0xCBDA), new BinaryCode("CBDA", "SET  3,D"));
	hashMap.put(new Integer(0xCBDB), new BinaryCode("CBDB", "SET  3,E"));
	hashMap.put(new Integer(0xCBDC), new BinaryCode("CBDC", "SET  3,H"));
	hashMap.put(new Integer(0xCBDD), new BinaryCode("CBDD", "SET  3,L"));
	hashMap.put(new Integer(0xCBDE), new BinaryCode("CBDE", "SET  3,(HL)"));
	hashMap.put(new Integer(0xCBDF), new BinaryCode("CBDF", "SET  3,A"));
	hashMap.put(new Integer(0xCBE0), new BinaryCode("CBE0", "SET  4,B"));
	hashMap.put(new Integer(0xCBE1), new BinaryCode("CBE1", "SET  4,C"));
	hashMap.put(new Integer(0xCBE2), new BinaryCode("CBE2", "SET  4,D"));
	hashMap.put(new Integer(0xCBE3), new BinaryCode("CBE3", "SET  4,E"));
	hashMap.put(new Integer(0xCBE4), new BinaryCode("CBE4", "SET  4,H"));
	hashMap.put(new Integer(0xCBE5), new BinaryCode("CBE5", "SET  4,L"));
	hashMap.put(new Integer(0xCBE6), new BinaryCode("CBE6", "SET  4,(HL)"));
	hashMap.put(new Integer(0xCBE7), new BinaryCode("CBE7", "SET  4,A"));
	hashMap.put(new Integer(0xCBE8), new BinaryCode("CBE8", "SET  5,B"));
	hashMap.put(new Integer(0xCBE9), new BinaryCode("CBE9", "SET  5,C"));
	hashMap.put(new Integer(0xCBEA), new BinaryCode("CBEA", "SET  5,D"));
	hashMap.put(new Integer(0xCBEB), new BinaryCode("CBEB", "SET  5,E"));
	hashMap.put(new Integer(0xCBEC), new BinaryCode("CBEC", "SET  5,H"));
	hashMap.put(new Integer(0xCBED), new BinaryCode("CBED", "SET  5,L"));
	hashMap.put(new Integer(0xCBEE), new BinaryCode("CBEE", "SET  5,(HL)"));
	hashMap.put(new Integer(0xCBEF), new BinaryCode("CBEF", "SET  5,A"));
	hashMap.put(new Integer(0xCBF0), new BinaryCode("CBF0", "SET  6,B"));
	hashMap.put(new Integer(0xCBF1), new BinaryCode("CBF1", "SET  6,C"));
	hashMap.put(new Integer(0xCBF2), new BinaryCode("CBF2", "SET  6,D"));
	hashMap.put(new Integer(0xCBF3), new BinaryCode("CBF3", "SET  6,E"));
	hashMap.put(new Integer(0xCBF4), new BinaryCode("CBF4", "SET  6,H"));
	hashMap.put(new Integer(0xCBF5), new BinaryCode("CBF5", "SET  6,L"));
	hashMap.put(new Integer(0xCBF6), new BinaryCode("CBF6", "SET  6,(HL)"));
	hashMap.put(new Integer(0xCBF7), new BinaryCode("CBF7", "SET  6,A"));
	hashMap.put(new Integer(0xCBF8), new BinaryCode("CBF8", "SET  7,B"));
	hashMap.put(new Integer(0xCBF9), new BinaryCode("CBF9", "SET  7,C"));
	hashMap.put(new Integer(0xCBFA), new BinaryCode("CBFA", "SET  7,D"));
	hashMap.put(new Integer(0xCBFB), new BinaryCode("CBFB", "SET  7,E"));
	hashMap.put(new Integer(0xCBFC), new BinaryCode("CBFC", "SET  7,H"));
	hashMap.put(new Integer(0xCBFD), new BinaryCode("CBFD", "SET  7,L"));
	hashMap.put(new Integer(0xCBFE), new BinaryCode("CBFE", "SET  7,(HL)"));
	hashMap.put(new Integer(0xCBFF), new BinaryCode("CBFF", "SET  7,A"));
	hashMap.put(new Integer(0xDD09), new BinaryCode("DD09", "ADD  IX,BC"));
	hashMap.put(new Integer(0xDD19), new BinaryCode("DD19", "ADD  IX,DE"));
	hashMap.put(new Integer(0xDD21), new BinaryCode("DD21@@@@", "LD   IX,@"));
	hashMap.put(new Integer(0xDD22), new BinaryCode("DD22@@@@", "LD   (@),IX"));
	hashMap.put(new Integer(0xDD23), new BinaryCode("DD23", "INC  IX"));
	hashMap.put(new Integer(0xDD24), new BinaryCode("DD24", "INC  IXH"));
	hashMap.put(new Integer(0xDD25), new BinaryCode("DD25", "DEC  IXH"));
	hashMap.put(new Integer(0xDD26), new BinaryCode("DD26##", "LD   IXH,#"));
	hashMap.put(new Integer(0xDD29), new BinaryCode("DD29", "ADD  IX,IX"));
	hashMap.put(new Integer(0xDD2A), new BinaryCode("DD2A@@@@", "LD   IX,(@)"));
	hashMap.put(new Integer(0xDD2B), new BinaryCode("DD2B", "DEC  IX"));
	hashMap.put(new Integer(0xDD2C), new BinaryCode("DD2C", "INC  IXL"));
	hashMap.put(new Integer(0xDD2D), new BinaryCode("DD2D", "DEC  IXL"));
	hashMap.put(new Integer(0xDD2E), new BinaryCode("DD2E##", "LD   IXL,#"));
	hashMap.put(new Integer(0xDD34), new BinaryCode("DD34$$", "INC  (IX+$)"));
	hashMap.put(new Integer(0xDD35), new BinaryCode("DD35$$", "DEC  (IX+$)"));
	hashMap.put(new Integer(0xDD36), new BinaryCode("DD36$$##", "LD   (IX+$),#"));
	hashMap.put(new Integer(0xDD39), new BinaryCode("DD39", "ADD  IX,SP"));
	hashMap.put(new Integer(0xDD44), new BinaryCode("DD44", "LD   B,IXH"));
	hashMap.put(new Integer(0xDD45), new BinaryCode("DD45", "LD   B,IXL"));
	hashMap.put(new Integer(0xDD46), new BinaryCode("DD46$$", "LD   B,(IX+$)"));
	hashMap.put(new Integer(0xDD4C), new BinaryCode("DD4C", "LD   C,IXH"));
	hashMap.put(new Integer(0xDD4D), new BinaryCode("DD4D", "LD   C,IXL"));
	hashMap.put(new Integer(0xDD4E), new BinaryCode("DD4E$$", "LD   C,(IX+$)"));
	hashMap.put(new Integer(0xDD54), new BinaryCode("DD54", "LD   D,IXH"));
	hashMap.put(new Integer(0xDD55), new BinaryCode("DD55", "LD   D,IXL"));
	hashMap.put(new Integer(0xDD56), new BinaryCode("DD56$$", "LD   D,(IX+$)"));
	hashMap.put(new Integer(0xDD5C), new BinaryCode("DD5C", "LD   E,IXH"));
	hashMap.put(new Integer(0xDD5D), new BinaryCode("DD5D", "LD   E,IXL"));
	hashMap.put(new Integer(0xDD5E), new BinaryCode("DD5E$$", "LD   E,(IX+$)"));
	hashMap.put(new Integer(0xDD60), new BinaryCode("DD60", "LD   IXH,B"));
	hashMap.put(new Integer(0xDD61), new BinaryCode("DD61", "LD   IXH,C"));
	hashMap.put(new Integer(0xDD62), new BinaryCode("DD62", "LD   IXH,D"));
	hashMap.put(new Integer(0xDD63), new BinaryCode("DD63", "LD   IXH,E"));
	hashMap.put(new Integer(0xDD64), new BinaryCode("DD64", "LD   IXH,IXH"));
	hashMap.put(new Integer(0xDD65), new BinaryCode("DD65", "LD   IXH,IXL"));
	hashMap.put(new Integer(0xDD66), new BinaryCode("DD66$$", "LD   H,(IX+$)"));
	hashMap.put(new Integer(0xDD67), new BinaryCode("DD67", "LD   IXH,A"));
	hashMap.put(new Integer(0xDD68), new BinaryCode("DD68", "LD   IXL,B"));
	hashMap.put(new Integer(0xDD69), new BinaryCode("DD69", "LD   IXL,C"));
	hashMap.put(new Integer(0xDD6A), new BinaryCode("DD6A", "LD   IXL,D"));
	hashMap.put(new Integer(0xDD6B), new BinaryCode("DD6B", "LD   IXL,E"));
	hashMap.put(new Integer(0xDD6C), new BinaryCode("DD6C", "LD   IXL,IXH"));
	hashMap.put(new Integer(0xDD6D), new BinaryCode("DD6D", "LD   IXL,IXL"));
	hashMap.put(new Integer(0xDD6E), new BinaryCode("DD6E$$", "LD   L,(IX+$)"));
	hashMap.put(new Integer(0xDD6F), new BinaryCode("DD6F", "LD   IXL,A"));
	hashMap.put(new Integer(0xDD70), new BinaryCode("DD70$$", "LD   (IX+$),B"));
	hashMap.put(new Integer(0xDD71), new BinaryCode("DD71$$", "LD   (IX+$),C"));
	hashMap.put(new Integer(0xDD72), new BinaryCode("DD72$$", "LD   (IX+$),D"));
	hashMap.put(new Integer(0xDD73), new BinaryCode("DD73$$", "LD   (IX+$),E"));
	hashMap.put(new Integer(0xDD74), new BinaryCode("DD74$$", "LD   (IX+$),H"));
	hashMap.put(new Integer(0xDD75), new BinaryCode("DD75$$", "LD   (IX+$),L"));
	hashMap.put(new Integer(0xDD77), new BinaryCode("DD77$$", "LD   (IX+$),A"));
	hashMap.put(new Integer(0xDD7C), new BinaryCode("DD7C", "LD   A,IXH"));
	hashMap.put(new Integer(0xDD7D), new BinaryCode("DD7D", "LD   A,IXL"));
	hashMap.put(new Integer(0xDD7E), new BinaryCode("DD7E$$", "LD   A,(IX+$)"));
	hashMap.put(new Integer(0xDD84), new BinaryCode("DD84", "ADD  A,IXH"));
	hashMap.put(new Integer(0xDD85), new BinaryCode("DD85", "ADD  A,IXL"));
	hashMap.put(new Integer(0xDD86), new BinaryCode("DD86$$", "ADD  A,(IX+$)"));
	hashMap.put(new Integer(0xDD8C), new BinaryCode("DD8C", "ADC  A,IXH"));
	hashMap.put(new Integer(0xDD8D), new BinaryCode("DD8D", "ADC  A,IXL"));
	hashMap.put(new Integer(0xDD8E), new BinaryCode("DD8E$$", "ADC  A,(IX+$)"));
	hashMap.put(new Integer(0xDD94), new BinaryCode("DD94", "SUB  IXH"));
	hashMap.put(new Integer(0xDD95), new BinaryCode("DD95", "SUB  IXL"));
	hashMap.put(new Integer(0xDD96), new BinaryCode("DD96$$", "SUB  (IX+$)"));
	hashMap.put(new Integer(0xDD9C), new BinaryCode("DD9C", "SBC  A,IXH"));
	hashMap.put(new Integer(0xDD9D), new BinaryCode("DD9D", "SBC  A,IXL"));
	hashMap.put(new Integer(0xDD9E), new BinaryCode("DD9E$$", "SBC  A,(IX+$)"));
	hashMap.put(new Integer(0xDDA4), new BinaryCode("DDA4", "AND  IXH"));
	hashMap.put(new Integer(0xDDA5), new BinaryCode("DDA5", "AND  IXL"));
	hashMap.put(new Integer(0xDDA6), new BinaryCode("DDA6$$", "AND  (IX+$)"));
	hashMap.put(new Integer(0xDDAC), new BinaryCode("DDAC", "XOR  IXH"));
	hashMap.put(new Integer(0xDDAD), new BinaryCode("DDAD", "XOR  IXL"));
	hashMap.put(new Integer(0xDDAE), new BinaryCode("DDAE$$", "XOR  (IX+$)"));
	hashMap.put(new Integer(0xDDB4), new BinaryCode("DDB4", "OR   IXH"));
	hashMap.put(new Integer(0xDDB5), new BinaryCode("DDB5", "OR   IXL"));
	hashMap.put(new Integer(0xDDB6), new BinaryCode("DDB6$$", "OR   (IX+$)"));
	hashMap.put(new Integer(0xDDBC), new BinaryCode("DDBC", "CP   IXH"));
	hashMap.put(new Integer(0xDDBD), new BinaryCode("DDBD", "CP   IXL"));
	hashMap.put(new Integer(0xDDBE), new BinaryCode("DDBE$$", "CP   (IX+$)"));
	hashMap.put(new Integer(0xDDCB), new BinaryCode("DDCB", "<skip,next>"));
	hashMap.put(new Integer(0xDDE1), new BinaryCode("DDE1", "POP  IX"));
	hashMap.put(new Integer(0xDDE3), new BinaryCode("DDE3", "EX   (SP),IX"));
	hashMap.put(new Integer(0xDDE5), new BinaryCode("DDE5", "PUSH IX"));
	hashMap.put(new Integer(0xDDE9), new BinaryCode("DDE9", "JP   (IX)"));
	hashMap.put(new Integer(0xDDF9), new BinaryCode("DDF9", "LD   SP,IX"));
	hashMap.put(new Integer(0xED40), new BinaryCode("ED40", "IN   B,(C)"));
	hashMap.put(new Integer(0xED41), new BinaryCode("ED41", "OUT  (C),B"));
	hashMap.put(new Integer(0xED42), new BinaryCode("ED42", "SBC  HL,BC"));
	hashMap.put(new Integer(0xED43), new BinaryCode("ED43@@@@", "LD   (@),BC"));
	hashMap.put(new Integer(0xED44), new BinaryCode("ED44", "NEG"));
	hashMap.put(new Integer(0xED45), new BinaryCode("ED45", "RETN"));
	hashMap.put(new Integer(0xED46), new BinaryCode("ED46", "IM   0"));
	hashMap.put(new Integer(0xED47), new BinaryCode("ED47", "LD   I,A"));
	hashMap.put(new Integer(0xED48), new BinaryCode("ED48", "IN   C,(C)"));
	hashMap.put(new Integer(0xED49), new BinaryCode("ED49", "OUT  (C),C"));
	hashMap.put(new Integer(0xED4A), new BinaryCode("ED4A", "ADC  HL,BC"));
	hashMap.put(new Integer(0xED4B), new BinaryCode("ED4B@@@@", "LD   BC,(@)"));
	hashMap.put(new Integer(0xED4D), new BinaryCode("ED4D", "RETI"));
	hashMap.put(new Integer(0xED4F), new BinaryCode("ED4F", "LD   R,A"));
	hashMap.put(new Integer(0xED50), new BinaryCode("ED50", "IN   D,(C)"));
	hashMap.put(new Integer(0xED51), new BinaryCode("ED51", "OUT  (C),D"));
	hashMap.put(new Integer(0xED52), new BinaryCode("ED52", "SBC  HL,DE"));
	hashMap.put(new Integer(0xED53), new BinaryCode("ED53@@@@", "LD   (@),DE"));
	hashMap.put(new Integer(0xED56), new BinaryCode("ED56", "IM   1"));
	hashMap.put(new Integer(0xED57), new BinaryCode("ED57", "LD   A,I"));
	hashMap.put(new Integer(0xED58), new BinaryCode("ED58", "IN   E,(C)"));
	hashMap.put(new Integer(0xED59), new BinaryCode("ED59", "OUT  (C),E"));
	hashMap.put(new Integer(0xED5A), new BinaryCode("ED5A", "ADC  HL,DE"));
	hashMap.put(new Integer(0xED5B), new BinaryCode("ED5B@@@@", "LD   DE,(@)"));
	hashMap.put(new Integer(0xED5E), new BinaryCode("ED5E", "IM   2"));
	hashMap.put(new Integer(0xED5F), new BinaryCode("ED5F", "LD   A,R"));
	hashMap.put(new Integer(0xED60), new BinaryCode("ED60", "IN   H,(C)"));
	hashMap.put(new Integer(0xED61), new BinaryCode("ED61", "OUT  (C),H"));
	hashMap.put(new Integer(0xED62), new BinaryCode("ED62", "SBC  HL,HL"));
	hashMap.put(new Integer(0xED67), new BinaryCode("ED67", "RRD"));
	hashMap.put(new Integer(0xED68), new BinaryCode("ED68", "IN   L,(C)"));
	hashMap.put(new Integer(0xED69), new BinaryCode("ED69", "OUT  (C),L"));
	hashMap.put(new Integer(0xED6A), new BinaryCode("ED6A", "ADC  HL,HL"));
	hashMap.put(new Integer(0xED6F), new BinaryCode("ED6F", "RLD"));
	hashMap.put(new Integer(0xED70), new BinaryCode("ED70", "IN   F,(C)"));
	hashMap.put(new Integer(0xED71), new BinaryCode("ED71", "OUT  (C),0x00"));
	hashMap.put(new Integer(0xED72), new BinaryCode("ED72", "SBC  HL,SP"));
	hashMap.put(new Integer(0xED73), new BinaryCode("ED73@@@@", "LD   (@),SP"));
	hashMap.put(new Integer(0xED78), new BinaryCode("ED78", "IN   A,(C)"));
	hashMap.put(new Integer(0xED79), new BinaryCode("ED79", "OUT  (C),A"));
	hashMap.put(new Integer(0xED7A), new BinaryCode("ED7A", "ADC  HL,SP"));
	hashMap.put(new Integer(0xED7B), new BinaryCode("ED7B@@@@", "LD   SP,(@)"));
	hashMap.put(new Integer(0xEDA0), new BinaryCode("EDA0", "LDI"));
	hashMap.put(new Integer(0xEDA1), new BinaryCode("EDA1", "CPI"));
	hashMap.put(new Integer(0xEDA2), new BinaryCode("EDA2", "INI"));
	hashMap.put(new Integer(0xEDA3), new BinaryCode("EDA3", "OUTI"));
	hashMap.put(new Integer(0xEDA8), new BinaryCode("EDA8", "LDD"));
	hashMap.put(new Integer(0xEDA9), new BinaryCode("EDA9", "CPD"));
	hashMap.put(new Integer(0xEDAA), new BinaryCode("EDAA", "IND"));
	hashMap.put(new Integer(0xEDAB), new BinaryCode("EDAB", "OUTD"));
	hashMap.put(new Integer(0xEDB0), new BinaryCode("EDB0", "LDIR"));
	hashMap.put(new Integer(0xEDB1), new BinaryCode("EDB1", "CPIR"));
	hashMap.put(new Integer(0xEDB2), new BinaryCode("EDB2", "INIR"));
	hashMap.put(new Integer(0xEDB3), new BinaryCode("EDB3", "OTIR"));
	hashMap.put(new Integer(0xEDB8), new BinaryCode("EDB8", "LDDR"));
	hashMap.put(new Integer(0xEDB9), new BinaryCode("EDB9", "CPDR"));
	hashMap.put(new Integer(0xEDBA), new BinaryCode("EDBA", "INDR"));
	hashMap.put(new Integer(0xEDBB), new BinaryCode("EDBB", "OTDR"));
	hashMap.put(new Integer(0xFD09), new BinaryCode("FD09", "ADD  IY,BC"));
	hashMap.put(new Integer(0xFD19), new BinaryCode("FD19", "ADD  IY,DE"));
	hashMap.put(new Integer(0xFD21), new BinaryCode("FD21@@@@", "LD   IY,@"));
	hashMap.put(new Integer(0xFD22), new BinaryCode("FD22@@@@", "LD   (@),IY"));
	hashMap.put(new Integer(0xFD23), new BinaryCode("FD23", "INC  IY"));
	hashMap.put(new Integer(0xFD24), new BinaryCode("FD24", "INC  IYH"));
	hashMap.put(new Integer(0xFD25), new BinaryCode("FD25", "DEC  IYH"));
	hashMap.put(new Integer(0xFD26), new BinaryCode("FD26##", "LD   IYH,#"));
	hashMap.put(new Integer(0xFD29), new BinaryCode("FD29", "ADD  IY,IY"));
	hashMap.put(new Integer(0xFD2A), new BinaryCode("FD2A@@@@", "LD   IY,(@)"));
	hashMap.put(new Integer(0xFD2B), new BinaryCode("FD2B", "DEC  IY"));
	hashMap.put(new Integer(0xFD2C), new BinaryCode("FD2C", "INC  IYL"));
	hashMap.put(new Integer(0xFD2D), new BinaryCode("FD2D", "DEC  IYL"));
	hashMap.put(new Integer(0xFD2E), new BinaryCode("FD2E##", "LD   IYL,#"));
	hashMap.put(new Integer(0xFD34), new BinaryCode("FD34$$", "INC  (IY+$)"));
	hashMap.put(new Integer(0xFD35), new BinaryCode("FD35$$", "DEC  (IY+$)"));
	hashMap.put(new Integer(0xFD36), new BinaryCode("FD36$$##", "LD   (IY+$),#"));
	hashMap.put(new Integer(0xFD39), new BinaryCode("FD39", "ADD  IY,SP"));
	hashMap.put(new Integer(0xFD44), new BinaryCode("FD44", "LD   B,IYH"));
	hashMap.put(new Integer(0xFD45), new BinaryCode("FD45", "LD   B,IYL"));
	hashMap.put(new Integer(0xFD46), new BinaryCode("FD46$$", "LD   B,(IY+$)"));
	hashMap.put(new Integer(0xFD4C), new BinaryCode("FD4C", "LD   C,IYH"));
	hashMap.put(new Integer(0xFD4D), new BinaryCode("FD4D", "LD   C,IYL"));
	hashMap.put(new Integer(0xFD4E), new BinaryCode("FD4E$$", "LD   C,(IY+$)"));
	hashMap.put(new Integer(0xFD54), new BinaryCode("FD54", "LD   D,IYH"));
	hashMap.put(new Integer(0xFD55), new BinaryCode("FD55", "LD   D,IYL"));
	hashMap.put(new Integer(0xFD56), new BinaryCode("FD56$$", "LD   D,(IY+$)"));
	hashMap.put(new Integer(0xFD5C), new BinaryCode("FD5C", "LD   E,IYH"));
	hashMap.put(new Integer(0xFD5D), new BinaryCode("FD5D", "LD   E,IYL"));
	hashMap.put(new Integer(0xFD5E), new BinaryCode("FD5E$$", "LD   E,(IY+$)"));
	hashMap.put(new Integer(0xFD60), new BinaryCode("FD60", "LD   IYH,B"));
	hashMap.put(new Integer(0xFD61), new BinaryCode("FD61", "LD   IYH,C"));
	hashMap.put(new Integer(0xFD62), new BinaryCode("FD62", "LD   IYH,D"));
	hashMap.put(new Integer(0xFD63), new BinaryCode("FD63", "LD   IYH,E"));
	hashMap.put(new Integer(0xFD64), new BinaryCode("FD64", "LD   IYH,IYH"));
	hashMap.put(new Integer(0xFD65), new BinaryCode("FD65", "LD   IYH,IYL"));
	hashMap.put(new Integer(0xFD66), new BinaryCode("FD66$$", "LD   H,(IY+$)"));
	hashMap.put(new Integer(0xFD67), new BinaryCode("FD67", "LD   IYH,A"));
	hashMap.put(new Integer(0xFD68), new BinaryCode("FD68", "LD   IYL,B"));
	hashMap.put(new Integer(0xFD69), new BinaryCode("FD69", "LD   IYL,C"));
	hashMap.put(new Integer(0xFD6A), new BinaryCode("FD6A", "LD   IYL,D"));
	hashMap.put(new Integer(0xFD6B), new BinaryCode("FD6B", "LD   IYL,E"));
	hashMap.put(new Integer(0xFD6C), new BinaryCode("FD6C", "LD   IYL,IYH"));
	hashMap.put(new Integer(0xFD6D), new BinaryCode("FD6D", "LD   IYL,IYL"));
	hashMap.put(new Integer(0xFD6E), new BinaryCode("FD6E$$", "LD   L,(IY+$)"));
	hashMap.put(new Integer(0xFD6F), new BinaryCode("FD6F", "LD   IYL,A"));
	hashMap.put(new Integer(0xFD70), new BinaryCode("FD70$$", "LD   (IY+$),B"));
	hashMap.put(new Integer(0xFD71), new BinaryCode("FD71$$", "LD   (IY+$),C"));
	hashMap.put(new Integer(0xFD72), new BinaryCode("FD72$$", "LD   (IY+$),D"));
	hashMap.put(new Integer(0xFD73), new BinaryCode("FD73$$", "LD   (IY+$),E"));
	hashMap.put(new Integer(0xFD74), new BinaryCode("FD74$$", "LD   (IY+$),H"));
	hashMap.put(new Integer(0xFD75), new BinaryCode("FD75$$", "LD   (IY+$),L"));
	hashMap.put(new Integer(0xFD77), new BinaryCode("FD77$$", "LD   (IY+$),A"));
	hashMap.put(new Integer(0xFD7C), new BinaryCode("FD7C", "LD   A,IYH"));
	hashMap.put(new Integer(0xFD7D), new BinaryCode("FD7D", "LD   A,IYL"));
	hashMap.put(new Integer(0xFD7E), new BinaryCode("FD7E$$", "LD   A,(IY+$)"));
	hashMap.put(new Integer(0xFD84), new BinaryCode("FD84", "ADD  A,IYH"));
	hashMap.put(new Integer(0xFD85), new BinaryCode("FD85", "ADD  A,IYL"));
	hashMap.put(new Integer(0xFD86), new BinaryCode("FD86$$", "ADD  A,(IY+$)"));
	hashMap.put(new Integer(0xFD8C), new BinaryCode("FD8C", "ADC  A,IYH"));
	hashMap.put(new Integer(0xFD8D), new BinaryCode("FD8D", "ADC  A,IYL"));
	hashMap.put(new Integer(0xFD8E), new BinaryCode("FD8E$$", "ADC  A,(IY+$)"));
	hashMap.put(new Integer(0xFD94), new BinaryCode("FD94", "SUB  IYH"));
	hashMap.put(new Integer(0xFD95), new BinaryCode("FD95", "SUB  IYL"));
	hashMap.put(new Integer(0xFD96), new BinaryCode("FD96$$", "SUB  (IY+$)"));
	hashMap.put(new Integer(0xFD9C), new BinaryCode("FD9C", "SBC  A,IYH"));
	hashMap.put(new Integer(0xFD9D), new BinaryCode("FD9D", "SBC  A,IYL"));
	hashMap.put(new Integer(0xFD9E), new BinaryCode("FD9E$$", "SBC  A,(IY+$)"));
	hashMap.put(new Integer(0xFDA4), new BinaryCode("FDA4", "AND  IYH"));
	hashMap.put(new Integer(0xFDA5), new BinaryCode("FDA5", "AND  IYL"));
	hashMap.put(new Integer(0xFDA6), new BinaryCode("FDA6$$", "AND  (IY+$)"));
	hashMap.put(new Integer(0xFDAC), new BinaryCode("FDAC", "XOR  IYH"));
	hashMap.put(new Integer(0xFDAD), new BinaryCode("FDAD", "XOR  IYL"));
	hashMap.put(new Integer(0xFDAE), new BinaryCode("FDAE$$", "XOR  (IY+$)"));
	hashMap.put(new Integer(0xFDB4), new BinaryCode("FDB4", "OR  IYH"));
	hashMap.put(new Integer(0xFDB5), new BinaryCode("FDB5", "OR  IYL"));
	hashMap.put(new Integer(0xFDB6), new BinaryCode("FDB6$$", "OR  (IY+$)"));
	hashMap.put(new Integer(0xFDBC), new BinaryCode("FDBC", "CP  IYH"));
	hashMap.put(new Integer(0xFDBD), new BinaryCode("FDBD", "CP  IYL"));
	hashMap.put(new Integer(0xFDBE), new BinaryCode("FDBE$$", "CP  (IY+$)"));
	hashMap.put(new Integer(0xFDCB), new BinaryCode("FDCB", "<skip,next>"));
	hashMap.put(new Integer(0xFDE1), new BinaryCode("FDE1", "POP  IY"));
	hashMap.put(new Integer(0xFDE3), new BinaryCode("FDE3", "EX   (SP),IY"));
	hashMap.put(new Integer(0xFDE5), new BinaryCode("FDE5", "PUSH IY"));
	hashMap.put(new Integer(0xFDE9), new BinaryCode("FDE9", "JP   (IY)"));
	hashMap.put(new Integer(0xFDF9), new BinaryCode("FDF9", "LD   SP,IY"));
	hashMap.put(new Integer(0xDDCB0000), new BinaryCode("DDCB$$00", "LD   B,RLC (IX+$)"));
	hashMap.put(new Integer(0xDDCB0001), new BinaryCode("DDCB$$01", "LD   C,RLC (IX+$)"));
	hashMap.put(new Integer(0xDDCB0002), new BinaryCode("DDCB$$02", "LD   D,RLC (IX+$)"));
	hashMap.put(new Integer(0xDDCB0003), new BinaryCode("DDCB$$03", "LD   E,RLC (IX+$)"));
	hashMap.put(new Integer(0xDDCB0004), new BinaryCode("DDCB$$04", "LD   H,RLC (IX+$)"));
	hashMap.put(new Integer(0xDDCB0005), new BinaryCode("DDCB$$05", "LD   L,RLC (IX+$)"));
	hashMap.put(new Integer(0xDDCB0006), new BinaryCode("DDCB$$06", "RLC  (IX+$)"));
	hashMap.put(new Integer(0xDDCB0007), new BinaryCode("DDCB$$07", "LD   A,RLC (IX+$)"));
	hashMap.put(new Integer(0xDDCB0008), new BinaryCode("DDCB$$08", "LD   B,RRC (IX+$)"));
	hashMap.put(new Integer(0xDDCB0009), new BinaryCode("DDCB$$09", "LD   C,RRC (IX+$)"));
	hashMap.put(new Integer(0xDDCB000A), new BinaryCode("DDCB$$0A", "LD   D,RRC (IX+$)"));
	hashMap.put(new Integer(0xDDCB000B), new BinaryCode("DDCB$$0B", "LD   E,RRC (IX+$)"));
	hashMap.put(new Integer(0xDDCB000C), new BinaryCode("DDCB$$0C", "LD   H,RRC (IX+$)"));
	hashMap.put(new Integer(0xDDCB000D), new BinaryCode("DDCB$$0D", "LD   L,RRC (IX+$)"));
	hashMap.put(new Integer(0xDDCB000E), new BinaryCode("DDCB$$0E", "RRC  (IX+$)"));
	hashMap.put(new Integer(0xDDCB000F), new BinaryCode("DDCB$$0F", "LD   A,RRC (IX+$)"));
	hashMap.put(new Integer(0xDDCB0010), new BinaryCode("DDCB$$10", "LD   B,RL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0011), new BinaryCode("DDCB$$11", "LD   C,RL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0012), new BinaryCode("DDCB$$12", "LD   D,RL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0013), new BinaryCode("DDCB$$13", "LD   E,RL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0014), new BinaryCode("DDCB$$14", "LD   H,RL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0015), new BinaryCode("DDCB$$15", "LD   L,RL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0016), new BinaryCode("DDCB$$16", "RL   (IX+$)"));
	hashMap.put(new Integer(0xDDCB0017), new BinaryCode("DDCB$$17", "LD   A,RL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0018), new BinaryCode("DDCB$$18", "LD   B,RR (IX+$)"));
	hashMap.put(new Integer(0xDDCB0019), new BinaryCode("DDCB$$19", "LD   C,RR (IX+$)"));
	hashMap.put(new Integer(0xDDCB001A), new BinaryCode("DDCB$$1A", "LD   D,RR (IX+$)"));
	hashMap.put(new Integer(0xDDCB001B), new BinaryCode("DDCB$$1B", "LD   E,RR (IX+$)"));
	hashMap.put(new Integer(0xDDCB001C), new BinaryCode("DDCB$$1C", "LD   H,RR (IX+$)"));
	hashMap.put(new Integer(0xDDCB001D), new BinaryCode("DDCB$$1D", "LD   L,RR (IX+$)"));
	hashMap.put(new Integer(0xDDCB001E), new BinaryCode("DDCB$$1E", "RR   (IX+$)"));
	hashMap.put(new Integer(0xDDCB001F), new BinaryCode("DDCB$$1F", "LD   A,RR (IX+$)"));
	hashMap.put(new Integer(0xDDCB0020), new BinaryCode("DDCB$$20", "LD   B,SLA (IX+$)"));
	hashMap.put(new Integer(0xDDCB0021), new BinaryCode("DDCB$$21", "LD   C,SLA (IX+$)"));
	hashMap.put(new Integer(0xDDCB0022), new BinaryCode("DDCB$$22", "LD   D,SLA (IX+$)"));
	hashMap.put(new Integer(0xDDCB0023), new BinaryCode("DDCB$$23", "LD   E,SLA (IX+$)"));
	hashMap.put(new Integer(0xDDCB0024), new BinaryCode("DDCB$$24", "LD   H,SLA (IX+$)"));
	hashMap.put(new Integer(0xDDCB0025), new BinaryCode("DDCB$$25", "LD   L,SLA (IX+$)"));
	hashMap.put(new Integer(0xDDCB0026), new BinaryCode("DDCB$$26", "SLA  (IX+$)"));
	hashMap.put(new Integer(0xDDCB0027), new BinaryCode("DDCB$$27", "LD   A,SLA (IX+$)"));
	hashMap.put(new Integer(0xDDCB0028), new BinaryCode("DDCB$$28", "LD   B,SRA (IX+$)"));
	hashMap.put(new Integer(0xDDCB0029), new BinaryCode("DDCB$$29", "LD   C,SRA (IX+$)"));
	hashMap.put(new Integer(0xDDCB002A), new BinaryCode("DDCB$$2A", "LD   D,SRA (IX+$)"));
	hashMap.put(new Integer(0xDDCB002B), new BinaryCode("DDCB$$2B", "LD   E,SRA (IX+$)"));
	hashMap.put(new Integer(0xDDCB002C), new BinaryCode("DDCB$$2C", "LD   H,SRA (IX+$)"));
	hashMap.put(new Integer(0xDDCB002D), new BinaryCode("DDCB$$2D", "LD   L,SRA (IX+$)"));
	hashMap.put(new Integer(0xDDCB002E), new BinaryCode("DDCB$$2E", "SRA  (IX+$)"));
	hashMap.put(new Integer(0xDDCB002F), new BinaryCode("DDCB$$2F", "LD   A,SRA (IX+$)"));
	hashMap.put(new Integer(0xDDCB0030), new BinaryCode("DDCB$$30", "LD   B,SLL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0031), new BinaryCode("DDCB$$31", "LD   C,SLL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0032), new BinaryCode("DDCB$$32", "LD   D,SLL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0033), new BinaryCode("DDCB$$33", "LD   E,SLL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0034), new BinaryCode("DDCB$$34", "LD   H,SLL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0035), new BinaryCode("DDCB$$35", "LD   L,SLL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0036), new BinaryCode("DDCB$$36", "SLL  (IX+$)"));
	hashMap.put(new Integer(0xDDCB0037), new BinaryCode("DDCB$$37", "LD   A,SLL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0038), new BinaryCode("DDCB$$38", "LD   B,SRL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0039), new BinaryCode("DDCB$$39", "LD   C,SRL (IX+$)"));
	hashMap.put(new Integer(0xDDCB003A), new BinaryCode("DDCB$$3A", "LD   D,SRL (IX+$)"));
	hashMap.put(new Integer(0xDDCB003B), new BinaryCode("DDCB$$3B", "LD   E,SRL (IX+$)"));
	hashMap.put(new Integer(0xDDCB003C), new BinaryCode("DDCB$$3C", "LD   H,SRL (IX+$)"));
	hashMap.put(new Integer(0xDDCB003D), new BinaryCode("DDCB$$3D", "LD   L,SRL (IX+$)"));
	hashMap.put(new Integer(0xDDCB003E), new BinaryCode("DDCB$$3E", "SRL  (IX+$)"));
	hashMap.put(new Integer(0xDDCB003F), new BinaryCode("DDCB$$3F", "LD   A,SRL (IX+$)"));
	hashMap.put(new Integer(0xDDCB0046), new BinaryCode("DDCB$$46", "BIT  0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB004E), new BinaryCode("DDCB$$4E", "BIT  1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0056), new BinaryCode("DDCB$$56", "BIT  2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB005E), new BinaryCode("DDCB$$5E", "BIT  3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0066), new BinaryCode("DDCB$$66", "BIT  4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB006E), new BinaryCode("DDCB$$6E", "BIT  5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0076), new BinaryCode("DDCB$$76", "BIT  6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB007E), new BinaryCode("DDCB$$7E", "BIT  7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0080), new BinaryCode("DDCB$$80", "LD   B,RES 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0081), new BinaryCode("DDCB$$81", "LD   C,RES 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0082), new BinaryCode("DDCB$$82", "LD   D,RES 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0083), new BinaryCode("DDCB$$83", "LD   E,RES 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0084), new BinaryCode("DDCB$$84", "LD   H,RES 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0085), new BinaryCode("DDCB$$85", "LD   L,RES 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0086), new BinaryCode("DDCB$$86", "RES  0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0087), new BinaryCode("DDCB$$87", "LD   A,RES 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0088), new BinaryCode("DDCB$$88", "LD   B,RES 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0089), new BinaryCode("DDCB$$89", "LD   C,RES 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB008A), new BinaryCode("DDCB$$8A", "LD   D,RES 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB008B), new BinaryCode("DDCB$$8B", "LD   E,RES 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB008C), new BinaryCode("DDCB$$8C", "LD   H,RES 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB008D), new BinaryCode("DDCB$$8D", "LD   L,RES 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB008E), new BinaryCode("DDCB$$8E", "RES  1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB008F), new BinaryCode("DDCB$$8F", "LD   A,RES 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0090), new BinaryCode("DDCB$$90", "LD   B,RES 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0091), new BinaryCode("DDCB$$91", "LD   C,RES 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0092), new BinaryCode("DDCB$$92", "LD   D,RES 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0093), new BinaryCode("DDCB$$93", "LD   E,RES 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0094), new BinaryCode("DDCB$$94", "LD   H,RES 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0095), new BinaryCode("DDCB$$95", "LD   L,RES 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0096), new BinaryCode("DDCB$$96", "RES  2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0097), new BinaryCode("DDCB$$97", "LD   A,RES 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0098), new BinaryCode("DDCB$$98", "LD   B,RES 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB0099), new BinaryCode("DDCB$$99", "LD   C,RES 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB009A), new BinaryCode("DDCB$$9A", "LD   D,RES 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB009B), new BinaryCode("DDCB$$9B", "LD   E,RES 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB009C), new BinaryCode("DDCB$$9C", "LD   H,RES 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB009D), new BinaryCode("DDCB$$9D", "LD   L,RES 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB009E), new BinaryCode("DDCB$$9E", "RES  3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB009F), new BinaryCode("DDCB$$9F", "LD   A,RES 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00A0), new BinaryCode("DDCB$$A0", "LD   B,RES 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00A1), new BinaryCode("DDCB$$A1", "LD   C,RES 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00A2), new BinaryCode("DDCB$$A2", "LD   D,RES 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00A3), new BinaryCode("DDCB$$A3", "LD   E,RES 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00A4), new BinaryCode("DDCB$$A4", "LD   H,RES 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00A5), new BinaryCode("DDCB$$A5", "LD   L,RES 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00A6), new BinaryCode("DDCB$$A6", "RES  4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00A7), new BinaryCode("DDCB$$A7", "LD   A,RES 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00A8), new BinaryCode("DDCB$$A8", "LD   B,RES 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00A9), new BinaryCode("DDCB$$A9", "LD   C,RES 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00AA), new BinaryCode("DDCB$$AA", "LD   D,RES 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00AB), new BinaryCode("DDCB$$AB", "LD   E,RES 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00AC), new BinaryCode("DDCB$$AC", "LD   H,RES 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00AD), new BinaryCode("DDCB$$AD", "LD   L,RES 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00AE), new BinaryCode("DDCB$$AE", "RES  5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00AF), new BinaryCode("DDCB$$AF", "LD   A,RES 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00B0), new BinaryCode("DDCB$$B0", "LD   B,RES 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00B1), new BinaryCode("DDCB$$B1", "LD   C,RES 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00B2), new BinaryCode("DDCB$$B2", "LD   D,RES 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00B3), new BinaryCode("DDCB$$B3", "LD   E,RES 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00B4), new BinaryCode("DDCB$$B4", "LD   H,RES 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00B5), new BinaryCode("DDCB$$B5", "LD   L,RES 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00B6), new BinaryCode("DDCB$$B6", "RES  6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00B7), new BinaryCode("DDCB$$B7", "LD   A,RES 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00B8), new BinaryCode("DDCB$$B8", "LD   B,RES 7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00B9), new BinaryCode("DDCB$$B9", "LD   C,RES 7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00BA), new BinaryCode("DDCB$$BA", "LD   D,RES 7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00BB), new BinaryCode("DDCB$$BB", "LD   E,RES 7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00BC), new BinaryCode("DDCB$$BC", "LD   H,RES 7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00BD), new BinaryCode("DDCB$$BD", "LD   L,RES 7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00BE), new BinaryCode("DDCB$$BE", "RES  7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00BF), new BinaryCode("DDCB$$BF", "LD   A,RES 7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00C0), new BinaryCode("DDCB$$C0", "LD   B,SET 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00C1), new BinaryCode("DDCB$$C1", "LD   C,SET 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00C2), new BinaryCode("DDCB$$C2", "LD   D,SET 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00C3), new BinaryCode("DDCB$$C3", "LD   E,SET 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00C4), new BinaryCode("DDCB$$C4", "LD   H,SET 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00C5), new BinaryCode("DDCB$$C5", "LD   L,SET 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00C6), new BinaryCode("DDCB$$C6", "SET  0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00C8), new BinaryCode("DDCB$$C8", "LD   B,SET 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00C9), new BinaryCode("DDCB$$C9", "LD   C,SET 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00CA), new BinaryCode("DDCB$$CA", "LD   D,SET 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00CB), new BinaryCode("DDCB$$CB", "LD   E,SET 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00CC), new BinaryCode("DDCB$$CC", "LD   H,SET 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00CD), new BinaryCode("DDCB$$CD", "LD   L,SET 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00CE), new BinaryCode("DDCB$$CE", "SET  1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00CF), new BinaryCode("DDCB$$CF", "LD   A,SET 1,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00D0), new BinaryCode("DDCB$$D0", "LD   B,SET 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00C7), new BinaryCode("DDCB$$C7", "LD   A,SET 0,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00D1), new BinaryCode("DDCB$$D1", "LD   C,SET 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00D2), new BinaryCode("DDCB$$D2", "LD   D,SET 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00D3), new BinaryCode("DDCB$$D3", "LD   E,SET 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00D4), new BinaryCode("DDCB$$D4", "LD   H,SET 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00D5), new BinaryCode("DDCB$$D5", "LD   L,SET 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00D6), new BinaryCode("DDCB$$D6", "SET  2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00D7), new BinaryCode("DDCB$$D7", "LD   A,SET 2,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00D8), new BinaryCode("DDCB$$D8", "LD   B,SET 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00D9), new BinaryCode("DDCB$$D9", "LD   C,SET 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00DA), new BinaryCode("DDCB$$DA", "LD   D,SET 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00DB), new BinaryCode("DDCB$$DB", "LD   E,SET 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00DC), new BinaryCode("DDCB$$DC", "LD   H,SET 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00DD), new BinaryCode("DDCB$$DD", "LD   L,SET 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00DE), new BinaryCode("DDCB$$DE", "SET  3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00DF), new BinaryCode("DDCB$$DF", "LD   A,SET 3,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00E0), new BinaryCode("DDCB$$E0", "LD   B,SET 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00E1), new BinaryCode("DDCB$$E1", "LD   C,SET 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00E2), new BinaryCode("DDCB$$E2", "LD   D,SET 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00E3), new BinaryCode("DDCB$$E3", "LD   E,SET 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00E4), new BinaryCode("DDCB$$E4", "LD   H,SET 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00E5), new BinaryCode("DDCB$$E5", "LD   L,SET 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00E6), new BinaryCode("DDCB$$E6", "SET  4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00E7), new BinaryCode("DDCB$$E7", "LD   A,SET 4,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00E8), new BinaryCode("DDCB$$E8", "LD   B,SET 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00E9), new BinaryCode("DDCB$$E9", "LD   C,SET 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00EA), new BinaryCode("DDCB$$EA", "LD   D,SET 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00EB), new BinaryCode("DDCB$$EB", "LD   E,SET 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00EC), new BinaryCode("DDCB$$EC", "LD   H,SET 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00ED), new BinaryCode("DDCB$$ED", "LD   L,SET 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00EE), new BinaryCode("DDCB$$EE", "SET  5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00EF), new BinaryCode("DDCB$$EF", "LD   A,SET 5,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00F0), new BinaryCode("DDCB$$F0", "LD   B,SET 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00F1), new BinaryCode("DDCB$$F1", "LD   C,SET 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00F2), new BinaryCode("DDCB$$F2", "LD   D,SET 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00F3), new BinaryCode("DDCB$$F3", "LD   E,SET 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00F4), new BinaryCode("DDCB$$F4", "LD   H,SET 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00F5), new BinaryCode("DDCB$$F5", "LD   L,SET 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00F6), new BinaryCode("DDCB$$F6", "SET  6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00F7), new BinaryCode("DDCB$$F7", "LD   A,SET 6,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00F8), new BinaryCode("DDCB$$F8", "LD   B,SET 7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00F9), new BinaryCode("DDCB$$F9", "LD   C,SET 7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00FA), new BinaryCode("DDCB$$FA", "LD   D,SET 7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00FB), new BinaryCode("DDCB$$FB", "LD   E,SET 7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00FC), new BinaryCode("DDCB$$FC", "LD   H,SET 7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00FD), new BinaryCode("DDCB$$FD", "LD   L,SET 7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00FE), new BinaryCode("DDCB$$FE", "SET  7,(IX+$)"));
	hashMap.put(new Integer(0xDDCB00FF), new BinaryCode("DDCB$$FF", "LD   A,SET 7,(IX+$)"));
	hashMap.put(new Integer(0xFDCB0000), new BinaryCode("FDCB$$00", "LD   B,RLC (IY+$)"));
	hashMap.put(new Integer(0xFDCB0001), new BinaryCode("FDCB$$01", "LD   C,RLC (IY+$)"));
	hashMap.put(new Integer(0xFDCB0002), new BinaryCode("FDCB$$02", "LD   D,RLC (IY+$)"));
	hashMap.put(new Integer(0xFDCB0003), new BinaryCode("FDCB$$03", "LD   E,RLC (IY+$)"));
	hashMap.put(new Integer(0xFDCB0004), new BinaryCode("FDCB$$04", "LD   H,RLC (IY+$)"));
	hashMap.put(new Integer(0xFDCB0005), new BinaryCode("FDCB$$05", "LD   L,RLC (IY+$)"));
	hashMap.put(new Integer(0xFDCB0006), new BinaryCode("FDCB$$06", "RLC  (IY+$)"));
	hashMap.put(new Integer(0xFDCB0007), new BinaryCode("FDCB$$07", "LD   A,RLC (IY+$)"));
	hashMap.put(new Integer(0xFDCB0008), new BinaryCode("FDCB$$08", "LD   B,RRC (IY+$)"));
	hashMap.put(new Integer(0xFDCB0009), new BinaryCode("FDCB$$09", "LD   C,RRC (IY+$)"));
	hashMap.put(new Integer(0xFDCB000A), new BinaryCode("FDCB$$0A", "LD   D,RRC (IY+$)"));
	hashMap.put(new Integer(0xFDCB000B), new BinaryCode("FDCB$$0B", "LD   E,RRC (IY+$)"));
	hashMap.put(new Integer(0xFDCB000C), new BinaryCode("FDCB$$0C", "LD   H,RRC (IY+$)"));
	hashMap.put(new Integer(0xFDCB000D), new BinaryCode("FDCB$$0D", "LD   L,RRC (IY+$)"));
	hashMap.put(new Integer(0xFDCB000E), new BinaryCode("FDCB$$0E", "RRC  (IY+$)"));
	hashMap.put(new Integer(0xFDCB000F), new BinaryCode("FDCB$$0F", "LD   A,RRC (IY+$)"));
	hashMap.put(new Integer(0xFDCB0010), new BinaryCode("FDCB$$10", "LD   B,RL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0011), new BinaryCode("FDCB$$11", "LD   C,RL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0012), new BinaryCode("FDCB$$12", "LD   D,RL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0013), new BinaryCode("FDCB$$13", "LD   E,RL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0014), new BinaryCode("FDCB$$14", "LD   H,RL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0015), new BinaryCode("FDCB$$15", "LD   L,RL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0016), new BinaryCode("FDCB$$16", "RL   (IY+$)"));
	hashMap.put(new Integer(0xFDCB0017), new BinaryCode("FDCB$$17", "LD   A,RL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0018), new BinaryCode("FDCB$$18", "LD   B,RR (IY+$)"));
	hashMap.put(new Integer(0xFDCB0019), new BinaryCode("FDCB$$19", "LD   C,RR (IY+$)"));
	hashMap.put(new Integer(0xFDCB001A), new BinaryCode("FDCB$$1A", "LD   D,RR (IY+$)"));
	hashMap.put(new Integer(0xFDCB001B), new BinaryCode("FDCB$$1B", "LD   E,RR (IY+$)"));
	hashMap.put(new Integer(0xFDCB001C), new BinaryCode("FDCB$$1C", "LD   H,RR (IY+$)"));
	hashMap.put(new Integer(0xFDCB001D), new BinaryCode("FDCB$$1D", "LD   L,RR (IY+$)"));
	hashMap.put(new Integer(0xFDCB001E), new BinaryCode("FDCB$$1E", "RR   (IY+$)"));
	hashMap.put(new Integer(0xFDCB001F), new BinaryCode("FDCB$$1F", "LD   A,RR (IY+$)"));
	hashMap.put(new Integer(0xFDCB0020), new BinaryCode("FDCB$$20", "LD   B,SLA (IY+$)"));
	hashMap.put(new Integer(0xFDCB0021), new BinaryCode("FDCB$$21", "LD   C,SLA (IY+$)"));
	hashMap.put(new Integer(0xFDCB0022), new BinaryCode("FDCB$$22", "LD   D,SLA (IY+$)"));
	hashMap.put(new Integer(0xFDCB0023), new BinaryCode("FDCB$$23", "LD   E,SLA (IY+$)"));
	hashMap.put(new Integer(0xFDCB0024), new BinaryCode("FDCB$$24", "LD   H,SLA (IY+$)"));
	hashMap.put(new Integer(0xFDCB0025), new BinaryCode("FDCB$$25", "LD   L,SLA (IY+$)"));
	hashMap.put(new Integer(0xFDCB0026), new BinaryCode("FDCB$$26", "SLA  (IY+$)"));
	hashMap.put(new Integer(0xFDCB0027), new BinaryCode("FDCB$$27", "LD   A,SLA (IY+$)"));
	hashMap.put(new Integer(0xFDCB0028), new BinaryCode("FDCB$$28", "LD   B,SRA (IY+$)"));
	hashMap.put(new Integer(0xFDCB0029), new BinaryCode("FDCB$$29", "LD   C,SRA (IY+$)"));
	hashMap.put(new Integer(0xFDCB002A), new BinaryCode("FDCB$$2A", "LD   D,SRA (IY+$)"));
	hashMap.put(new Integer(0xFDCB002B), new BinaryCode("FDCB$$2B", "LD   E,SRA (IY+$)"));
	hashMap.put(new Integer(0xFDCB002C), new BinaryCode("FDCB$$2C", "LD   H,SRA (IY+$)"));
	hashMap.put(new Integer(0xFDCB002D), new BinaryCode("FDCB$$2D", "LD   L,SRA (IY+$)"));
	hashMap.put(new Integer(0xFDCB002E), new BinaryCode("FDCB$$2E", "SRA  (IY+$)"));
	hashMap.put(new Integer(0xFDCB002F), new BinaryCode("FDCB$$2F", "LD   A,SRA (IY+$)"));
	hashMap.put(new Integer(0xFDCB0030), new BinaryCode("FDCB$$30", "LD   B,SLL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0031), new BinaryCode("FDCB$$31", "LD   C,SLL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0032), new BinaryCode("FDCB$$32", "LD   D,SLL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0033), new BinaryCode("FDCB$$33", "LD   E,SLL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0034), new BinaryCode("FDCB$$34", "LD   H,SLL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0035), new BinaryCode("FDCB$$35", "LD   L,SLL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0036), new BinaryCode("FDCB$$36", "SLL  (IY+$)"));
	hashMap.put(new Integer(0xFDCB0037), new BinaryCode("FDCB$$37", "LD   A,SLL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0038), new BinaryCode("FDCB$$38", "LD   B,SRL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0039), new BinaryCode("FDCB$$39", "LD   C,SRL (IY+$)"));
	hashMap.put(new Integer(0xFDCB003A), new BinaryCode("FDCB$$3A", "LD   D,SRL (IY+$)"));
	hashMap.put(new Integer(0xFDCB003B), new BinaryCode("FDCB$$3B", "LD   E,SRL (IY+$)"));
	hashMap.put(new Integer(0xFDCB003C), new BinaryCode("FDCB$$3C", "LD   H,SRL (IY+$)"));
	hashMap.put(new Integer(0xFDCB003D), new BinaryCode("FDCB$$3D", "LD   L,SRL (IY+$)"));
	hashMap.put(new Integer(0xFDCB003E), new BinaryCode("FDCB$$3E", "SRL  (IY+$)"));
	hashMap.put(new Integer(0xFDCB003F), new BinaryCode("FDCB$$3F", "LD   A,SRL (IY+$)"));
	hashMap.put(new Integer(0xFDCB0040), new BinaryCode("FDCB$$40", "BIT  0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0048), new BinaryCode("FDCB$$48", "BIT  1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0050), new BinaryCode("FDCB$$50", "BIT  2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0058), new BinaryCode("FDCB$$58", "BIT  3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0060), new BinaryCode("FDCB$$60", "BIT  4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0068), new BinaryCode("FDCB$$68", "BIT  5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0070), new BinaryCode("FDCB$$70", "BIT  6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0078), new BinaryCode("FDCB$$78", "BIT  7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0080), new BinaryCode("FDCB$$80", "LD   B,RES 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0081), new BinaryCode("FDCB$$81", "LD   C,RES 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0082), new BinaryCode("FDCB$$82", "LD   D,RES 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0083), new BinaryCode("FDCB$$83", "LD   E,RES 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0084), new BinaryCode("FDCB$$84", "LD   H,RES 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0085), new BinaryCode("FDCB$$85", "LD   L,RES 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0086), new BinaryCode("FDCB$$86", "RES  0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0087), new BinaryCode("FDCB$$87", "LD   A,RES 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0088), new BinaryCode("FDCB$$88", "LD   B,RES 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0089), new BinaryCode("FDCB$$89", "LD   C,RES 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB008A), new BinaryCode("FDCB$$8A", "LD   D,RES 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB008B), new BinaryCode("FDCB$$8B", "LD   E,RES 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB008C), new BinaryCode("FDCB$$8C", "LD   H,RES 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB008D), new BinaryCode("FDCB$$8D", "LD   L,RES 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB008E), new BinaryCode("FDCB$$8E", "RES  1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB008F), new BinaryCode("FDCB$$8F", "LD   A,RES 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0090), new BinaryCode("FDCB$$90", "LD   B,RES 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0091), new BinaryCode("FDCB$$91", "LD   C,RES 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0092), new BinaryCode("FDCB$$92", "LD   D,RES 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0093), new BinaryCode("FDCB$$93", "LD   E,RES 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0094), new BinaryCode("FDCB$$94", "LD   H,RES 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0095), new BinaryCode("FDCB$$95", "LD   L,RES 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0096), new BinaryCode("FDCB$$96", "RES  2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0097), new BinaryCode("FDCB$$97", "LD   A,RES 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0098), new BinaryCode("FDCB$$98", "LD   B,RES 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB0099), new BinaryCode("FDCB$$99", "LD   C,RES 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB009A), new BinaryCode("FDCB$$9A", "LD   D,RES 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB009B), new BinaryCode("FDCB$$9B", "LD   E,RES 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB009C), new BinaryCode("FDCB$$9C", "LD   H,RES 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB009D), new BinaryCode("FDCB$$9D", "LD   L,RES 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB009E), new BinaryCode("FDCB$$9E", "RES  3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB009F), new BinaryCode("FDCB$$9F", "LD   A,RES 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00A0), new BinaryCode("FDCB$$A0", "LD   B,RES 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00A1), new BinaryCode("FDCB$$A1", "LD   C,RES 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00A2), new BinaryCode("FDCB$$A2", "LD   D,RES 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00A3), new BinaryCode("FDCB$$A3", "LD   E,RES 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00A4), new BinaryCode("FDCB$$A4", "LD   H,RES 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00A5), new BinaryCode("FDCB$$A5", "LD   L,RES 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00A6), new BinaryCode("FDCB$$A6", "RES  4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00A7), new BinaryCode("FDCB$$A7", "LD   A,RES 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00A8), new BinaryCode("FDCB$$A8", "LD   B,RES 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00A9), new BinaryCode("FDCB$$A9", "LD   C,RES 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00AA), new BinaryCode("FDCB$$AA", "LD   D,RES 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00AB), new BinaryCode("FDCB$$AB", "LD   E,RES 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00AC), new BinaryCode("FDCB$$AC", "LD   H,RES 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00AD), new BinaryCode("FDCB$$AD", "LD   L,RES 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00AE), new BinaryCode("FDCB$$AE", "RES  5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00AF), new BinaryCode("FDCB$$AF", "LD   A,RES 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00B0), new BinaryCode("FDCB$$B0", "LD   B,RES 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00B1), new BinaryCode("FDCB$$B1", "LD   C,RES 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00B2), new BinaryCode("FDCB$$B2", "LD   D,RES 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00B3), new BinaryCode("FDCB$$B3", "LD   E,RES 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00B4), new BinaryCode("FDCB$$B4", "LD   H,RES 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00B5), new BinaryCode("FDCB$$B5", "LD   L,RES 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00B6), new BinaryCode("FDCB$$B6", "RES  6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00B7), new BinaryCode("FDCB$$B7", "LD   A,RES 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00B8), new BinaryCode("FDCB$$B8", "LD   B,RES 7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00B9), new BinaryCode("FDCB$$B9", "LD   C,RES 7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00BA), new BinaryCode("FDCB$$BA", "LD   D,RES 7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00BB), new BinaryCode("FDCB$$BB", "LD   E,RES 7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00BC), new BinaryCode("FDCB$$BC", "LD   H,RES 7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00BD), new BinaryCode("FDCB$$BD", "LD   L,RES 7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00BE), new BinaryCode("FDCB$$BE", "RES  7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00BF), new BinaryCode("FDCB$$BF", "LD   A,RES 7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00C0), new BinaryCode("FDCB$$C0", "LD   B,SET 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00C1), new BinaryCode("FDCB$$C1", "LD   C,SET 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00C2), new BinaryCode("FDCB$$C2", "LD   D,SET 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00C3), new BinaryCode("FDCB$$C3", "LD   E,SET 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00C4), new BinaryCode("FDCB$$C4", "LD   H,SET 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00C5), new BinaryCode("FDCB$$C5", "LD   L,SET 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00C6), new BinaryCode("FDCB$$C6", "SET  0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00C7), new BinaryCode("FDCB$$C7", "LD   A,SET 0,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00C8), new BinaryCode("FDCB$$C8", "LD   B,SET 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00C9), new BinaryCode("FDCB$$C9", "LD   C,SET 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00CA), new BinaryCode("FDCB$$CA", "LD   D,SET 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00CB), new BinaryCode("FDCB$$CB", "LD   E,SET 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00CC), new BinaryCode("FDCB$$CC", "LD   H,SET 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00CD), new BinaryCode("FDCB$$CD", "LD   L,SET 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00CE), new BinaryCode("FDCB$$CE", "SET  1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00CF), new BinaryCode("FDCB$$CF", "LD   A,SET 1,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00D0), new BinaryCode("FDCB$$D0", "LD   B,SET 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00D1), new BinaryCode("FDCB$$D1", "LD   C,SET 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00D2), new BinaryCode("FDCB$$D2", "LD   D,SET 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00D3), new BinaryCode("FDCB$$D3", "LD   E,SET 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00D4), new BinaryCode("FDCB$$D4", "LD   H,SET 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00D5), new BinaryCode("FDCB$$D5", "LD   L,SET 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00D6), new BinaryCode("FDCB$$D6", "SET  2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00D7), new BinaryCode("FDCB$$D7", "LD   A,SET 2,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00D8), new BinaryCode("FDCB$$D8", "LD   B,SET 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00D9), new BinaryCode("FDCB$$D9", "LD   C,SET 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00DA), new BinaryCode("FDCB$$DA", "LD   D,SET 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00DB), new BinaryCode("FDCB$$DB", "LD   E,SET 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00DC), new BinaryCode("FDCB$$DC", "LD   H,SET 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00DD), new BinaryCode("FDCB$$DD", "LD   L,SET 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00DE), new BinaryCode("FDCB$$DE", "SET  3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00DF), new BinaryCode("FDCB$$DF", "LD   A,SET 3,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00E0), new BinaryCode("FDCB$$E0", "LD   B,SET 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00E1), new BinaryCode("FDCB$$E1", "LD   C,SET 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00E2), new BinaryCode("FDCB$$E2", "LD   D,SET 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00E3), new BinaryCode("FDCB$$E3", "LD   E,SET 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00E4), new BinaryCode("FDCB$$E4", "LD   H,SET 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00E5), new BinaryCode("FDCB$$E5", "LD   L,SET 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00E6), new BinaryCode("FDCB$$E6", "SET  4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00E7), new BinaryCode("FDCB$$E7", "LD   A,SET 4,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00E8), new BinaryCode("FDCB$$E8", "LD   B,SET 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00E9), new BinaryCode("FDCB$$E9", "LD   C,SET 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00EA), new BinaryCode("FDCB$$EA", "LD   D,SET 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00EB), new BinaryCode("FDCB$$EB", "LD   E,SET 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00EC), new BinaryCode("FDCB$$EC", "LD   H,SET 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00ED), new BinaryCode("FDCB$$ED", "LD   L,SET 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00EE), new BinaryCode("FDCB$$EE", "SET  5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00EF), new BinaryCode("FDCB$$EF", "LD   A,SET 5,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00F0), new BinaryCode("FDCB$$F0", "LD   B,SET 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00F1), new BinaryCode("FDCB$$F1", "LD   C,SET 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00F2), new BinaryCode("FDCB$$F2", "LD   D,SET 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00F3), new BinaryCode("FDCB$$F3", "LD   E,SET 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00F4), new BinaryCode("FDCB$$F4", "LD   H,SET 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00F5), new BinaryCode("FDCB$$F5", "LD   L,SET 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00F6), new BinaryCode("FDCB$$F6", "SET  6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00F7), new BinaryCode("FDCB$$F7", "LD   A,SET 6,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00F8), new BinaryCode("FDCB$$F8", "LD   B,SET 7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00F9), new BinaryCode("FDCB$$F9", "LD   C,SET 7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00FA), new BinaryCode("FDCB$$FA", "LD   D,SET 7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00FB), new BinaryCode("FDCB$$FB", "LD   E,SET 7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00FC), new BinaryCode("FDCB$$FC", "LD   H,SET 7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00FD), new BinaryCode("FDCB$$FD", "LD   L,SET 7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00FE), new BinaryCode("FDCB$$FE", "SET  7,(IY+$)"));
	hashMap.put(new Integer(0xFDCB00FF), new BinaryCode("FDCB$$FF", "LD   A,SET 7,(IY+$)"));
  } // Constructor

}
