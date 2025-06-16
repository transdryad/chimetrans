# Chime-Trans
In honor of pride month, a transpiler/assembler from pseudo assembly code to Chime (my lanmguage/bytecode? in midi files).

For examples of basic programming concepts implemented in this assembly, see the snippets folder or any file with a .chl extension.

Supports java 21 and above.

Copyright 2025 Hazel Viswanath <viswanath.hazel@gmail.com>.
Licensed under GPL 3 or above.

Chime-Trans is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Chime-Trans is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See LICENSE in the project root for more details.

You should have received a copy of the GNU General Public License along with Chime-Trans. If not, see <https://www.gnu.org/licenses/>.

The GPL also applies to the files within the snippets and libs directories.

# Usage

```bash
java -jar chimetrans-1.0-all.jar <filename.chl>
```

Use with --now for immediate execution. Use -o <outputfilename> to specify an output file.

# Why & How

I made this because it's very annoying to write code in a sheet music editor.
I struggled a lot with making labels and functions properly refer to the correct instruction. Turns out, it was an off-by-one error.

# Language reference:

There is a hidden accumulator variable that is often used to store the result of instructions: currentVal.

Labels are defined like this:
```code
_label:
```
And referred to without the colon.

| Instruction | Args | Description                                                                               |
|:------------|:-----|:------------------------------------------------------------------------------------------|
| bgn         | 0    | Starts every program.                                                                     |
| add         | 2    | Adds arguments.                                                                           |
| sub         | 2    | Subtracts arguments.                                                                      |
| mul         | 2    | Multiplies arguments.                                                                     |
| div         | 2    | Divides arguments.                                                                        |
| prt         | 0    | Prints currentVal.                                                                        |
| pch         | 0    | Prints current val as an ascii/unicode code point.                                        |
| pln         | 0    | Prints a new line.                                                                        |
| psh         | 1    | Pushes currentVal to the given stack.                                                     |
| pop         | 1    | Pops from the given stack into currentVal.                                                |
| ipt         | 0    | Get one byte of input (one char) from stdin and put it in currentVal.                     |
| ldi         | 1    | Loads/holds the given value in currentVal.                                                |
| cva         | N/A  | Refers to currentVal in the context of an argument to an instruction.                     |
| evl         | 2    | Compares the given instructions. Gives 1 for <, 2 for ==, and 3 for >.                    |
| jmp         | 1    | Jumps to a given index or label.                                                          |
| jeq         | 2    | Jumps to a given index or label if the second argument is equal to currentVal.            |
| jid         | N/A  | Evaluates to the index that must be pushed to the call stack, as in snippets/function.chl |
| end         | 0    | Marks the program's endpoint.                                                             |
