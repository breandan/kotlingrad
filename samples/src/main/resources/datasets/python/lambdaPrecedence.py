def lambda0(a, b):
    return lambda c, d: a * b


def lambda1(a, b):
    return lambda c, d, *e, f, **g: c * d + e - 2 + f - g


def lambda2(a, b):
    return lambda c, d: c + (d - a) * (lambda e, f: e / f)
