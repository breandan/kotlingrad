def lambda2(a, b):
    return lambda c, d: c + (d - a) * (lambda e, f: e / f)
