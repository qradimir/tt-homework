grammar Constraint;

@header{
import ru.itmo.ctddev.sorokin.tt.common.Structure;
import ru.itmo.ctddev.sorokin.tt.constraints.Constraint;
import ru.itmo.ctddev.sorokin.tt.constraints.ConstraintFactoryKt;
import ru.itmo.ctddev.sorokin.tt.constraints.TypeInstance;
import ru.itmo.ctddev.sorokin.tt.constraints.TypeScheme;
}

constraint returns[Structure<Constraint> ret]
    : /*conjunction*/ OBR l=constraint AND r=constraint CBR
        { $ret = ConstraintFactoryKt.conjunction($l.ret, $r.ret); }
    | /*existence*/ OBR EXIST TVAR DOT c=constraint CBR
        { $ret = ConstraintFactoryKt.existence($TVAR.text, $c.ret); }
    | /*substitute*/ VAR SUBST type
        { $ret = ConstraintFactoryKt.substitution($VAR.text, $type.ret); }
    | /*inference*/ lt=type EQ rt=type
        { $ret = ConstraintFactoryKt.inference($lt.ret, $rt.ret); }
    | /*defining*/ OBR DEF VAR COLON ts=type_scheme IN c=constraint
        { $ret = ConstraintFactoryKt.defining($VAR.text, $ts.ret, $c.ret); }
    ;

type returns[Structure<TypeInstance> ret]
    : l=atom_type TO r=type
        { $ret = ConstraintFactoryKt.typeApplication($l.ret, $r.ret); }
    ;

atom_type returns[Structure<TypeInstance> ret]
    : OBR type CBR  { $ret = $type.ret; }
    | TVAR          { $ret = ConstraintFactoryKt.typeSimple($TVAR.text); }
    ;

type_scheme returns[Structure<TypeScheme> ret] locals [List<String> tVarList]
    : type
        { $ret = ConstraintFactoryKt.typeSchemeMono($type.ret); }
    | FRALL
        { $tVarList = new ArrayList<>(1); }
    ( TVAR
        { $tVarList.add($TVAR.text); }
    )+ OSBR c=constraint CSBR DOT type
        { $ret = ConstraintFactoryKt.typeScheme($tVarList, $c.ret, $type.ret); }
    ;

OBR   : '(';
CBR   : ')';
OSBR  : '[';
CSBR  : ']';
AND   : '&';
DEF   : 'def';
IN    : 'in';
TO    : '->';
FRALL : '@';
COLON : ':';
DOT   : '.';
EXIST : '?';
EQ    : '=';
SUBST : '<';
VAR   :  [a-z] [a-z0-9\']*;
TVAR  : '\'' VAR;
WS    : (' ' | '\t')+ -> skip;