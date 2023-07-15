@file:OptIn(UnstableKMathAPI::class)

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.html.br
import kotlinx.html.dom.append
import kotlinx.html.h3
import space.kscience.kmath.UnstableKMathAPI
import space.kscience.kmath.data.XYErrorColumnarData
import space.kscience.kmath.distributions.NormalDistribution
import space.kscience.kmath.expressions.autodiff
import space.kscience.kmath.expressions.symbol
import space.kscience.kmath.functions.*
import space.kscience.kmath.operations.DoubleBufferOps.Companion.map
import space.kscience.kmath.operations.asIterable
import space.kscience.kmath.operations.toList
import space.kscience.kmath.optimization.*
import space.kscience.kmath.random.RandomGenerator
import space.kscience.kmath.real.DoubleVector
import space.kscience.kmath.real.same
import space.kscience.kmath.real.step
import space.kscience.plotly.Plotly
import space.kscience.plotly.models.ScatterMode
import space.kscience.plotly.models.TraceValues
import space.kscience.plotly.plotDiv
import space.kscience.plotly.scatter

// Forward declaration of symbols that will be used in expressions.
val a by symbol
val b by symbol
val c by symbol
val d by symbol

/**
 * Shortcut to use buffers in plotly
 */
operator fun TraceValues.invoke(vector: DoubleVector) {
    numbers = vector.asIterable()
}


suspend fun main() {
    val sigma = 1.0
    //A generator for a normally distributed values
    val generator = NormalDistribution(0.0, sigma)

    //A chain/flow of random values with the given seed
    val chain = generator.sample(RandomGenerator.default(112667))


    //Create a uniformly distributed x values like numpy.arrange
    val x = 1.0..100.0 step 1.0

    val polynomial = Polynomial(1.0, -1.0e-2, 8.0e-3, -1e-4)

    //Perform an operation on each x value (much more effective, than numpy)
    val y = x.map { arg -> polynomial.value(arg) + chain.next() }

    // create same errors for all xs
    val yErr = DoubleVector.same(x.size, sigma)

    val result = XYErrorColumnarData.of(x, y, yErr).fitWith(
        QowOptimizer,
        Double.autodiff,
        mapOf(a to 1.0, b to 0.0, c to 0.0, d to 0.0),
        OptimizationParameters(a, b, c, d)
    ) { arg ->
        //bind variables to autodiff context
        val a = bindSymbolOrNull(a) ?: zero
        val b = bindSymbolOrNull(b) ?: zero
        val c = bindSymbolOrNull(c) ?: zero
        val d = bindSymbolOrNull(d) ?: zero
        a * arg.pow(3) + b * arg.pow(2) + c * arg + d
    }


    val resultPolynomial = Polynomial(
        result.resultPoint[d] ?: 0.0,
        result.resultPoint[c] ?: 0.0,
        result.resultPoint[b] ?: 0.0,
        result.resultPoint[a] ?: 0.0,
    )

    document.getElementById("playground")!!.append {
        plotDiv {
            scatter {
                mode = ScatterMode.markers
                x(x)
                y(y)
                error_y {
                    array = yErr.toList()
                }
                name = "data"
            }
            scatter {
                mode = ScatterMode.lines
                x(x)
                y(x.map { resultPolynomial.value(it) })
                name = "fit"
            }
        }
        br()
        h3 {
            +"Fit result: $resultPolynomial"
        }
        h3 {
            +"Chi2/dof = ${result.chiSquaredOrNull!! / result.dof}"
        }
    }

}