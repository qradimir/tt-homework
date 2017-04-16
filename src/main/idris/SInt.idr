module SInt

import Setoid
import SNat

%default total

%access public export

data SInt: Type where
  -- MkInt x y  means (x - y)
  MkInt : SNat -> SNat -> SInt

natToInt: SNat -> SInt
natToInt x = MkInt (S x) One

plus: SInt -> SInt -> SInt
plus (MkInt a b) (MkInt c d) = MkInt (a + c) (b + d)

neg: SInt -> SInt
neg (MkInt a b) = MkInt b a

minus: SInt -> SInt -> SInt
minus (MkInt a b) (MkInt c d) = MkInt (a + d) (b + c)

multToNat: SInt -> SNat -> SInt
multToNat (MkInt a b) n = MkInt (a * n) (b * n)

mult: SInt -> SInt -> SInt
mult (MkInt a b) (MkInt c d) =  MkInt (a * c + b * d) (b * c + a * d)

intFromInteger : Integer -> SInt
intFromInteger n  = if n > 0 then
                      MkInt (S $ fromInteger n) One
                    else if n < 0 then
                      MkInt One (S $ fromInteger (-n))
                    else
                      MkInt One One

intToInteger: SInt -> Integer
intToInteger (MkInt One One) = 0
intToInteger (MkInt One (S x)) = - (toInteger x)
intToInteger (MkInt (S x) One) = toInteger x
intToInteger (MkInt (S x) (S y)) = intToInteger (assert_smaller (MkInt (S x) (S y)) (MkInt x y))

implementation Num SInt where
  (+) = plus
  (*) = mult

  fromInteger = intFromInteger

implementation Neg SInt where
  negate = neg
  (-)    = minus

  abs n@(MkInt a b) = if (b > a) then MkInt b a else n

data SIntEq : Relation SInt where
  -- a + d = c + b  => a - b = c - d
  EQ : (a + d = c + b) -> SIntEq (MkInt a b) (MkInt c d)

eqProof: SIntEq (MkInt a b) (MkInt c d) -> (a + d = c + b)
eqProof (EQ eq) = eq

SIntEqIsReflective : Reflective SIntEq
SIntEqIsReflective (MkInt a b) = EQ $ Refl{x = a + b}

SIntEqIsSymmetric : Symmetric SIntEq
SIntEqIsSymmetric _ _ (EQ eq) = EQ $ sym eq

SIntEqIsTransitve : Transitive SIntEq
SIntEqIsTransitve (MkInt a b) (MkInt c d) (MkInt e f) (EQ eq1) (EQ eq2) = EQ $ res where

  sum  : (a + d) + (c + f) = (c + b) + (e + d)
  elD1 : (c + f) + (a + d) = (c + b) + (e + d)
  elD2 : ((c + f) + a) + d = (c + b) + (e + d)
  elD3 : ((c + f) + a) + d = ((c + b) + e) + d

  elD  : (c + f) + a = (c + b) + e
  elC1 : c + (f + a) = (c + b) + e
  elC2 : c + (f + a) = c + (b + e)

  elC : f + a = b + e
  res : a + f = e + b

  sum  = reflPlusRefl eq1 eq2
  elD1 = trans (plusCommutative (c + f) (a + d)) sum
  elD2 = trans (sym $ plusAssociative (c + f) a d) elD1
  elD3 = trans elD2 (plusAssociative (c + b) e d)
  elD  = plusRightCancel d elD3
  elC1 = trans (plusAssociative c f a) elD
  elC2 = trans elC1 (sym $ plusAssociative c b e)
  elC  = plusLeftCancel c elC2
  res  = trans (trans (plusCommutative a f) elC) (plusCommutative b e)

refl: (SIntEq a a)
refl = SIntEqIsReflective _

sym: (SIntEq a b) -> (SIntEq b a)
sym eq = SIntEqIsSymmetric _ _ eq

