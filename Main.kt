import kotlin.random.Random

const val DIM = 1_000_000_000
const val THREAD_NUM = 8
val arr = IntArray(DIM)
var globalMin = Int.MAX_VALUE
var globalIndex = -1
val lock = Any()
var finished = 0

fun main() {

    initArray()
    val startTime = System.currentTimeMillis()
    val partSize = DIM / THREAD_NUM

    for (i in 0 until THREAD_NUM) {
        val start = i * partSize
        val end = if (i == THREAD_NUM - 1) DIM else start + partSize

        Thread {
            var localMin = Int.MAX_VALUE
            var localIndex = -1

            for (j in start until end) {
                if (arr[j] < localMin) {
                    localMin = arr[j]
                    localIndex = j
                }
            }

            synchronized(lock) {
                if (localMin < globalMin) {
                    globalMin = localMin
                    globalIndex = localIndex
                }
                finished++
                if (finished == THREAD_NUM) {
                    val endTime = System.currentTimeMillis()
                    println("Minimum element: $globalMin")
                    println("Index: $globalIndex")
                    println("Execution time: ${endTime - startTime} ms")
                }
            }
        }.start()
    }
}

fun initArray() {
    for (i in 0 until DIM) {
        arr[i] = Random.nextInt(1, 1_000_000)
    }
    val index = Random.nextInt(DIM)
    arr[index] = -100
    println("Negative element inserted at index: $index")
}