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
  /**
   * The main method. See usage() for a functional description.
   * 
   * @param args
   *          the argument list.
   */
  public static void main(String[] args) {
	// process command line options and arguments.
	if (args.length == 1) {
	  String fileName = args[args.length - 1];
	  if (fileName.endsWith(".bin")) {
		// do the main work.
		disassemble(fileName);
	  } else {
		usage();
	  }
	} else {
	  usage();
	}
  } // main()

  private static void usage() {
	System.out.println("Usage: java -jar dasmZ80.jar filename.ext");
	System.out.println(" where filename.ext is file to be disassembled");
	System.out.println(" which must have extension .bin");
	System.out.println(" which must be in binary format.");
	System.out.println(" Binary code is assumed to be Z80 compatible.");
	System.out.println(" Start address is assumed to be 0x0000.");
	System.exit(1);
  } // usage()

  protected static void disassemble(String fileName) {
	BinFileReader reader = new BinFileReader();
	ListingWriter lstWriter = new ListingWriter();
	try {
	  reader.open(fileName);
	  lstWriter.open(fileName);
	  disassemble(fileName, reader, lstWriter);
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

  protected static HashMap<Integer, AssemblyCode> disassemble(String fileName, ByteReader reader,
      AbstractWriter writer) {
	Decoder decoder = new Decoder();
	HashMap<Integer, AssemblyCode> decoded = new HashMap<Integer, AssemblyCode>();
	Map<Integer, Definition> portReferences = new HashMap<Integer, Definition>();
	Map<Integer, Definition> memoryReferences = new HashMap<Integer, Definition>();
	int address = 0;
	int lineNr = 0;
	Byte nextByte = null;

	try {
	  while ((nextByte = reader.getByte()) != null) {
		AssemblyCode asmCode = decoder.get(address, nextByte, reader, portReferences, memoryReferences);
		decoded.put(++lineNr, asmCode);
		address += asmCode.getBytes().size();
		if (asmCode.getMnemonic().startsWith("RET")) {
		  decoded.put(++lineNr, new AssemblyCode(address, ""));
		}
	  }
	  writeDefinitions(fileName, writer, portReferences, memoryReferences);
	  writeOutput(address, decoded, writer);
	  writeReferences(writer, portReferences, memoryReferences);
	} catch (IllegalOpcodeException e) {
	  System.out.print(e.getMessage());
	  String msg = e.getMessage().trim();
	  decoded.put(++lineNr, new AssemblyCode(address, msg));
	  decoded.put(++lineNr, new AssemblyCode(address, ""));
	  writeDefinitions(fileName, writer, portReferences, memoryReferences);
	  writeOutput(address, decoded, writer);
	  writeRemainderOfInput(address, reader, writer);
	  writeReferences(writer, portReferences, memoryReferences);
	} catch (IOException e) {
	  System.out.println("Error reading from input file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}
	return decoded;
  } // disassemble()

  private static void writeDefinitions(String fileName, AbstractWriter writer, Map<Integer, Definition> portReferences,
      Map<Integer, Definition> memoryReferences) {
	String msg = String.format(";File generated by dasmZ80.jar Z80 disassembler from %s", fileName);
	try {
	  writer.write(new AssemblyCode(0, msg));
	  writer.write(new AssemblyCode(0, ""));

	  if (portReferences.size() > 0) {
		writer.write(new AssemblyCode(0, ";I/O Port definitions"));
		SortedSet<Integer> keys = new TreeSet<>(portReferences.keySet());
		for (Integer key : keys) {
		  Definition def = portReferences.get(key);
		  msg = String.format("0000%12s%s", " ", def.getName());
		  if (msg.length() < 23) {
			String format = "%" + (23 - msg.length()) + "s";
			msg += String.format(format, " ");
		  }
		  writer.write(msg + String.format(" EQU  %02X\n", def.getValue()));
		}
	  }

	  writer.write(new AssemblyCode(0, ""));
	  writer.write(new AssemblyCode(0, String.format("org 0x%04X", 0)));
	} catch (IOException e) {
	  System.out.println("Error writing to output file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}
  } // writeDefinitions()

  protected static void writeOutput(int address, HashMap<Integer, AssemblyCode> decoded, AbstractWriter writer) {
	try {
	  SortedSet<Integer> keys = new TreeSet<>(decoded.keySet());
	  for (Integer key : keys) {
		writer.write(decoded.get(key));
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

  private static void writeReferences(AbstractWriter writer, Map<Integer, Definition> portReferences,
      Map<Integer, Definition> memoryReferences) {
	try {
	  writer.write("\nI/O-port cross reference list:\n");
	  // Sort port addresses.
	  SortedSet<Integer> ports = new TreeSet<>(portReferences.keySet());
	  // Write references to port addresses.
	  for (Integer port : ports) {
		String msg = String.format("%02X:", port);
		int i = 0;
		for (Integer reference : portReferences.get(port).getReferences()) {
		  msg += String.format(" %04X", reference);
		  if (++i == 8) {
			i = 0;
			msg += "\n";
			writer.write(msg);
			msg = String.format("%3s", " ");
		  }
		}
		if (i > 0) {
		  msg += "\n";
		  writer.write(msg);
		}
	  }

	  writer.write("\nMemory cross reference list:\n");
	  // Sort memory addresses.
	  SortedSet<Integer> labels = new TreeSet<>(memoryReferences.keySet());
	  // Write references to memory addresses.
	  for (Integer label : labels) {
		String msg = String.format("%04X:", label);
		int i = 0;
		for (Integer reference : memoryReferences.get(label).getReferences()) {
		  msg += String.format(" %04X", reference);
		  if (++i == 4) {
			i = 0;
			msg += "\n";
			writer.write(msg);
			msg = String.format("%5s", " ");
		  }
		}
		if (i > 0) {
		  msg += "\n";
		  writer.write(msg);
		}
	  }
	} catch (IOException e) {
	  System.out.println("Error writing to output file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}
  }

}