trans: (SIntEq a b) -> (SIntEq b c) -> (SIntEq a c)
trans eq1 eq2 = SIntEqIsTransitve _ _ _ eq1 eq2

SIntSetoid : Setoid
SIntSetoid = MkSetoid SInt SIntEq $ MkEquality SIntEqIsReflective SIntEqIsSymmetric SIntEqIsTransitve

data SIntIsNZ : SInt -> Type where
  NZ : Not (a = b) -> SIntIsNZ $ MkInt a b

data SIntIsZ : SInt -> Type where
  Z  : a = b -> SIntIsZ $ MkInt a b

data SIntDecideZ: SInt -> Type where
  IsZ  : SIntIsZ  a -> SIntDecideZ a
  IsNZ : SIntIsNZ a -> SIntDecideZ a

negativeisNZ : SIntIsNZ $ MkInt One (S x)
negativeisNZ = NZ uninhabited

positiveIsNZ : SIntIsNZ $ MkInt (S x) One
positiveIsNZ = NZ (uninhabited . sym)

zeroIsZ : SIntIsZ $ MkInt x x
zeroIsZ = Z Refl


isZero : (x:SInt) -> SIntDecideZ x
isZero (MkInt One One)         = IsZ  $ zeroIsZ
isZero (MkInt One (S x))       = IsNZ $ negativeisNZ
isZero (MkInt (S x) One)       = IsNZ $ positiveIsNZ
isZero (MkInt (S x) (S y))  with (isZero (assert_smaller (MkInt (S x) (S y)) (MkInt x y)))
              | IsNZ (NZ pr) = IsNZ $ NZ (pr . succInjective)
              | IsZ  (Z  pr) = IsZ  $ Z  (cong pr)

toNatAbs : (i:SInt) -> SIntIsNZ i -> SNat
toNatAbs (MkInt One   One  ) (NZ aProof) = void (aProof Refl)
toNatAbs (MkInt One   (S x)) _           = x
toNatAbs (MkInt (S x) One  ) _           = x
toNatAbs (MkInt (S x) (S y)) (NZ aProof) = toNatAbs (assert_smaller (MkInt (S x) (S y)) (MkInt x y)) (NZ $ aProof . cong)

data Sign: SInt -> Type where
  LessZero    : {a: SNat} -> {b: SNat} -> {x:SNat} -> (pr: a + x = b) -> Sign $ MkInt a b
  GreaterZero : {a: SNat} -> {b: SNat} -> {x:SNat} -> (pr: a = b + x) -> Sign $ MkInt a b
  IsZero      : {a: SNat} -> {b: SNat}             -> (pr: a = b)     -> Sign $ MkInt a b

sign: (x: SInt) -> Sign x
sign (MkInt One One)        = IsZero Refl
sign (MkInt One (S t))      = LessZero $ Refl
sign (MkInt (S t) One)      = GreaterZero $ Refl
sign (MkInt (S x) (S y)) with (sign (assert_smaller (MkInt (S x) (S y)) (MkInt x y)))
         | (LessZero pr)    = LessZero $ cong pr
         | (GreaterZero pr) = GreaterZero $ cong pr
         | (IsZero pr)      = IsZero $ cong pr

implementation Show SInt where
  show x = show $ intToInteger x

-- -- proofs

lemma_0left : (a,b,c:SNat) -> (a + a * b) + c = a * (S b) + c * One
lemma_0left a b c =
  rewrite multRightSuccPlus a b in
  rewrite multOneRightNeutral c in
          Refl

lemma_0right : (a,b,c:SNat) -> a + (b + b * c) = a * One + b * (S c)
lemma_0right a b c =
  rewrite multOneRightNeutral a in
  rewrite multRightSuccPlus b c in
          Refl

