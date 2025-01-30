package com.github.hanwelmer.dasmZ80;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

public class TestReadSymbols extends DasmZ80 {

  @Test
  public void testPortSymbols() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add(";I/O addresses:");
	input.add("0003            port03  EQU  0x03");
	input.add("00FE            SIO_A_C EQU  0xFE           ;SIO channel A, command register.");
	input.add(";");
	input.add("        ;SIO channel A, command register is initialised with (see 0x0321: 0x0322, 0x0324..0x032F):");
	input.add("        ;");
	input.add("        ;0x6E = 0156 -> Reset Rx CRC checker, Reset TxInt Pending, select register 6.");

	Symbols symbols = readSymbols(input);
	ArrayList<Symbol> ports = symbols.getSymbolsByType(SymbolType.portAddress);

	assert (ports.size() == 2);
	assert (ports.get(0).getType() == SymbolType.portAddress);
	assert (ports.get(0).getName().equals("port03"));
	assert (ports.get(0).getValue().equals(new Integer(3)));

	assert (ports.get(1).getType() == SymbolType.portAddress);
	assert (ports.get(1).getName().equals("SIO_A_C"));
	assert (ports.get(1).getValue().equals(new Integer(0xFE)));
	assert (ports.get(1).getComments().size() == 5);
	assert (ports.get(1).getComments().get(0).equals(";SIO channel A, command register."));
	assert (ports.get(1).getComments().get(1).equals(";"));
	assert (ports.get(1).getComments().get(2)
	    .equals(";SIO channel A, command register is initialised with (see 0x0321: 0x0322, 0x0324..0x032F):"));
	assert (ports.get(1).getComments().get(3).equals(";"));
	assert (ports.get(1).getComments().get(4)
	    .equals(";0x6E = 0156 -> Reset Rx CRC checker, Reset TxInt Pending, select register 6."));
  }

  @Test
  public void testMemorySymbols() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add(";Memory addresses:");
	input.add(
	    "1B00            vectors EQU  0x1B00         ;Base address for interrupt vector table. This byte initialised to 0xD8.");
	input.add("1B01            lbl1B01 EQU  0x1B01");
	input.add("C103            lblC103 EQU  0xC103");
	Symbols symbols = readSymbols(input);
	ArrayList<Symbol> labels = symbols.getSymbolsByType(SymbolType.memoryAddress);

	assert (labels.size() == 3);
	assert (labels.get(0).getType() == SymbolType.memoryAddress);
	assert (labels.get(0).getName().equals("vectors"));
	assert (labels.get(0).getValue().equals(new Integer(0x1B00)));
	assert (labels.get(0).getComments().get(0)
	    .equals(";Base address for interrupt vector table. This byte initialised to 0xD8."));

	assert (labels.get(1).getType() == SymbolType.memoryAddress);
	assert (labels.get(1).getName().equals("lbl1B01"));
	assert (labels.get(1).getValue().equals(new Integer(0x1B01)));

	assert (labels.get(2).getType() == SymbolType.memoryAddress);
	assert (labels.get(2).getName().equals("lblC103"));
	assert (labels.get(2).getValue().equals(new Integer(0xC103)));
  }

  @Test
  public void testConstants() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add(";Constants:");
	input.add("0C08            c12ports EQU 12 * 256 + 8 ;Initialize 12 ports starting with port18.");
	input.add("0C11            cIniP11 EQU 12 * 256 + 11");
	Symbols symbols = readSymbols(input);
	ArrayList<Symbol> constants = symbols.getSymbolsByType(SymbolType.constant);

	assert (constants.size() == 2);

	assert (constants.get(0).getType() == SymbolType.constant);
	assert (constants.get(0).getName().equals("cIniP11"));
	assert (constants.get(0).getValue().equals(new Integer(0x0C11)));

	assert (constants.get(1).getType() == SymbolType.constant);
	assert (constants.get(1).getName().equals("c12ports"));
	assert (constants.get(1).getValue().equals(new Integer(0x0C08)));
	assert (constants.get(1).getComments().get(0).equals(";Initialize 12 ports starting with port18."));
  }

  @Test
  public void testEntryPoints() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("0000 reset   ENTRY 0x0000");
	input.add("0100 hundred ENTRY base + offset");
	input.add("0200 more    ENTRY base + offset;comment");
	input.add("0300 final   ENTRY 0x0300       ;comment");

	Symbols symbols = readSymbols(input);
	ArrayList<Symbol> entries = symbols.getSymbolsByType(SymbolType.entryPoint);

	assert (entries.size() == 4);
	assert (entries.get(0).getType() == SymbolType.entryPoint);
	assert (entries.get(0).getName().equals("reset"));
	assert (entries.get(0).getValue().equals(new Integer(0)));
	assert (entries.get(0).getExpression().contentEquals("0x0000"));
	assert (entries.get(0).getComments().size() == 0);

	assert (entries.get(1).getType() == SymbolType.entryPoint);
	assert (entries.get(1).getName().equals("hundred"));
	assert (entries.get(1).getValue().equals(new Integer(256)));
	assert (entries.get(1).getExpression().contentEquals("base + offset"));
	assert (entries.get(1).getComments().size() == 0);

	assert (entries.get(2).getType() == SymbolType.entryPoint);
	assert (entries.get(2).getName().equals("more"));
	assert (entries.get(2).getValue().equals(new Integer(512)));
	assert (entries.get(2).getExpression().contentEquals("base + offset"));
	assert (entries.get(2).getComments().size() == 1);
	assert (entries.get(2).getComments().get(0).equals(";comment"));

	assert (entries.get(3).getType() == SymbolType.entryPoint);
	assert (entries.get(3).getName().equals("final"));
	assert (entries.get(3).getValue().equals(new Integer(0x0300)));
	assert (entries.get(3).getExpression().contentEquals("0x0300"));
	assert (entries.get(3).getComments().size() == 1);
	assert (entries.get(3).getComments().get(0).equals(";comment"));

	// check entry points
	assert (symbols.getEntryPoints().size() == 4);
	Symbol entryPoint = symbols.getEntryPoints().get(0);
	assert (entryPoint != null);
	assert (entryPoint == entries.get(0));
	entryPoint = symbols.getEntryPoints().get(0x0100);
	assert (entryPoint != null);
	assert (entryPoint == entries.get(1));
	entryPoint = symbols.getEntryPoints().get(0x0200);
	assert (entryPoint != null);
	assert (entryPoint == entries.get(2));
	entryPoint = symbols.getEntryPoints().get(0x0300);
	assert (entryPoint != null);
	assert (entryPoint == entries.get(3));

  }

  @Test
  public void testMixedSymbols() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("                        ;");
	input.add("                        ;I/O addresses:");
	input.add("0003            port03  EQU  0x03");
	input.add("00FE            SIO_A_C EQU  0xFE           ;SIO channel A, command register.");
	input.add("                        ;");
	input.add(
	    "                        ;SIO channel A, command register is initialised with (see 0x0321: 0x0322, 0x0324..0x032F):");
	input.add("                        ;");
	input.add("                        ;0x6E = 0156 -> Reset Rx CRC checker, Reset TxInt Pending, select register 6.");
	input.add("                        ;");
	input.add("                        ;Memory addresses:");
	input.add(
	    "1B00            vectors EQU  0x1B00         ;Base address for interrupt vector table. This byte initialised to 0xD8.");
	input.add("1B01            lbl1B01 EQU  0x1B01");
	input.add("                        ;");
	input.add("                        ;Constants:");
	input.add("0C08            c12ports EQU 12 * 256 + 8 ;Initialize 12 ports starting with port18.");
	input.add("0C11            cIniP11 EQU 12 * 256 + 11");
	input.add("        ;");
	input.add("        ;Entry points");
	input.add("0000 reset   ENTRY 0x0000");
	input.add("0100 hundred ENTRY base + offset");
	input.add("0200 more    ENTRY base + offset;comment");
	input.add("0300 final   ENTRY 0x0300       ;comment");

	Symbols symbols = readSymbols(input);
	ArrayList<Symbol> ports = symbols.getSymbolsByType(SymbolType.portAddress);

	assert (ports.size() == 2);
	assert (ports.get(0).getType() == SymbolType.portAddress);
	assert (ports.get(0).getName().equals("port03"));
	assert (ports.get(0).getValue().equals(new Integer(3)));
	assert (ports.get(0).getExpression().contentEquals("0x03"));

	assert (ports.get(1).getType() == SymbolType.portAddress);
	assert (ports.get(1).getName().equals("SIO_A_C"));
	assert (ports.get(1).getValue().equals(new Integer(254)));
	assert (ports.get(1).getExpression().contentEquals("0xFE"));
	assert (ports.get(1).getComments().size() == 6);
	assert (ports.get(1).getComments().get(0).equals(";SIO channel A, command register."));
	assert (ports.get(1).getComments().get(1).equals(";"));
	assert (ports.get(1).getComments().get(2)
	    .equals(";SIO channel A, command register is initialised with (see 0x0321: 0x0322, 0x0324..0x032F):"));
	assert (ports.get(1).getComments().get(3).equals(";"));
	assert (ports.get(1).getComments().get(4)
	    .equals(";0x6E = 0156 -> Reset Rx CRC checker, Reset TxInt Pending, select register 6."));
	assert (ports.get(1).getComments().get(1).equals(";"));

	ArrayList<Symbol> labels = symbols.getSymbolsByType(SymbolType.memoryAddress);

	assert (labels.size() == 2);
	assert (labels.get(0).getType() == SymbolType.memoryAddress);
	assert (labels.get(0).getName().equals("vectors"));
	assert (labels.get(0).getValue().equals(new Integer(0x1B00)));
	assert (labels.get(0).getExpression().contentEquals("0x1B00"));
	assert (labels.get(0).getComments().get(0)
	    .equals(";Base address for interrupt vector table. This byte initialised to 0xD8."));

	assert (labels.get(1).getType() == SymbolType.memoryAddress);
	assert (labels.get(1).getName().equals("lbl1B01"));
	assert (labels.get(1).getValue().equals(new Integer(0x1B01)));
	assert (labels.get(1).getExpression().contentEquals("0x1B01"));

	ArrayList<Symbol> constants = symbols.getSymbolsByType(SymbolType.constant);

	assert (constants.size() == 2);
	assert (constants.get(0).getType() == SymbolType.constant);

	assert (constants.get(0).getType() == SymbolType.constant);
	assert (constants.get(0).getName().equals("cIniP11"));
	assert (constants.get(0).getValue().equals(new Integer(0x0C11)));
	assert (constants.get(0).getExpression().contentEquals("12 * 256 + 11"));

	assert (constants.get(1).getName().equals("c12ports"));
	assert (constants.get(1).getValue().equals(new Integer(0x0C08)));
	assert (constants.get(1).getExpression().contentEquals("12 * 256 + 8"));
	assert (constants.get(1).getComments().get(0).equals(";Initialize 12 ports starting with port18."));
  }
}
