module Setoid

%access public export

Relation : Type -> Type
Relation A = A -> A -> Type

RelationProperty : Type -> Type
RelationProperty A = Relation A -> Type

Reflective : {A: Type} -> RelationProperty A
Reflective {A} R = (x : A) -> R x x

Symmetric : {A: Type} -> RelationProperty A
Symmetric {A} R = (x: A) -> (y: A) -> R x y -> R y x

Transitive : {A: Type} -> RelationProperty A
Transitive {A} R = (x: A) -> (y: A) -> (z: A) -> R x y -> R y z -> R x z

ReflSafe : {X:Type} -> {Y:Type} -> (f : X -> Y) -> Type
ReflSafe {X} f = (a:X) -> (b:X) -> (f a = f b) -> (a = b)

data Equality : {A: Type} -> RelationProperty A where
  MkEquality : {A: Type} -> {R: Relation A} -> Reflective R -> Symmetric R -> Transitive R -> Equality R

record Setoid where
  constructor MkSetoid
  C: Type
  CEq: Relation C
  CEqProof: Equality CEq

prRefl : Reflective (=)
prRefl _ = Refl

prSym : Symmetric (=)
prSym _ _ = sym

prTrans : Transitive (=)
prTrans _ _ _ = trans

IntensionalSetoid : Type -> Setoid
IntensionalSetoid t = MkSetoid t (=) $ MkEquality prRefl prSym prTrans