lemma_0both : (a,b,x,y:SNat) -> a + b + (a * x + b * y) = a * (S x) + b * (S y)
lemma_0both a b x y =
  rewrite sym $ plusAssociative a b (a * x + b * y) in
  rewrite plusSwapingLeft b (a * x) (b * y) in
  rewrite sym $ multRightSuccPlus b y in
  rewrite plusAssociative a (a * x) (b * (S y)) in
  rewrite sym $ multRightSuccPlus a x in
          Refl

equalPlusEqual : {l1:SInt} -> {r1:SInt} -> {l2:SInt} -> {r2:SInt} -> SIntEq l1 r1 -> SIntEq l2 r2 -> SIntEq (l1+l2) (r1+r2)
equalPlusEqual {l1=MkInt a1 b1} {r1=MkInt c1 d1} {l2=MkInt a2 b2} {r2=MkInt c2 d2} (EQ eq1) (EQ eq2) = EQ $
  rewrite plusRegrouping a1 a2 d1 d2 in
  rewrite plusRegrouping c1 c2 b1 b2 in
  rewrite eq1 in
  rewrite eq2 in
          Refl

equalMultEqualLemma: {a,b,c,d:SNat} -> (x,y:SNat) -> (a + d) = (b + c) -> a + (b + x * b) + ((y + x) * c + y * d) = y * c + (y + x) * d + (a + x * a + b)
equalMultEqualLemma x y {a=a} {b=b} {c=c} {d=d} eq =
  rewrite multDistributesOverPlusLeft y x d in
  rewrite multDistributesOverPlusLeft y x c in                          -- all mult over plus expanded
  rewrite plusCommutative (y * c + (y * d + x * d)) (a + x * a + b) in
  rewrite plusAssociative (y * c) (y * d) (x * d) in
  rewrite plusCommutative (y * c + y * d) (x * d) in
  rewrite plusAssociative (a + x * a + b) (x * d) (y * c + y * d) in    -- right refl side: y*c + y*d in right anchor
  rewrite plusSwapingRight (y * c) (x * c) (y * d) in
  rewrite plusCommutative (y * c + y * d) (x * c) in
  rewrite plusAssociative (a + (b + x * b)) (x * c) (y * c + y * d) in  -- left refl side: y*c + y*d in right anchor
  rewrite plusSwapingRight a (x * a) b in
  rewrite sym $ plusAssociative (a + b) (x * a) (x * d) in              -- right refl side : a + b in left anchor
  rewrite plusAssociative a b (x * b) in
  rewrite sym $ plusAssociative (a + b) (x * b) (x * c) in              -- left refl side : a + b in left anchor
  rewrite sym $ multDistributesOverPlusRight x a d in
  rewrite sym $ multDistributesOverPlusRight x b c in
  rewrite eq in
          Refl

equalMultEqual : {l1:SInt} -> {r1:SInt} -> {l2:SInt} -> {r2:SInt} -> SIntEq l1 r1 -> SIntEq l2 r2 -> SIntEq (l1*l2) (r1*r2)
equalMultEqual {l1=MkInt One One} {r1=MkInt c1 d1} {l2=MkInt a2 b2} {r2=MkInt c2 d2} (EQ eq1) (EQ eq2) = EQ $
    rewrite cdEQ in
    rewrite plusCommutative (a2 + b2) (c1 * c2 + c1 * d2) in
            Refl
  where
    cdEQ : (d1 = c1)
    cdEQ = succInjective $ rewrite eq1 in rewrite plusOneRightSucc c1 in Refl
equalMultEqual {l1=MkInt (S x) One} {r1=MkInt c1 d1} {l2=MkInt a2 b2} {r2=MkInt c2 d2} (EQ eq1) (EQ eq2) = EQ $
    rewrite cdEQ in
            plusReverse $ equalMultEqualLemma x d1 (trans eq2 (plusCommutative c2 b2))
  where
    cdEQ : (c1 = d1 + x)
    cdEQ = succInjective $ rewrite plusCommutative d1 x in rewrite eq1 in rewrite plusOneRightSucc c1 in Refl
