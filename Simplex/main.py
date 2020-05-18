from fractions import Fraction
from itertools import combinations

INFINITY = 10000000000000

class Tableau:

    def __init__(self,A,b,c):
        self.m = len(A)
        self.n = len(A[0])

        assert (self.m <= self.n) # Assumption on A

        self.A = []
        for i in range(self.m):
            assert(len(A[i])==self.n) # Checks that every row of A has the same length
            self.A.append([Fraction(aij) for aij in A[i]])

        assert(len(b)==self.m)  # Checks dimension of b
        self.b = [Fraction(bi) for bi in b]
        assert(len(c)==self.n) # checks dimension of c
        self.c = [Fraction(ci) for ci in c]

        self.hasStandardForm = False
        self.solved = False
        self.finiteSolution = None


    def standardizeTableau(self,basis):
        self.B = basis
        self.N = [i for i in range(1, self.n + 1) if i not in self.B]

        Tfull = [[Fraction(0)] + [-self.c[self.B[j]-1] for j in range(0,self.m)] + [-self.c[self.N[j]-1] for j in range(0,self.n-self.m)]]
        for i in range(self.m):
            Tfull.append([self.b[i]]+[self.A[i][self.B[j]-1] for j in range(0,self.m)] + [self.A[i][self.N[j]-1] for j in range(0,self.n-self.m)])

        for i in range(1,self.m+1):
            k = i
            while k<=self.m and Tfull[k][i] == 0:
                k+=1
            if k>self.m:
                # Ist keine Basis
                return None

            pivotRow = [Tfull[k][j]/Tfull[k][i] for j in range(0,self.n+1)]

            # Switch Rows k and i
            for j in range(0,self.n+1):
                Tfull[k][j] = Tfull[i][j]
                Tfull[i][j] = pivotRow[j]

            for k in range(0,self.m+1):
                if k != i:
                    entry = Tfull[k][i]
                    for j in range(0,self.n+1):
                        Tfull[k][j] = Tfull[k][j] - entry * pivotRow[j]

        T = [[Tfull[0][0]] + [Tfull[0][self.m+j] for j in range(1,self.n-self.m+1)]]
        for i in range(1,self.m+1):
            T.append([Tfull[i][0]] + [Tfull[i][self.m+j] for j in range(1,self.n-self.m+1)])

        return T



    def determinePivotElement(self):
        # More Auswahlregeln

        #Dantzig's Rule:
        s = 0
        maxS = 0
        for j in range(1,self.n-self.m+1):
            if self.T[0][j] > maxS:
                maxS = self.T[0][j]
                s = j

        if s == 0:
            # Problem geloest:
            return (0,0)

        p = 0
        minP = INFINITY
        for i in range(1,self.m+1):
            if self.T[i][s] > 0:
                if self.T[i][0] / self.T[i][s] < minP:
                    minP = self.T[i][0] / self.T[i][s]
                    p = i

        if p == 0:
            # Problem unbeschraenkt:
            return (0,s)

        return(p,s)


    def pivotStep(self,p,s):
        temp = self.B[p-1]
        self.B[p-1] = self.N[s-1]
        self.N[s-1] = temp

        pivotElement = self.T[p][s]
        for j in range(0,self.n-self.m+1):
            if j == s:
                self.T[p][j] = 1/pivotElement
            else:
                self.T[p][j] = self.T[p][j] / pivotElement

        for i in range(0,self.m+1):
            if i != p:
                element = self.T[i][s]
                for j in range(0,self.n-self.m+1):
                    if j == s:
                        self.T[i][s] = - self.T[i][s] / pivotElement
                    else:
                        self.T[i][j] = self.T[i][j] - element * self.T[p][j]

        return True


    def simplex(self,basis,verbose=False):
        if not self.hasStandardForm:
            self.T = self.standardizeTableau(basis)
            self.hasStandardForm = True

        while not self.solved:
            (p,s) = self.determinePivotElement()
            if verbose:
                self.printTableau()
                print("Pivot-Element: ", p, s)

            if s == 0:
                self.solved = True
                self.finiteSolution = True
            elif p == 0:
                self.solved = True
                self.finiteSolution = False
            else:
                self.pivotStep(p,s)

        return self.finiteSolution


    def determineAllVertices(self,onlyFeasible=True):
        allPossibleBasis = combinations(range(1,self.n+1),self.m)
        for posBasis in allPossibleBasis:
            T = self.standardizeTableau(posBasis)
            if not T is None:
                x = [Fraction(0) for _ in range(self.n)]
                feasible = True
                for i in range(1,self.m+1):
                    x[posBasis[i-1]-1] = T[i][0]
                    if T[i][0]<0:
                        feasible = False

                if feasible or not onlyFeasible:
                    print("Basis",posBasis,"with solution",x)




    def getCurrentSolution(self):
        x = [Fraction(0) for _ in range(self.n)]
        for i in range(1,self.m+1):
            x[self.B[i-1]-1] = self.T[i][0]
        return x


    def getCurrentValue(self):
        return self.T[0][0]


    def getCurrentReducedCostVector(self):
        r = [self.T[0][j] for j in range(1,self.n-self.n+1)]
        return r


    def printTableau(self):
        maxSize = 8
        def elementString(s,ss="",sss=""):
            s = str(s)+str(ss)+str(sss)
            return " " + s + (maxSize-len(s)-1)*" "

        print("------------------------------------------------------------------")
        print(elementString(""),elementString(""),"|",end="")
        for j in range(1, self.n - self.m + 1):
            print(elementString("x_"+str(self.N[j-1])), end="")
        print("\n", end="")
        print(elementString(""),elementString(self.T[0][0].numerator, "/", self.T[0][0].denominator), "|", end="")
        for j in range(1, self.n - self.m + 1):
            print(elementString(self.T[0][j].numerator, "/", self.T[0][j].denominator), end="")
        print("\n", end="")
        print("------------------------------------------------------------------")
        for i in range(1,self.m+1):
            print(elementString("x_"+str(self.B[i-1])),elementString(self.T[i][0].numerator,"/",self.T[i][0].denominator),"|",end="")
            for j in range(1,self.n-self.m+1):
                print(elementString(self.T[i][j].numerator,"/",self.T[i][j].denominator),end="")
            print("\n",end="")
        print("------------------------------------------------------------------")


# A = [[0,3,1,1,0,0,0],[1,0,2,0,1,0,0],[1,2,0,0,0,1,0],[1,-1,0,0,0,0,1]]
# b = [3,10,6,4]
# c = [-4,-7,-3,0,0,0,0]
# basis = [4,5,6,7]

# A = [[1,2,3,1]]
# b = [1]
# c = [-1,0,0,0]
# basis = [4]

A = [[0,3,1,1,0,0,0],
     [1,3,1,0,1,0,0],
     [1,2,0,0,0,1,0],
     [1,-1,0,0,0,0,1]]
b = [3,5,4,2]
c = [-4,-7,-3,0,0,0,0]
basis = [4,5,6,7]

t = Tableau(A,b,c)
t.simplex(basis,True)

print(t.getCurrentSolution())
print(t.getCurrentValue())


t.determineAllVertices()
