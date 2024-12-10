# dasmZ80
Java command line application that disassembles a binary file to a Z80 assembler listing file.

Currently a first version is available in the download folder. Future updates and/or bug fixes may become available upon request.

## Development:
The project was developed using:
* openjdk 11.0.2
* Apache Maven 3.6.3
* Eclipse Java EE IDE Neon.3 Release (4.6.3)

### maven
* open commandline tool
* nagivate to subfolder 
* mvn install

## Usage:
Open a Windows Command Prompt window, navigate to the local folder and enter:

`java -jar dasmZ80.jar [-s file.sym] filename.ext`

`where filename.ext is file to be disassembled.`
`  and -s file.sym is an optional input file with symbol definitions and comments.`

The symbol file contains label definitions, constant definitions and entry point definitions.

An entry point definition is as follows:
`0000 ENTRY 0x0000 ;comment`
where 
* 0000 is the 16-bit hexadecimal value assigned to the entry point
* ENTRY is the entry point statement
* 0x0000 is the expression used to calculate the value for the entry point (decimal or hexadecimal constant expression).
* comment is an optional comment.
The disassembler uses one or more entry points to determine execution paths through the binary code in the binary input file. If no entry points are defined, a single entry point 0x0000 is assumed.

Label and constant definitions are as follows:
`0010 name EQU 0x10 ;comment`
where 
* 0010 is the 16-bit hexadecimal value assigned to the symbol
* name is the name of the symbol (max 8 characters; first character must be a..aA..Z_
* EQU is the equate statement
* 0x10 is the expression used to calculate the value for the symbol (decimal or hexadecimal constant expression).
* comment is an optional comment.

If a line contains:
`;I/O addresses:`
then the subsequent symbol definitions are interpreted as symbols for I/O port values until the start of memory symbols or constant symbols or end of file.

If a line contains:
`;Memory addresses:`
then the subsequent symbol definitions are interpreted as symbols for memory addresses until the start of I/O port values or constant symbols or end of file.

If a line contains:
`;Constants:`
then the subsequent symbol definitions are interpreted as symbols for any other constants until the start of I/O port values or memory symbols or end of file.

All other lines that start with a ; (semicolon), except optional preceding whitespace, are ignored.