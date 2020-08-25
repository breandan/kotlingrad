def sum(a, b, *c, d, e):
    return ((a + b) * d) + e


def sum2(a, b, *c, d, e):
    return sum(a, b, d=4, e=6)


def sum3():
    x = 0
    if (x >= 0):
        x = x + 1
        x = x + 1
    return x
