package com.github.hanwelmer.dasmZ80;

import org.junit.Test;

public class TestDisassemble extends DasmZ80 {

  @Test
  public void testIllegalOpcode0x03() {
	Byte[] bytes = { 0xDD - 256, 0xDC - 256, 0x34, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();
	disassemble("test", reader, writer);
	assert (writer.output.size() > 0);
	assert ("                               ;Unprocessed binary code from input file\n".equals(writer.output.get(7)));
	assert ("                               ;0000: DD DC 34 12\n".equals(writer.output.get(9)));
  }

  @Test
  public void testIllegalOpcode0x22() {
	Byte[] bytes = { 0x00, 0x02, 0x01, 0x34, 0x12, 0xDD - 256, 0xDC - 256, 0x34, 0x12, 0x09, 0x0A, 0x0B, 0x0C, 0x0D,
	    0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
	    0x20, 0x21, 0x22 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();
	disassemble("test", reader, writer);
	assert (writer.output.size() > 0);
	assert ("       NOP\n".equals(writer.output.get(4)));
	assert ("       LD   (BC),A\n".equals(writer.output.get(5)));
	assert ("       LD   BC,0x1234\n".equals(writer.output.get(6)));
	assert ("                               ;Unprocessed binary code from input file\n".equals(writer.output.get(10)));
	assert ("                               ;0005: DD DC 34 12 09 0A 0B 0C 0D 0E 0F\n".equals(writer.output.get(12)));
	assert ("                               ;0010: 10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F\n"
	    .equals(writer.output.get(13)));
	assert ("                               ;0020: 20 21 22\n".equals(writer.output.get(14)));
  }

}
