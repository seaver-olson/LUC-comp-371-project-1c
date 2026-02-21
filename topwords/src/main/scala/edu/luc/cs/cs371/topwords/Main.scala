package edu.luc.cs.cs371.topwords

import mainargs.{main, arg, ParserForMethods}
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging:
  
  @main
  def run(
    @arg(short = 'c', doc = "Size of word cloud")
    cloudSize: Int = 10,
    
    @arg(short = 'l', doc = "Minimum length")
    lengthAtLeast: Int = 6,
    
    @arg(short = 'w', doc = "Size of window")
    windowSize: Int = 1000
  ): Unit =
    
    logger.debug(s"howMany=$cloudSize minLength=$lengthAtLeast lastNWords=$windowSize")
    
    try
      // Read from stdin
      val lines = scala.io.Source.stdin.getLines()
      
      // Convert lines
      import scala.language.unsafeNulls
      val words = lines.flatMap(line => line.split("(?U)[^\\p{Alpha}0-9']+"))
      
      // Process the stream 
      val wordClouds = TopWordsProcessor.processStream(
        words = words,
        cloudSize = cloudSize,
        minLength = lengthAtLeast,
        windowSize = windowSize
      )
      
      // Print each word cloud
      wordClouds.foreach { cloud =>
        val output = cloud.map { case (word, freq) => s"$word: $freq" }.mkString(" ")
        println(output)
      }
      
    catch
      case e: java.io.IOException if e.getMessage != null && e.getMessage.contains("Broken pipe") =>
        // Graceful termination
        logger.debug("Received SIGPIPE, terminating gracefully")
      case e: Exception =>
        logger.error(s"Error processing input: ${e.getMessage}", e)
        sys.exit(1)

  def main(args: Array[String]): Unit =
    ParserForMethods(this).runOrExit(args)
