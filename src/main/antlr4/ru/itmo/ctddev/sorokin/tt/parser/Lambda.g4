grammar Lambda;

@header{
import ru.itmo.ctddev.sorokin.tt.lambdas.LambdaFactoryKt;
import ru.itmo.ctddev.sorokin.tt.lambdas.LambdaStructure;
}

let_expression returns[LambdaStructure ret]
    : LET VAR EQ def=let_expression IN expr=let_expression
        { $ret = LambdaFactoryKt.let($VAR.text, $def.ret, $expr.ret); }
    | expression { $ret = $expression.ret; }
    ;

expression returns[LambdaStructure ret]
    : application abstraction  { $ret = LambdaFactoryKt.application($application.ret, $abstraction.ret); }
    | application { $ret = $application.ret; }
    | abstraction { $ret = $abstraction.ret; }
    ;

abstraction returns[LambdaStructure ret]
    : LAMBDA VAR DOT expression { $ret = LambdaFactoryKt.abstraction($VAR.text, $expression.ret); }
    ;

application returns[LambdaStructure ret]
    : app=application atomic { $ret = LambdaFactoryKt.application($app.ret, $atomic.ret); }
    | atomic { $ret = $atomic.ret; }
    ;

atomic returns[LambdaStructure ret]
    : OBR let_expression CBR { $ret = $let_expression.ret; }
    | VAR { $ret = LambdaFactoryKt.variable($VAR.text); }
    ;
LET : 'let';
IN : 'in';
EQ : '=';
OBR : '(';
CBR : ')';
LAMBDA : '\\';
DOT : '.';
VAR :  [a-z] [a-z0-9\']*;
WS : (' ' | '\t')+ -> skip;