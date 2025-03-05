package com.github.hanwelmer.dasmZ80;

import java.io.IOException;

import org.junit.Test;

public class TestComments extends DasmZ80 {

  @Test
  public void testEndPointComments() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("0000 reset   ENTRY 0x0000");
	input.add("0004 more    ENTRY 0x0004       ;Hello");
	input.add("; world.");
	input.add("0008 final   ENTRY 0x0004       ;Hello world.");
	Symbols symbols = readSymbols(input);

	Byte[] bytes = { 0xCD - 256, 0x04, 0x00, 0xC9 - 256, 0x00, 0xCF - 256, 0x00, 0xC9 - 256, 0xC9 - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();

	startAddress = 0;
	finalAddress = reader.getSize();
	disassembleToWriter("test", reader, writer, symbols);

	// assert (writer.output.size() == 18);
	int index = 1;
	assert ("                        ;\n".equals(writer.output.get(index++)));
	assert ("0000                    org 0x0000\n".equals(writer.output.get(index++)));
	assert ("0000                    ;\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Entry point: reset\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 CD0400   reset:    CALL more           ;Hello\n".equals(writer.output.get(index++)));
	assert ("0003 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0004                    ;\n".equals(writer.output.get(index++)));
	assert ("0004                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Entry point: more\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Hello\n".equals(writer.output.get(index++)));
	assert ("0004                    ;*  world.\n".equals(writer.output.get(index++)));
	assert ("0004                    ;*\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* 0x0000 (0x0000 reset)\n".equals(writer.output.get(index++)));
	assert ("0004                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0004 00       more:     NOP\n".equals(writer.output.get(index++)));
	assert ("0005 CF                 RST  final          ;Hello world.\n".equals(writer.output.get(index++)));
	assert ("0006 00                 NOP\n".equals(writer.output.get(index++)));
	assert ("0007 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0008                    ;\n".equals(writer.output.get(index++)));
	assert ("0008                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* Entry point: final\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* Hello world.\n".equals(writer.output.get(index++)));
	assert ("0008                    ;*\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* 0x0005 (0x0004 more: Hello)\n".equals(writer.output.get(index++)));
	assert ("0008                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0008 C9       final:    RET\n".equals(writer.output.get(index++)));
	assert ("0009                    ;\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testSingleLineComment1() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("                        ;Comments:");
	input.add("0001 ;Handle interrupts via vector table.");
	Symbols symbols = readSymbols(input);

	Byte[] bytes = { 0x00, 0x00, 0x00, 0xC9 - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();

	startAddress = 0;
	finalAddress = reader.getSize();
	disassembleToWriter("test", reader, writer, symbols);

	int index = 1;
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
	assert ("0000 00       ep0000:   NOP\n".equals(writer.output.get(index++)));
	assert ("0001 00                 NOP                 ;Handle interrupts via vector table.\n"
	    .equals(writer.output.get(index++)));
	assert ("0002 00                 NOP\n".equals(writer.output.get(index++)));
	assert ("0003 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0004                    ;\n".equals(writer.output.get(index++)));
	assert ("0004                    end\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testSingleLineComment2() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("                        ;Comments:");
	input.add("0001 ED5E IM 2 ;Handle interrupts via vector table.");
	Symbols symbols = readSymbols(input);

	Byte[] bytes = { 0x00, 0xED - 256, 0x5E, 0xC9 - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();

	startAddress = 0;
	finalAddress = reader.getSize();
	disassembleToWriter("test", reader, writer, symbols);

	int index = 1;
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
	assert ("0000 00       ep0000:   NOP\n".equals(writer.output.get(index++)));
	assert ("0001 ED5E               IM   2              ;Handle interrupts via vector table.\n"
	    .equals(writer.output.get(index++)));
	assert ("0003 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0004                    ;\n".equals(writer.output.get(index++)));
	assert ("0004                    end\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testTwoLineComment1() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("                        ;Comments:");
	input.add("0001           ;Handle interrupts ...");
	input.add("               ;... via vector table.");
	Symbols symbols = readSymbols(input);

	Byte[] bytes = { 0x00, 0xED - 256, 0x5E, 0xC9 - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();

	startAddress = 0;
	finalAddress = reader.getSize();
	disassembleToWriter("test", reader, writer, symbols);

	int index = 1;
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
	assert ("0000 00       ep0000:   NOP\n".equals(writer.output.get(index++)));
	assert ("0001 ED5E               IM   2              ;Handle interrupts ...\n".equals(writer.output.get(index++)));
	assert ("0003                                        ;... via vector table.\n".equals(writer.output.get(index++)));
	assert ("0003 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0004                    ;\n".equals(writer.output.get(index++)));
	assert ("0004                    end\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testTwoLineComment2() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("                        ;Comments:");
	input.add("0001 ED5E IM 2 ;Handle interrupts ...");
	input.add("               ;... via vector table.");
	Symbols symbols = readSymbols(input);

	Byte[] bytes = { 0x00, 0xED - 256, 0x5E, 0xC9 - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();

	startAddress = 0;
	finalAddress = reader.getSize();
	disassembleToWriter("test", reader, writer, symbols);

	int index = 1;
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
	assert ("0000 00       ep0000:   NOP\n".equals(writer.output.get(index++)));
	assert ("0001 ED5E               IM   2              ;Handle interrupts ...\n".equals(writer.output.get(index++)));
	assert ("0003                                        ;... via vector table.\n".equals(writer.output.get(index++)));
	assert ("0003 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0004                    ;\n".equals(writer.output.get(index++)));
	assert ("0004                    end\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testCallLineComment1() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("0000           reset    ENTRY 0x0000");
	input.add("0004           more     ENTRY 0x0004       ;Hello world.");
	input.add("                        ;Comments:");
	input.add("0004                    ;Handle interrupts via vector table.");
	Symbols symbols = readSymbols(input);

	Byte[] bytes = { 0xCD - 256, 0x04, 0x00, 0xC9 - 256, 0xED - 256, 0x5E, 0xC9 - 256 };
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
	assert ("0000                    ;* Entry point: reset\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 CD0400   reset:    CALL more           ;Hello world.\n".equals(writer.output.get(index++)));
	assert ("0003 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0004                    ;\n".equals(writer.output.get(index++)));
	assert ("0004                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Entry point: more\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Hello world.\n".equals(writer.output.get(index++)));
	assert ("0004                    ;*\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* 0x0000 (0x0000 reset)\n".equals(writer.output.get(index++)));
	assert ("0004                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0004 ED5E     more:     IM   2              ;Handle interrupts via vector table.\n"
	    .equals(writer.output.get(index++)));
	assert ("0006 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0007                    ;\n".equals(writer.output.get(index++)));
	assert ("0007                    end\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testCallLineComment2() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("0000           reset    ENTRY 0x0000");
	input.add("0004           more     ENTRY 0x0004       ;Hello world.");
	input.add("                        ;Comments:");
	input.add("0004 ED5E               IM 2  ;Handle interrupts via vector table.");
	Symbols symbols = readSymbols(input);

	Byte[] bytes = { 0xCD - 256, 0x04, 0x00, 0xC9 - 256, 0xED - 256, 0x5E, 0xC9 - 256 };
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
	assert ("0000                    ;* Entry point: reset\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 CD0400   reset:    CALL more           ;Hello world.\n".equals(writer.output.get(index++)));
	assert ("0003 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0004                    ;\n".equals(writer.output.get(index++)));
	assert ("0004                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Entry point: more\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Hello world.\n".equals(writer.output.get(index++)));
	assert ("0004                    ;*\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* 0x0000 (0x0000 reset)\n".equals(writer.output.get(index++)));
	assert ("0004                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0004 ED5E     more:     IM   2              ;Handle interrupts via vector table.\n"
	    .equals(writer.output.get(index++)));
	assert ("0006 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0007                    ;\n".equals(writer.output.get(index++)));
	assert ("0007                    end\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testCallLineComment3() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("0000           reset    ENTRY 0x0000");
	input.add("0004           more     ENTRY 0x0004       ;Hello ...");
	input.add("                        ;... world.");
	input.add("                        ;Comments:");
	input.add("0004                    ;Handle interrupts ...");
	input.add("                        ;... via vector table.");
	Symbols symbols = readSymbols(input);

	Byte[] bytes = { 0xCD - 256, 0x04, 0x00, 0xC9 - 256, 0xED - 256, 0x5E, 0xC9 - 256 };
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
	assert ("0000                    ;* Entry point: reset\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 CD0400   reset:    CALL more           ;Hello ...\n".equals(writer.output.get(index++)));
	assert ("0003 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0004                    ;\n".equals(writer.output.get(index++)));
	assert ("0004                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Entry point: more\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Hello ...\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* ... world.\n".equals(writer.output.get(index++)));
	assert ("0004                    ;*\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* 0x0000 (0x0000 reset)\n".equals(writer.output.get(index++)));
	assert ("0004                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0004 ED5E     more:     IM   2              ;Handle interrupts ...\n".equals(writer.output.get(index++)));
	assert ("0006                                        ;... via vector table.\n".equals(writer.output.get(index++)));
	assert ("0006 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0007                    ;\n".equals(writer.output.get(index++)));
	assert ("0007                    end\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testCallLineComment4() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("0000           reset    ENTRY 0x0000");
	input.add("0004           more     ENTRY 0x0004       ;Hello ...");
	input.add("                        ;... world.");
	input.add("                        ;Comments:");
	input.add("0004 ED5E               IM 2  ;Handle interrupts ...");
	input.add("                              ;... via vector table.");
	Symbols symbols = readSymbols(input);

	Byte[] bytes = { 0xCD - 256, 0x04, 0x00, 0xC9 - 256, 0xED - 256, 0x5E, 0xC9 - 256 };
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
	assert ("0000                    ;* Entry point: reset\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 CD0400   reset:    CALL more           ;Hello ...\n".equals(writer.output.get(index++)));
	assert ("0003 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0004                    ;\n".equals(writer.output.get(index++)));
	assert ("0004                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Entry point: more\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Hello ...\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* ... world.\n".equals(writer.output.get(index++)));
	assert ("0004                    ;*\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0004                    ;* 0x0000 (0x0000 reset)\n".equals(writer.output.get(index++)));
	assert ("0004                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0004 ED5E     more:     IM   2              ;Handle interrupts ...\n".equals(writer.output.get(index++)));
	assert ("0006                                        ;... via vector table.\n".equals(writer.output.get(index++)));
	assert ("0006 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0007                    ;\n".equals(writer.output.get(index++)));
	assert ("0007                    end\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testResetLineComment1() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("0000           reset    ENTRY 0x0000");
	input.add("0002           ep0002   ENTRY 0x0002");
	input.add("0008           reset8   ENTRY 0x0008       ;Hello.");
	input.add("0010           reset10  ENTRY 0x0010       ;Hello too.");
	input.add("                        ;Comments:");
	// input.add("0000 185D reset: JR reset8-$ ;entry point after hardware
	// reset.");
	input.add("0000 185D               JR reset8   ;entry point after hardware reset.");
	input.add("0005 D7                 RST reset10 ;pop BC and increment DE module 128.");
	input.add("0008 C1                 POP BC      ;pop BC and set Carry.");
	input.add("0010                                ;pop BC and increment DE module 128.");
	// input.add("0008 C1 reset8: POP BC ;pop BC and set Carry.");
	Symbols symbols = readSymbols(input);

	Byte[] bytes = { 0x18, 0x06, 0x37, 0xCB - 256, 0xEE - 256, 0xD7 - 256, 0x00, 0xC9 - 256, 0xC1 - 256, 0x37,
	    0xC9 - 256, 0xDD - 256, 0xCB - 256, 0x02, 0xF6 - 256, 0xCF - 256, 0xC1 - 256, 0xCF - 256, 0xC9 - 256 };
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
	assert ("0000                    ;* Entry point: reset\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 1806     reset:    JR   reset8-$       ;entry point after hardware reset.\n"
	    .equals(writer.output.get(index++)));
	assert ("0002                    ;\n".equals(writer.output.get(index++)));
	assert ("0002                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0002                    ;* Entry point: ep0002\n".equals(writer.output.get(index++)));
	assert ("0002                    ;*\n".equals(writer.output.get(index++)));
	assert ("0002                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0002                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0002 37       ep0002:   SCF\n".equals(writer.output.get(index++)));
	assert ("0003 CBEE               SET  5,(HL)\n".equals(writer.output.get(index++)));
	assert ("0005 D7                 RST  reset10        ;pop BC and increment DE module 128.\n"
	    .equals(writer.output.get(index++)));
	assert ("0006 00                 NOP\n".equals(writer.output.get(index++)));
	assert ("0007 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0008                    ;\n".equals(writer.output.get(index++)));
	assert ("0008                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* Entry point: reset8\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* Hello.\n".equals(writer.output.get(index++)));
	assert ("0008                    ;*\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* 0x0000 (0x0000 reset)\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* 0x0011 (0x0010 reset10: Hello too.)\n".equals(writer.output.get(index++)));
	assert ("0008                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0008 C1       reset8:   POP  BC             ;pop BC and set Carry.\n".equals(writer.output.get(index++)));
	assert ("0009 37                 SCF\n".equals(writer.output.get(index++)));
	assert ("000A C9                 RET\n".equals(writer.output.get(index++)));
	assert ("000B                    ;\n".equals(writer.output.get(index++)));
	assert ("000B DD                 DB   0xDD\n".equals(writer.output.get(index++)));
	assert ("000C CB02F6CF           DB   0xCB, 0x02, 0xF6, 0xCF\n".equals(writer.output.get(index++)));
	assert ("0010                    ;\n".equals(writer.output.get(index++)));
	assert ("0010                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0010                    ;* Entry point: reset10\n".equals(writer.output.get(index++)));
	assert ("0010                    ;* Hello too.\n".equals(writer.output.get(index++)));
	assert ("0010                    ;*\n".equals(writer.output.get(index++)));
	assert ("0010                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0010                    ;* 0x0005 (0x0002 ep0002)\n".equals(writer.output.get(index++)));
	assert ("0010                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0010 C1       reset10:  POP  BC             ;pop BC and increment DE module 128.\n"
	    .equals(writer.output.get(index++)));
	assert ("0011 CF                 RST  reset8         ;Hello.\n".equals(writer.output.get(index++)));
	assert ("0012 C9                 RET\n".equals(writer.output.get(index++)));
	assert ("0013                    ;\n".equals(writer.output.get(index++)));
	assert ("0013                    end\n".equals(writer.output.get(index++)));
  }

  @Test
  public void testFollowThrough() throws IOException {
	ReadSymbolsFromArray input = new ReadSymbolsFromArray();
	input.add("0000           reset    ENTRY 0x0000");
	input.add("0002           ep0002   ENTRY 0x0002");
	input.add("0008           reset8   ENTRY 0x0008       ;Hello.");
	input.add("                        ;Comments:");
	input.add("0000                    ;entry point after hardware reset.");
	input.add("0008                    ;pop BC and set Carry.");
	Symbols symbols = readSymbols(input);

	Byte[] bytes = { 0x18, 0x06, 0x37, 0xDD - 256, 0xCB - 256, 0x0E, 0xEE - 256, 0x00, 0xC1 - 256, 0x37, 0xC9 - 256 };
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
	assert ("0000                    ;* Entry point: reset\n".equals(writer.output.get(index++)));
	assert ("0000                    ;*\n".equals(writer.output.get(index++)));
	assert ("0000                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0000                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0000 1806     reset:    JR   reset8-$       ;entry point after hardware reset.\n"
	    .equals(writer.output.get(index++)));
	assert ("0002                    ;\n".equals(writer.output.get(index++)));
	assert ("0002                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0002                    ;* Entry point: ep0002\n".equals(writer.output.get(index++)));
	assert ("0002                    ;*\n".equals(writer.output.get(index++)));
	assert ("0002                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0002                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0002 37       ep0002:   SCF\n".equals(writer.output.get(index++)));
	assert ("0003 DDCB0EEE           SET  5,(IX+14)\n".equals(writer.output.get(index++)));
	assert ("0007 00                 NOP\n".equals(writer.output.get(index++)));
	assert ("0008                    ;\n".equals(writer.output.get(index++)));
	assert ("0008                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* Entry point: reset8\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* Hello.\n".equals(writer.output.get(index++)));
	assert ("0008                    ;*\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* Called by:\n".equals(writer.output.get(index++)));
	assert ("0008                    ;* 0x0000 (0x0000 reset)\n".equals(writer.output.get(index++)));
	assert ("0008                    ;****************\n".equals(writer.output.get(index++)));
	assert ("0008 C1       reset8:   POP  BC             ;pop BC and set Carry.\n".equals(writer.output.get(index++)));
	assert ("0009 37                 SCF\n".equals(writer.output.get(index++)));
	assert ("000A C9                 RET\n".equals(writer.output.get(index++)));

	assert ("000B                    ;\n".equals(writer.output.get(index++)));
	assert ("000B                    end\n".equals(writer.output.get(index++)));
  }

}
