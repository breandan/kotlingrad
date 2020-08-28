def test1(a, b, c):
    del a
    del a, b, c
    del a.x, b.y