equalMultEqual {l1=MkInt One (S x)} {r1=MkInt c1 d1} {l2=MkInt a2 b2} {r2=MkInt c2 d2} (EQ eq1) (EQ eq2) = EQ $
    rewrite cdEQ in
    equalMultEqualLemma x c1 (trans eq2 (plusCommutative c2 b2))
  where
    cdEQ : (d1 = c1 + x)
    cdEQ = succInjective $ rewrite eq1 in rewrite plusSuccRightSucc c1 x in Refl
equalMultEqual {l1=MkInt (S x) (S y)} {r1=MkInt c1 d1} {l2=MkInt a2 b2} {r2=MkInt c2 d2} (EQ eq1) (EQ eq2) = EQ $
    let hyp = eqProof $ equalMultEqual {l1=assert_smaller (MkInt (S x) (S y)) (MkInt x y)} {r1=MkInt c1 d1} {l2 = MkInt a2 b2} {r2=MkInt c2 d2} (EQ cdEQ) (EQ eq2) in
    rewrite plusAssociative (a2 + x * a2) b2 (y * b2) in
    rewrite plusSwapingRight a2 (x * a2) b2 in
    rewrite sym $ plusAssociative (a2 + b2) (x * a2) (y * b2) in
    rewrite sym $ plusAssociative (a2 + b2) (x * a2 + y * b2) (d1 * c2 + c1 * d2) in
    rewrite sym $ plusAssociative a2 (y * a2) (b2 + x * b2) in
    rewrite plusSwapingLeft (y * a2) b2 (x * b2) in
    rewrite plusAssociative a2 b2 (y * a2 + x * b2) in
    rewrite plusSwapingLeft (c1 * c2 + d1 * d2) (a2 + b2) (y * a2 + x * b2) in
    rewrite hyp in
            Refl
  where
    cdEQ : (x + d1 = c1 + y)
    cdEQ = succInjective $ rewrite eq1 in rewrite plusSuccRightSucc c1 y in Refl

intMultCommutative: (l: SInt) -> (r: SInt) -> SIntEq (l * r) (r * l)
intMultCommutative (MkInt a b) (MkInt c d) = EQ $
  rewrite multCommutative c a in
  rewrite multCommutative d b in
  rewrite multCommutative d a in
  rewrite multCommutative c b in
  rewrite plusCommutative (a * d) (b * c) in
          Refl

intMultAssociative: (l: SInt) -> (c: SInt) -> (r: SInt) -> SIntEq (l * (c * r)) ((l * c) * r)
intMultAssociative (MkInt a b) (MkInt c d) (MkInt e f) = EQ $
    rewrite expanding1 a b in
    rewrite expanding1 b a in
    rewrite expanding2 a b in
    rewrite expanding2 b a in
    rewrite regroping a b in
    rewrite regroping b a in
    rewrite parentheses a b in
    rewrite parentheses b a in
            Refl
  where
    expanding1: (x,y : SNat) -> (x * (c * e + d * f) + y * (d * e + c * f)) = (x * (c * e) + x * (d * f) + (y * (d * e) + y * (c * f)))
    expanding1 x y =
      rewrite multDistributesOverPlusRight x (c * e) (d * f) in
      rewrite multDistributesOverPlusRight y (d * e) (c * f) in
              Refl
    expanding2: (x,y : SNat) -> ((y * c + x * d) * e + (x * c + y * d) * f) = (y * c * e + x * d * e + (x * c * f + y * d * f))
    expanding2 x y =
      rewrite multDistributesOverPlusLeft (y * c) (x * d) e in
      rewrite multDistributesOverPlusLeft (x * c) (y * d) f in
              Refl
    regroping: (x,y : SNat) -> (x * c * e + y * d * e + (y * c * f + x * d * f)) = (x * c * e +  x * d * f + (y * d * e + y * c * f))
    regroping x y =
      rewrite plusCommutative (y * c * f) (x * d * f) in
      rewrite plusAssociative (x * c * e + y * d * e) (x * d * f) (y * c * f) in
      rewrite plusCommutative (x * c * e) (y * d * e) in
      rewrite sym $ plusAssociative (y * d * e) (x * c * e) (x * d * f) in
      rewrite plusCommutative (y * d * e) (x * c * e + x * d * f) in
      rewrite sym $ plusAssociative (x * c * e + x * d * f) (y * d * e) (y * c * f) in
              Refl
    parentheses: (x,y : SNat) -> (x * (c * e) + x * (d * f) + (y * (d * e) + y * (c * f))) = (x * c * e + x * d * f + (y * d * e + y * c * f))
    parentheses x y =
      rewrite multAssociative x c e in
      rewrite multAssociative x d f in
      rewrite multAssociative y d e in
      rewrite multAssociative y c f in
              Refl

