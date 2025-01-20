package com.github.hanwelmer.dasmZ80;

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
	assert (writer.output.size() == 11);
	assert ("0000 C0       ep0000:   RET  NZ\n".equals(writer.output.get(4)));
	assert ("0001 00                 NOP\n".equals(writer.output.get(5)));
	assert ("0002 C9                 RET\n".equals(writer.output.get(6)));
	assert ("0003                    ;\n".equals(writer.output.get(7)));
	assert ("ep0000  =0000:\n".equals(writer.output.get(10)));
  }

  @Test
  public void testIllegalOpcode0x03() {
	Byte[] bytes = { 0xDD - 256, 0xDC - 256, 0x34, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	finalAddress = reader.getSize();
	StringWriter writer = new StringWriter();
	disassembleToWriter("test", reader, writer, symbols);
	assert (writer.output.size() == 8);
	assert ("0000          ep0000:   Unsupported code 0xDDDC at address 0x0000\n\n".equals(writer.output.get(4)));
	// FIXME
	// assert ("0000 DDDC3412 DB 0xDD, 0xDC, 0x34,
	// 0x12\n".equals(writer.output.get(5)));
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
	assert (writer.output.size() == 15);
	assert ("0000 00       ep0000:   NOP\n".equals(writer.output.get(7)));
	assert ("0001 02                 LD   (BC),A\n".equals(writer.output.get(8)));
	assert ("0002 013412             LD   BC,lbl1234\n".equals(writer.output.get(9)));
	assert ("0005                    Unsupported code 0xDDDC at address 0x0005\n\n".equals(writer.output.get(10)));
	// FIXME
	// assert ("0005 DDDC34 DB 0xDD - 256, 0xDC - 256,
	// 0x34\n".equals(writer.output.get(11)));
	// assert ("0008 12090A0B DB 0x12, 0x09, 0x0A,
	// 0x0B\n".equals(writer.output.get(12)));
	// assert ("000C 0C0D0E0F DB 0x0C, 0x0D, 0x0E,
	// 0x0F\n".equals(writer.output.get(13)));
	// assert ("0010 10111213 DB 0x10, 0x11, 0x12,
	// 0x13\n".equals(writer.output.get(14)));
	// assert ("0014 14151617 DB 0x14, 0x15, 0x16,
	// 0x17\n".equals(writer.output.get(15)));
	// assert ("0018 18191A1B DB 0x18, 0x19, 0x1A,
	// 0x1B\n".equals(writer.output.get(16)));
	// assert ("001C 1C1D1E1F DB 0x1C, 0x1D, 0x1E,
	// 0x1F\n".equals(writer.output.get(17)));
	// assert ("0020 202122 DB 0x20, 0x21,
	// 0x22\n".equals(writer.output.get(18)));
	// assert (" ;\n".equals(writer.output.get(19)));
	// assert ("0023 end\n".equals(writer.output.get(20)));
  }

  @Test
  public void testPortReferenceTable() {
	Byte[] bytes = { 0xDB - 256, 0x12, 0xD3 - 256, 0xFE - 256, 0xD3 - 256, 0x12, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	finalAddress = reader.getSize();
	StringWriter writer = new StringWriter();
	disassembleToWriter("test", reader, writer, symbols);
	assert (writer.output.size() == 19);
	assert ("                        ;I/O addresses:\n".equals(writer.output.get(2)));
	assert ("0012            port12  EQU  0x12\n".equals(writer.output.get(3)));
	assert ("00FE            portFE  EQU  0xFE\n".equals(writer.output.get(4)));
	assert ("0000 DB12     ep0000:   IN   A,(port12)\n".equals(writer.output.get(8)));
	assert ("0002 D3FE               OUT  (portFE),A\n".equals(writer.output.get(9)));
	assert ("0004 D312               OUT  (port12),A\n".equals(writer.output.get(10)));
	assert ("0006 00                 NOP\n".equals(writer.output.get(11)));
	assert ("\nI/O address cross reference list:\n".equals(writer.output.get(14)));
	assert ("port12  =12: 0000 0004\n".equals(writer.output.get(15)));
	assert ("portFE  =FE: 0002\n".equals(writer.output.get(16)));
  }

  @Test
  public void testMemoryReferenceTable() {
	Byte[] bytes = { 0xCD - 256, 0x03, 0x00, 0xC2 - 256, 0x03, 0x00, 0x10, 0xF8 - 256, 0x38, 0xF6 - 256, 0x00,
	    0xC3 - 256, 0x03, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	finalAddress = reader.getSize();
	StringWriter writer = new StringWriter();
	disassembleToWriter("test", reader, writer, symbols);
	int index = 15;
	assert (writer.output.size() == index);
	index -= 11;
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
  public void testNoEntrypoints() {
	Byte[] bytes = { 0xCD - 256, 0x07, 0x00, 0xC3 - 256, 0x03, 0x00, 0x00, 0x10, 0xFE - 256, 0x38, 0xF5 - 256,
	    0xC3 - 256, 0x03, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	finalAddress = reader.getSize();
	StringWriter writer = new StringWriter();
	disassembleToWriter("test", reader, writer, symbols);
	assert (writer.output.size() == 16);
	assert ("0000 CD0700   ep0000:   CALL lbl0007\n".equals(writer.output.get(4)));
	assert ("0003 C30300   lbl0003:  JP   lbl0003\n".equals(writer.output.get(5)));
	assert ("0006                    ;\n".equals(writer.output.get(6)));
	// FIXME
	// assert ("0006 00 DB 0x00\n".equals(writer.output.get(11)));
	assert ("0007 10FE     lbl0007:  DJNZ lbl0007-$\n".equals(writer.output.get(7)));
	assert ("0009 38F5               JR   C,ep0000-$\n".equals(writer.output.get(8)));
	assert ("000B C30300             JP   lbl0003\n".equals(writer.output.get(9)));
	assert ("000E                    ;\n".equals(writer.output.get(10)));
	assert ("000E                    end\n".equals(writer.output.get(11)));
	assert ("\nMemory address cross reference list:\n".equals(writer.output.get(12)));
	assert ("ep0000  =0000: 0009\n".equals(writer.output.get(13)));
	assert ("lbl0003 =0003: 0003 000B\n".equals(writer.output.get(14)));
	assert ("lbl0007 =0007: 0000 0007\n".equals(writer.output.get(15)));
  }

  @Test
  public void testTwoEntrypoints() {
	Byte[] bytes = { 0xCD - 256, 0x06, 0x00, 0xC3 - 256, 0x00, 0x00, 0xC2 - 256, 0x06, 0x00, 0x10, 0xFB - 256, 0x38,
	    0xF9 - 256, 0x00 };
	symbols.clear();
	symbols.getOrMakeSymbol("start", SymbolType.entryPoint, 0, "0x0000");
	symbols.getOrMakeSymbol("entry", SymbolType.entryPoint, 6, "0x0006");
	ByteReader reader = new ReadFromArray(bytes);
	finalAddress = reader.getSize();
	StringWriter writer = new StringWriter();
	disassembleToWriter("test", reader, writer, symbols);
	symbols.clear();
	assert (writer.output.size() == 15);
	assert ("0000 CD0600   start:    CALL entry\n".equals(writer.output.get(3)));
	assert ("0003 C30000             JP   start\n".equals(writer.output.get(4)));
	assert ("0006                    ;\n".equals(writer.output.get(5)));
	assert ("0006 C20600   entry:    JP   NZ,entry\n".equals(writer.output.get(6)));
	assert ("0009 10FB               DJNZ entry-$\n".equals(writer.output.get(7)));
	assert ("000B 38F9               JR   C,entry-$\n".equals(writer.output.get(8)));
	assert ("000D 00                 NOP\n".equals(writer.output.get(9)));
	assert ("000E                    ;\n".equals(writer.output.get(10)));
	assert ("000E                    end\n".equals(writer.output.get(11)));
	assert ("\nMemory address cross reference list:\n".equals(writer.output.get(12)));
	assert ("entry   =0006: 0000 0006 0009 000B\n".equals(writer.output.get(13)));
	assert ("start   =0000: 0003\n".equals(writer.output.get(14)));
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
	assert ("0012            p12     EQU  	0x12\n".equals(writer.output.get(3)));
	assert ("00FE            pFE     EQU    0xFE\n".equals(writer.output.get(4)));
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
	assert (writer.output.size() == 18);
	assert ("0000 CD0300   ep0000:   CALL loop\n".equals(writer.output.get(7)));
	assert ("0003 C20300   loop:     JP   NZ,loop\n".equals(writer.output.get(8)));
	assert ("0006 10FB               DJNZ loop-$\n".equals(writer.output.get(9)));
	assert ("0008 38F9               JR   C,loop-$\n".equals(writer.output.get(10)));
	assert ("000A 00                 NOP\n".equals(writer.output.get(11)));
	assert ("000B C30300             JP   loop\n".equals(writer.output.get(12)));
	assert ("\nMemory address cross reference list:\n".equals(writer.output.get(15)));
	assert ("ep0000  =0000:\n".equals(writer.output.get(16)));
	assert ("loop    =0003: 0000 0003 0006 0008 000B\n".equals(writer.output.get(17)));
  }

  @Test
  public void testReset() {
	Byte[] bytes = { 0xE7 - 256, 0x18, 0xFD - 256, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	    0xC9 - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	finalAddress = reader.getSize();
	StringWriter writer = new StringWriter();
	disassembleToWriter("test", reader, writer, symbols);
	int index = 13;
	assert (writer.output.size() == index);
	index -= 9;
	assert ("0000 E7       ep0000:   RST  lbl0020\n".equals(writer.output.get(index++)));
	assert ("0001 18FD               JR   ep0000-$\n".equals(writer.output.get(index++)));
	assert ("0003                    ;\n".equals(writer.output.get(index++)));
	assert ("0020 C9       lbl0020:  RET\n".equals(writer.output.get(index++)));
	assert ("0021                    ;\n".equals(writer.output.get(index++)));
	assert ("0021                    end\n".equals(writer.output.get(index++)));
	assert ("\nMemory address cross reference list:\n".equals(writer.output.get(index++)));
	assert ("ep0000  =0000: 0001\n".equals(writer.output.get(index++)));
	assert ("lbl0020 =0020: 0000\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testLinesOutsideReaderAddressRange() {
	Byte[] bytes = { 0xCD - 256, 0x07, 0x00, 0xC3 - 256, 0x03, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	finalAddress = reader.getSize();
	StringWriter writer = new StringWriter();
	disassembleToWriter("test", reader, writer, symbols);
	int index = 15;
	assert (writer.output.size() == index);
	index = 1;
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("                        ;Memory addresses:\n".equals(writer.output.get(index++)));
	assert ("0007            lbl0007 EQU  0x0007\n".equals(writer.output.get(index++)));
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("0000                    org 0x0000\n".equals(writer.output.get(index++)));
	assert ("0000                    No entry points defined; assuming 0x0000 as entry point\n"
	    .equals(writer.output.get(index++)));
	assert ("0000 CD0700   ep0000:   CALL lbl0007\n".equals(writer.output.get(index++)));
	assert ("0003 C30300   lbl0003:  JP   lbl0003\n".equals(writer.output.get(index++)));
	assert ("0006                    ;\n".equals(writer.output.get(index++)));
	assert ("0006                    end\n".equals(writer.output.get(index++)));
	assert ("\nMemory address cross reference list:\n".equals(writer.output.get(index++)));
	assert ("ep0000  =0000:\n".equals(writer.output.get(index++)));
	assert ("lbl0003 =0003: 0003\n".equals(writer.output.get(index++)));
	assert ("lbl0007 =0007: 0000\n".equals(writer.output.get(index++)));
  }

}
