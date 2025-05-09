# dasmZ80 download folder
This page contains released versions of the Z80 Disassembler.

## Version 1.4.1
Bug fix: Use constant symbol when loading BC, DE or HL.
Bug fix: Sort entry point references by address.

## Version 1.4.0:
Accept user defined comments from symbol definition file.

## Version 1.3.1:
Bug fix: allow same value for symbols defined for port addresses and symbols defined for memory addresses (including entry points and discovered labels). 

## Version 1.3.0:
Merge unvisited code with disassembled output.

## Version 1.2.2:
Bug fix: Distinguish between defined memory addresses, defined entry points and discovered entry points (aka labels).
See Usage for more details. The same limitations apply as for version 1.0.0.

## Version 1.2.1:
Bug fix: Recognize call/jump/reset addresses as discovered entry points.
See Usage for more details. The same limitations apply as for version 1.0.0.

## Version 1.2.0:
Refactored to use entry points of execution paths during disassembly.
See Usage for more details. The same limitations apply as for version 1.0.0.

## Version 1.1.0:
Added optional input file (extension .sym) with symbol and constant definitions. See Usage for more details. The same limitations apply as for version 1.0.0.

## Version 1.0.0:
This is an initial version which suits my personal needs. Future updates and/or bug fixes may become available upon request.

Known limitations:
* Input file must have ".bin" extension.
* Input file must be in binary format.
* Binary code is assumed to be Z80 compatible.
* Start address is assumed to be 0x0000.
* Output file appears in same folder as input file.
* Output file has same filename as input file.
* Output file has ".lst" extension.
* Output file is in listing format (no assembler compatible input as it includes address and binary code).

## Usage:
Download one of the executable jar files to a local folder.
In order to use the disassembler you need a Java runtime.
See README.md in the root folder for usage instructions.
