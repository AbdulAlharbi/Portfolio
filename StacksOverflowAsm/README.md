## **3) `StacksOverflowAsm/README.md`**

<details>
<summary><code>StacksOverflowAsm/README.md</code></summary>

```markdown
# Stacks and Overflow (x86-64 Assembly)

An x86 assembly project demonstrating:

- **Syscalls**: read, write, and exit on Linux
- **Buffer usage** with potential overflow if not careful
- **Basic stack frames** for functions (`push rbp`, `mov rbp, rsp`, etc.)
- **Caesar cipher** transformation of input data

## Building & Running

1. Install **NASM** and **ld** (GNU binutils).
2. Assemble:
   ```bash
   nasm -f elf64 stacks_overflow.asm -o stacks_overflow.o

3. Link: ld stacks_overflow.o -o stacks_overflow
4. Run: ./stacks_overflow
5. Type some text and press Ctrl+D to end input. The program will apply a Caesar shift and write the output.

Folder Contents
stacks_overflow.asm: main assembly code
requirements.txt: minimal environment info

Notes
Modify caesarOffset in the .data section to change the shift amount.
Investigate resb 100 vs. read size to explore potential overflows.
Have fun exploring low-level Linux assembly!

</details>

---