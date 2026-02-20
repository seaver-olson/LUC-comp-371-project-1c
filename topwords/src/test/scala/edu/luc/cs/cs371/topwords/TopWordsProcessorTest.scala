package edu.luc.cs.cs371.topwords

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class TopWordsProcessorTest extends AnyFunSuite with Matchers:

  // Test 1: Empty input (boundary case)
  test("no updates with empty input"):
    val observer = new TestObserver()
    val processor = new TopWordsProcessor(
      cloudSize = 3,
      minLength = 2,
      windowSize = 5,
      observer = observer
    )
    
    observer.hasUpdates shouldBe false
    observer.getUpdateCount shouldBe 0

  // Test 2: Words shorter than minimum length should be ignored
  test("ignores words shorter than minimum length"):
    val observer = new TestObserver()
    val processor = new TopWordsProcessor(
      cloudSize = 3,
      minLength = 4,
      windowSize = 5,
      observer = observer
    )
    
    processor.processWord("a")
    processor.processWord("to")
    processor.processWord("the")
    
    observer.hasUpdates shouldBe false

  // Test 3: No output until window is full
  test("no output until window is full"):
    val observer = new TestObserver()
    val processor = new TopWordsProcessor(
      cloudSize = 3,
      minLength = 2,
      windowSize = 5,
      observer = observer
    )
    
    processor.processWord("hello")
    processor.processWord("world")
    processor.processWord("hello")
    processor.processWord("scala")
    
    observer.getUpdateCount shouldBe 0
    
    processor.processWord("hello")
    observer.getUpdateCount shouldBe 1

  // Test 4: Basic word cloud generation
  test("generates correct word cloud"):
    val observer = new TestObserver()
    val processor = new TopWordsProcessor(
      cloudSize = 3,
      minLength = 2,
      windowSize = 5,
      observer = observer
    )
    
    val words = List("aa", "bb", "cc", "aa", "bb", "aa")
    words.foreach(processor.processWord)
    
    val latestCloud = observer.getLatestUpdate
    latestCloud shouldBe defined
    
    val cloud = latestCloud.get
    cloud.size shouldBe 3
    cloud(0) shouldBe ("aa", 3)
    cloud(1) shouldBe ("bb", 2)
    cloud(2) shouldBe ("cc", 1)

  // Test 5: Sliding window behavior
  test("maintains sliding window correctly"):
    val observer = new TestObserver()
    val processor = new TopWordsProcessor(
      cloudSize = 3,
      minLength = 2,
      windowSize = 5,
      observer = observer
    )
    
    List("aa", "bb", "cc", "dd", "ee").foreach(processor.processWord)
    
    processor.processWord("ff")
    
    val cloud = observer.getLatestUpdate.get
    cloud.exists(_._1 == "aa") shouldBe false
    cloud.exists(_._1 == "ff") shouldBe true

  // Test 6: Cloud size limit
  test("respects cloud size limit"):
    val observer = new TestObserver()
    val processor = new TopWordsProcessor(
      cloudSize = 2,
      minLength = 2,
      windowSize = 5,
      observer = observer
    )
    
    List("aa", "bb", "cc", "dd", "ee").foreach(processor.processWord)
    
    val cloud = observer.getLatestUpdate.get
    cloud.size shouldBe 2

  // Test 7: Multiple updates
  test("generates multiple updates as words are processed"):
    val observer = new TestObserver()
    val processor = new TopWordsProcessor(
      cloudSize = 2,
      minLength = 2,
      windowSize = 3,
      observer = observer
    )
    
    val words = List("aa", "bb", "cc", "dd", "ee")
    words.foreach(processor.processWord)
    
    observer.getUpdateCount shouldBe 3

  // Test 8: Large input scalability test
  test("handles large input efficiently"):
    val observer = new TestObserver()
    val processor = new TopWordsProcessor(
      cloudSize = 10,
      minLength = 6,
      windowSize = 1000,
      observer = observer
    )
    
    val startTime = System.currentTimeMillis()
    
    for i <- 1 to 10000 do
      processor.processWord(s"word${i % 100}")
    
    val endTime = System.currentTimeMillis()
    val duration = endTime - startTime
    
    duration should be < 5000L
    observer.getUpdateCount should be > 0
