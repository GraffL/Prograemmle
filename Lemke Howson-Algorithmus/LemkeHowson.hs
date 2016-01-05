import Data.Ratio
import Data.Maybe
import Data.List
import Data.List.Split

type Var = String

data Monom = Monom Rational Var
data Monoms = Monoms [Monom]
getMonoms :: Monoms -> [Monom]
getMonoms (Monoms m) = m
data Gleichung = Gleichung Var Monoms
data GLS = GLS [Gleichung]

instance Show GLS where
    show (GLS []) = ""
    show (GLS (g:gs)) = show g ++ "\n" ++ show (GLS gs)

instance Show Gleichung where
    show (Gleichung x xs) = x ++ " = " ++ show xs

instance Show Monoms where
    show (Monoms []) = ""
    show (Monoms [m]) = show m
    show (Monoms (m:n:ms)) = 
        if isNegative n
        then show m ++ " " ++ show (Monoms (n:ms))
        else show m ++ " +" ++ show (Monoms (n:ms))
        
isNegative :: Monom -> Bool
isNegative (Monom r _) = r < 0
    
instance Show Monom where
    show (Monom a "") = showBruch a
    show (Monom a v) = showBruch a ++ "*" ++ v

showBruch :: Rational -> String
showBruch r = 
    if denominator r == 1 
    then show $ numerator r
    else show (numerator r) ++ "/" ++ show (denominator r)
    
    
