package edu.luc.cs.cs371.topwords

import com.typesafe.scalalogging.StrictLogging
import mainargs.{arg, main, ParserForMethods}//https://contributors.scala-lang.org/t/mainargs-a-small-convenient-dependency-free-library-for-command-line-argument-parsing-in-scala/4661
import scala.io.Source

object Main extends StrictLogging:
  @main
  def topwords(
    @arg(name = "cloud-size", short = 'c', doc = "Number of words in the cloud") 
    cloudSize: Int = 10,
    @arg(name = "length-at-least", short = 'l', doc = "Minimum word length to consider")
    minLength: Int = 6,
    @arg(name = "window-size", short = 'w', doc = "Size of the moving window")
    windowSize: Int = 1000,
    @arg(name = "every-k-steps", short = 'k', doc = "Generate update every k steps (0 = every step)")//extra credit argument 
    everyKSteps: Int = 0,
    @arg(name = "min-frequency", short = 'f', doc = "Minimum frequency to include in cloud")
    minFrequency: Int = 0
  ): Unit =
    try
      logger.debug(s"howMany=$cloudSize minLength=$minLength lastNWords=$windowSize everyKSteps=$everyKSteps minFrequency=$minFrequency")
      
      val observer = new ConsoleObserver(minFrequency)
      val processor = new TopWordsProcessor(cloudSize, minLength, windowSize, everyKSteps,observer)
      
      logger.debug("Reading words from standard input...")
      val lines = Source.stdin.getLines()
      val words = lines.flatMap(l => l.split("(?U)[^\\p{Alpha}0-9']+"))
      //case insensitive processing for extra credit
      for word <- words do 
        processor.processWord(word.toLowerCase)
      
      logger.debug("Processing complete")
    catch
      case _: java.io.IOException =>
        System.err.flush()
        System.exit(0)
      case e: Exception =>
        logger.error("Error during processing", e)
        System.exit(1)
        
def main(args: Array[String]): Unit =
    ParserForMethods(this).runOrExit(args)
  
