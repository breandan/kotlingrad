def listgen1():
    testlist = [1,2,3,4,5,6]
    genlist = [(x**2 + 4) for x in testlist if x % 2 == 0 if x > 2]
    return genlist