addMonom :: Monoms -> Monom -> Monoms
addMonom (Monoms []) m = Monoms [m]
addMonom (Monoms (n@(Monom an vn):ns)) m@(Monom am vm)
    | vn == vm  = Monoms ((Monom (an+am) vn):ns)
    | otherwise = Monoms (n:ns')
    where ns' = getMonoms (addMonom (Monoms ns) m)

addMonoms :: Monoms -> Monoms -> Monoms
addMonoms n (Monoms [])     = n
addMonoms n (Monoms (m:ms)) = addMonoms (addMonom n m) (Monoms ms )   

multMonom :: Monom -> Rational -> Monom
multMonom (Monom a v) b = Monom (a*b) v

multMonoms :: Monoms -> Rational -> Monoms
multMonoms (Monoms []) _ = Monoms []
multMonoms (Monoms (m:ms)) b = Monoms ((multMonom m b):(getMonoms $ multMonoms (Monoms ms) b))


findMonom :: Monoms -> Var -> Maybe Monom
findMonom (Monoms []) v = Nothing
findMonom (Monoms ((Monom am vm):ms)) v
    | vm == v   = Just (Monom am vm)
    | otherwise = findMonom (Monoms ms) v

removeVar :: Monoms -> Var -> Monoms
removeVar (Monoms m) v = Monoms (deleteBy isVar (Monom 0 v) m)
    where isVar (Monom a v) (Monom b w) = v == w
    
substVar :: Monoms -> Gleichung -> Monoms
substVar m (Gleichung v n) = 
    if isNothing zuSubst
    then m
    else addMonoms (multMonoms n substA) (removeVar m v)
    where zuSubst = findMonom m v
          Just (Monom substA substV) = zuSubst
    
    
einsetzenInGl :: Gleichung -> Gleichung -> Gleichung
einsetzenInGl g (Gleichung vh mh) = 
    Gleichung vh (substVar mh g)

normalisiereGl :: Gleichung -> Gleichung
normalisiereGl g@(Gleichung vg mg) = Gleichung v (entfNullMonoms m)
    where (Gleichung v m) = normalisiereGl' g

entfNullMonoms :: Monoms -> Monoms
entfNullMonoms (Monoms []) = Monoms []
entfNullMonoms (Monoms (m:ms)) = 
    if isNullMonom m
    then entfNullMonoms (Monoms ms)
    else Monoms (m:ms')
    where (Monoms ms') = entfNullMonoms (Monoms ms)

isNullMonom :: Monom -> Bool
isNullMonom (Monom a _) = a == 0
    
normalisiereGl' :: Gleichung -> Gleichung
normalisiereGl' (Gleichung v m) = 
    if isNothing basisMonom 
    then Gleichung v m
    else Gleichung v (removeVar (multMonoms m (1/(1-basisA))) v)
    where basisMonom = findMonom m v
          Just (Monom basisA basisV) = basisMonom    
          
solveGlFor :: Gleichung -> Var -> Maybe Gleichung
solveGlFor (Gleichung vg mg) v =
    if isNothing solveMonom 
    then Nothing
    else Just $ normalisiereGl $ Gleichung v (multMonoms (addMonom (removeVar mg v) (Monom (-1) vg)) (-1/solveA))
    where solveMonom = findMonom mg v
          Just (Monom solveA solveV) = solveMonom    

maxValueFor :: Gleichung -> Var -> Maybe Rational
maxValueFor (Gleichung vg mg) v =
    if isNothing changeMonom
    then Nothing
    else if changeA >= 0 then Nothing
         else Just (-constA / changeA)
    where changeMonom = findMonom mg v
          Just (Monom changeA changeV) = changeMonom    
          constMonom = findMonom mg ""
          Just (Monom constA _) = constMonom

          
makeBasisvar :: Var -> GLS -> GLS
makeBasisvar v (GLS (g:gs)) = 
    if isThightest (GLS gs) g v
    then GLS (g':gs')
    else makeBasisvar v (GLS (gs ++ [g]))
    where (Just g') = solveGlFor g v
          gs' = map (normalisiereGl.(einsetzenInGl g')) gs


isThightest :: GLS -> Gleichung -> Var -> Bool
isThightest gls g v =
    if isNothing (maxValueFor g v)
    then False
    else isThightest' gls (maxValueFor g v) v
    where isThightest' (GLS []) maxval v = True
          isThightest' (GLS (h:hs)) maxval v =
            if isNothing (maxValueFor h v) 
            then isThightest' (GLS hs) maxval v
            else (maxval <= (maxValueFor h v)) && isThightest (GLS hs) g v
            
            
            
            
makeTableaus :: Int -> Int -> [Rational] -> [Rational] -> (GLS, GLS)
makeTableaus m n a b = (gls1, gls2)
    where gls1 = makeGLS ("r", 1) ("y", m+1) (chunksOf n a)
          gls2 = makeGLS ("s", m+1) ("x", 1) (chunksOf m b)

makeGLS :: (Var, Int) -> (Var, Int) -> [[Rational]] -> GLS
makeGLS _ _ [] = GLS []
makeGLS (vs, ks) (vz, kz) (r:rs) = GLS (g:gs)
    where g = Gleichung (vs ++ (show ks)) (addMonom (multMonoms (makeMonoms (vz, kz) r) (-1)) (Monom 1 ""))
          GLS gs = makeGLS (vs, ks+1) (vz, kz) rs         

makeMonoms :: (Var, Int) -> [Rational] -> Monoms
makeMonoms _ [] = Monoms []
makeMonoms (v, k) (b:bs) = Monoms ((Monom b (v ++ (show k))):ms)
    where (Monoms ms) = (makeMonoms (v, k+1) bs)
    
    
    
--------

  
main = do
    (gls1, gls2) <- initialize
    (gls3, gls4) <- step (gls1, gls2)
    
    putStrLn "\nLetzter Zustand der Gleichungssysteme:"
    putStrLn . show $ gls3
    putStrLn . show $ gls4
  
    
initialize :: IO (GLS, GLS)
initialize = do
    putStrLn "Nutzenmatrix von SP1 eingeben (in der Form 1%1 1%2 3%4 1%2)"
    a_input <- getLine
    putStrLn "Transponierte Nutzenmatrix von SP2 eingeben (in der Form 1%1 1%2 3%4 1%2)"
    b_input <- getLine
    putStrLn "Zahl der reinen Strategien von SP1"
    m_input <- getLine
    putStrLn "Zahl der reinen Strategien von SP2"
    n_input <- getLine

    return $ makeTableaus (read m_input) (read n_input) (map read $ words a_input) (map read $ words b_input)
    

step :: (GLS, GLS) -> IO (GLS, GLS)
step (gls1, gls2) = do
    putStrLn "Gleichungen für 1. Polyeder:"
    putStrLn . show $ gls1
    putStrLn "Gleichungen für 2. Polyeder:"
    putStrLn . show $ gls2
    putStrLn "Welche Variable soll zur Basisvariable werden ('q' zum Beenden)"
    var <- getLine
    if var == "q" then
        return (gls1, gls2)
    else do
        putStrLn "In welchem Gleichungssystem (1/2)?"
        nr <- getLine
        if read nr == 1 then
            step (makeBasisvar var gls1, gls2)
        else
            step (gls1, makeBasisvar var gls2)
           
    
    
    
-- Test-Daten    
a = [1,3,0,0,0,2,2,1,1]
b = [2,1,0,1,3,0,0,1,3]
(gls1, gls2) = makeTableaus 3 3 a b    
    
c = [4,1,0,0,3,1,1,2,3]
d = [1,2,3,2,0,1,0,2,1]
(gls3,gls4) = makeTableaus 3 3 c d

