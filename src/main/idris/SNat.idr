module SNat

import Setoid

%default total
%access public export


-- natural numbers strating from 1
data SNat: Type where
  -- 1
  One: SNat
  -- successor
  S: SNat -> SNat


plus: SNat -> SNat -> SNat
plus One   y = S y
plus (S x) y = S $ plus x y

mult: SNat -> SNat -> SNat
mult One y   = y
mult (S x) y = plus y $ mult x  y


implementation Num SNat where
  (+) = plus
  (*) = mult

  fromInteger 1 = One
  fromInteger n =
    if (n > 1) then
      S (fromInteger (assert_smaller n (n - 1)))
    else
      One

toInteger: SNat -> Integer
toInteger One = 1
toInteger (S x) = (toInteger (assert_smaller (S x) x)) + 1

implementation Eq SNat where
  (==) One   One   = True
  (==) (S l) (S r) = l == r
  (==) _     _     = False

implementation Ord SNat where
  compare One   One   = EQ
  compare One   _     = LT
  compare _     One   = GT
  compare (S x) (S y) = compare x y

implementation MinBound SNat where
  minBound = One

implementation Show SNat where
  show x = show $ toInteger x

implementation Uninhabited (One = S n) where
  uninhabited Refl impossible

SNatSetoid : Setoid
SNatSetoid = IntensionalSetoid SNat

-- proofs

reflPlusRefl : {a:SNat} -> {b:SNat} -> {c:SNat} -> {d:SNat} ->
               (a = b) -> (c = d) -> (a + c = b + d)
reflPlusRefl eq1 eq2 = rewrite eq1 in rewrite eq2 in Refl

succInjective: {l:SNat} -> (S l = S r) -> (l = r)
succInjective Refl = Refl

plusLeftCancel : (l:SNat) -> (l + r1) = (l + r2) -> r1 = r2
plusLeftCancel One   eq = succInjective eq
plusLeftCancel (S l) eq = plusLeftCancel l (succInjective eq)

plusOneRightSucc : (n : SNat) -> n + One = S n
plusOneRightSucc One   = Refl
plusOneRightSucc (S n) = rewrite (plusOneRightSucc n) in Refl

plusSuccRightSucc : (l : SNat) -> (r : SNat) -> S (l + r) = l + (S r)
plusSuccRightSucc One   r = Refl
plusSuccRightSucc (S l) r = rewrite (plusSuccRightSucc l r) in Refl

plusCommutative : (l : SNat) -> (r : SNat) -> l + r = r + l
plusCommutative One   r = rewrite (plusOneRightSucc r) in Refl
plusCommutative (S l) r = rewrite (plusCommutative l r) in rewrite (plusSuccRightSucc r l) in Refl

plusAssociative : (l : SNat) -> (c : SNat) -> (r : SNat) -> l + (c + r) = (l + c) + r
plusAssociative One   c r = Refl
plusAssociative (S l) c r = rewrite (plusAssociative l c r) in Refl

plusRegrouping : (a : SNat) -> (b: SNat) -> (c: SNat) -> (d: SNat) -> (a + b) + (c + d) = (a + c) + (b + d)
plusRegrouping a b c d =
  rewrite sym $ plusAssociative a b (c + d) in
  rewrite plusAssociative b c d in
  rewrite plusCommutative b c in
  rewrite sym $ plusAssociative c b d in
  rewrite plusAssociative a c (b + d) in
          Refl

plusXIsNotEqualToX : (x: SNat) -> (x = x + y) -> Void
plusXIsNotEqualToX One   = uninhabited
plusXIsNotEqualToX (S x) = (plusXIsNotEqualToX x) . succInjective

plusRotating: (l : SNat) -> (c: SNat) -> (r: SNat) -> l + (c + r) = r + (l + c)
plusRotating l c r = trans (plusAssociative l c r) (plusCommutative (l + c) r)

plusSwapingLeft: (l : SNat) -> (c: SNat) -> (r: SNat) -> l + (c + r) = c + (l + r)
plusSwapingLeft l c r = trans (plusAssociative l c r) (trans (rewrite plusCommutative l c in Refl) (sym $ plusAssociative c l r))

