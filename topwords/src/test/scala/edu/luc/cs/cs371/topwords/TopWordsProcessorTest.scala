package edu.luc.cs.cs371.topwords

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class TopWordsProcessorTest extends AnyFunSuite with Matchers:

  // Test 1: Empty input
  test("no output with empty input"):
    val words = Iterator.empty[String]
    val clouds = TopWordsProcessor.processStream(words, cloudSize = 3, minLength = 2, windowSize = 5)
    clouds.toList shouldBe empty

  // Test 2: Words shorter than minimum length should be ignored
  test("ignores words shorter than minimum length"):
    val words = Iterator("a", "to", "the")
    val clouds = TopWordsProcessor.processStream(words, cloudSize = 3, minLength = 4, windowSize = 5)
    clouds.toList shouldBe empty

  // Test 3: No output until window is full
  test("no output until window is full"):
    val words = Iterator("hello", "world", "hello", "scala")
    val clouds = TopWordsProcessor.processStream(words, cloudSize = 3, minLength = 2, windowSize = 5)
    clouds.toList shouldBe empty  // Only 4 words, need 5

  // Test 4: Output starts when window is full
  test("generates output when window reaches full size"):
    val words = Iterator("aa", "bb", "cc", "dd", "ee")
    val clouds = TopWordsProcessor.processStream(words, cloudSize = 3, minLength = 2, windowSize = 5)
    val cloudsList = clouds.toList
    
    cloudsList should have size 1  // Exactly at window size
    cloudsList.head should not be empty

  // Test 5: Basic word cloud generation with repeats
  test("generates correct word cloud with frequency counts"):
    val words = Iterator("aa", "bb", "cc", "aa", "bb", "aa", "dd")
    val clouds = TopWordsProcessor.processStream(words, cloudSize = 3, minLength = 2, windowSize = 5)
    val cloudsList = clouds.toList
    
    cloudsList.last should contain (("aa", 2))
    cloudsList.last should contain (("bb", 1))

  // Test 6: Sliding window evicts old words
  test("maintains sliding window correctly"):
    val words = Iterator("aa", "bb", "cc", "dd", "ee", "ff", "gg")
    val clouds = TopWordsProcessor.processStream(words, cloudSize = 5, minLength = 2, windowSize = 5)
    val cloudsList = clouds.toList
    
    // After 7 words with window=5, "aa" and "bb" should be evicted
    val lastCloud = cloudsList.last
    lastCloud.exists(_._1 == "aa") shouldBe false
    lastCloud.exists(_._1 == "bb") shouldBe false
    lastCloud.exists(_._1 == "gg") shouldBe true

  // Test 7: Cloud size limit
  test("respects cloud size limit"):
    val words = Iterator("aa", "bb", "cc", "dd", "ee", "ff")
    val clouds = TopWordsProcessor.processStream(words, cloudSize = 2, minLength = 2, windowSize = 5)
    val cloudsList = clouds.toList
    
    cloudsList.foreach { cloud =>
      cloud.size should be <= 2
    }

  // Test 8: Frequency ordering
  test("orders by frequency descending"):
    val words = Iterator("aa", "aa", "aa", "bb", "bb", "cc")
    val clouds = TopWordsProcessor.processStream(words, cloudSize = 3, minLength = 2, windowSize = 5)
    val lastCloud = clouds.toList.last
    
    lastCloud.head._1 shouldBe "aa"  // Most frequent first
    lastCloud.head._2 shouldBe 2

  // Test 9: Large input scalability
  test("handles large input efficiently"):
    val words = (1 to 10000).iterator.map(i => s"word${i % 100}")
    val startTime = System.currentTimeMillis()
    
    val clouds = TopWordsProcessor.processStream(words, cloudSize = 10, minLength = 4, windowSize = 1000)
    val cloudsList = clouds.toList
    
    val duration = System.currentTimeMillis() - startTime
    
    duration should be < 5000L
    cloudsList should not be empty

  // Test 10: Interactive behavior
  test("produces continuous output as stream is processed"):
    val words = Iterator("aa", "bb", "cc", "dd", "ee", "ff", "gg", "hh")
    val clouds = TopWordsProcessor.processStream(words, cloudSize = 3, minLength = 2, windowSize = 5)
    
    val cloudsList = clouds.toList
    cloudsList.size shouldBe 4  // 8 words - 5 window + 1 = 4 outputs
