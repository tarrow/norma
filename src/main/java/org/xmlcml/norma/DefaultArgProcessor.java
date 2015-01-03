package org.xmlcml.norma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DefaultArgProcessor {

	
	private static final Logger LOG = Logger
			.getLogger(DefaultArgProcessor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public static final String MINUS = "-";
	public static final String[] DEFAULT_EXTENSIONS = {"html", "xml"};
	public final static String H = "-h";
	public final static String HELP = "--help";
	public static final String I = "-i";
	public static final String INPUT = "--input";
	public static final String INPUT_HELP = "\nINPUT:\nInput stream (Files, directories, URLs), Norma tries to guess reasonable actions. \n"
			+ "also expands some simple wildcards. The argument can either be a single object, or a list. Within objects\n"
			+ "the content of curly brackets {...} is expanded as wildcards (cannot recurse). There can be multiple {...}\n"
			+ "within an object and all are expanded (but be sensible - this could generate the known universe and crash the\n"
			+ "system. (If this is misused it will be withdrawn). Objects (URLs, files) can be mixed but it's probably a\n"
			+ "poor idea.\n"
			+ "\n"
			+ "The logic is: \n"
			+ "(a) if an object starts with 'www' or 'http:' or 'https;' it's assumed to be a URL\n"
			+ "(b) if it is a directory, then the contents (filtered by extension) are added to the list as files\n"
			+ "(c) if it's a file it's added to the list\n"
			+ "the wildcards in files and URLs are then expanded and the results added to the list\n"
			+ "\n"
			+ "Current wildcards:\n"
			+ "  {n1:n2} n1,n2 integers: generate n1 ... n2 inclusive\n"
			+ "  {foo,bar,plugh} list of strings\n"
			+"";
			
	
	public static final String O = "-o";
	public static final String OUTPUT = "--output";
	public static final String OUTPUT_HELP = "\nOUTPUT\n Output is to local filestore ATM. If there is only one input\n"
			+ "after wildcard expansion then a filename can be given. Else the argument must be a writeable directory; Norma\n"
			+ "will do her best to create filenames derived from the input names. Directory structures will not be preserved\n"
			+ "See also --recursive and --extensions";
	
	public static final String R = "-r";
	public static final String RECURSIVE = "--recursive";
	public static final String RECURSIVE_HELP = "\nRECURSIVE input directories\n "
			+ "If the input is a directory then by default only the first level is searched\n"
			+ "if the --recursive flag is set then all files in the directory tree may be input\n"
			+ "See also --extensions";
	
	public static final String E = "-e";
	public static final String EXTENSIONS = "--extensions";
	public static final String EXTENSIONS_HELP = "\nEXTENSIONS \n "
			+ "When a directory or directories are searched then all files are input by default\n"
			+ "It is possible to limit the search by using only certain extensions(which "
			+ "See also --recursive";

	
	private static final Pattern INTEGER_RANGE_PATTERN = Pattern.compile("(\\d+):(\\d+)");
	public static Pattern GENERAL_PATTERN = Pattern.compile("\\{([^\\}]*)\\}");
	
	protected String output;
	protected List<String> extensions = Arrays.asList(DEFAULT_EXTENSIONS);
	private boolean recursive = false;
	protected List<String> inputList;
	
	public DefaultArgProcessor() {
		
	}

	
	public void expandWildcardsExhaustively() {
		while (expandWildcardsOnce());
	}
	
	public boolean expandWildcardsOnce() {
		boolean change = false;
		List<String> newInputList = new ArrayList<String>();
		for (String input : inputList) {
			List<String> expanded = expandWildcardsOnce(input);
			newInputList.addAll(expanded);
			change |= (expanded.size() > 1 || !expanded.get(0).equals(input));
		}
		inputList = newInputList;
		return change;
	}


	/** expand expressions/wildcards in input.
	 * 
	 * @param input
	 * @return
	 */
	private List<String> expandWildcardsOnce(String input) {
		Matcher matcher = GENERAL_PATTERN.matcher(input);
		List<String> inputs = new ArrayList<String>(); 
		if (matcher.find()) {
			String content = matcher.group(1);
			String pre = input.substring(0, matcher.start());
			String post = input.substring(matcher.end());
			inputs = expandIntegerMatch(content, pre, post);
			if (inputs.size() == 0) {
				inputs = expandStrings(content, pre, post);
			} 
			if (inputs.size() == 0) {
				LOG.error("Cannot expand "+content);
			}
		} else {
			inputs.add(input);
		}
		return inputs;
	}

	private List<String> expandIntegerMatch(String content, String pre, String post) {
		List<String> stringList = new ArrayList<String>();
		Matcher matcher = INTEGER_RANGE_PATTERN.matcher(content);
		if (matcher.find()) {
			int start = Integer.parseInt(matcher.group(1));
			int end = Integer.parseInt(matcher.group(2));
			for (int i = start; i <= end; i++) {
				String s = pre + i + post;
				stringList.add(s);
			}
		}
		return stringList;
	}

	private List<String> expandStrings(String content, String pre, String post) {
		List<String> newStringList = new ArrayList<String>();
		List<String> vars = Arrays.asList(content.split("\\|"));
		for (String var : vars) {
			newStringList.add(pre + var + post);
		}
		
		return newStringList;
	}

	/** read tokens until next - sign.
	 * 
	 * leave iterator ready to read next minus
	 * 
	 * @param listIterator
	 * @return
	 */
	protected static List<String> createTokenListUpToNextMinus(ListIterator<String> listIterator) {
		List<String> list = new ArrayList<String>();
		while (listIterator.hasNext()) {
			String next = listIterator.next();
			if (next.startsWith(MINUS)) {
				listIterator.previous();
				break;
			}
			list.add(next);
		}
		return list;
	}

	protected void checkHasNext(ListIterator<String> listIterator) {
		if (!listIterator.hasNext()) {
			throw new RuntimeException("ran off end; expected more arguments");
		}
	}

	protected void processInput(ListIterator<String> listIterator) {
		List<String> inputs = createTokenListUpToNextMinus(listIterator);
		if (inputs.size() == 0) {
			inputList = new ArrayList<String>();
			LOG.error("Must give at least one input");
		} else {
			inputList = inputs;
		}
	}

	protected void processOutput(ListIterator<String> listIterator) {
		checkHasNext(listIterator);
		output = listIterator.next();
	}

	protected void processRecursive(ListIterator<String> listIterator) {
		recursive = true;
	}

	protected void processExtensions(ListIterator<String> listIterator) {
		extensions = createTokenListUpToNextMinus(listIterator);
	}

	public List<String> getInputList() {
		ensureInputList();
		return inputList;
	}

	private void ensureInputList() {
		if (inputList == null) {
			inputList = new ArrayList<String>();
		}
	}

	public String getOutput() {
		return output;
	}

	public boolean isRecursive() {
		return recursive;
	}

	protected boolean parseArgs(String[] args) {
		boolean parsed = true;
		ListIterator<String> listIterator = Arrays.asList(args).listIterator();
		while (listIterator.hasNext()) {
			parsed &= parseArgs(listIterator);
		}
		return parsed;
	}
	
	protected boolean parseArgs(ListIterator<String> listIterator) {
		boolean processed = false;
		if (listIterator.hasNext()) {
			processed = true;
			String arg = listIterator.next();
//			LOG.info("arg: "+arg);
			if (!arg.startsWith(MINUS)) {
				LOG.error("Parsing failed at: ("+arg+"), expected \"-\" trying to recover");
			} else if (E.equals(arg) || EXTENSIONS.equals(arg)) {
				processExtensions(listIterator);
			} else if (I.equals(arg) || INPUT.equals(arg)) {
				processInput(listIterator); 
			} else if (O.equals(arg) || OUTPUT.equals(arg)) {
				processOutput(listIterator); 
			} else if (R.equals(arg) || RECURSIVE.equals(arg)) {
				processRecursive(listIterator); 
			} else if (H.equals(arg) || HELP.equals(arg)) {
				processHelp();
			} else {
				processed = false;
//				listIterator.previous();
//				LOG.error("Unknown arg: ("+arg+"), trying to recover");
			}
		}
		return processed;
	}
	
	protected void processHelp() {
		System.out.println(INPUT_HELP);
		System.out.println(OUTPUT_HELP);
		System.out.println(RECURSIVE_HELP);
		System.out.println(EXTENSIONS_HELP);
	}

}
