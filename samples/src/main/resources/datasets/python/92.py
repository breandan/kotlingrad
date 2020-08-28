def test4(a, x, list):
    b = (yield from a)
    c = (yield a)
    d = (yield a, x)
    e = (z**2 for z in list if x % 2 == 0)
    f = (yield (z**2 for z in list if x % 2 == 0))
    # grammar does not accept it, but Intellij (SDK Python 3.6) does accept it:
    # g = (yield z**2 for z in list if x % 2 == 0)
    h = ((yield z**2) for z in list if x % 2 == 0)
    i = ()
    return b, c, d, e