intMultRightCancel: (l1,l2,r:SInt) -> SIntIsNZ r -> SIntEq (l1 * r) (l2 * r) -> SIntEq l1 l2
intMultRightCancel (MkInt a b) (MkInt c d) (MkInt One One) (NZ nz) _ = void $ nz Refl
intMultRightCancel (MkInt a b) (MkInt c d) (MkInt (S x) One) _ (EQ eq) = EQ $
  multRightCancel (a + d) (c + b) x $
  rewrite multDistributesOverPlusLeft a d x in
  rewrite multDistributesOverPlusLeft c b x in
  plusLeftCancel (c + d + (b + a)) $
  trans (plusReverse4Left c d b a (a * x + d * x)) $
  rewrite regrop_lemma a b c d x in
  rewrite regrop_lemma c d a b x in
  rewrite lemma_0left a x b in
  rewrite lemma_0left d x c in
  rewrite lemma_0left c x d in
  rewrite lemma_0left b x a in
          eq
  where
    regrop_lemma : (a,b,c,d,x:SNat) ->  a + b + (d + c) + (a * x + d * x) = a + a * x + b + (d + d * x + c)
    regrop_lemma a b c d x =
      rewrite plusSwapingRight a (a * x) b in
      rewrite plusSwapingRight d (d * x) c in
      rewrite plusAssociative (a + b + a * x) (d + c) (d * x) in
      rewrite plusSwapingRight (a + b) (a * x) (d + c) in
      rewrite sym $ plusAssociative (a + b + (d + c)) (a * x) (d * x) in
              Refl
intMultRightCancel (MkInt a b) (MkInt c d) (MkInt One (S x)) _ (EQ eq) = EQ $
  plusReverse $
  multRightCancel (b + c) (d + a) x $
  rewrite multDistributesOverPlusLeft b c x in
  rewrite multDistributesOverPlusLeft d a x in
  plusLeftCancel (c + d + (b + a)) $
  trans (plusReverse4Left c d b a (b * x + c * x)) $
  rewrite regrop_lemma a b c d x in
  rewrite regrop_lemma c d a b x in
  rewrite lemma_0right a b x in
  rewrite lemma_0right d c x in
  rewrite lemma_0right c d x in
  rewrite lemma_0right b a x in
          eq
  where
    regrop_lemma : (a,b,c,d,x:SNat) ->  a + b + (d + c) + (b * x + c * x) = a + (b + b * x) + (d + (c + c * x))
    regrop_lemma a b c d x =
      rewrite plusAssociative a b (b * x) in
      rewrite plusAssociative d c (c * x) in
      rewrite plusAssociative (a + b + b * x) (d + c) (c * x) in
      rewrite plusSwapingRight (a + b) (b * x) (d + c) in
      rewrite sym $ plusAssociative (a + b + (d + c)) (b * x) (c * x) in
              Refl
