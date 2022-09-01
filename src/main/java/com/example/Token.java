package com.example;

public class Token {
	private String name;
	private String text = "";
	private SyntaxTree.Block object = null;
	private int line;

	public Token(String name, String text) {
		this.text = text;
		this.name = name;
	}

	public Token(String name, String text, int line) {
		this.text = text;
		this.name = name;
		this.line = line;
	}

	public Token() {
		name = "NONE";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
	public SyntaxTree.Block getObject() {
		return object;
	}
 
	public void setObject(SyntaxTree.Block object) {
		this.object = object;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	@Override
	public String toString() {
		return name + " : " + text;
	}
}
