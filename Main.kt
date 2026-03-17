import kotlin.random.Random

const val DIM = 1_000_000_000
const val THREAD_NUM = 4
val arr = IntArray(DIM)
var globalMin = Int.MAX_VALUE
var globalIndex = -1
val lock = Any()

fun main() {

    initArray()
    val threads = ArrayList<Thread>()
    val startTime = System.currentTimeMillis()
    val partSize = DIM / THREAD_NUM

    for (i in 0 until THREAD_NUM) {
        val start = i * partSize
        val end = if (i == THREAD_NUM - 1) DIM else start + partSize

        val thread = Thread {
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
            }
        }
        threads.add(thread)
        thread.start()
    }
    for (t in threads) {
        t.join()
    }
    val endTime = System.currentTimeMillis()
    println("Minimum element: $globalMin")
    println("Index: $globalIndex")
    println("Execution time: ${endTime - startTime} ms")
}

fun initArray() {
    for (i in 0 until DIM) {
        arr[i] = Random.nextInt(1, 1_000_000)
    }
    val index = Random.nextInt(DIM)
    arr[index] = -100
    println("Negative element inserted at index: $index")
}