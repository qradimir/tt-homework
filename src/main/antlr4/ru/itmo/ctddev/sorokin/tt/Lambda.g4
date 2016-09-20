grammar Lambda;

expression returns[LambdaStructure ret]
    : fst=application  { $ret = $fst.ret; }
      (nxt=application { $ret = LambdaStructure.Factory.application($ret, $nxt.ret); })
      lst=abstraction  { $ret = LambdaStructure.Factory.application($ret, $lst.ret); }
    | application { $ret = $application.ret; }
    | abstraction { $ret = $abstraction.ret; }
    ;

abstraction returns[LambdaStructure ret]
    : '\\' VAR '.' expression { $ret = LambdaStructure.Factory.abstraction($VAR.text, $expression.ret); }
    ;

application returns[LambdaStructure ret]
    : app=application atomic { $ret = LambdaStructure.Factory.application($app.ret, $atomic.ret); }
    | atomic { $ret = $atomic.ret; }
    ;

atomic returns[LambdaStructure ret]
    : '(' expression ')' { $ret = $expression.ret; }
    | VAR { $ret = LambdaStructure.Factory.variable($VAR.text); }
    ;

VAR : ' '? [a-z] [a-z0-9\']* { setText(getText().trim()); };
WS : (' ' | '\t')+ -> skip;