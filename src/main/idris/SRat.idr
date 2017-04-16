module SRat

import Setoid
import SNat
import SInt

%default total
%access public export

data SRat : Type where
  MkRat : SInt -> SNat -> SRat

intToRat : SInt -> SRat
intToRat x = MkRat x One

natToRat : SNat -> SRat
natToRat = intToRat . natToInt

implementation Num SRat where

  -- (a - b)/x + (c - d)/y  ==  ((ya + xc) - (yb + xd))/xy
  (+) (MkRat (MkInt a b) x) (MkRat (MkInt c d) y) = MkRat (MkInt (y*a+x*c) (y*b+x*d)) (x*y)


  -- (a - b)/x * (c - d)/y  ==  ((ac + bd) - (ad + bc))/xy
  (*) (MkRat (MkInt a b) x) (MkRat (MkInt c d) y) = MkRat (MkInt (a*c+b*d) (a*d+b*c)) (x*y)

  fromInteger n = MkRat (fromInteger n) 1


implementation Neg SRat where

  negate (MkRat i n) = MkRat (negate i) n

  -- (a - b)/x - (c - d)/y  ==  ((ya + xd) - (yb + xc))/xy
  (-) (MkRat (MkInt a b) x) (MkRat (MkInt c d) y) = MkRat (MkInt (y*a+x*d) (y*b+x*c)) (x*y)

  abs (MkRat i n) = MkRat (abs i) n

implementation Show SRat where
  show (MkRat p q) = (show p) ++ "/" ++ (show q)

data SRatIsNZ: SRat -> Type where
  NZ : Not (x = y) -> SRatIsNZ $ MkRat (MkInt x y) q

data SRatIsZ: SRat -> Type where
  Z  : x = y -> SRatIsZ $ MkRat (MkInt x y) q

data SRatDecideZ: SRat -> Type where
  IsZ  : SRatIsZ  a -> SRatDecideZ a
  IsNZ : SRatIsNZ a -> SRatDecideZ a

numeratorIsNZ: SIntIsNZ p -> SRatIsNZ (MkRat p q)
numeratorIsNZ (NZ pr) = NZ pr

numeratorIsZ: SIntIsZ p -> SRatIsZ (MkRat p q)
numeratorIsZ (Z pr) = Z pr

-- (a-b)/x / (c-d)/y  = (a-b)y/x(c-d)
ratDiv: SRat -> (q:SRat) -> SRatIsNZ q -> SRat
ratDiv (MkRat (MkInt a b) x) (MkRat (MkInt c d) y) (NZ nzPr) with (sign (MkInt c d))
  | IsZero pr      = void (nzPr pr)
  | LessZero pr    = MkRat (MkInt (b*y) (a*y)) (x * (toNatAbs (MkInt c d) (NZ nzPr)))
  | GreaterZero pr = MkRat (MkInt (a*y) (b*y)) (x * (toNatAbs (MkInt c d) (NZ nzPr)))


isZero: (r:SRat) -> SRatDecideZ r
isZero (MkRat p q) with (isZero p)
  | (IsZ pr)  = IsZ  $ numeratorIsZ  pr
  | (IsNZ pr) = IsNZ $ numeratorIsNZ pr

div: SRat -> SRat -> Maybe SRat
div p q with (isZero q)
  | (IsNZ pr) = Just $ ratDiv p q pr
  | _         = Nothing

data SRatEq : Relation SRat where
  EQ : SIntEq (p1 * (natToInt q2)) (p2 * (natToInt q1)) -> SRatEq (MkRat p1 q1) (MkRat p2 q2)

SRatEqIsReflective: Reflective SRatEq
SRatEqIsReflective (MkRat p q) = EQ $ refl

SRatEqIsSymmetric: Symmetric SRatEq
SRatEqIsSymmetric (MkRat p1 q1) (MkRat p2 q2) (EQ eq) = EQ $ sym eq

SRatEqIsTransitive: Transitive SRatEq
SRatEqIsTransitive (MkRat p1 q1) (MkRat p2 q2) (MkRat p3 q3) (EQ eq1) (EQ eq2) with (isZero p2)
  | IsNZ p2IsNZ = EQ $ res
    where
    nq1: SInt
    nq1 = natToInt q1
    nq2: SInt
    nq2 = natToInt q2
    nq3: SInt
    nq3 = natToInt q3

    prod : SIntEq (p1 * nq2 * (p2 * nq3)) (p2 * nq1 * (p3 * nq2))
    elQ1 : SIntEq (p2 * nq3 * (p1 * nq2)) (p2 * nq1 * (p3 * nq2))
    elQ2 : SIntEq (p2 * nq3 * p1 * nq2)   (p2 * nq1 * (p3 * nq2))
    elQ3 : SIntEq (p2 * nq3 * p1 * nq2)   (p2 * nq1 * p3 * nq2)

    elQ  : SIntEq (p2 * nq3 * p1)   (p2 * nq1 * p3)
    elP1 : SIntEq (p2 * (nq3 * p1)) (p2 * nq1 * p3)
    elP2 : SIntEq (p2 * (nq3 * p1)) (p2 * (nq1 * p3))
    elP3 : SIntEq (nq3 * p1 * p2)   (nq1 * p3 * p2)

    elP  : SIntEq (nq3 * p1) (nq1 * p3)
    res  : SIntEq (p1 * nq3) (p3 * nq1)

    prod = equalMultEqual eq1 eq2
    elQ1 = trans (intMultCommutative (p2 * nq3) (p1 * nq2)) prod
    elQ2 = trans (sym $ intMultAssociative (p2 * nq3) p1 nq2) elQ1
    elQ3 = trans elQ2 (intMultAssociative (p2 * nq1) p3 nq2)
    elQ  = intMultRightCancel (p2 * nq3 * p1) (p2 * nq1 * p3) nq2 positiveIsNZ elQ3
    elP1 = trans (intMultAssociative p2 nq3 p1) elQ
    elP2 = trans elP1 (sym $ intMultAssociative p2 nq1 p3)
    elP3 = trans (trans (intMultCommutative (nq3 * p1) p2) elP2) (intMultCommutative p2 (nq1 * p3))
    elP  = intMultRightCancel (nq3 * p1) (nq1 * p3) p2 p2IsNZ elP3
    res  = trans (trans (intMultCommutative p1 nq3) elP) (intMultCommutative nq1 p3)
  SRatEqIsTransitive (MkRat p1 q1) (MkRat (MkInt k k) q2) (MkRat p3 q3) (EQ eq1) (EQ eq2) |  IsZ (Z $ Refl) = EQ $ zeroEqualsZero leftIsZ rightIsZ
    where
    leftIsZ: SIntIsZ $ p1 * (natToInt q3)
    leftIsZ = multZeroIsZero $ multZeroRightNZ (equalToZIsZ zeroIsZ $ sym eq1) positiveIsNZ

    rightIsZ: SIntIsZ $ p3 * (natToInt q1)
    rightIsZ = multZeroIsZero $ multZeroRightNZ (equalToZIsZ zeroIsZ eq2) positiveIsNZ
