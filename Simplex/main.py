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


    def simplex(self,basis,verbose=False, tex=False, stepwise=False):
        # The simplex-algorithm
        # basis is a list containing the numbers of m variables that form a basis
        # verbose == True prints the current tableau after every step
        # stepwise == True stops after every pivot-step, continue with enter, end stepwise procedure with "r" and enter
        # tex == True prints the tableaus in LaTeX-code
        if not self.hasStandardForm:
            self.T = self.standardizeTableau(basis)
            self.hasStandardForm = True

        while not self.solved:
            (p,s) = self.determinePivotElement()
            if verbose:
                self.printTableau()
                if stepwise:
                    cont = input()
                    if cont == "r":
                        stepwise = False
                print("Pivot-Element: ", p, s)
            if tex:
                self.printTableau(True,pivot=(p,s))
            if stepwise:
                cont = input()
                if cont == "r":
                    stepwise = False

            if s == 0:
                self.solved = True
                self.finiteSolution = True
            elif p == 0:
                self.solved = True
                self.finiteSolution = False
            else:
                self.pivotStep(p,s)

        return self.finiteSolution


    def determineAllVertices(self,onlyFeasible=True,noDuplicates=False):
        # Determines all bases with corresponding vertex of the given polyhedron
        # onlyFeasible excludes infeasible bases
        # noDuplicates only gives back one base for every vertex (relevant for degenerate vertives)

        allPossibleBasis = combinations(range(1,self.n+1),self.m)
        allVertices = {}
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
                    if not (noDuplicates and x in allVertices.values()):
                        allVertices[posBasis] = x

        return allVertices


    def determineAllEdges(self,bidirect=False):
        # Determines all Edges of the given polyhedron (i.e. pairs of neighbouring vertices)
        # bidirect=True lists all edges in both directions
        # If multiple bases represent the same vertex, on bases is chosen to represent this vertex

        # A dictonary containing for all bases the corresponding vertex (if feasible)
        allVerticesWDup = self.determineAllVertices(onlyFeasible=True, noDuplicates=False)
        # The same dictonary but for points represented by multiple bases only one is present
        allVertices = self.determineAllVertices(onlyFeasible=True, noDuplicates=True)

        # A dictonary containing for every representation-base the set of bases they represent
        vertexDictUnique = {}
        # A dictonary containing for every base, by which base it are represented
        vertexDictMult = {}
        # Build up both these dictonaries:
        while len(allVerticesWDup) > 0:
            (basis,x) = allVerticesWDup.popitem()
            for key in allVertices:
                value = allVertices[key]
                if value == x:
                    if key not in vertexDictUnique:
                        vertexDictUnique[key] = []
                    vertexDictUnique[key].append(basis)
                    vertexDictMult[basis] = key
                    break

        # A dictonary containing for every representing base all the (representing) neighboring bases
        allEdges = {}
        # Building this dictonary:
        while len(allVertices) > 0:
            (basis, x) = allVertices.popitem()

            # Create entry if not already present
            if basis not in allEdges:
                allEdges[basis] = []

            # Consider all possibles bases representing the current vertex:
            for varBasis in vertexDictUnique[basis]:
                # Consider all base-elements, which could be replaced
                for k in range(0, self.m):
                    # Consider all possible replacements:
                    for i in range(1, self.n + 1):
                        # Create the potential neighbor bases:
                        nBasis = list(varBasis).copy()
                        nBasis[k] = i
                        nBasis.sort()
                        nBasis = tuple(nBasis)
                        # Check whether this is a base
                        if nBasis in vertexDictMult:
                            # Check whether it corresponse to a different vertex and the resulting edge is not already in the list
                            if vertexDictMult[nBasis] != basis and vertexDictMult[nBasis] not in allEdges[basis]:
                                # Only add the edge if we want edges in both directions or the reverse edge is not already included:
                                if bidirect or vertexDictMult[nBasis] not in allEdges:
                                    allEdges[basis].append(vertexDictMult[nBasis])

        return allEdges



    def printAllVertices(self,onlyFeasible=True,noDuplicates=False):
        allVertices = self.determineAllVertices(onlyFeasible,noDuplicates)
        while len(allVertices) > 0:
            (basis,x) = allVertices.popitem()
            print("Basis", basis, "with solution", x)


    def printAllEdges(self,tex=False,style=""):
        # Prints all Edges of the given polyhedron (i.e. pairs of neighbouring vertices)
        # Either as text or in tikz-Format (with style as attribute for each edge).
        # The latter implicetly assumes that the orginal problem has only three variables
        # (i.e. its feasibility region can be drawn in 3D space) and all other variables are only slack variables.

        allVertices = self.determineAllVertices(onlyFeasible=True,noDuplicates=False)
        allEdges = self.determineAllEdges()

        while len(allEdges) > 0:
            (basis, edgeList) = allEdges.popitem()
            for nBasis in edgeList:
                x = allVertices[basis]
                y = allVertices[nBasis]

                if tex:
                    # TeX-Style:
                    vertexOne = str(x[2].numerator) + "/" + str(x[2].denominator) + ", " \
                                + str(x[0].numerator) + "/" + str(x[0].denominator) + ", "\
                                + str(x[1].numerator) + "/" + str(x[1].denominator)
                    y = allVertices[tuple(nBasis)]
                    vertexTwo = str(y[2].numerator) + "/" + str(y[2].denominator) + ", " \
                                + str(y[0].numerator) + "/" + str(y[0].denominator) + ", " \
                                + str(y[1].numerator) + "/" + str(y[1].denominator)

                    print("\\draw[",style,"] (",vertexOne,") -- (",vertexTwo,");")
                else:
                    # Plain Text:
                    print(basis, " -- ", nBasis, ": ", x, " -- ", allVertices[tuple(nBasis)])




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


    def printTableau(self,tex=False,pivot=(0,0)):
        if tex:
            def fTs(fract):
                if fract.denominator == 1:
                    return str(fract.numerator)
                return str(fract.numerator) + "/" + str(fract.denominator)

            print("\\begin{tabular}{c|c|",(self.n-self.m)*"c","|}",sep="")
            print("\t","\\multicolumn{1}{c}{} & \\multicolumn{1}{c}{} ",end="")
            for nB in self.N:
                print("& \\multicolumn{1}{c}{$x_"+str(nB)+"$}",end=" ")
            print("\\\\ \\cline{2-"+str(self.n-self.m+2)+"}")

            print("$z(x),r_N$ &", end="")
            print("$",fTs(self.T[0][0]),"$", end="")
            for j in range(1, self.n - self.m + 1):
                print("& $", fTs(self.T[0][j]), "$", end="")
            print("\\\\\\cline{2-"+str(self.n-self.m+2)+"}\n", end="")

            for i in range(1, self.m + 1):
                print("$x_" + str(self.B[i - 1]), "$ &", end="")
                print("$",fTs(self.T[i][0]),"$", end="")
                for j in range(1, self.n - self.m + 1):
                    if i==pivot[0] and j==pivot[1]:
                        print("& $\\boxed{", fTs(self.T[i][j]), "}$", end="")
                    else:
                        print("& $",fTs(self.T[i][j]), "$", end="")
                print("\\\\\n", end="")

            print("\\cline{2-"+str(self.n-self.m+2)+"}")
            print("\\end{tabular}")

        else:
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


# Das Optimierungsproblem von Blatt 6/Aufgabe 1
# A = [[0,3,1,1,0,0,0],
#      [1,3,1,0,1,0,0],
#      [1,2,0,0,0,1,0],
#      [1,-1,0,0,0,0,1]]
# b = [3,5,4,2]
# c = [-4,-7,-3,0,0,0,0]
# basis = [4,5,6,7]

# Das Optimierungsproblem von Blatt 6/Aufgabe 3
# Zeigt das Kreiseln der Auswahlregel von Dantzig
A = [[Fraction(1,4),-8,-1,9,1,0,0],
     [Fraction(1,2),-12,Fraction(-1,2),3,0,1,0],
     [0,0,1,0,0,0,1]]
b = [0,0,1]
c = [Fraction(-3,4),20,Fraction(-1,2),6,0,0,0]
basis = [5,6,7]

t = Tableau(A,b,c)
t.simplex(basis,tex=False,verbose=True,stepwise=True)

print(t.getCurrentSolution())
print(t.getCurrentValue())