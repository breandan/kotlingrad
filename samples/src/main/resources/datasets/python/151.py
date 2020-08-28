def dictgen4():
    testdict = {1: 10, 2: 20, 3: 30}
    gendict = {(x**(2 + 4)): x for x in testdict if x % 2 == 0 if x > 2}
    return gendict
