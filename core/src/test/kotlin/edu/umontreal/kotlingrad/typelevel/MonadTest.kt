package edu.umontreal.kotlingrad.typelevel

import org.junit.jupiter.api.Test

class MonadTest {
  @Test
  fun testMonad() {
    QA().andThen(QT())
  }
}