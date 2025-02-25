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

  private static final String IDENTIFY_COMMENT = ";Comments:";
  private static final String IDENTIFY_CONSTANT = ";Constants:";
  private static final String IDENTIFY_IO = ";I/O addresses:";
  private static final String IDENTIFY_MEMORY = ";Memory addresses:";
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
	  if (line.contains(IDENTIFY_COMMENT)) {
		type = SymbolType.comment;
	  } else if (line.contains(IDENTIFY_CONSTANT)) {
		type = SymbolType.constant;
	  } else if (line.contains(IDENTIFY_IO)) {
		type = SymbolType.portAddress;
	  } else if (line.contains(IDENTIFY_MEMORY)) {
		type = SymbolType.memoryAddress;
	  } else {
		// 0010 name EQU expression;comment
		// or:
		// 0010 name ENTRY expression;comment
		// or:
		// 0010 ;some comment
		// or:
		// ;some comment
		String firstWord = input.getWord();
		if (firstWord.length() != 0) {
		  char firstChar = firstWord.charAt(0);
		  if (firstChar == ';') {
			if (previousSymbol != null && previousSymbol.getComments().size() > 0) {
			  previousSymbol.addComment(firstWord);
			}
		  } else {
			// value
			if (firstWord.length() == 0 || !isHexDigit(firstWord.charAt(0))) {
			  throw new IOException(
			      String.format("value must begin with a hexadecimal digit, received: %s", firstWord));
			}
			Integer value = Integer.decode("0x" + firstWord);

			if (type != SymbolType.comment) {
			  previousSymbol = parseNonCommentSymbol(input, type, value, symbols);
			} else {
			  previousSymbol = symbols.getOrMakeCommentSymbol(value);
			}
			// skip until comment
			String comment = input.getWord();
			while (comment.length() > 0 && comment.charAt(0) != ';') {
			  comment = input.getWord();
			}
			if (comment.length() > 0 && comment.charAt(0) == ';') {
			  previousSymbol.addComment(comment);
			}
		  }
		}
	  }
	}
	return symbols;

  } // readSymbols()

  private static Symbol parseNonCommentSymbol(AbstractSymbolReader input, SymbolType type, Integer value,
      Symbols symbols) throws IOException {
	// name
	String name = input.getWord();
	char firstChar = name.charAt(0);
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
	return symbols.getOrMakeSymbol(name, type, value, expression);
  }

  private static boolean isHexDigit(char character) {
	return Character.isDigit(character) || character >= 'A' && character <= 'F';
  }

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
	// Add default entry point if none are defined
	HashMap<Integer, Symbol> entryPoints = symbols.getEntryPoints();
	AssemblyCode prefix = null;
	if (entryPoints.isEmpty()) {
	  String addr = String.format("%04X", startAddress);
	  entryPoints.put(startAddress, new Symbol("ep" + addr, SymbolType.entryPoint, startAddress, "0x" + addr));

	  String msg = String.format("No entry points defined; assuming 0x%s as entry point", addr);
	  prefix = new AssemblyCode(startAddress, msg);
	}

	// Do the actual disassembly.
	HashMap<Integer, Path> paths = disassembleReader(fileName, reader, symbols);

	// Fill in symbols to memory addresses and comments.
	fillInSymbols(paths, symbols);

	// Write everything to the output file.
	try {
	  writeDefinitions(fileName, writer, symbols);
	  if (paths.size() != 0 && prefix != null) {
		writer.write(prefix);
	  }
	  writeOutput(writer, reader, paths, symbols);
	  writeReferences(writer, symbols);
	} catch (IOException e) {
	  System.out.println("Error writing to output file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}
  } // disassembleToWriter()

  protected static HashMap<Integer, Path> disassembleReader(String fileName, ByteReader reader, Symbols symbols) {
	Decoder decoder = new Decoder();
	decoder.setReader(reader);

	// loop through all entry points
	HashMap<Integer, Symbol> entryPoints = symbols.getEntryPoints();
	HashMap<Integer, Path> paths = new HashMap<Integer, Path>();
	try {
	  while (!entryPoints.isEmpty()) {
		// Get first entry point.
		Object[] keys = entryPoints.keySet().toArray();
		Symbol entryPoint = entryPoints.get(keys[0]);
		// Eat current entry point.
		entryPoints.remove(entryPoint.getValue());
		if (entryPoint.getValue() >= startAddress && entryPoint.getValue() < finalAddress
		    && pointNotVisited(paths, entryPoint.getValue())) {
		  // Add current entry point to symbol list
		  symbols.addAsMemoryAddress(entryPoint);
		  // Disassemble code path that starts at the entry point.
		  ArrayList<AssemblyCode> codePath = disassemblePath(entryPoint, decoder, entryPoints, symbols);
		  paths.put(entryPoint.getValue(), new Path(entryPoint, codePath));
		}
	  }
	} catch (IOException e) {
	  System.out.println("Error reading from input file.");
	  System.out.println(e.getMessage());
	  e.printStackTrace();
	}

	return paths;
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
	  boolean exit = false;
	  while (!exit) {
		nextInstruction = decoder.get(address, symbols);
		if (nextInstruction == null) {
		  exit = true;
		} else {
		  decoded.add(nextInstruction);
		  exit = nextInstruction.isExit();
		  // Determine next address.
		  address += nextInstruction.getBytes().size();
		  // Add comments from comment symbols.
		  Symbol commentSymbol = symbols.getComments().get(nextInstruction.getAddress());
		  if (commentSymbol != null) {
			nextInstruction.setComment(commentSymbol.getComments().get(0));
			for (int index = 1; index < commentSymbol.getComments().size(); index++) {
			  String comment = commentSymbol.getComments().get(index);
			  // use a space as mnemonic to enforce line number in
			  // AssemblyCode.toString().
			  decoded.add(new AssemblyCode(address, " ", comment));
			}
		  }
		}
	  }
	} catch (IllegalOpcodeException e) {
	  decoded.add(new AssemblyCode(address, e.getMessage()));
	  System.out.print(e.getMessage());
	}
	return decoded;
  } // disassemble()

  private static void fillInSymbols(HashMap<Integer, Path> paths, Symbols symbols) {
	// Collect symbols to be filled in as labels in a single array.
	ArrayList<Symbol> symbolList = symbols.getSymbolsByType(SymbolType.entryPoint);
	symbolList.addAll(symbols.getSymbolsByType(SymbolType.label));
	symbolList.addAll(symbols.getSymbolsByType(SymbolType.memoryAddress));

	// Fill in the labels in the decoded instructions in all execution paths.
	paths.forEach((Integer entryPoint, Path path) -> fillInLabels(path.decoded, symbolList));

	// Fill in the comments. Note that this may overwrite comment from entry
	// point in case of jump or call statements.
	// FIXME
	// paths.forEach((Integer entryPoint, Path path) ->
	// fillInComments(path.decoded, symbols.getComments()));
  } // fillInSymbols()

  private static void fillInLabels(ArrayList<AssemblyCode> decoded, ArrayList<Symbol> symbols) {
	for (Symbol symbol : symbols) {
	  // Look up the line where the label must be set.
	  int index = 0;
	  while (index < decoded.size() && decoded.get(index).getAddress() <= symbol.getValue()) {
		index++;
	  }
	  // Set the label.
	  if (index > 0 && decoded.get(index - 1).getAddress() == symbol.getValue()) {
		decoded.get(index - 1).setLabel(symbol.getName());
	  }
	}
  } // fillInLabels()

  private static Object fillInComments(ArrayList<AssemblyCode> decoded, HashMap<Integer, Symbol> comments) {
	for (AssemblyCode code : decoded) {
	  if (code.getBytes() != null && code.getBytes().size() > 0) {
		Symbol comment = comments.get(code.getAddress());
		if (comment != null) {
		  code.setComment(comment.getComments().get(0));
		}
	  }

	}
	return null;
  }

  protected static void writeDefinitions(String fileName, AbstractWriter writer, Symbols symbols) throws IOException {
	// Write symbol definitions, grouped by symbol type and sorted by address.
	String msg = String.format(";File generated by dasmZ80.jar Z80 disassembler from %s", fileName);
	writer.write(new AssemblyCode(0, null, msg));

	// Collect symbols to port addresses.
	ArrayList<SymbolSortedByValue> symbolList = new ArrayList<SymbolSortedByValue>();
	for (Symbol symbol : symbols.getSymbolsByType(SymbolType.portAddress)) {
	  symbolList.add(new SymbolSortedByValue(symbol));
	}
	if (symbolList.size() > 0) {
	  // Sort the symbols by address.
	  Object[] sortedSymbolList = symbolList.toArray();
	  Arrays.sort(sortedSymbolList);

	  // Write the symbols.
	  writer.write(new AssemblyCode(0, null, ";"));
	  writer.write(new AssemblyCode(0, null, IDENTIFY_IO));
	  for (Object symbol : sortedSymbolList) {
		writer.write(((Symbol) symbol).toString());
	  }
	}

	// Collect defined entry points, symbols to memory addresses and
	// discovered labels.
	symbolList.clear();
	for (Symbol symbol : symbols.getSymbolsByType(SymbolType.memoryAddress)) {
	  symbolList.add(new SymbolSortedByValue(symbol));
	}
	for (Symbol symbol : symbols.getSymbolsByType(SymbolType.label)) {
	  symbolList.add(new SymbolSortedByValue(symbol));
	}
	if (symbolList.size() > 0) {
	  // Sort the symbols by address.
	  Object[] sortedSymbolList = symbolList.toArray();
	  Arrays.sort(sortedSymbolList);

	  // Write the symbols.
	  boolean toDo = true;
	  for (Object object : sortedSymbolList) {
		Symbol symbol = (Symbol) object;
		// Ignore defined entry points and discovered labels within the range
		// of the disassembled code.
		if (symbol.getType() == SymbolType.memoryAddress || symbol.getValue() < startAddress
		    || symbol.getValue() >= finalAddress) {
		  // write a message if this is the first symbol to a memory address.
		  if (toDo) {
			writer.write(new AssemblyCode(0, null, ";"));
			writer.write(new AssemblyCode(0, null, IDENTIFY_MEMORY));
			toDo = false;
		  }
		  writer.write(symbol.toString());
		}
	  }
	}

	// Collect symbols to constants.
	symbolList.clear();
	for (Symbol symbol : symbols.getSymbolsByType(SymbolType.constant)) {
	  symbolList.add(new SymbolSortedByValue(symbol));
	}
	if (symbolList.size() > 0) {
	  // Sort the symbols by address.
	  Object[] sortedSymbolList = symbolList.toArray();
	  Arrays.sort(sortedSymbolList);

	  // Write the symbols.
	  writer.write(new AssemblyCode(0, null, ";"));
	  writer.write(new AssemblyCode(0, null, IDENTIFY_CONSTANT));
	  for (Object symbol : sortedSymbolList) {
		writer.write(((Symbol) symbol).toString());
	  }
	}

	writer.write(new AssemblyCode(0, null, ";"));
  } // writeDefinitions()

  protected static void writeOutput(AbstractWriter writer, ByteReader reader, HashMap<Integer, Path> paths,
      Symbols symbols) throws IOException {
	writer.write(new AssemblyCode(startAddress, String.format("org 0x%04X", startAddress)));

	// Output disassembled execution paths, sorted by entry point, intermixed
	// with unvisited input code.
	Object[] keys = paths.keySet().toArray();
	Arrays.sort(keys);
	int nextAddress = startAddress;
	int readerAddress = nextAddress;
	for (Object key : keys) {
	  nextAddress = (Integer) key;
	  Path path = paths.get((Integer) key);
	  // Output unvisited input code if necessary.
	  if (readerAddress != nextAddress) {
		writeUnvisitedCode(readerAddress, nextAddress, writer, reader);
		readerAddress = nextAddress;
	  }
	  writeEntryPoint(writer, path.entryPoint, paths, symbols);
	  // Output disassembled instructions of the execution path.
	  for (AssemblyCode line : path.decoded) {
		writer.write(line);
		if (line.getBytes() != null) {
		  nextAddress += line.getBytes().size();
		}
	  }
	  readerAddress = nextAddress;
	}

	// Add unvisited code beyond last decoded address.
	writeUnvisitedCode(readerAddress, reader.getSize(), writer, reader);
	writer.write(new AssemblyCode(reader.getSize(), "end"));
  } // writeOutput()

  private static void writeEntryPoint(AbstractWriter writer, Symbol entryPoint, HashMap<Integer, Path> paths,
      Symbols symbols) throws IOException {
	// Start the entry point with a blank line.
	writer.write(new AssemblyCode(entryPoint.getValue(), ";"));

	// Followed by entry point name and comments from symbol definition file.
	writer.write(new AssemblyCode(entryPoint.getValue(), ";****************"));
	writer.write(new AssemblyCode(entryPoint.getValue(), ";* Entry point: " + entryPoint.getName()));
	for (String comment : entryPoint.getComments()) {
	  writer.write(new AssemblyCode(entryPoint.getValue(), ";* " + comment.substring(1)));
	}
	writer.write(new AssemblyCode(entryPoint.getValue(), ";*"));

	// And finally the list of references to this entry point.
	writer.write(new AssemblyCode(entryPoint.getValue(), ";* Called by:"));
	for (Integer reference : entryPoint.getReferences()) {
	  Symbol calledBy = getEntryPointFor(reference, paths);
	  String ref = String.format(";* 0x%04X (0x%04X %s", reference, calledBy.getValue(), calledBy.getName());
	  if (calledBy.getComments().size() != 0) {
		ref += ": ";
		ref += calledBy.getComments().get(0).substring(1);
	  }
	  ref += ")";
	  writer.write(new AssemblyCode(entryPoint.getValue(), ref));
	}
	writer.write(new AssemblyCode(entryPoint.getValue(), ";****************"));
  }

  // Return entry point of path containing the reference.
  private static Symbol getEntryPointFor(Integer reference, HashMap<Integer, Path> paths) {
	Symbol result = null;
	Object[] keys = paths.keySet().toArray();
	Arrays.sort(keys);
	for (Object key : keys) {
	  Path path = paths.get((Integer) key);
	  if (result == null && path.startAddress <= reference && reference < path.nextAddress) {
		result = path.entryPoint;
	  }
	}
	return result;
  }

  private static void writeUnvisitedCode(int fromAddress, int toAddress, AbstractWriter writer, ByteReader reader)
      throws IOException {
	// add a blank line before the unvisited code.
	writer.write(new AssemblyCode(fromAddress, ";"));
	// Output unvisited code, 4 bytes per line..
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

  private static void writeReferences(AbstractWriter writer, Symbols symbols) throws IOException {
	// Write symbols and references, grouped by symbol type and sorted by symbol
	// name.
	// Write references to port addresses.
	ArrayList<SymbolSortedByName> symbolList = new ArrayList<SymbolSortedByName>();
	for (Symbol symbol : symbols.getSymbolsByType(SymbolType.portAddress)) {
	  symbolList.add(new SymbolSortedByName(symbol));
	}
	if (symbolList.size() > 0) {
	  Object[] sortedSymbolList = symbolList.toArray();
	  Arrays.sort(sortedSymbolList);
	  writer.write("\nI/O address cross reference list:\n");
	  for (Object symbol : sortedSymbolList) {
		writeSymbolReferences((Symbol) symbol, 2, writer);
	  }
	}

	// Write references to entry points, labels and memory addresses.
	symbolList.clear();
	for (Symbol symbol : symbols.getSymbolsByType(SymbolType.label)) {
	  symbolList.add(new SymbolSortedByName(symbol));
	}
	for (Symbol symbol : symbols.getSymbolsByType(SymbolType.entryPoint)) {
	  symbolList.add(new SymbolSortedByName(symbol));
	}
	for (Symbol symbol : symbols.getSymbolsByType(SymbolType.memoryAddress)) {
	  symbolList.add(new SymbolSortedByName(symbol));
	}
	if (symbolList.size() > 0) {
	  Object[] sortedSymbolList = symbolList.toArray();
	  Arrays.sort(sortedSymbolList);
	  writer.write("\nMemory address cross reference list:\n");
	  for (Object symbol : sortedSymbolList) {
		writeSymbolReferences((Symbol) symbol, 4, writer);
	  }
	}

	// Write references to constants.
	symbolList.clear();
	for (Symbol symbol : symbols.getSymbolsByType(SymbolType.constant)) {
	  symbolList.add(new SymbolSortedByName(symbol));
	}
	if (symbolList.size() > 0) {
	  Object[] sortedSymbolList = symbolList.toArray();
	  Arrays.sort(sortedSymbolList);
	  writer.write("\nConstants reference list:\n");
	  for (Object symbol : sortedSymbolList) {
		writeSymbolReferences((Symbol) symbol, 4, writer);
	  }
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
