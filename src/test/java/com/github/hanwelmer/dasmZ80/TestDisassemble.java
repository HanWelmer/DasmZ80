package com.github.hanwelmer.dasmZ80;

import java.io.IOException;

import org.junit.Test;

public class TestDisassemble extends DasmZ80 {

  Symbols symbols = new Symbols();

  @Test
  public void testReturn() {
	Byte[] bytes = { 0xC0 - 256, 0x00, 0xC9 - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	finalAddress = reader.getSize();
	StringWriter writer = new StringWriter();

	disassembleToWriter("test", reader, writer, symbols);
	int index = 5;
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Entry point: ep0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 C0       ep0000:   RET  NZ\n".equals(writer.output.get(index++)));
	assert ("0001 00                 NOP\n".equals(writer.output.get(index++)));
	assert ("0002 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0003                    ;\n".equals(writer.output.get(index++)));
	assert ("0003                    end\n".equals(writer.output.get(index++)));
	assert ("\nMemory address cross reference list:\n".equals(writer.output.get(index++)));
	assert ("ep0000  =0000:\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testIllegalOpcode0x03() {
	Byte[] bytes = { 0xDD - 256, 0xDC - 256, 0x34, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	finalAddress = reader.getSize();
	StringWriter writer = new StringWriter();

	disassembleToWriter("test", reader, writer, symbols);
	int index = 5;
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Entry point: ep0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000          ep0000:   Unsupported code 0xDDDC at address 0x0000\n\n".equals(writer.output.get(index++)));
	assert ("0000                    ;\n".equals(writer.output.get(index++)));
	assert ("0000 DDDC3412           DB   0xDD, 0xDC, 0x34, 0x12\n".equals(writer.output.get(index++)));
	assert ("0004                    end\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testIllegalOpcode0x22() {
	Byte[] bytes = { 0x00, 0x02, 0x01, 0x34, 0x12, 0xDD - 256, 0xDC - 256, 0x34, 0x12, 0x09, 0x0A, 0x0B, 0x0C, 0x0D,
	    0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
	    0x20, 0x21, 0x22 };
	ByteReader reader = new ReadFromArray(bytes);
	finalAddress = reader.getSize();
	StringWriter writer = new StringWriter();

	disassembleToWriter("test", reader, writer, symbols);
	int index = 8;
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Entry point: ep0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 00       ep0000:   NOP\n".equals(writer.output.get(index++)));
	assert ("0001 02                 LD   (BC),A\n".equals(writer.output.get(index++)));
	assert ("0002 013412             LD   BC,lbl1234\n".equals(writer.output.get(index++)));
	assert ("0005                    Unsupported code 0xDDDC at address 0x0005\n\n".equals(writer.output.get(index++)));
	assert ("0005                    ;\n".equals(writer.output.get(index++)));
	assert ("0005 DDDC34             DB   0xDD, 0xDC, 0x34\n".equals(writer.output.get(index++)));
	assert ("0008 12090A0B           DB   0x12, 0x09, 0x0A, 0x0B\n".equals(writer.output.get(index++)));
	assert ("000C 0C0D0E0F           DB   0x0C, 0x0D, 0x0E, 0x0F\n".equals(writer.output.get(index++)));
	assert ("0010 10111213           DB   0x10, 0x11, 0x12, 0x13\n".equals(writer.output.get(index++)));
	assert ("0014 14151617           DB   0x14, 0x15, 0x16, 0x17\n".equals(writer.output.get(index++)));
	assert ("0018 18191A1B           DB   0x18, 0x19, 0x1A, 0x1B\n".equals(writer.output.get(index++)));
	assert ("001C 1C1D1E1F           DB   0x1C, 0x1D, 0x1E, 0x1F\n".equals(writer.output.get(index++)));
	assert ("0020 202122             DB   0x20, 0x21, 0x22\n".equals(writer.output.get(index++)));
	assert ("0023                    end\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testPortReferenceTable() {
	Byte[] bytes = { 0xDB - 256, 0x12, 0xD3 - 256, 0xFE - 256, 0xD3 - 256, 0x12, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	finalAddress = reader.getSize();
	StringWriter writer = new StringWriter();

	symbols.clear();
	disassembleToWriter("test", reader, writer, symbols);
	int index = 2;
	assert ("                        ;I/O addresses:\n".equals(writer.output.get(index++)));
	assert ("0012          port12    EQU  0x12\n".equals(writer.output.get(index++)));
	assert ("00FE          portFE    EQU  0xFE\n".equals(writer.output.get(index++)));
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("0000                    No entry points defined; assuming 0x0000 as entry point\n"
	    .equals(writer.output.get(index++)));
	assert ("0000                    org 0x0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Entry point: ep0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 DB12     ep0000:   IN   A,(port12)\n".equals(writer.output.get(index++)));
	assert ("0002 D3FE               OUT  (portFE),A\n".equals(writer.output.get(index++)));
	assert ("0004 D312               OUT  (port12),A\n".equals(writer.output.get(index++)));
	assert ("0006 00                 NOP\n".equals(writer.output.get(index++)));
	assert ("0007                    ;\n".equals(writer.output.get(index++)));
	assert ("0007                    end\n".equals(writer.output.get(index++)));
	assert ("\nI/O address cross reference list:\n".equals(writer.output.get(index++)));
	assert ("port12  =12: 0000 0004\n".equals(writer.output.get(index++)));
	assert ("portFE  =FE: 0002\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testMemoryReferenceTable() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("                      ;Entry points");
	input.add("0000        ep0000     ENTRY 0x0000");
	symbols = readSymbols(input);

	Byte[] bytes = { 0xCD - 256, 0x03, 0x00, 0xC2 - 256, 0x03, 0x00, 0x10, 0xF8 - 256, 0x38, 0xF6 - 256, 0x00,
	    0xC3 - 256, 0x03, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();
	startAddress = 0;
	finalAddress = reader.getSize();

	disassembleToWriter("test", reader, writer, symbols);
	int index = 1;
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("0000                    org 0x0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Entry point: ep0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* 0x0006 (0x0000 ep0000)\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* 0x0008 (0x0000 ep0000)\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 CD0300   ep0000:   CALL lbl0003\n".equals(writer.output.get(index++)));
	assert ("0003 C20300   lbl0003:  JP   NZ,lbl0003\n".equals(writer.output.get(index++)));
	assert ("0006 10F8               DJNZ ep0000-$\n".equals(writer.output.get(index++)));
	assert ("0008 38F6               JR   C,ep0000-$\n".equals(writer.output.get(index++)));
	assert ("000A 00                 NOP\n".equals(writer.output.get(index++)));
	assert ("000B C30300             JP   lbl0003\n".equals(writer.output.get(index++)));
	assert ("000E                    ;\n".equals(writer.output.get(index++)));
	assert ("000E                    end\n".equals(writer.output.get(index++)));
	assert ("\nMemory address cross reference list:\n".equals(writer.output.get(index++)));
	assert ("ep0000  =0000: 0006 0008\n".equals(writer.output.get(index++)));
	assert ("lbl0003 =0003: 0000 0003 000B\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testNoEntrypoints() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("                      ;Entry points");
	input.add("0000        ep0000    ENTRY 0x0000");
	symbols = readSymbols(input);

	Byte[] bytes = { 0xCD - 256, 0x07, 0x00, 0xC3 - 256, 0x03, 0x00, 0x00, 0x10, 0xFE - 256, 0x38, 0xF5 - 256,
	    0xC3 - 256, 0x03, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();
	startAddress = 0;
	finalAddress = reader.getSize();

	disassembleToWriter("test", reader, writer, symbols);

	int index = 1;
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("0000                    org 0x0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Entry point: ep0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* 0x0009 (0x0007 lbl0007)\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 CD0700   ep0000:   CALL lbl0007\n".equals(writer.output.get(index++)));
	assert ("0003 C30300   lbl0003:  JP   lbl0003\n".equals(writer.output.get(index++)));
	assert ("0006                    ;\n".equals(writer.output.get(index++)));
	assert ("0006 00                 DB   0x00\n".equals(writer.output.get(index++)));
	assert ("0007 10FE     lbl0007:  DJNZ lbl0007-$\n".equals(writer.output.get(index++)));
	assert ("0009 38F5               JR   C,ep0000-$\n".equals(writer.output.get(index++)));
	assert ("000B C30300             JP   lbl0003\n".equals(writer.output.get(index++)));
	assert ("000E                    ;\n".equals(writer.output.get(index++)));
	assert ("000E                    end\n".equals(writer.output.get(index++)));
	assert ("\nMemory address cross reference list:\n".equals(writer.output.get(index++)));
	assert ("ep0000  =0000: 0009\n".equals(writer.output.get(index++)));
	assert ("lbl0003 =0003: 0003 000B\n".equals(writer.output.get(index++)));
	assert ("lbl0007 =0007: 0000 0007\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testTwoEntrypoints() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("                      ;Entry points");
	input.add("0000        start     ENTRY 0x0000");
	input.add("0006        entry     ENTRY 0x0006");
	symbols = readSymbols(input);

	Byte[] bytes = { 0xCD - 256, 0x06, 0x00, 0xC3 - 256, 0x00, 0x00, 0xC2 - 256, 0x06, 0x00, 0x10, 0xFB - 256, 0x38,
	    0xF9 - 256, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();
	startAddress = 0;
	finalAddress = reader.getSize();

	disassembleToWriter("test", reader, writer, symbols);

	int index = 1;
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("0000                    org 0x0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Entry point: start\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* 0x0003 (0x0000 start)\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 CD0600   start:    CALL entry\n".equals(writer.output.get(index++)));
	assert ("0003 C30000             JP   start\n".equals(writer.output.get(index++)));
	assert ("0006                    ;\n".equals(writer.output.get(index++)));
	assert ("0006                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0006                    ;* Entry point: entry\n".equals(writer.output.get(index++)));
	assert ("0006                    ;*\n".equals(writer.output.get(index++)));
	assert ("0006                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0006                    ;* 0x0000 (0x0000 start)\n".equals(writer.output.get(index++)));
	assert ("0006                    ;* 0x0006 (0x0006 entry)\n".equals(writer.output.get(index++)));
	assert ("0006                    ;* 0x0009 (0x0006 entry)\n".equals(writer.output.get(index++)));
	assert ("0006                    ;* 0x000B (0x0006 entry)\n".equals(writer.output.get(index++)));
	assert ("0006                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0006 C20600   entry:    JP   NZ,entry\n".equals(writer.output.get(index++)));
	assert ("0009 10FB               DJNZ entry-$\n".equals(writer.output.get(index++)));
	assert ("000B 38F9               JR   C,entry-$\n".equals(writer.output.get(index++)));
	assert ("000D 00                 NOP\n".equals(writer.output.get(index++)));
	assert ("000E                    ;\n".equals(writer.output.get(index++)));
	assert ("000E                    end\n".equals(writer.output.get(index++)));
	assert ("\nMemory address cross reference list:\n".equals(writer.output.get(index++)));
	assert ("entry   =0006: 0000 0006 0009 000B\n".equals(writer.output.get(index++)));
	assert ("start   =0000: 0003\n".equals(writer.output.get(index++)));
  }

  public void testPortSymbols() {
	Symbols portSymbols = new Symbols();
	portSymbols.getOrMakeSymbol("p12", SymbolType.portAddress, 12, "0x0C");
	portSymbols.getOrMakeSymbol("pFE", SymbolType.portAddress, 254, "0xFE");

	Byte[] bytes = { 0xDB - 256, 0x12, 0xD3 - 256, 0xFE - 256, 0xD3 - 256, 0x12, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	finalAddress = reader.getSize();
	StringWriter writer = new StringWriter();

	disassembleToWriter("test", reader, writer, portSymbols);
	assert (writer.output.size() == 16);
	assert ("                        ;I/O addresses]\n".equals(writer.output.get(2)));
	assert ("0012          p12       EQU  	0x12\n".equals(writer.output.get(3)));
	assert ("00FE          pFE       EQU    0xFE\n".equals(writer.output.get(4)));
	assert ("0000 DB12               IN   A,(p12)\n".equals(writer.output.get(7)));
	assert ("0002 D3FE               OUT  (pFE),A\n".equals(writer.output.get(8)));
	assert ("0004 D312               OUT  (p12),A\n".equals(writer.output.get(9)));
	assert ("0006 00                 NOP\n".equals(writer.output.get(10)));
	assert ("\nI/O-port cross reference list:\n".equals(writer.output.get(12)));
	assert ("p12     =12: 0000 0004\n".equals(writer.output.get(13)));
	assert ("pFE     =FE: 0002\n".equals(writer.output.get(14)));
	assert ("\nMemory cross reference list:\n".equals(writer.output.get(15)));
  }

  @Test
  public void testMemorySymbols() {
	Symbols symbols = new Symbols();
	symbols.getOrMakeSymbol("loop", SymbolType.memoryAddress, 3, "0x0003");

	Byte[] bytes = { 0xCD - 256, 0x03, 0x00, 0xC2 - 256, 0x03, 0x00, 0x10, 0xFB - 256, 0x38, 0xF9 - 256, 0x00,
	    0xC3 - 256, 0x03, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	finalAddress = reader.getSize();
	StringWriter writer = new StringWriter();

	disassembleToWriter("test", reader, writer, symbols);
	int index = 1;
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("                        ;Memory addresses:\n".equals(writer.output.get(index++)));
	assert ("0003          loop      EQU  0x0003\n".equals(writer.output.get(index++)));
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("0000                    No entry points defined; assuming 0x0000 as entry point\n"
	    .equals(writer.output.get(index++)));
	assert ("0000                    org 0x0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Entry point: ep0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 CD0300   ep0000:   CALL loop\n".equals(writer.output.get(index++)));
	assert ("0003 C20300   loop:     JP   NZ,loop\n".equals(writer.output.get(index++)));
	assert ("0006 10FB               DJNZ loop-$\n".equals(writer.output.get(index++)));
	assert ("0008 38F9               JR   C,loop-$\n".equals(writer.output.get(index++)));
	assert ("000A 00                 NOP\n".equals(writer.output.get(index++)));
	assert ("000B C30300             JP   loop\n".equals(writer.output.get(index++)));
	assert ("000E                    ;\n".equals(writer.output.get(index++)));
	assert ("000E                    end\n".equals(writer.output.get(index++)));
	assert ("\nMemory address cross reference list:\n".equals(writer.output.get(index++)));
	assert ("ep0000  =0000:\n".equals(writer.output.get(index++)));
	assert ("loop    =0003: 0000 0003 0006 0008 000B\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testReset() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("                      ;Entry points");
	input.add("0000        ep0000    ENTRY 0x0000");
	symbols = readSymbols(input);

	Byte[] bytes = { 0xE7 - 256, 0x18, 0xFD - 256, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	    0xC9 - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();
	startAddress = 0;
	finalAddress = reader.getSize();

	disassembleToWriter("test", reader, writer, symbols);
	int index = 1;
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("0000                    org 0x0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Entry point: ep0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* 0x0001 (0x0000 ep0000)\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 E7       ep0000:   RST  lbl0020\n".equals(writer.output.get(index++)));
	assert ("0001 18FD               JR   ep0000-$\n".equals(writer.output.get(index++)));
	assert ("0003                    ;\n".equals(writer.output.get(index++)));
	assert ("0003 00                 DB   0x00\n".equals(writer.output.get(index++)));
	assert ("0004 00000000           DB   0x00, 0x00, 0x00, 0x00\n".equals(writer.output.get(index++)));
	assert ("0008 00000000           DB   0x00, 0x00, 0x00, 0x00\n".equals(writer.output.get(index++)));
	assert ("000C 00000000           DB   0x00, 0x00, 0x00, 0x00\n".equals(writer.output.get(index++)));
	assert ("0010 00000000           DB   0x00, 0x00, 0x00, 0x00\n".equals(writer.output.get(index++)));
	assert ("0014 00000000           DB   0x00, 0x00, 0x00, 0x00\n".equals(writer.output.get(index++)));
	assert ("0018 00000000           DB   0x00, 0x00, 0x00, 0x00\n".equals(writer.output.get(index++)));
	assert ("001C 00000000           DB   0x00, 0x00, 0x00, 0x00\n".equals(writer.output.get(index++)));
	assert ("0020 C9       lbl0020:  RET\n".equals(writer.output.get(index++)));
	assert ("0021                    ;\n".equals(writer.output.get(index++)));
	assert ("0021                    end\n".equals(writer.output.get(index++)));
	assert ("\nMemory address cross reference list:\n".equals(writer.output.get(index++)));
	assert ("ep0000  =0000: 0001\n".equals(writer.output.get(index++)));
	assert ("lbl0020 =0020: 0000\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testLinesOutsideReaderAddressRange() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("                      ;Entry points");
	input.add("0000        ep0000    ENTRY 0x0000");
	symbols = readSymbols(input);

	Byte[] bytes = { 0xCD - 256, 0x07, 0x00, 0xC3 - 256, 0x03, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();
	startAddress = 0;
	finalAddress = reader.getSize();

	disassembleToWriter("test", reader, writer, symbols);
	int index = 1;
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("                        ;Memory addresses:\n".equals(writer.output.get(index++)));
	assert ("0007          lbl0007   EQU  0x0007\n".equals(writer.output.get(index++)));
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("0000                    org 0x0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Entry point: ep0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 CD0700   ep0000:   CALL lbl0007\n".equals(writer.output.get(index++)));
	assert ("0003 C30300   lbl0003:  JP   lbl0003\n".equals(writer.output.get(index++)));
	assert ("0006                    ;\n".equals(writer.output.get(index++)));
	assert ("0006                    end\n".equals(writer.output.get(index++)));
	assert ("\nMemory address cross reference list:\n".equals(writer.output.get(index++)));
	assert ("ep0000  =0000:\n".equals(writer.output.get(index++)));
	assert ("lbl0003 =0003: 0003\n".equals(writer.output.get(index++)));
	assert ("lbl0007 =0007: 0000\n".equals(writer.output.get(index++)));
  }

  @Test
  public void test2SymbolsSameValue() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("                      ;");
	input.add("                      ;I/O addresses:");
	input.add("0008        port08    EQU  0x08");
	input.add("                      ;");
	input.add("                      ;Entry points");
	input.add("0000        reset     ENTRY 0x0000     ;entry point after reset.");
	input.add("0008        reset8    ENTRY 0x0008     ;pop BC and set Carry.");
	symbols = readSymbols(input);

	Byte[] bytes = { 0x18, 0xFE - 256, 0x37, 0xDD - 256, 0xCB - 256, 0x0E, 0xEE - 256, 0xD7 - 256, 0xC1 - 256, 0x37,
	    0xD3 - 256, 0x08, 0xCF - 256, 0xC9 - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();
	startAddress = 0;
	finalAddress = reader.getSize();

	disassembleToWriter("test", reader, writer, symbols);
	int index = 1;
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("                        ;I/O addresses:\n".equals(writer.output.get(index++)));
	assert ("0008          port08    EQU  0x08\n".equals(writer.output.get(index++)));
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("0000                    org 0x0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Entry point: reset\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* entry point after reset.\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* 0x0000 (0x0000 reset: entry point after reset.)\n"
	    .equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 18FE     reset:    JR   reset-$\n".equals(writer.output.get(index++)));
	assert ("0002                    ;\n".equals(writer.output.get(index++)));
	assert ("0002 37DD               DB   0x37, 0xDD\n".equals(writer.output.get(index++)));
	assert ("0004 CB0EEED7           DB   0xCB, 0x0E, 0xEE, 0xD7\n".equals(writer.output.get(index++)));
	assert ("0008                    ;\n".equals(writer.output.get(index++)));
	assert ("0008                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* Entry point: reset8\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* pop BC and set Carry.\n".equals(writer.output.get(index++)));
	assert ("0008                    ;*\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* 0x000C (0x0008 reset8: pop BC and set Carry.)\n"
	    .equals(writer.output.get(index++)));
	assert ("0008                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0008 C1       reset8:   POP  BC\n".equals(writer.output.get(index++)));
	assert ("0009 37                 SCF\n".equals(writer.output.get(index++)));
	assert ("000A D308               OUT  (port08),A\n".equals(writer.output.get(index++)));
	assert ("000C CF                 RST  reset8         ;pop BC and set Carry.\n".equals(writer.output.get(index++)));
	assert ("000D C9                 RET\n".equals(writer.output.get(index++)));
	assert ("000E                    ;\n".equals(writer.output.get(index++)));
	assert ("000E                    end\n".equals(writer.output.get(index++)));
	assert ("\nI/O address cross reference list:\n".equals(writer.output.get(index++)));
	assert ("port08  =08: 000A\n".equals(writer.output.get(index++)));
	assert ("\nMemory address cross reference list:\n".equals(writer.output.get(index++)));
	assert ("reset   =0000: 0000\n".equals(writer.output.get(index++)));
	assert ("reset8  =0008: 000C\n".equals(writer.output.get(index++)));
  }

}
