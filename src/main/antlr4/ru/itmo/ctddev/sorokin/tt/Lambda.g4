grammar Lambda;

expression returns[Lambda ret]
    : fst=application  { $ret = $fst.ret; }
      (nxt=application { $ret = new Application($ret, $nxt.ret); })
      lst=abstraction  { $ret = new Application($ret, $lst.ret); }
    | application { $ret = $application.ret; }
    | abstraction { $ret = $abstraction.ret; }
    ;

abstraction returns[Lambda ret]
    : '\\' VAR '.' expression { $ret = new Abstraction($VAR.text, $expression.ret); }
    ;

application returns[Lambda ret]
    : app=application SPACE atomic { $ret = new Application($app.ret, $atomic.ret); }
    | atomic { $ret = $atomic.ret; }
    ;

atomic returns[Lambda ret]
    : '(' expression ')' { $ret = $expression.ret; }
    | VAR { $ret = new Variable($VAR.text); }
    ;

VAR : [a-z] [a-z0-9\']* ;
SPACE : ' ' ;