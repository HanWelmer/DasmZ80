package com.github.hanwelmer.dasmZ80;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Z80 disassembler, written in Java.
 */
// Known limitations:
// - Input file must be in binary format (Intel Hex format not supported yet).
// - Input filename must have extension bin.
// - Start address is assumed to be 0x0000 (no CLI parameter yet to define start
// address).
// - Binary code is assumed to be Z80 compatible (no CLI parameter yet to define
// type of processor).
// - Disassembler stops at first unrecognized or illegal opcode (No input file
// yet to define addresses or ranges for data blocks).
// - Output in listing format (no CLI parameter yet to select assembler or
// listing output format).
// - Output is to system output (screen or pipe) (no CLI argument yet to define
// output filename).
public class DasmZ80 {

  private static int startAddress = 0;
  private static int finalAddress = 0;

  /**
   * The main method. See usage() for a functional description.
   * 
   * @param args
   *          the argument list.
   */
  public static void main(String[] args) {
	// process command line options and arguments.
	if (args.length == 0) {
	  usage();
	}

	int index = 0;
	String fileName = "";
	String symbolsFileName = "";
	while (index < args.length) {
	  String arg = args[index];
	  if ("-s".equals(arg)) {
		index++;
		if (index < args.length) {
		  symbolsFileName = args[index];
		} else {
		  usage();
		}
	  } else if (arg.startsWith("-")) {
		usage();
	  } else {
		fileName = arg;
	  }
	  index++;
	}

	Map<Integer, Symbol> symbols = new HashMap<Integer, Symbol>();
	if (symbolsFileName.endsWith(".sym")) {
	  // process the symbols file.
	  symbols = readSymbols(symbolsFileName);
	} else {
	  usage();
	}

	if (fileName.endsWith(".bin")) {
	  // do the main work.
	  disassemble(fileName, symbols);
	} else {
	  usage();
	}
  } // main()

  private static Map<Integer, Symbol> readSymbols(String symbolsFileName) {
	Map<Integer, Symbol> symbols = new HashMap<Integer, Symbol>();
	return symbols;
  }

  private static void usage() {
	System.out.println("Usage: java -jar dasmZ80.jar [-s file.sym] filename.ext");
	System.out.println(" where filename.ext is file to be disassembled");
	System.out.println("  and -s file.sym is an optional input file with symbol definitions and comments.");
	System.out.println("File filename must have extension .bin");
	System.out.println(" and must be in binary format.");
	System.out.println(" Binary code is assumed to be Z80 compatible.");
	System.out.println(" Start address is assumed to be 0x0000.");
	System.exit(1);
  } // usage()

  protected static void disassemble(String fileName, Map<Integer, Symbol> symbols) {
	BinFileReader reader = new BinFileReader();
	ListingWriter lstWriter = new ListingWriter();
	try {
	  reader.open(fileName);
	  lstWriter.open(fileName);
	  disassemble(fileName, reader, lstWriter, symbols);
	} catch (FileNotFoundException e) {
	  System.out.println("Error opening file " + fileName);
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	} catch (IOException e) {
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	} finally {
	  reader.close();
	  if (lstWriter != null) {
		lstWriter.close();
	  }
	}
  } // disassemble()

