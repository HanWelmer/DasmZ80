package com.github.hanwelmer.dasmZ80;

import java.util.HashMap;

import org.junit.Test;

public class TestDisassemble extends DasmZ80 {

  @Test
  public void testNop() {
    ConfigurationParameters parameters = null;
    Byte[] bytes = { 0x00 };
    ByteReader reader = new ReadFromArray(bytes);
    HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
    assert (result != null);
    assert (result.size() == 1);
    assert (result.get(0) != null);
    assert ("       NOP\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testHalt() {
    ConfigurationParameters parameters = null;
    Byte[] bytes = { 0x76 };
    ByteReader reader = new ReadFromArray(bytes);
    HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
    assert (result != null);
    assert (result.size() == 1);
    assert (result.get(0) != null);
    assert ("       HALT\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBcA() {
    ConfigurationParameters parameters = null;
    Byte[] bytes = { 0x02 };
    ByteReader reader = new ReadFromArray(bytes);
    HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
    assert (result != null);
    assert (result.size() == 1);
    assert (result.get(0) != null);
    assert ("       LD   (BC),A\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBcWordSmall() {
    ConfigurationParameters parameters = null;
    Byte[] bytes = { 0x01, 0x34, 0x12 };
    ByteReader reader = new ReadFromArray(bytes);
    HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
    assert (result != null);
    assert (result.size() == 1);
    assert (result.get(0) != null);
    assert ("       LD   BC,0x1234\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBcWordBig() {
    ConfigurationParameters parameters = null;
    Byte[] bytes = { 0x01, 0xDE - 256, 0xBC - 256 };
    ByteReader reader = new ReadFromArray(bytes);
    HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
    assert (result != null);
    assert (result.size() == 1);
    assert (result.get(0) != null);
    assert ("       LD   BC,0xBCDE\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBByteSmall() {
    ConfigurationParameters parameters = null;
    Byte[] bytes = { 0x06, 0x12 };
    ByteReader reader = new ReadFromArray(bytes);
    HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
    assert (result != null);
    assert (result.size() == 1);
    assert (result.get(0) != null);
    assert ("       LD   B,0x12\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBByteBig() {
    ConfigurationParameters parameters = null;
    Byte[] bytes = { 0x06, 0xAB - 256 };
    ByteReader reader = new ReadFromArray(bytes);
    HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
    assert (result != null);
    assert (result.size() == 1);
    assert (result.get(0) != null);
    assert ("       LD   B,0xAB\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testDjnzForward() {
    ConfigurationParameters parameters = null;
    Byte[] bytes = { 0x10, 0x12 };
    ByteReader reader = new ReadFromArray(bytes);
    HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
    assert (result != null);
    assert (result.size() == 1);
    assert (result.get(0) != null);
    assert ("       DJNZ lbl0014\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testDjnzBackwards() {
    ConfigurationParameters parameters = null;
    Byte[] bytes = { 0x10, -2 };
    ByteReader reader = new ReadFromArray(bytes);
    HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
    assert (result != null);
    assert (result.size() == 1);
    assert (result.get(0) != null);
    assert ("       DJNZ lbl0000\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testOutPortASmall() {
    ConfigurationParameters parameters = null;
    Byte[] bytes = { 0xD3 - 256, 0x12 };
    ByteReader reader = new ReadFromArray(bytes);
    HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
    assert (result != null);
    assert (result.size() == 1);
    assert (result.get(0) != null);
    assert ("       OUT  (0x12),A\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testOutPortABig() {
    ConfigurationParameters parameters = null;
    Byte[] bytes = { 0xD3 - 256, 0xAB - 256 };
    ByteReader reader = new ReadFromArray(bytes);
    HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
    assert (result != null);
    assert (result.size() == 1);
    assert (result.get(0) != null);
    assert ("       OUT  (0xAB),A\n".equals(result.get(0).toAsmString()));
  }

}
