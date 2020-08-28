if __name__ == '__main__':
    building = Building(5)
    elevator = Elevator(10, building)
    elevator.up()
    elevator.up()
    elevator.down()
    print(elevator.floor)def func(a, b, c=3, d=4, *list, e=6, f, **keywords) :
    return 1