  protected static void disassemble(String fileName, ByteReader reader, AbstractWriter writer,
      Map<Integer, Symbol> symbols) {
	Decoder decoder = new Decoder();
	ArrayList<AssemblyCode> decoded = new ArrayList<AssemblyCode>();
	int address = startAddress;
	Byte nextByte = null;

	try {
	  // Disassemble the input file.
	  while ((nextByte = reader.getByte()) != null) {
		AssemblyCode asmCode = decoder.get(address, nextByte, reader, symbols);
		decoded.add(asmCode);
		address += asmCode.getBytes().size();
		if (asmCode.getMnemonic().startsWith("RET")) {
		  decoded.add(new AssemblyCode(address, ""));
		}
	  }
	  finalAddress = address;

	  // Fill in the memory address labels.
	  fillInLabels(decoded, symbols);

	  // Write everything to the output file.
	  writeDefinitions(fileName, writer, symbols);
	  writeOutput(address, decoded, writer);
	  writeReferences(writer, symbols);
	} catch (IllegalOpcodeException e) {
	  System.out.print(e.getMessage());
	  String msg = e.getMessage().trim();
	  decoded.add(new AssemblyCode(address, msg));
	  decoded.add(new AssemblyCode(address, ""));

	  fillInLabels(decoded, symbols);
	  writeDefinitions(fileName, writer, symbols);
	  writeOutput(address, decoded, writer);
	  writeRemainderOfInput(address, reader, writer);
	  writeReferences(writer, symbols);
	} catch (IOException e) {
	  System.out.println("Error reading from input file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}
  } // disassemble()

  private static void fillInLabels(ArrayList<AssemblyCode> decoded, Map<Integer, Symbol> symbols) {
	// Write references to memory addresses.
	int index = 0;
	SortedSet<Integer> addresses = new TreeSet<>(symbols.keySet());
	for (Integer labelAddress : addresses) {
	  Symbol symbol = symbols.get(labelAddress);
	  // only symbols for memory address
	  if (symbol.getType() == SymbolType.memoryAddress) {
		// Look up the line where the label must be set.
		while (index < decoded.size() && decoded.get(index).getAddress() <= labelAddress) {
		  index++;
		}
		// Set the label.
		if (index > 0 && decoded.get(index - 1).getAddress() == labelAddress) {
		  decoded.get(index - 1).setLabel(symbol.getName());
		}
	  }
	}
  } // fillInLabels()

  private static void writeDefinitions(String fileName, AbstractWriter writer, Map<Integer, Symbol> symbols) {
	String msg = String.format(";File generated by dasmZ80.jar Z80 disassembler from %s", fileName);
	try {
	  writer.write(new AssemblyCode(0, null, msg));
	  writer.write(new AssemblyCode(0, null, ";"));

	  if (symbols.size() > 0) {
		SortedSet<Integer> keys = new TreeSet<>(symbols.keySet());

		boolean done = false;
		for (Integer key : keys) {
		  Symbol symbol = symbols.get(key);
		  if (symbol.getType() == SymbolType.portAddress) {
			if (!done) {
			  writer.write(new AssemblyCode(0, null, ";I/O Port definitions"));
			  done = true;
			}
			writer.write(String.format("%16s%-7s EQU  %02X\n", " ", symbol.getName(), symbol.getValue()));
		  }
		}

		done = false;
		for (Integer key : keys) {
		  Symbol symbol = symbols.get(key);
		  if (symbol.getType() == SymbolType.memoryAddress
		      && (symbol.getValue() < startAddress || symbol.getValue() > finalAddress)) {
			if (!done) {
			  writer.write(new AssemblyCode(0, null, ";Memory addresses"));
			  done = true;
			}
			writer.write(String.format("%16s%-7s EQU  %04X\n", " ", symbol.getName(), symbol.getValue()));
		  }
		}

		done = false;
		for (Integer key : keys) {
		  Symbol symbol = symbols.get(key);
		  if (symbol.getType() == SymbolType.constant) {
			if (!done) {
			  writer.write(new AssemblyCode(0, null, ";Constants"));
			  done = true;
			}
			writer.write(String.format("%16s%-7s EQU  %04X\n", " ", symbol.getName(), symbol.getValue()));
		  }
		}
	  }

	  writer.write(new AssemblyCode(0, null, ";"));
	  writer.write(new AssemblyCode(0, String.format("org 0x%04X", 0)));
	} catch (IOException e) {
	  System.out.println("Error writing to output file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}
  } // writeDefinitions()

  protected static void writeOutput(int address, ArrayList<AssemblyCode> decoded, AbstractWriter writer) {
	try {
	  for (AssemblyCode line : decoded) {
		writer.write(line);
	  }
	  writer.write(new AssemblyCode(address, "end"));
	} catch (IOException e) {
	  System.out.println("Error writing to output file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}
  } // writeOutput()

  private static void writeRemainderOfInput(int address, ByteReader reader, AbstractWriter writer) {
	try {
	  int startOfLine = address;
	  writer.write(new AssemblyCode(startOfLine, ";"));
	  writer.write(new AssemblyCode(startOfLine, ";Unprocessed binary code from input file"));
	  writer.write(new AssemblyCode(startOfLine, ";"));

	  reader.seek(address);
	  Byte nextByte;
	  ArrayList<Byte> bytes = new ArrayList<>();
	  while ((nextByte = reader.getByte()) != null) {
		bytes.add(nextByte);
		address++;
		if (address % 16 == 0) {
		  writer.write(new AssemblyCode(startOfLine, bytes));
		  bytes.clear();
		  startOfLine = address;
		}
	  }
	  if (bytes.size() != 0) {
		writer.write(new AssemblyCode(startOfLine, bytes));
	  }
	} catch (IOException e) {
	  System.out.println("Error reading from input file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}
  }

  private static void writeReferences(AbstractWriter writer, Map<Integer, Symbol> symbols) {
	try {
	  // Sort symbol values.
	  SortedSet<Integer> symbolValues = new TreeSet<>(symbols.keySet());

	  // Write references to port addresses.
	  boolean done = false;
	  for (Integer value : symbolValues) {
		Symbol symbol = symbols.get(value);
		if (symbol.getType() == SymbolType.portAddress) {
		  if (!done) {
			writer.write("\nI/O-port cross reference list:\n");
			done = true;
		  }
		  writeReferencesTo(symbol, 2, writer);
		}
	  }

	  // Write references to memory addresses.
	  done = false;
	  for (Integer value : symbolValues) {
		Symbol symbol = symbols.get(value);
		if (symbol.getType() == SymbolType.memoryAddress) {
		  if (!done) {
			writer.write("\nMemory cross reference list:\n");
			done = true;
		  }
		  writeReferencesTo(symbol, 4, writer);
		}
	  }

	  // Write references to constants.
	  done = false;
	  for (Integer value : symbolValues) {
		Symbol symbol = symbols.get(value);
		if (symbol.getType() == SymbolType.constant) {
		  if (!done) {
			writer.write("\nConstants reference list:\n");
			done = true;
		  }
		  writeReferencesTo(symbol, 4, writer);
		}
	  }
	} catch (IOException e) {
	  System.out.println("Error writing to output file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}
  }

  private static void writeReferencesTo(Symbol symbol, int length, AbstractWriter writer) throws IOException {
	int i = 0;
	String format1 = "%-8s=%0" + length + "X:";
	String format2 = "%3" + (length + 1) + "s";
	String msg = String.format(format1, symbol.getName(), symbol.getValue());
	for (Integer reference : symbol.getReferences()) {
	  msg += String.format(" %04X", reference);
	  if (++i == 8) {
		i = 0;
		msg += "\n";
		writer.write(msg);
		msg = String.format(format2, " ");
	  }
	}
	if (i > 0) {
	  msg += "\n";
	  writer.write(msg);
	}
  }

}
