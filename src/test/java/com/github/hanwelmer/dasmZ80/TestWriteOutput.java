package com.github.hanwelmer.dasmZ80;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

public class TestWriteOutput extends DasmZ80 {

  @Test
  public void testWriteOutput() {
	ArrayList<AssemblyCode> decoded = new ArrayList<AssemblyCode>();

	ArrayList<Byte> bytes0x00 = new ArrayList<Byte>();
	bytes0x00.add((byte) 0x00);
	decoded.add(new AssemblyCode(0, bytes0x00, "start", "NOP", ";comment"));

	ArrayList<Byte> bytes0x02 = new ArrayList<Byte>();
	bytes0x02.add((byte) 0x02);
	decoded.add(new AssemblyCode(1, bytes0x02, null, "LD (BC),A", ";comment"));

	ArrayList<Byte> bytes0x76 = new ArrayList<Byte>();
	bytes0x76.add((byte) 0x76);
	decoded.add(new AssemblyCode(2, bytes0x76, null, "HALT", ";comment"));

	String fileName = "testWriteOutput.bin";
	ListingWriter writer = new ListingWriter();
	try {
	  writer.open(fileName);
	  writeOutput(3, decoded, writer);
	} catch (IOException e) {
	  e.printStackTrace();
	} finally {
	  writer.close();
	}
  }

  @Test
  public void testWriteDefinitions() {
	Symbols symbols = new Symbols();
	Symbol newSymbol = symbols.getOrMakeSymbol("port03", SymbolType.portAddress, 0x03, "0x03");

	newSymbol = symbols.getOrMakeSymbol("SIO_A_C", SymbolType.portAddress, 0xFE, "0xFE");
	newSymbol.add(";SIO channel A, command register.");
	newSymbol.add(";");
	newSymbol.add(";SIO channel A, command register is initialised with (see 0x0321: 0x0322, 0x0324..0x032F):");
	newSymbol.add(";");
	newSymbol.add(";0x6E = 0156 -> Reset Rx CRC checker, Reset TxInt Pending, select register 6.");
	newSymbol.add(";");

	newSymbol = symbols.getOrMakeSymbol("vectors", SymbolType.memoryAddress, 0x1B00, "0x1B00");
	newSymbol.add(";Base address for interrupt vector table. This byte initialised to 0xD8.");

	newSymbol = symbols.getOrMakeSymbol("lbl1B01", SymbolType.memoryAddress, 0x1B01, "0x1B01");

	newSymbol = symbols.getOrMakeSymbol("lbl0004", SymbolType.memoryAddress, 4, "0x0004");
	newSymbol = symbols.getOrMakeSymbol("lbl0005", SymbolType.memoryAddress, 5, "0x0005");
	newSymbol = symbols.getOrMakeSymbol("lbl0006", SymbolType.memoryAddress, 6, "0x0006");

	newSymbol = symbols.getOrMakeSymbol("c12ports", SymbolType.constant, 0x0C08, "12*256+port08");
	newSymbol.add(";Initialize 12 ports starting with port18.");

	newSymbol = symbols.getOrMakeSymbol("cIniP11", SymbolType.constant, 0x0C11, "12*256+SIO_A_C");

	StringWriter writer = new StringWriter();
	writeDefinitions("testWriteDefinitions()", writer, symbols, 5);

	assert (writer.output.size() == 16);

	String msg3 = "0003            port03  EQU  0x03\n";
	String msg4 = "00FE            SIO_A_C EQU  0xFE           ;SIO channel A, command register.\n";
	msg4 += "                        ;\n";
	msg4 += "                        ;SIO channel A, command register is initialised with (see 0x0321: 0x0322, 0x0324..0x032F):\n";
	msg4 += "                        ;\n";
	msg4 += "                        ;0x6E = 0156 -> Reset Rx CRC checker, Reset TxInt Pending, select register 6.\n";
	msg4 += "                        ;\n";
	assert (writer.output.get(2).equals("                        ;I/O addresses:\n"));
	assert (writer.output.get(3).equals(msg3));
	assert (writer.output.get(4).equals(msg4));

	String msg7 = "0006            lbl0006 EQU  0x0006\n";
	String msg8 = "1B00            vectors EQU  0x1B00         ;Base address for interrupt vector table. This byte initialised to 0xD8.\n";
	String msg9 = "1B01            lbl1B01 EQU  0x1B01\n";
	assert (writer.output.get(6).equals("                        ;Memory addresses:\n"));
	assert (writer.output.get(7).equals(msg7));
	assert (writer.output.get(8).equals(msg8));
	assert (writer.output.get(9).equals(msg9));

	String msg12 = "0C08            c12ports EQU 12*256+port08  ;Initialize 12 ports starting with port18.\n";
	String msg13 = "0C11            cIniP11 EQU  12*256+SIO_A_C\n";
	assert (writer.output.get(11).equals("                        ;Constants:\n"));
	assert (writer.output.get(12).equals(msg12));
	assert (writer.output.get(13).equals(msg13));
  }
}
