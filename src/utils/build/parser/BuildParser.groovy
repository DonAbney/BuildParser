package utils.build.parser

import groovy.time.*
import java.util.concurrent.TimeUnit
import groovy.xml.MarkupBuilder

class BuildParser {
	
	File inputFile
	File outputFile
	
	Integer brokenBuildCount = 0
	Integer goodCount = 0
	
	long totalGoodBuildTime = 0
	long overallDuration = 0
	
	def upTimes = []
	
	BuildParser(inputFile) {
		this.inputFile = inputFile
	}
	
	Integer parseFile() {
		def feed = new XmlSlurper().parse(inputFile)

		String firstBuildTime
		String lastBuildTime
		String goodBuildStartTime
		String goodBuildEndTime
		
		Integer numberOfEntries = feed.entry.size()
		
		feed.entry.eachWithIndex {thisEntry, i ->
			String entryTime = thisEntry.published.text()
			if (i == 0) lastBuildTime = entryTime
			if (i == numberOfEntries - 1) firstBuildTime = entryTime
			
			if (thisEntry.text().contains("broken since this build")) {
				goodBuildEndTime = entryTime
				brokenBuildCount++
			} else if (thisEntry.text().contains("broken since build #")) {
				brokenBuildCount++
			} else if (thisEntry.text().contains("back to normal")) {
				goodBuildStartTime = entryTime
				def goodBuildTime = calculateDuration(goodBuildEndTime, goodBuildStartTime)
				totalGoodBuildTime += goodBuildTime
				upTimes.add(goodBuildTime)
				goodCount++
			} else {
				goodCount++
			}
		}
		
		overallDuration += calculateDuration(lastBuildTime, firstBuildTime)
		writeOutput()
	
		return numberOfEntries
	}
	
	void writeOutput() {
		
		outputFile = new File((inputFile.parentFile.toString() + '/output.html'))

		if (outputFile.exists()) {
			def backupFile = new File(outputFile.absolutePath + 'backup')
			backupFile.append(outputFile.getText())
			outputFile.write('')
		}
		
//		outputFile.append('Number of GOOD builds = ' + goodCount + '\r\n')
//		outputFile.append('Number of BROKEN builds = ' + brokenBuildCount + '\r\n')
//		outputFile.append('Total duration in build file is ' + formatTime(overallDuration) + '\r\n')
//		outputFile.append('Total build UP time is ' + formatTime(totalGoodBuildTime) + '\r\n')
//		outputFile.append('Total build DOWN time is ' + formatTime(overallDuration - totalGoodBuildTime) + '\r\n')
//		outputFile.append('\r\n')
//		
//		outputFile.append('The list of UP time durations (trying to show wobble) \r\n')
//		
//		upTimes.each {
//			outputFile.append(formatTime(it) + '\r\n')
//		}
		
		def writer = new StringWriter()
		def builder = new MarkupBuilder(writer)
		
		builder.html {
			head {
				title("Jenkins Build Report"){}
				style(type:"text/css", '''  
			    body {  
			        margin: 30px;  
			        padding: 30px;  
			        background-color: #cccccc  
				}
				ul {
					list-style: none
				}
			    ''')
			}
			body {
				p('Number of GOOD builds = ' + goodCount)
				p('Number of BROKEN builds = ' + brokenBuildCount)
				p('Total duration in build file is ' + formatTime(overallDuration))
				p('Total build UP time is ' + formatTime(totalGoodBuildTime))
				p('Total build DOWN time is ' + formatTime(overallDuration - totalGoodBuildTime))
				p("The list of UP time durations")
				ul {
					upTimes.each {
						li (formatTime(it))
					}
				}
			}
		}
		
		outputFile.append(writer.toString())
		
	}
	
	public long calculateDuration(startTime, endTime) {
		
		def start = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", startTime)
		def end = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", endTime)
		return TimeCategory.minus(start, end).toMilliseconds()
	}
	
	public String formatTime(long duration) {

		return String.format("%d hours, %d min",
			TimeUnit.MILLISECONDS.toHours(duration),
			TimeUnit.MILLISECONDS.toMinutes(duration) -
			TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)))
	}
	
}
