package edu.luc.cs.cs371.topwords

//Observer to receive word cloud updates
trait WordCloudObserver:
  //WordCloud should be a list of (word, freq)
  def onUpdate(wordCloud: List[(String, Int)]): Unit 

