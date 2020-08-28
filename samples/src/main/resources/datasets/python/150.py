def listgen3():
    testlist = [1,2,3,4,5,6]
    genlist = [x**y for x, y in testlist if x % 2 == 0 if x > 2]
    return genlist
