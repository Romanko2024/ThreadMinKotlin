import kotlin.random.Random

class ArrClass(private val dim: Int, private val threadNum: Int) {
    val arr = IntArray(dim)
    private var globalMin = Int.MAX_VALUE
    private var globalIndex = -1
    private var threadCount = 0
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val monitor = Object()

    init {
        for (i in 0 until dim) {
            arr[i] = Random.nextInt(1, 1_000_000)
        }
        val index = Random.nextInt(dim)
        arr[index] = -100
        println("Negative element inserted at index: $index")
    }

    fun partMin(startIndex: Int, finishIndex: Int): Pair<Int, Int> {
        var localMin = Int.MAX_VALUE
        var localIndex = -1
        for (i in startIndex until finishIndex) {
            if (arr[i] < localMin) {
                localMin = arr[i]
                localIndex = i
            }
        }
        return Pair(localMin, localIndex)
    }
    fun collectMin(value: Int, index: Int) {
        synchronized(monitor) {
            if (value < globalMin) {
                globalMin = value
                globalIndex = index
            }
        }
    }
    fun incThreadCount() {
        synchronized(monitor) {
            threadCount++
            monitor.notify()
        }
    }
    fun getMin(): Pair<Int, Int> {
        synchronized(monitor) {
            while (threadCount < threadNum) {
                try {
                    monitor.wait()
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return Pair(globalMin, globalIndex)
                }
            }
            return Pair(globalMin, globalIndex)
        }
    }
    fun threadMin(): Pair<Int, Int> {
        val threads = Array(threadNum) { i ->
            val partSize = dim / threadNum
            val start = i * partSize
            val end = if (i == threadNum - 1) dim else start + partSize
            ThreadMin(start, end, this)
        }

        val startTime = System.currentTimeMillis()
        threads.forEach { it.start() }
        val result = getMin()
        val endTime = System.currentTimeMillis()
        println("Execution time: ${endTime - startTime} ms")

        return result
    }
}

class ThreadMin(
    private val startIndex: Int,
    private val finishIndex: Int,
    private val arrClass: ArrClass
) : Thread() {

    override fun run() {
        val (min, index) = arrClass.partMin(startIndex, finishIndex)
        arrClass.collectMin(min, index)
        arrClass.incThreadCount()
    }
}

fun main() {
    val dim = 1_000_000_000
    val threadNum = 20

    val arrClass = ArrClass(dim, threadNum)
    val result = arrClass.threadMin()

    println("min: ${result.first} index: ${result.second}")
}