package utils.build.parser

class JenkinsReport {

	static main(args) {
		
		File inputFile = validateInput(args)
		
		if (inputFile) {
			BuildParser parser = new BuildParser(inputFile)
			parser.parseFile()
		}
	}
	
	public static File validateInput(args) {
		
		File inputFile = new File (args[0])
		
		if (inputFile.isFile()) {
			return inputFile
		} else {
			println 'Something is wrong with the input file. The world will now implode.'
			return null
		}
	}

}
