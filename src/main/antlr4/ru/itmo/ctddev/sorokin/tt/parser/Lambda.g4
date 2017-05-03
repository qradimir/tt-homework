grammar Lambda;

@header{
import ru.itmo.ctddev.sorokin.tt.common.Structure;
import ru.itmo.ctddev.sorokin.tt.lambdas.Lambda;
import ru.itmo.ctddev.sorokin.tt.lambdas.LambdaStructureKt;
import java.util.Collections;
}

let_expression returns[Structure<Lambda> ret]
    : LET VAR EQ def=let_expression IN expr=let_expression
        { $ret = LambdaStructureKt.let($VAR.text, $def.ret, $expr.ret); }
    | expression { $ret = $expression.ret; }
    ;

expression returns[Structure<Lambda> ret]
    : application abstraction  { $ret = LambdaStructureKt.application($application.ret, $abstraction.ret); }
    | application { $ret = $application.ret; }
    | abstraction { $ret = $abstraction.ret; }
    ;

abstraction returns[Structure<Lambda> ret] locals [List<String> variables]
    : LAMBDA
        { $variables = new ArrayList<>(1); }
    ( VAR
        { $variables.add($VAR.text); }
    )+
    DOT expression
        { $ret = $expression.ret; Collections.reverse($variables); for(String variable: $variables) { $ret = LambdaStructureKt.abstraction(variable, $ret); }}
    ;

application returns[Structure<Lambda> ret]
    : app=application atomic { $ret = LambdaStructureKt.application($app.ret, $atomic.ret); }
    | atomic { $ret = $atomic.ret; }
    ;

atomic returns[Structure<Lambda> ret]
    : OBR let_expression CBR { $ret = $let_expression.ret; }
    | VAR { $ret = LambdaStructureKt.variable($VAR.text); }
    ;

LET : 'let';
IN : 'in';
EQ : '=';
OBR : '(';
CBR : ')';
LAMBDA : '\\';
DOT : '.';
VAR :  [a-zA-Z] [a-zA-Z0-9\']*;
WS : (' ' | '\t' | '\n')+ -> skip;