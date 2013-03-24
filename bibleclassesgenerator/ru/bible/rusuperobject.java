package ru.bible;

import java.util.LinkedHashMap;

public class rusuperobject {

public Integer bible;
public Integer chapter;
public Integer poem;
public String poemtext;

public static LinkedHashMap<String, rusuperobject> biblemap;
static {
	biblemap = new LinkedHashMap<String, rusuperobject>();
}

rusuperobject(Integer bible, Integer chapter, Integer poem, String poemtext) {
	this.bible = bible != null ? bible : 1;
	this.chapter = chapter != null ? chapter : 1;
	this.poem = poem != null ? poem : 1;
	this.poemtext = poemtext != null ? poemtext : "poemtext";
}

}