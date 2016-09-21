grammar Lambda;

expression returns[LambdaStructure ret]
    : fst=application  { $ret = $fst.ret; }
      (nxt=application { $ret = LambdaFactoryKt.application($ret, $nxt.ret); })
      lst=abstraction  { $ret = LambdaFactoryKt.application($ret, $lst.ret); }
    | application { $ret = $application.ret; }
    | abstraction { $ret = $abstraction.ret; }
    ;

abstraction returns[LambdaStructure ret]
    : '\\' VAR '.' expression { $ret = LambdaFactoryKt.abstraction($VAR.text, $expression.ret); }
    ;

application returns[LambdaStructure ret]
    : app=application atomic { $ret = LambdaFactoryKt.application($app.ret, $atomic.ret); }
    | atomic { $ret = $atomic.ret; }
    ;

atomic returns[LambdaStructure ret]
    : '(' expression ')' { $ret = $expression.ret; }
    | VAR { $ret = LambdaFactoryKt.variable($VAR.text); }
    ;

VAR : ' '? [a-z] [a-z0-9\']* { setText(getText().trim()); };
WS : (' ' | '\t')+ -> skip;