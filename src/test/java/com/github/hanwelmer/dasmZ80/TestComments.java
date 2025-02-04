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
}
