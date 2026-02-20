package edu.luc.cs.cs371.topwords

import scala.collection.mutable.ListBuffer

// Test observer that collects word clouds for verification 
class TestObserver extends WordCloudObserver:
  private val updates = ListBuffer[List[(String, Int)]]()
  
  def onUpdate(wordCloud: List[(String, Int)]): Unit =
    updates += wordCloud
  
  // Get all collected word clouds 
  def getUpdates: List[List[(String, Int)]] = updates.toList
  
  // Get the most recent word cloud 
  def getLatestUpdate: Option[List[(String, Int)]] = updates.lastOption
  
  // Get the number of updates received 
  def getUpdateCount: Int = updates.size
  
  // Clear all collected updates 
  def clear(): Unit = updates.clear()
  
  // Check if any updates were received 
  def hasUpdates: Boolean = updates.nonEmpty
