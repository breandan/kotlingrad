package edu.umontreal.kotlingrad.samples

import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT
import guru.nidi.graphviz.engine.Format.SVG
import java.awt.FlowLayout
import javax.swing.*

fun main() {
    with(DoublePrecision) {
        val t = (1 + x * 2 - 3 + y + z / y).d(y).d(x) + z / y * 3 - 2
        t.render()
    }
}

fun Fun<*>.render() {
    val image = graph(directed = true) {
        edge["color" eq "black", Arrow.NORMAL]

        graph[Rank.dir(LEFT_TO_RIGHT)]

        toGraph()
    }.toGraphviz().render(SVG).toImage().let { image ->
        JFrame().apply {
            contentPane.layout = FlowLayout()
            contentPane.add(JLabel(ImageIcon(image)))
            pack()
            isVisible = true
        }
    }
//    }.toFile(File("example.svg"))
}

