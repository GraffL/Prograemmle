import Data.List
-- Bessere primes und primeFactors-Funktionen:
-- import Data.Numbers.Primes


-- Lösung der Aufgabe:
primesWhereInverseIsNperiodic n =
    filter (hasOrdInP 10 n) candidates
    where candidates = primeFactors' (10^n-1)

    

-- Bestimme Ordnung:
powersofmodp :: Integer -> Integer -> [Integer]
powersofmodp x p = [x^i `mod` p | i<-[1..]]

hasOrdInP :: Integer -> Integer -> Integer -> Bool
hasOrdInP x y p = not (1 `elem` (genericTake (y-1) powers)) && (genericIndex powers (y-1) == 1)
    where powers = powersofmodp x p
    

-- Primfaktorzerkegung:
primeFactors' n = map snd (primeFactorization n)

primeFactorization n = encode $ arePrimeFactors (primesSmaller n) n

arePrimeFactors :: [Integer] -> Integer -> [Integer]
arePrimeFactors _ 1 = []
arePrimeFactors [] _ = []
arePrimeFactors (p:ps) n = 
    if (n `mod` p) == 0 then p:(arePrimeFactors (p:ps) (n `div` p))
    else arePrimeFactors ps n
    
primesSmaller n = takeWhile (<= n) primes''


encode :: Eq a => [a] -> [(Int,a)]
encode [] = []
encode (c:cs) = (length (takeWhile (==c) cs) + 1,c) : encode (dropWhile (==c) cs)



    
-- Langsames Sieb des Eratosteles:
primefilter :: [Integer] -> [Integer]
primefilter [] = []
primefilter (x:xs) =
    if x==0 then primefilter xs
            else x:(primefilter $ zipWith (*) pFilter xs)
                where pFilter = concat $ repeat ((genericReplicate (x-1) 1) ++ [0])
               
               
primes'' :: [Integer]               
primes'' = primefilter [2..]


-- Sehr langsamer Primzahlsuchalgorithmus
nthPrime 1 = 2
nthPrime n = (nextPrimes !! 0)
    where nextPrimes = filter currentlyPrime [last(currentPrimes)..]
          currentlyPrime x = all (0 /=) [x `mod` p | p <- currentPrimes]
          currentPrimes = [nthPrime i | i <- [1..(n-1)]]
          
primes' = [nthPrime n | n <- [1..]]