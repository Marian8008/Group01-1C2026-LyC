include macros.asm
include macros2.asm
include number.asm

.MODEL LARGE
.386
.STACK 200h
MAXTEXTSIZE equ 50

.DATA
i1 dd ?
i2 dd ?
i3 dd ?
i dd ?
a dd ?
x dd ?
f1 dd ?
f2 dd ?
f3 dd ?
s1 db MAXTEXTSIZE dup(?), '$'
s2 db MAXTEXTSIZE dup(?), '$'
s3 db MAXTEXTSIZE dup(?), '$'

_1 dd 1.0
_3 dd 3.0
_10 dd 10.0
_5 dd 5.0
_10x0 dd 10.0
_1x0 dd 1.0
_str1 db "Prueba de while, write, NOT", '$'
_str2 db "Prueba de if simple, OR", '$'
_str3 db "Prueba de if else, read, AND", '$'
_str4 db "Prueba de float, bloques anidados", '$'
_str5 db "hola", '$'

@aux1 dd ?
@aux2 dd ?
@aux3 dd ?

.CODE
START:
mov AX,@DATA
mov DS,AX
mov ES,AX

FLD _1
FSTP i1

FLD _3
FSTP i2

L1:
FLD i1
FLD i2
FXCH
FCOMP
FSTSW AX
FFREE
SAHF
JL L2
displayString _str1
FLD i1
FLD _1
FADD
FSTP @aux1

FLD @aux1
FSTP i1

JMP L1
L2:

FLD i2
FLD i1
FXCH
FCOMP
FSTSW AX
FFREE
SAHF
JG L4
FLD i1
FLD _10
FXCH
FCOMP
FSTSW AX
FFREE
SAHF
JE L3
L4:
displayString _str2
L3:

FLD i2
FLD i1
FXCH
FCOMP
FSTSW AX
FFREE
SAHF
JE L5
FLD i2
FLD _10
FXCH
FCOMP
FSTSW AX
FFREE
SAHF
JE L5
displayString _str3
FLD _10x0
FSTP f1

FLD _1x0
FSTP f2

L7:
FLD f2
FLD f1
FXCH
FCOMP
FSTSW AX
FFREE
SAHF
JG L8
displayString _str4
FLD f1
FLD _1x0
FSUB
FSTP @aux2

FLD @aux2
FSTP f1

FLD f1
FLD f2
FXCH
FCOMP
FSTSW AX
FFREE
SAHF
JL L9
LEA SI, _str5
LEA DI, s3
STRCPY

L9:

JMP L7
L8:

JMP L6
L5:
getString s1
L6:

FLD _1
FSTP i

L10:
FLD i
FLD _5
FXCH
FCOMP
FSTSW AX
FFREE
SAHF
JG L11
GetInteger i
FLD i
FLD _1
FADD
FSTP @aux3

FLD @aux3
FSTP i

JMP L10
L11:

FLD _3
FSTP a


mov AX,4C00h
int 21h
END START
