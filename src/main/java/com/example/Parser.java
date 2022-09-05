package com.example;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static class RemovedObjectsPair {
		private Integer index;
		private final Token token;

		private RemovedObjectsPair(Integer index, Token token) {
			this.index = index;
			this.token = token;
		}
	}

	private final ArrayList<RemovedObjectsPair> removedObjects = new ArrayList<>();

	private Parser parent = null;

	public interface CompilerLambda {
		SyntaxTree.Block run(Parser tokens);
	}
	private final ArrayList<Token> tokens;
	private boolean saveTexts = false;

	public void setSaveTexts(boolean saveTexts) {
		this.saveTexts = saveTexts;
	}

	public Parser(ArrayList<Token> tokens) {
		this.tokens = tokens;
	}

	public void remove(String token) {
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).getName().equals(token)) {
				removedObjects.add(new RemovedObjectsPair(i, tokens.get(i)));
				tokens.remove(i);
				i--;
			}
		}
	}

	public void purge(String token) {
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).getName().equals(token)) {
				tokens.remove(i);
				i--;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder tmp = new StringBuilder();
		for(Token t : tokens) {
			tmp.append(t).append("\n");
		}
		return tmp.toString();
	}

	// Replace the previous model with newName and store the output of lambda in its object.
	public void replace(String model, String newName, CompilerLambda lambda, String... includedRemovedObjects) {
		List<String> included = Arrays.asList(includedRemovedObjects);
		String map = " " + this.getMap(includedRemovedObjects) + " ";
		Pattern	pattern = Pattern.compile(" " + model + " ");
		Matcher matcher = pattern.matcher(map);
		if (!matcher.find()) {
			return ;
		}
		String matched = matcher.group(0).substring(1);
		map = map.substring(1);
		int index;
		index = map.indexOf(matched);
		int listIndex = 0;
		for (int i = 0; i < index; i++) {
			if (map.charAt(i) == ' ') {
				listIndex++;
			}
		}
		StringBuilder text = null;
		if (saveTexts) text = new StringBuilder();
		int tmp = listIndex;
		ArrayList<Token> tmpTokens = new ArrayList<>();
		String[] split = matched.split(" ");
		for (int i = 0; i < split.length; i++) {
			if (included.contains(split[i])) {
				int finalI = i;
				int finalListIndex = listIndex;
				Token token = removedObjects.stream().filter(
								removedObjectsPair -> removedObjectsPair.index == finalListIndex + finalI)
						.findFirst().get().token;
				tmpTokens.add(token);
				if (saveTexts) text.append(token.getText());
				listIndex--;
				continue;
			}
			tmpTokens.add(tokens.get(listIndex + i));
			if (saveTexts) text.append(tokens.get(listIndex + i).getText());
			if (i != matched.split(" ").length - 1) {
				for (RemovedObjectsPair entry : removedObjects) {
					if (entry.index > listIndex + i) {
						entry.index--;
					}
				}
			}
			tokens.remove(listIndex + i);
			listIndex--;
		}
		listIndex = tmp;
		Token t = new Token(newName, saveTexts? text.toString():null);
		t.setLine(tmpTokens.get(0).getLine());
		tokens.add(listIndex, t);
		Parser parser = new Parser(tmpTokens);
		parser.parent = this;
		t.setObject(lambda.run(parser));
		this.replace(model, newName, lambda);
	}

	public Parser getParent() {
		return parent;
	}

	public String getMap(String... includedRemovedObjects) {
		List<String> included = Arrays.asList(includedRemovedObjects);
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < tokens.size(); i++) {
			tmp.append(tokens.get(i).getName()).append(" ");
			for (RemovedObjectsPair removedObjectsPair : removedObjects) {
				if (included.contains(removedObjectsPair.token.getName()) && removedObjectsPair.index == i + 1) {
					tmp.append(removedObjectsPair.token.getName()).append(" ");
					break;
				}
			}
		}
		try {
			return tmp.substring(0, tmp.length() - 1);
		} catch(IndexOutOfBoundsException ignore) {}
		return "";
	}

	public ArrayList<Token> getTokens() {
		return tokens;
	}

	public boolean contains(String token) {
		for (Token t : tokens) {
			if (Objects.equals(t.getName(), token)) return true;
		}
		return false;
	}

	public int findFirst(String token) {
		for (int i = 0; i < tokens.size(); i++) {
			if (Objects.equals(tokens.get(i).getName(), token)) return i;
		}
		return -1;
	}

	public int findAfter(int i, String token) {
		for (; i < tokens.size(); i++) {
			if (Objects.equals(tokens.get(i).getName(), token)) return i;
		}
		return -1;
	}

	public int count(String name) {
		int count = 0;
		for (Token token : tokens) {
			if (token.getName().equals(name)) count++;
		}
		return count;
	}
}
