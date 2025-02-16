;==============================================================================
; File: x86_64_caesar.asm
; Description: Intro to Syscalls, Buffer Processing, and Functions in x86-64
;              Demonstrates stack usage, reading/writing from the OS, 
;              and a Caesar cipher transformation.
; 
; To assemble and link:
;   nasm -felf64 x86_64_caesar.asm -o x86_64_caesar.o
;   ld x86_64_caesar.o -o x86_64_caesar
; Then run:
;   ./x86_64_caesar
; 
; By default, the program will read from STDIN until EOF or no more data is 
; available, apply a Caesar offset to each character, and write the result 
; to STDOUT. 
;
; For testing, you can redirect input from a file:
;   ./x86_64_caesar < input.txt
; or type into the terminal and press Ctrl+D (EOF) to end.
;==============================================================================
 
section .bss
charBuffer      resb 100      ;Buffer array to hold characters

section .data
bufferMax       dq   100      ;Maximum number of characters to read at once
caesarOffset    db   6        ;Offset for Caesar cipher

;Messages and message constants
LF              equ  10       ;ASCII char const for Linux linefeed
NULL            equ  0        ;ASCII char const for NULL

welcomeMessage  db  "Starting reading...", LF, NULL
doneMessage     db  LF, "Done!", LF, NULL
writeError      db  "Could not write to file!", LF, NULL
lineFeed        db  LF, NULL

welcomeLength   equ 20
doneLength      equ 6
errorLength     equ 25

;------------- Constants -------------
EXIT_SUCCESS    equ  0        ;Exit code for success
EXIT_FAIL       equ  1        ;Exit code for error
TRUE            equ  1
FALSE           equ  0

;File descriptors for common files
STDIN           equ  0
STDOUT          equ  1
STDERR          equ  2

;Syscall codes (for rax)
SYS_read        equ  0
SYS_write       equ  1
SYS_open        equ  2
SYS_close       equ  3
SYS_exit        equ  60       ;Syscall code for terminating the program

;Flags for file opens (not heavily used in this sample)
O_CREATE        equ  64
O_APPEND        equ  1024
O_RDONLY        equ  000000q
O_WRONLY        equ  000001q

section .text
global _start
global populateBuffer
global translateCharsFromBuffer
global writeBuffer
global translateFile

;------------------------------------------------------------------------------
; _start: Program entry point
;------------------------------------------------------------------------------
_start:

    ; Print the welcome message
    mov   rax, SYS_write
    mov   rdi, STDOUT
    mov   rsi, welcomeMessage
    mov   rdx, welcomeLength
    syscall

    ; Call the "main" function: translateFile
    call  translateFile

    ; Print the done message
    mov   rax, SYS_write
    mov   rdi, STDOUT
    mov   rsi, doneMessage
    mov   rdx, doneLength
    syscall

    ; Exit the program
    mov   eax, SYS_exit      ; Syscall: exit
    mov   edi, EXIT_SUCCESS  ; Return code: 0
    syscall

;------------------------------------------------------------------------------
; translateFile:
;   Reads from STDIN and performs repeated Caesar transformation 
;   until no more data remains (EOF).
;------------------------------------------------------------------------------
translateFile:
    push  rbp
    mov   rbp, rsp

mainLoop:
    ; Populate the buffer using populateBuffer
    ;  rdi = file descriptor (STDIN)
    ;  rsi = buffer address (charBuffer)
    mov   rdi, STDIN
    mov   rsi, charBuffer
    call  populateBuffer    ; RAX will have # of chars read

    ; Compare the number of chars read (RAX) to bufferMax
    ; If RAX < bufferMax, we jump to lastCall (means we didn't fill the entire buffer)
    cmp   rax, [bufferMax]
    jl    lastCall

    ; We got a "full" buffer (or at least 100 chars). 
    ; Translate all of them, then go back to read again.
    mov   rdi, rax          ; rdi = number of characters read
    mov   rsi, charBuffer   ; rsi = buffer address
    call  translateCharsFromBuffer
    jmp   mainLoop

lastCall:
    ; If RAX == 0, we have EOF or no more data
    cmp   rax, 0
    je    doneTranslation

    ; We got something less than bufferMax but more than 0; 
    ; translate the last few characters.
    mov   rdi, rax
    mov   rsi, charBuffer
    call  translateCharsFromBuffer

doneTranslation:
    pop   rbp
    ret

;------------------------------------------------------------------------------
; populateBuffer:
;   Reads into the buffer from the given file descriptor. 
;   Arguments:
;     rdi = file descriptor
;     rsi = address of buffer
;   Returns (# of characters read) in RAX.
;------------------------------------------------------------------------------
populateBuffer:
    push  rbp
    mov   rbp, rsp

    ; rax = SYS_read
    ; rdi = file descriptor (already set by caller)
    ; rsi = buffer address (already set by caller)
    ; rdx = max # of bytes to read
    mov   rax, SYS_read
    mov   rdx, [bufferMax]
    syscall

    ; On return, RAX has the number of bytes read.
    leave
    ret

;------------------------------------------------------------------------------
; writeBuffer:
;   Writes the given number of characters from a buffer to a file descriptor.
;   Arguments:
;     rdi = file descriptor (e.g., STDOUT)
;     rsi = address of buffer
;     rdx = number of characters to write
;   Returns the syscall result in RAX. 
;------------------------------------------------------------------------------
writeBuffer:
    push  rbp
    push  rbx              ; We'll use rbx, so preserve it
    mov   rbp, rsp

    ; rax = SYS_write
    ; rdi, rsi, rdx are set by the caller
    mov   rax, SYS_write
    syscall               ; RAX = # bytes written, or negative if error

    ; Check if write was successful
    mov   rbx, 0
    cmp   rax, rbx
    jg    noError         ; If RAX > 0, we wrote successfully

    ; If there was an error (RAX <= 0), display an error message and exit(1)
    mov   rax, SYS_write
    mov   rdi, STDOUT
    mov   rsi, writeError
    mov   rdx, errorLength
    syscall

    mov   eax, SYS_exit
    mov   edi, EXIT_FAIL
    syscall

noError:
    pop   rbx
    pop   rbp
    ret

;------------------------------------------------------------------------------
; translateCharsFromBuffer:
;   Applies a Caesar cipher to each byte in the buffer, then writes it out.
;   Arguments:
;     rdi = number of characters in the buffer
;     rsi = address of the buffer
;   No explicit return.
;------------------------------------------------------------------------------
translateCharsFromBuffer:
    push  rbp
    mov   rbp, rsp

    ; Number of chars to process stored in RDI
    ; Buffer address is in RSI

    mov   rbx, rdi         ; We'll use RBX to hold the loop limit (# chars)
    xor   rcx, rcx         ; RCX = 0 (loop counter)

translation_loop:
    ; Load one byte from the buffer
    mov   al, [rsi + rcx]
    ; Add the Caesar offset
    add   al, [caesarOffset]
    ; Store it back
    mov   [rsi + rcx], al

    inc   rcx
    cmp   rcx, rbx
    jl    translation_loop

    ; Now write the buffer out to STDOUT
    ;   rdi = STDOUT
    ;   rsi = buffer address
    ;   rdx = number of chars (rbx)
    mov   rdi, STDOUT
    mov   rdx, rbx
    ; rsi is already correct
    call  writeBuffer

    leave
    ret
