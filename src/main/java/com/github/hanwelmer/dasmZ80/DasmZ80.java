package com.github.hanwelmer.dasmZ80;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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

  private static final String IDENTIFY_IO = ";I/O addresses:";
  private static final String IDENTIFY_MEMORY = ";Memory addresses:";
  private static final String IDENTIFY_CONSTANT = ";Constants:";
  protected static int startAddress = 0;
  protected static int finalAddress = 0;

  /**
   * The main method. See usage() for a functional description.
   * 
   * @param args
   *          the argument list.
   */
  public static void main(String[] args) {
	// process command line options and arguments.
	String fileName = "";
	String symbolsFileName = "";

	if (args.length != 1 && args.length != 3) {
	  usage(); // usage exits main.
	}

	if (args.length == 1) {
	  fileName = args[0];
	} else {
	  if (!args[0].startsWith("-")) {
		System.out.println("Expected an option, with '-' prefix.");
		usage(); // usage exits main.
	  }
	  if (!"-s".equals(args[0])) {
		System.out.println("Expected '-s' option.");
		usage(); // usage exits main.
	  }

	  symbolsFileName = args[1];
	  fileName = args[2];
	  // check symbols file extension.
	  if (!symbolsFileName.endsWith(".sym")) {
		System.out.println("Symbols file must have '.sym' extension.");
		usage(); // usage exits main.
	  }
	}

	// check symbols file extension.
	if (!fileName.endsWith(".bin")) {
	  System.out.println("File to be disassembled must have '.bin' extension.");
	  usage(); // usage exits main.
	}

	// do the main work.
	Symbols symbols = readSymbolsFile(symbolsFileName);
	disassembleFile(fileName, symbols);
  } // main()

  private static void usage() {
	System.out.println("Usage: java -jar dasmZ80.jar [-s file.sym] filename.ext");
	System.out.println(" where filename.ext is file to be disassembled");
	System.out
	    .println("  and -s file.sym is an optional input file with symbol definitions, comments and entry points.");
	System.out.println("File filename must have extension .bin");
	System.out.println(" and must be in binary format.");
	System.out.println(" Binary code is assumed to be Z80 compatible.");
	System.out.println(" Start address is assumed to be 0x0000.");
	System.exit(1);
  } // usage()

  private static Symbols readSymbolsFile(String fileName) {
	Symbols symbols = new Symbols();
	SymbolFileReader input = new SymbolFileReader();
	try {
	  input.open(fileName);
	  symbols = readSymbols(input);
	} catch (FileNotFoundException e) {
	  System.out.println(e.getMessage());
	} catch (IOException e) {
	  System.out.println(e.getMessage());
	} finally {
	  if (input != null) {
		input.close();
	  }
	}
	return symbols;
  } // readSymbolsFile()

  protected static Symbols readSymbols(AbstractSymbolReader input) throws IOException {
	Symbols symbols = new Symbols();
	SymbolType type = SymbolType.constant;
	Symbol previousSymbol = null;
	while (input.ready()) {
	  String line = input.readLine().trim();
	  if (line.contains(IDENTIFY_IO)) {
		type = SymbolType.portAddress;
	  } else if (line.contains(IDENTIFY_MEMORY)) {
		type = SymbolType.memoryAddress;
	  } else if (line.contains(IDENTIFY_CONSTANT)) {
		type = SymbolType.constant;
	  } else {
		// 0010 name EQU expression;comment
		// or:
		// 0010 name ENTRY expression;comment
		// or:
		// ;comment
		String firstWord = input.getWord();
		if (firstWord.length() != 0) {
		  char firstChar = firstWord.charAt(0);
		  if (firstChar == ';') {
			if (previousSymbol != null && previousSymbol.getComments().size() > 0) {
			  previousSymbol.addComment(firstWord);
			}
		  } else {
			// value
			if (firstWord.length() == 0 || !(Character.isDigit(firstWord.charAt(0))
			    || firstWord.charAt(0) >= 'A' && firstWord.charAt(0) <= 'F')) {
			  throw new IOException(String.format("value must begin with a digit, received: %s", firstWord));
			}
			Integer value = Integer.decode("0x" + firstWord);

			// name
			String name = input.getWord();
			if (!(Character.isLetter(firstChar) || firstChar != '_')) {
			  throw new IOException(String.format("symbol must begin with a character, received: %s", name));
			}

			// EQU or ENTRY
			String statement = input.getWord();
			if ("ENTRY".equals(statement)) {
			  type = SymbolType.entryPoint;
			} else if (!"EQU".equals(statement)) {
			  throw new IOException(String.format("expected EQU, received: %s", statement));
			}

			// expression
			String expression = input.getValue().trim();
			if (expression.length() == 0) {
			  throw new IOException(String.format("expression expected"));
			}

			previousSymbol = symbols.getOrMakeSymbol(name, type, value, expression);

			// comment
			String comment = input.getWord();
			if (comment.length() > 0 && comment.charAt(0) == ';') {
			  previousSymbol.addComment(comment);
			}
		  }
		}
	  }
	}
	return symbols;
  } // readSymbols()

  protected static void disassembleFile(String fileName, Symbols symbols) {
	BinFileReader reader = new BinFileReader();
	ListingWriter writer = new ListingWriter();
	try {
	  reader.open(fileName);
	  finalAddress = reader.getSize();
	  writer.open(changeExtension(fileName));
	  disassembleToWriter(fileName, reader, writer, symbols);
	} catch (FileNotFoundException e) {
	  System.out.println("Error opening file " + fileName);
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	} catch (IOException e) {
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	} finally {
	  reader.close();
	  if (writer != null) {
		writer.close();
	  }
	}
  } // disassembleFile()

  private static String changeExtension(String fileName) {
	if (fileName.endsWith(".bin")) {
	  return fileName.replace(".bin", ".lst");
	} else if (fileName.endsWith(".hex")) {
	  return fileName.replace(".hex", ".lst");
	} else {
	  return fileName + ".lst";
	}
  } // changeExtension()

  protected static void disassembleToWriter(String fileName, ByteReader reader, AbstractWriter writer,
      Symbols symbols) {
	// Do the actual disassembly.
	ArrayList<AssemblyCode> decoded = disassembleReader(fileName, reader, symbols);

	// Fill in symbols to memory addresses.
	fillInLabels(decoded, symbols);

	// Write everything to the output file.
	writeDefinitions(fileName, writer, symbols);
	writeOutput(writer, reader, decoded);
	writeReferences(writer, symbols);
  } // disassembleToWriter()

  protected static ArrayList<AssemblyCode> disassembleReader(String fileName, ByteReader reader, Symbols symbols) {
	Decoder decoder = new Decoder();
	decoder.setReader(reader);
	ArrayList<AssemblyCode> decoded = new ArrayList<AssemblyCode>();

	// Add default entry point if none are defined
	HashMap<Integer, Symbol> entryPoints = symbols.getEntryPoints();
	if (entryPoints.isEmpty()) {
	  String addr = String.format("%04X", startAddress);
	  entryPoints.put(startAddress, new Symbol("ep" + addr, SymbolType.entryPoint, startAddress, "0x" + addr));

	  String msg = String.format("No entry points defined; assuming 0x%s as entry point", addr);
	  decoded.add(new AssemblyCode(startAddress, msg));
	}

	// loop through all entry points
	HashMap<Integer, Path> paths = new HashMap<Integer, Path>();
	try {
	  while (!entryPoints.isEmpty()) {
		// Get first entry point.
		Object[] keys = entryPoints.keySet().toArray();
		Symbol point = entryPoints.get(keys[0]);
		// Eat current entry point.
		entryPoints.remove(point.getValue());
		if (point.getValue() >= startAddress && point.getValue() < finalAddress
		    && pointNotVisited(paths, point.getValue())) {
		  // Add current entry point to symbol list
		  symbols.addSymbol(point);
		  // Disassemble code path that starts at the entry point.
		  ArrayList<AssemblyCode> codePath = disassemblePath(point, decoder, entryPoints, symbols);
		  paths.put(point.getValue(), new Path(point, codePath));
		}
	  }
	} catch (IOException e) {
	  System.out.println("Error reading from input file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}

	// collate paths into single sorted list of decoded assembler instructions.
	Object[] keys = paths.keySet().toArray();
	Arrays.sort(keys);
	for (Object key : keys) {
	  decoded.addAll(paths.get((Integer) key).decoded);
	}

	return decoded;
  } // disassembleReader()

  private static boolean pointNotVisited(HashMap<Integer, Path> paths, Integer value) {
	if (paths == null) {
	  return true;
	}

	boolean found = false;
	for (HashMap.Entry<Integer, Path> entry : paths.entrySet()) {
	  Path path = entry.getValue();
	  if (value >= path.startAddress && value < path.nextAddress) {
		found = true;
	  }
	}
	return !found;
  } // pointNotVisited()

  private static ArrayList<AssemblyCode> disassemblePath(Symbol entryPoint, Decoder decoder,
      HashMap<Integer, Symbol> entryPoints, Symbols symbols) throws IOException {
	ArrayList<AssemblyCode> decoded = new ArrayList<AssemblyCode>();
	int address = entryPoint.getValue();
	AssemblyCode nextInstruction;

	try {
	  do {
		nextInstruction = decoder.get(address, symbols);
		if (nextInstruction != null) {
		  decoded.add(nextInstruction);
		  // Determine next address.
		  address += nextInstruction.getBytes().size();
		}
	  } while (nextInstruction != null && !nextInstruction.isExit());
	} catch (IllegalOpcodeException e) {
	  decoded.add(new AssemblyCode(address, e.getMessage()));
	  System.out.print(e.getMessage());
	}
	return decoded;
  } // disassemble()

  private static void fillInLabels(ArrayList<AssemblyCode> decoded, Symbols symbols) {
	fillInLabelTypes(decoded, symbols, SymbolType.entryPoint);
	fillInLabelTypes(decoded, symbols, SymbolType.label);
	fillInLabelTypes(decoded, symbols, SymbolType.memoryAddress);
  } // fillInLabels()

  private static void fillInLabelTypes(ArrayList<AssemblyCode> decoded, Symbols symbols, SymbolType symbolType) {
	int index = 0;
	for (Symbol symbol : symbols.getSymbolsByType(symbolType)) {
	  // Look up the line where the label must be set.
	  while (index < decoded.size() && decoded.get(index).getAddress() <= symbol.getValue()) {
		index++;
	  }
	  // Set the label.
	  if (index > 0 && decoded.get(index - 1).getAddress() == symbol.getValue()) {
		decoded.get(index - 1).setLabel(symbol.getName());
	  }
	}
  } // fillInLabelTypes()

  protected static void writeDefinitions(String fileName, AbstractWriter writer, Symbols symbols) {
	String msg = String.format(";File generated by dasmZ80.jar Z80 disassembler from %s", fileName);
	try {
	  writer.write(new AssemblyCode(0, null, msg));

	  ArrayList<Symbol> symbolList = symbols.getSymbolsByType(SymbolType.portAddress);
	  if (symbolList.size() > 0) {
		writer.write(new AssemblyCode(0, null, ";"));
		writer.write(new AssemblyCode(0, null, IDENTIFY_IO));
	  }
	  for (Symbol symbol : symbolList) {
		writer.write(symbol.toString());
	  }

	  symbolList = symbols.getSymbolsByType(SymbolType.memoryAddress);
	  symbolList.addAll(symbols.getSymbolsByType(SymbolType.label));
	  boolean done = false;
	  for (Symbol symbol : symbolList) {
		// ignore labels within the range of the disassembled code.
		if (symbol.getType() != SymbolType.label || symbol.getType() == SymbolType.label
		    && (symbol.getValue() < startAddress || symbol.getValue() > finalAddress)) {
		  if (!done) {
			writer.write(new AssemblyCode(0, null, ";"));
			writer.write(new AssemblyCode(0, null, IDENTIFY_MEMORY));
			done = true;
		  }
		  writer.write(symbol.toString());
		}
	  }

	  symbolList = symbols.getSymbolsByType(SymbolType.constant);
	  if (symbolList.size() > 0) {
		writer.write(new AssemblyCode(0, null, ";"));
		writer.write(new AssemblyCode(0, null, IDENTIFY_CONSTANT));
	  }
	  for (Symbol symbol : symbolList) {
		writer.write(symbol.toString());
	  }

	  writer.write(new AssemblyCode(0, null, ";"));
	  writer.write(new AssemblyCode(0, String.format("org 0x%04X", 0)));
	} catch (IOException e) {
	  System.out.println("Error writing to output file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}
  } // writeDefinitions()

  protected static void writeOutput(AbstractWriter writer, ByteReader reader, ArrayList<AssemblyCode> decoded) {
	try {
	  int nextAddress = startAddress;
	  int readerAddress = nextAddress;
	  for (AssemblyCode line : decoded) {
		nextAddress = line.getAddress();
		if (readerAddress != nextAddress) {
		  // add a blank line after a range of consecutive disassembled code..
		  writer.write(new AssemblyCode(readerAddress, ";"));
		  writeUnvisitedCode(readerAddress, nextAddress, writer, reader);
		}
		writer.write(line);
		if (line.getBytes() != null) {
		  nextAddress += line.getBytes().size();
		}
		readerAddress = nextAddress;
	  }
	  // Add unvisited code beyond last decoded address.
	  writer.write(new AssemblyCode(readerAddress, ";"));
	  writeUnvisitedCode(readerAddress, reader.getSize(), writer, reader);
	  writer.write(new AssemblyCode(reader.getSize(), "end"));
	} catch (IOException e) {
	  System.out.println("Error writing to output file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}
  } // writeOutput()

  private static void writeUnvisitedCode(int fromAddress, int toAddress, AbstractWriter writer, ByteReader reader)
      throws IOException {
	ArrayList<Byte> bytes = new ArrayList<Byte>();
	int lastAddress = fromAddress;
	while (fromAddress < toAddress) {
	  bytes.add(reader.getByte(fromAddress++));
	  if (fromAddress % 4 == 0) {
		writeDefineBytes(bytes, lastAddress, writer);
		lastAddress += bytes.size();
		bytes.clear();
	  }
	}
	writeDefineBytes(bytes, lastAddress, writer);
  } // writeUnvisitedCode()

  private static void writeDefineBytes(ArrayList<Byte> bytes, int lastAddress, AbstractWriter writer)
      throws IOException {
	if (bytes.size() != 0) {
	  String def = String.format("%04X ", lastAddress);
	  int spaces = 19;
	  for (Byte byt : bytes) {
		def += String.format("%02X", byt);
		spaces--;
		spaces--;
	  }
	  // String format = String.format("%dsDB ", spaces);
	  String format = "%" + spaces + "sDB  ";
	  def += String.format(format, " ");
	  String comma = " ";
	  for (Byte byt : bytes) {
		def += String.format("%s0x%02X", comma, byt);
		comma = ", ";
	  }
	  def += "\n";
	  writer.write(def);
	}
  } // writeDefineBytes()

  private static void writeReferences(AbstractWriter writer, Symbols symbols) {
	try {
	  // Write references to port addresses.
	  ArrayList<Symbol> symbolList = symbols.getSymbolsByType(SymbolType.portAddress);
	  if (symbolList.size() > 0) {
		writer.write("\nI/O address cross reference list:\n");
	  }
	  for (Symbol symbol : symbolList) {
		writeSymbolReferences(symbol, 2, writer);
	  }

	  // Write references to entry points, labels and memory addresses.
	  symbolList = symbols.getSymbolsByType(SymbolType.label);
	  symbolList.addAll(symbols.getSymbolsByType(SymbolType.entryPoint));
	  symbolList.addAll(symbols.getSymbolsByType(SymbolType.memoryAddress));
	  Object[] sortedSymbolList = symbolList.toArray();
	  Arrays.sort(sortedSymbolList);
	  if (symbolList.size() > 0) {
		writer.write("\nMemory address cross reference list:\n");
	  }
	  for (Object symbol : sortedSymbolList) {
		writeSymbolReferences((Symbol) symbol, 4, writer);
	  }

	  // Write references to constants.
	  symbolList = symbols.getSymbolsByType(SymbolType.constant);
	  if (symbolList.size() > 0) {
		writer.write("\nConstants reference list:\n");
	  }
	  for (Symbol symbol : symbolList) {
		writeSymbolReferences(symbol, 4, writer);
	  }
	} catch (IOException e) {
	  System.out.println("Error writing to output file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}
  } // writeReferences()

  private static void writeSymbolReferences(Symbol symbol, int length, AbstractWriter writer) throws IOException {
	boolean mustPrint = true;
	int i = 0;
	String format1 = "%-8s=%0" + length + "X:";
	String format2 = "%3" + (length + 1) + "s";
	String msg = String.format(format1, symbol.getName(), symbol.getValue());
	for (Integer reference : symbol.getReferences()) {
	  msg += String.format(" %04X", reference);
	  mustPrint = true;
	  if (++i == 8) {
		msg += "\n";
		writer.write(msg);
		msg = String.format(format2, " ");
		i = 0;
		mustPrint = false;
	  }
	}
	if (mustPrint) {
	  msg += "\n";
	  writer.write(msg);
	}
  } // writeReferencesTo()

}
