package com.github.hanwelmer.dasmZ80;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

public class TestWriteOutput extends DasmZ80 {

  @Test
  public void testWriteOutput() {
	ArrayList<AssemblyCode> decoded = new ArrayList<AssemblyCode>();

	Symbols symbols = new Symbols();
	Symbol entryPoint = symbols.getOrMakeSymbol("__start", SymbolType.entryPoint, new Integer(0), "0");

	ArrayList<Byte> bytes0x00 = new ArrayList<Byte>();
	bytes0x00.add((byte) 0x00);
	decoded.add(new AssemblyCode(0, bytes0x00, "start", "NOP", ";comment"));

	ArrayList<Byte> bytes0x02 = new ArrayList<Byte>();
	bytes0x02.add((byte) 0x02);
	decoded.add(new AssemblyCode(1, bytes0x02, null, "LD (BC),A", ";comment"));

	ArrayList<Byte> bytes0x76 = new ArrayList<Byte>();
	bytes0x76.add((byte) 0x76);
	decoded.add(new AssemblyCode(2, bytes0x76, null, "HALT", ";comment"));

	HashMap<Integer, Path> paths = new HashMap<Integer, Path>();
	paths.put(entryPoint.getValue(), new Path(entryPoint, decoded));

	ArrayList<Byte> allBytes = new ArrayList<Byte>();
	allBytes.addAll(bytes0x00);
	allBytes.addAll(bytes0x02);
	allBytes.addAll(bytes0x76);
	Byte[] bytes = allBytes.toArray(new Byte[allBytes.size()]);
	ByteReader reader = new ReadFromArray(bytes);

	String fileName = "testWriteOutput.bin";
	ListingWriter writer = new ListingWriter();
	try {
	  writer.open(fileName);
	  writeOutput(writer, reader, paths, symbols);
	} catch (IOException e) {
	  e.printStackTrace();
	} finally {
	  writer.close();
	}
  }

  @Test
  public void testWriteDefinitions() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("                      ;");
	input.add("                      ;I/O addresses:");
	input.add("0003        port03    EQU  0x03");
	input.add("00FE        SIO_A_C   EQU  0xFE       ;SIO channel A, command register.");
	input.add("                      ;");
	input.add(
	    "                      ;SIO channel A, command register is initialised with (see 0x0321: 0x0322, 0x0324..0x032F):");
	input.add("                      ;");
	input.add("                      ;0x6E = 0156 -> Reset Rx CRC checker, Reset TxInt Pending, select register 6.");
	input.add("                      ;");
	input.add("                      ;Memory addresses:");
	input.add(
	    "1B00        vectors   EQU  0x1B00     ;Base address for interrupt vector table. This byte initialised to 0xD8.");
	input.add("1B01        lbl1B01   EQU  0x1B01");
	input.add("0006        lbl0006   EQU  0x0006;");
	input.add("                      ;");
	input.add("                      ;Constants:");
	input.add("0C08        c12ports  EQU  12*256+port08 ;Initialize 12 ports starting with port18.");
	input.add("0C11        cIniP11   EQU  12*256+SIO_A_C");

	Symbols symbols = readSymbols(input);

	StringWriter writer = new StringWriter();
	writeDefinitions("testWriteDefinitions()", writer, symbols);

	assert (writer.output.size() == 15);

	String blank = "                        ;\n";
	String msgPort03 = "0003          port03    EQU  0x03\n";
	String msgSIO_A_C = "00FE          SIO_A_C   EQU  0xFE           ;SIO channel A, command register.\n";
	msgSIO_A_C += "                        ;\n";
	msgSIO_A_C += "                        ;SIO channel A, command register is initialised with (see 0x0321: 0x0322, 0x0324..0x032F):\n";
	msgSIO_A_C += "                        ;\n";
	msgSIO_A_C += "                        ;0x6E = 0156 -> Reset Rx CRC checker, Reset TxInt Pending, select register 6.\n";
	msgSIO_A_C += "                        ;\n";
	assert (writer.output.get(1).equals(blank));
	assert (writer.output.get(2).equals("                        ;I/O addresses:\n"));
	assert (writer.output.get(3).equals(msgPort03));
	assert (writer.output.get(4).equals(msgSIO_A_C));
	assert (writer.output.get(5).equals(blank));

	String msg7 = "0006          lbl0006   EQU  0x0006         ;\n";
	msg7 += "                        ;\n";
	String msg8 = "1B00          vectors   EQU  0x1B00         ;Base address for interrupt vector table. This byte initialised to 0xD8.\n";
	String msg9 = "1B01          lbl1B01   EQU  0x1B01\n";
	assert (writer.output.get(6).equals("                        ;Memory addresses:\n"));
	assert (writer.output.get(7).equals(msg7));
	assert (writer.output.get(8).equals(msg8));
	assert (writer.output.get(9).equals(msg9));
	assert (writer.output.get(10).equals(blank));

	String msg12 = "0C08          c12ports  EQU  12*256+port08  ;Initialize 12 ports starting with port18.\n";
	String msg13 = "0C11          cIniP11   EQU  12*256+SIO_A_C\n";
	assert (writer.output.get(11).equals("                        ;Constants:\n"));
	assert (writer.output.get(12).equals(msg12));
	assert (writer.output.get(13).equals(msg13));
	assert (writer.output.get(14).equals(blank));
  }
}
