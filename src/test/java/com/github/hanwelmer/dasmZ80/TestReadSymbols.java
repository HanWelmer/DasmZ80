package com.github.hanwelmer.dasmZ80;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

public class TestReadSymbols extends DasmZ80 {

  @Test
  public void testPortSymbols() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add(";I/O Ports:");
	input.add("port03  EQU  0x03");
	input.add("SIO_A_C EQU  0xFE           ;SIO channel A, command register.");
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
	assert (ports.get(1).getValue().equals(new Integer(254)));
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
	input.add(";Memory locations:");
	input.add("vectors EQU  0x1B00         ;Base address for interrupt vector table. This byte initialised to 0xD8.");
	input.add("lbl1B01 EQU  0x1B01");
	Symbols symbols = readSymbols(input);
	ArrayList<Symbol> labels = symbols.getSymbolsByType(SymbolType.memoryAddress);

	assert (labels.size() == 2);
	assert (labels.get(0).getType() == SymbolType.memoryAddress);
	assert (labels.get(0).getName().equals("vectors"));
	assert (labels.get(0).getValue().equals(new Integer(0x1B00)));
	assert (labels.get(0).getComments().get(0)
	    .equals(";Base address for interrupt vector table. This byte initialised to 0xD8."));

	assert (labels.get(1).getType() == SymbolType.memoryAddress);
	assert (labels.get(1).getName().equals("lbl1B01"));
	assert (labels.get(1).getValue().equals(new Integer(0x1B01)));
  }

  @Test
  public void testConstants() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add(";Constants:");
	// TODO
	// input.add("c12ports EQU 12 * 256 + 8 ;Initialize 12 ports starting with
	// port18.");
	// TODO
	// input.add("cIniP11 EQU 12 * 256 + 11");
	input.add("c12ports EQU 12 ;Initialize 12 ports starting with port18.");
	// TODO
	// input.add("cIniP11 EQU 12");
	Symbols symbols = readSymbols(input);
	ArrayList<Symbol> constants = symbols.getSymbolsByType(SymbolType.constant);

	assert (constants.size() == 1);
	assert (constants.get(0).getType() == SymbolType.constant);
	assert (constants.get(0).getName().equals("c12ports"));
	// TODO
	// assert (constants.get(0).getValue().equals(new Integer(0x0C08)));
	assert (constants.get(0).getValue().equals(new Integer(12)));
	assert (constants.get(0).getComments().get(0).equals(";Initialize 12 ports starting with port18."));

	// TODO
	// assert (constants.get(1).getType() == SymbolType.constant);
	// assert (constants.get(1).getName().equals("cIniP11"));
	// TODO
	// assert (constants.get(1).getValue().equals(new Integer(0x0C11)));
	// TODO
	// assert (constants.get(1).getValue().equals(new Integer(12)));
  }
}
