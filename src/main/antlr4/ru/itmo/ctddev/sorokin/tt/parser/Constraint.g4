grammar Constraint;

@header{
import ru.itmo.ctddev.sorokin.tt.common.Structure;
import ru.itmo.ctddev.sorokin.tt.constraints.Constraint;
import ru.itmo.ctddev.sorokin.tt.constraints.ConstraintStructureKt;
import ru.itmo.ctddev.sorokin.tt.constraints.TypeInstance;
import ru.itmo.ctddev.sorokin.tt.constraints.TypeScheme;
}

constraint returns[Structure<Constraint> ret]
    : /*conjunction*/ OBR l=constraint AND r=constraint CBR
        { $ret = ConstraintStructureKt.conjunction($l.ret, $r.ret); }
    | /*existence*/ OBR EXIST TVAR DOT c=constraint CBR
        { $ret = ConstraintStructureKt.existence($TVAR.text, $c.ret); }
    | /*substitute*/ VAR SUBST type
        { $ret = ConstraintStructureKt.substitution($VAR.text, $type.ret); }
    | /*inference*/ lt=type EQ rt=type
        { $ret = ConstraintStructureKt.inference($lt.ret, $rt.ret); }
    | /*defining*/ OBR DEF VAR COLON ts=type_scheme IN c=constraint
        { $ret = ConstraintStructureKt.defining($VAR.text, $ts.ret, $c.ret); }
    ;

type returns[Structure<TypeInstance> ret]
    : l=atom_type TO r=type
        { $ret = ConstraintStructureKt.typeApplication($l.ret, $r.ret); }
    | atom_type
        { $ret = $atom_type.ret; }
    ;

atom_type returns[Structure<TypeInstance> ret]
    : OBR type CBR  { $ret = $type.ret; }
    | TVAR          { $ret = ConstraintStructureKt.typeSimple($TVAR.text); }
    ;

type_scheme returns[Structure<TypeScheme> ret] locals [List<String> tVarList]
    : type
        { $ret = ConstraintStructureKt.typeSchemeMono($type.ret); }
    | FRALL
        { $tVarList = new ArrayList<>(1); }
    ( TVAR
        { $tVarList.add($TVAR.text); }
    )+ OSBR c=constraint CSBR DOT type
        { $ret = ConstraintStructureKt.typeScheme($tVarList, $c.ret, $type.ret); }
    ;

OBR   : '(';
CBR   : ')';
OSBR  : '[';
CSBR  : ']';
AND   : '^';
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