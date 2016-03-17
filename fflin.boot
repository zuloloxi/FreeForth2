1 constant [os]`
\ do dlopen now, and hook it to `boot to do dlopen for the turnkey case
: dlsetup libc@ 0- 0<> IF drop ;THEN drop "libc.so.6" #lib libc! ;
dlsetup
: libc.` wsparse libc@ #fun lit` #call ' call, ;
: libc_ libc@ #fun #call ; \ runtime version, turnkey safe

create `SEGVact 140 allot `SEGVact 140 0 fill
: `SEGVhndlr !"SEGV caught" ;
`SEGVhndlr ' `SEGVact!
$40000000 `SEGVact 132+ ! \ SA_NODEFER
: SEGVthrow 0 `SEGVact 11 3 "sigaction" libc_ drop ;
: linsetup dlsetup SEGVthrow ;

create ffpath "FFPATH" 1_ libc. getenv 0- 0<> IF zlen THEN dup 25+ ; allot
0- 0<> IF tuck ffpath ':' overc! 1+ place + ELSE drop ffpath THEN
":/usr/local/share/ff:.:^@" ( 24 ) rot place drop

ffpath zlen over+ swap 1+ dup >r
START dupc@ ':' = 2drop IF r> 2dup - swap 1- c! 1+ dup >r THEN 1+
ENTER <= UNTIL 2drop r> 1- 0 swap c!

create open' 80 allot
: openlib over dupc@ '.' = 2drop IF 1+ THEN
  dupc@ '.' = 2drop IF 1+ THEN c@ '/' = 2drop IF openr ;THEN 2>r ffpath
  START tuck 2dup open' place + '/' overc! 1+ 2r rot place drop over+ swap
  r + 1+ open' swap openr 0- 0>= IF 2rdrop nip ;THEN drop
  ENTER c@+ 0- 0= UNTIL 2rdrop 2drop -1 ;

: needs` ;` wsparse
: needed 2dup+ dupc@ >r dup>r '`' swap c! 1+ find 2r> c! 0= IF 2drop ;THEN 1-
  2dup openlib 0- 0< IF drop type space !"Can't_open_file." ;THEN
  >r marker tp@ eob over- under r read r> close drop
  over w@ [ "#!" drop w@ ] lit = 2drop
  IF bounds BEGIN c@+ 10- 0= drop UNTIL swap over- THEN eval ;

here 0 , dup : `main lit @ execute 0 exit
: -f` lit needs` "main" find 0- 0= drop IF swap ! `main ' `top !^ ELSE 2drop THEN ;
: quit `top ^^ `top ;

linsetup ' ossetup !^ `boot ' >r
"ff.ff" needed ' `exec ;