intMultRightCancel l1@(MkInt a b) l2@(MkInt c d) (MkInt (S x) (S y)) (NZ nz) (EQ eq) =
  intMultRightCancel l1 l2 (assert_smaller (MkInt (S x) (S y))(MkInt x y)) (NZ $ nz . cong) (EQ $
    plusLeftCancel (a + b + (d + c)) $
    rewrite plusReverse4Left a b d c (c * x + d * y + (b * x + a * y)) in
    trans (regrop a b c d x y) $
    trans eq (sym $ regrop c d a b x y)
    )
    where
      regrop: (a,b,c,d,x,y: SNat) -> a + b + (d + c) + (a * x + b * y + (d * x + c * y)) = a * (S x) + b * (S y) + (d * (S x) + c * (S y))
      regrop a b c d x y =
        rewrite sym $ plusAssociative (a + b) (d + c) (a * x + b * y + (d * x + c * y)) in
        rewrite plusSwapingLeft (d + c) (a * x + b * y) (d * x + c * y) in
        rewrite lemma_0both d c x y in
        rewrite plusAssociative (a + b) (a * x + b * y) (d * (S x) + c * (S y)) in
        rewrite lemma_0both a b x y in
                Refl

equalToZIsZ: SIntIsZ a -> SIntEq a b -> SIntIsZ b
equalToZIsZ {a=MkInt x x} {b=MkInt c d} (Z $ Refl) (EQ eqPr) = Z $
  sym $
  plusLeftCancel x $
  trans eqPr (plusCommutative c x)

zeroEqualsZero: SIntIsZ a -> SIntIsZ b -> SIntEq a b
zeroEqualsZero {a=MkInt x x} {b=MkInt y y} (Z $ Refl) (Z $ Refl) = EQ $ trans (plusCommutative x y) Refl

multZeroIsZero: SIntIsZ a -> SIntIsZ $ a * b
multZeroIsZero {b=MkInt a b} (Z $ Refl) = Z $ Refl

multZeroRightNZ: SIntIsZ $ l * r -> SIntIsNZ r -> SIntIsZ l
multZeroRightNZ {l=MkInt a b} {r=MkInt One One}     (Z $ multZ) (NZ $ rightNZ) = void $ rightNZ Refl
multZeroRightNZ {l=MkInt a b} {r=MkInt One (S x)}   (Z $ multZ) (NZ $ rightNZ) = Z $
  sym $
  multRightCancel b a x $
  plusLeftCancel (a + b) $
  trans (sym $ plusAssociative a b (b * x)) $
  trans (lemma_0right a b x) $
  rewrite plusCommutative a b in
  sym $
  trans (sym $ plusAssociative b a (a * x)) $
  trans (lemma_0right b a x) $
  sym $
  multZ
multZeroRightNZ {l=MkInt a b} {r=MkInt (S x) One}   (Z $ multZ) (NZ $ rightNZ) = Z $
  multRightCancel a b x $
  plusLeftCancel a $
  plusRightCancel b $
  trans (lemma_0left a x b) $
  sym $
  trans (plusCommutative (a + b * x) b) $
  trans (plusSwapingLeft b a (b * x)) $
  trans (plusCommutative a (b + b * x)) $
  trans (lemma_0left b x a) $
  sym $
  multZ
multZeroRightNZ {l=MkInt a b} {r=MkInt (S x) (S y)} (Z $ multZ) (NZ $ rightNZ) =
  multZeroRightNZ {l=MkInt a b} {r=assert_smaller (MkInt (S x) (S y)) (MkInt x y)} (Z $ hyp) (NZ $ rightNZ . cong)
    where
      hyp : a * x + b * y = b * x + a * y
      hyp =
        plusLeftCancel (a + b) $
        trans (lemma_0both a b x y) $
        sym $
        rewrite plusCommutative a b in
        trans (lemma_0both b a x y) $
        sym $
        multZ
