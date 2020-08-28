def dictgen5():
    testset = {1, 2, 3, 4, 5}
    genset = {(x**(2 + 4)) for x in testset if x % 2 == 0 if x > 2}
    return genset
