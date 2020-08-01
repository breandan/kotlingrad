package uk.neilgall.kanren

typealias GoalDecorator = (Goal) -> Goal

fun trace(name: String): GoalDecorator {
  fun traceGoal(goal: Goal, incomingState: State): Sequence<State> {
    val outgoingStates = goal(incomingState)
    println("$name($incomingState) -> ${outgoingStates.toList()}")
    return outgoingStates
  }

  return { goal -> { state: State -> traceGoal(goal, state) } }
}