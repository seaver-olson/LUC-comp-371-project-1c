package edu.luc.cs.cs371.topwords

import scala.collection.mutable

//i found that case classes have a lot of useful builtin methods as well as being immutable by default
//https://stackoverflow.com/questions/44607744/scala-case-classes-are-they-just-structs#:~:text=albeit%20a%20case%20class%20is%20immutable%20by%20default%2C
case class WindowState(
  window: List[String],//replacement for mutable Queue
  freqs: Map[String, Int],//replacement for mutable Map
  wordCount: Int
)

object WindowState:
  def empty: //for scan behavior
    WindowState = WindowState(List.empty, Map.empty, 0)
//stdin args + Observer Object 
object TopWordsProcessor:
  private val window = mutable.Queue[String]()//FIFO
  private val freqs = mutable.Map[String, Int]().withDefaultValue(0)
  private var stepCounter = 0;
  /**
   * Filter words shorter than minLength
   * Add word to window and update freqs
   * if window exceeds size : evict oldest word before generating cloud 
   * if window is full : generate and send word cloud to observer
   *
   * processStream processes one stream of Strings into that window
   * */
  def processStream(words: Iterator[String], cloudSize: Int, minLength: Int, windowSize: Int): Iterator[List[(String, Int)]] =
    words
      .filter(_.length >= minLength)//is the word greater or equal to minLength
      .scanLeft(WindowState.empty)((state, word) => //perform the immutable updateState on each word
        updateState(state, word, windowSize)
      )
      .drop(windowSize)//bounded
      .map(state => generateWordCloud(state, cloudSize))//maps to word Cloud
  //since windowState is immutable to updateState I will create a new instance of the WindowState with the new word
  def updateState(state: WindowState, newWord: String, windowSize: Int): WindowState = 
    val updatedWindow = state.window :+ newWord//:+ == append to end of list
    val updatedFreqs = state.freqs.updatedWith(word):
      case Some(count) => Some(count + 1)//if something is found in map then update frequency
      case None => Some(1)//else start the count
    if updatedWindow.size > windowSize then
      //evict
      val evictedWord = updatedWindow.head
      val afterEvict = updatedWindow.tail//strange method but okay https://www.geeksforgeeks.org/scala/scala-stack-tail-method-with-example/#:~:text=It%20returns%20a%20new%20stack%20that%20consists%20of%20all%20the%20elements%20except%20the%20first%20one.
      //re-eval freqs
      val trimmedFreqs = updatedFreqs.updatedWith(oldestWord): //either subtract one or remove freq count
        case Some(count) if count > 1 => Some(count -1)
        case _ => None
      
      WindowState(afterEvict, trimmedFreqs, state.wordCount + 1)
    else 
      WindowState(updatedWindow, updatedFreqs, state.wordCount + 1)
  private def generateWordCloud(): List[(String, Int)] =
    freqs
      .toList
      .sortBy {case (word, freq) => (-freq, word)}//sort by freq desc 
      .take(cloudSize)