plusSwapingRight: (l : SNat) -> (c: SNat) -> (r: SNat) -> (l + c) + r = (l + r) + c
plusSwapingRight l c r = trans (sym $ plusAssociative l c r) (trans (cong $ plusCommutative c r) (plusAssociative l r c))

plusReverse: {a,b,c,d: SNat} -> (a + b = c + d) -> (d + c = b + a)
plusReverse {a} {b} {c} {d} eq = sym $ trans (plusCommutative b a) (trans eq (plusCommutative c d))

plusReverse4Left: (a,b,c,d,t:SNat) -> a + b + (c + d) + t = d + c + (b + a) + t
plusReverse4Left a b c d t =
  rewrite plusCommutative b a in
  rewrite plusCommutative d c in
  rewrite plusCommutative (c + d) (a + b) in
          Refl

plusRightCancel : (r:SNat) -> (l1 + r) = (l2 + r) -> l1 = l2
plusRightCancel {l1} {l2} r eq = plusLeftCancel r $ trans (trans (plusCommutative r l1) eq) (plusCommutative l2 r)

multOneRightNeutral : (n:SNat) -> (n * One = n)
multOneRightNeutral One = Refl
multOneRightNeutral (S n) = rewrite (multOneRightNeutral n) in Refl

multLeftConstant : {l:SNat} -> {r:SNat} -> (n:SNat) -> (l = r) -> (n * l) = (n * r)
multLeftConstant n eq = cong eq

multRightConstant : {l:SNat} -> {r:SNat} -> (n:SNat) -> (l = r) -> (l * n) = (r * n)
multRightConstant n eq =  rewrite eq in Refl

multRightSuccPlus : (l : SNat) -> (r : SNat) -> l * (S r) = l + (l * r)
multRightSuccPlus One   r = Refl
multRightSuccPlus (S l) r =
  rewrite multRightSuccPlus l r in
  rewrite plusAssociative l r (l * r) in
  rewrite plusAssociative r l (l * r) in
  rewrite plusCommutative r l in
          Refl

multCommutative : (l: SNat) -> (r: SNat) -> (l * r) = (r * l)
multCommutative One  r = rewrite (multOneRightNeutral r) in Refl
multCommutative (S l) r =
  rewrite multCommutative l r in
  rewrite multRightSuccPlus r l in
          Refl

multRightSuccCancel : (l,r,n:SNat) -> (l * (S n) = r * (S n)) -> (l * n = r * n)
multRightSuccCancel One One n eq = Refl
multRightSuccCancel One (S x) n eq = void $ plusXIsNotEqualToX (S n) eq
multRightSuccCancel (S x) One n eq = void $ plusXIsNotEqualToX (S n) (sym eq)
multRightSuccCancel (S x) (S y) n eq = cong $ multRightSuccCancel x y n $ plusLeftCancel (S n) eq

multRightCancel : (l,r,n:SNat) -> (l * n = r * n) -> (l = r)
multRightCancel l r One eq =
  rewrite sym $ multOneRightNeutral l in
  rewrite sym $ multOneRightNeutral r in
          eq
multRightCancel l r (S x) eq =  multRightCancel l r x $ multRightSuccCancel l r x eq

multDistributesOverPlusLeft : (l : SNat) -> (c : SNat) -> (r : SNat) -> (l + c) * r = (l * r) + (c * r)
multDistributesOverPlusLeft One   c r = Refl
multDistributesOverPlusLeft (S l) c r =
  rewrite multDistributesOverPlusLeft l c r in
  rewrite plusAssociative r (l * r) (c * r) in
          Refl

multDistributesOverPlusRight : (l : SNat) -> (c : SNat) -> (r : SNat) -> l * (c + r) = (l * c) + (l * r)
multDistributesOverPlusRight l c r =
  rewrite multCommutative l (c + r) in
  rewrite multDistributesOverPlusLeft c r l in
  rewrite multCommutative l c in
  rewrite multCommutative l r in
          Refl

multAssociative : (l : SNat) -> (c : SNat) -> (r : SNat) -> l * (c * r) = (l * c) * r
multAssociative One c r = Refl
multAssociative (S l) c r =
  rewrite multDistributesOverPlusLeft c (l * c) r in
  rewrite multAssociative l c r in
          Refl
