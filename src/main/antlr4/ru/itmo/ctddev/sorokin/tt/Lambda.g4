grammar Lambda;

let_expression returns[LambdaStructure ret]
    : LET VAR '=' def=let_expression IN expr=let_expression
        { $ret = LambdaFactoryKt.let($VAR.text, $def.ret, $expr.ret); }
    | expression { $ret = $expression.ret; }
    ;

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
    : '(' let_expression ')' { $ret = $let_expression.ret; }
    | VAR { $ret = LambdaFactoryKt.variable($VAR.text); }
    ;

LET : 'let ';
IN : ' in';
VAR : ' '? [a-z] [a-z0-9\']* { setText(getText().trim()); };
WS : (' ' | '\t')+ -> skip;