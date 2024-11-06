package com.github.hanwelmer.dasmZ80;

import org.junit.Test;

public class TestDisassemble extends DasmZ80 {

  @Test
  public void testReturn() {
	Byte[] bytes = { 0xC9 - 256, 0xC0 - 256, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();
	disassemble("test", reader, writer);
	assert (writer.output.size() > 0);
	assert ("0000 C9                 RET\n".equals(writer.output.get(4)));
	assert ("0001\n".equals(writer.output.get(5)));
	assert ("0001 C0                 RET  NZ\n".equals(writer.output.get(6)));
	assert ("0002\n".equals(writer.output.get(7)));
	assert ("0002 00                 NOP\n".equals(writer.output.get(8)));
  }

  @Test
  public void testIllegalOpcode0x03() {
	Byte[] bytes = { 0xDD - 256, 0xDC - 256, 0x34, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();
	disassemble("test", reader, writer);
	assert (writer.output.size() > 0);
	assert ("0000                    ;Unprocessed binary code from input file\n".equals(writer.output.get(8)));
	assert ("0000 DDDC3412\n".equals(writer.output.get(10)));
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
	assert ("0000 00                 NOP\n".equals(writer.output.get(4)));
	assert ("0001 02                 LD   (BC),A\n".equals(writer.output.get(5)));
	assert ("0002 013412             LD   BC,0x1234\n".equals(writer.output.get(6)));
	assert ("0005                    ;Unprocessed binary code from input file\n".equals(writer.output.get(11)));
	assert ("0005 DDDC3412 090A0B0C 0D0E0F\n".equals(writer.output.get(13)));
	assert ("0010 10111213 14151617 18191A1B 1C1D1E1F\n".equals(writer.output.get(14)));
	assert ("0020 202122\n".equals(writer.output.get(15)));
  }

  @Test
  public void testPortReferenceTable() {
	Byte[] bytes = { 0xDB - 256, 0x12, 0xD3 - 256, 0xFE - 256, 0xD3 - 256, 0x12, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();
	disassemble("test", reader, writer);
	assert (writer.output.size() == 16);
	assert ("0000                    ;I/O Port definitions\n".equals(writer.output.get(2)));
	assert ("0000            port12  EQU  12\n".equals(writer.output.get(3)));
	assert ("0000            portFE  EQU  FE\n".equals(writer.output.get(4)));
	assert ("0000 DB12               IN   A,(port12)\n".equals(writer.output.get(7)));
	assert ("0002 D3FE               OUT  (portFE),A\n".equals(writer.output.get(8)));
	assert ("0004 D312               OUT  (port12),A\n".equals(writer.output.get(9)));
	assert ("0006 00                 NOP\n".equals(writer.output.get(10)));
	assert ("\nI/O-port cross reference list:\n".equals(writer.output.get(12)));
	assert ("port12  =12: 0000 0004\n".equals(writer.output.get(13)));
	assert ("portFE  =FE: 0002\n".equals(writer.output.get(14)));
	assert ("\nMemory cross reference list:\n".equals(writer.output.get(15)));
  }

  @Test
  public void testMemoryReferenceTable() {
	Byte[] bytes = { 0xCD - 256, 0x03, 0x00, 0xC3 - 256, 0x03, 0x00, 0xC2 - 256, 0x03, 0x00, 0x10, 0xF8 - 256, 0x38,
	    0xF6 - 256, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	StringWriter writer = new StringWriter();
	disassemble("test", reader, writer);
	assert (writer.output.size() == 15);
	assert ("0000 CD0300             CALL 0x0003\n".equals(writer.output.get(4)));
	assert ("0003 C30300             JP   0x0003\n".equals(writer.output.get(5)));
	assert ("0006 C20300             JP   NZ,0x0003\n".equals(writer.output.get(6)));
	assert ("0009 10F8               DJNZ lbl0003\n".equals(writer.output.get(7)));
	assert ("000B 38F6               JR   C,lbl0003\n".equals(writer.output.get(8)));
	assert ("000D 00                 NOP\n".equals(writer.output.get(9)));
	assert ("\nI/O-port cross reference list:\n".equals(writer.output.get(11)));
	assert ("\nMemory cross reference list:\n".equals(writer.output.get(12)));
	assert ("lbl0003 =0003: 0000 0003 0006 0009\n".equals(writer.output.get(13)));
	assert ("               000B\n".equals(writer.output.get(14)));
  }

}
