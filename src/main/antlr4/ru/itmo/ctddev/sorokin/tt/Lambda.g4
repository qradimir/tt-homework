grammar Lambda;

expression returns[LambdaStructure ret]
    : fst=application  { $ret = $fst.ret; }
      (nxt=application { $ret = LambdaStructureKt.makeApplication($ret, $nxt.ret); })
      lst=abstraction  { $ret = LambdaStructureKt.makeApplication($ret, $lst.ret); }
    | application { $ret = $application.ret; }
    | abstraction { $ret = $abstraction.ret; }
    ;

abstraction returns[LambdaStructure ret]
    : '\\' VAR '.' expression { $ret = LambdaStructureKt.makeAbstraction($VAR.text, $expression.ret); }
    ;

application returns[LambdaStructure ret]
    : app=application atomic { $ret = LambdaStructureKt.makeApplication($app.ret, $atomic.ret); }
    | atomic { $ret = $atomic.ret; }
    ;

atomic returns[LambdaStructure ret]
    : '(' expression ')' { $ret = $expression.ret; }
    | VAR { $ret = LambdaStructureKt.makeVariableReference($VAR.text); }
    ;

VAR : ' '? [a-z] [a-z0-9\']* { setText(getText().trim()); };
WS : (' ' | '\t')+ -> skip;