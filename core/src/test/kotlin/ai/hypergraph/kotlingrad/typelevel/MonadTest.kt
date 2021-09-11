package ai.hypergraph.kotlingrad.typelevel

import org.junit.jupiter.api.Test

class MonadTest {
  @Test
  fun testMonad() {
    QA().andThen(QT())
  }
}