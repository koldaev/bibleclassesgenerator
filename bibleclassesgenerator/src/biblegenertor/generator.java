package biblegenertor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import java.io.*;
import java.lang.reflect.Field;

public class generator {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException, SecurityException, IllegalArgumentException, IllegalAccessException {

	
		Connection             conn  = null;        
        
		//переменные генерации классов Библии
		PreparedStatement   pstmt = null;
        ResultSet            rs    = null;

        //переменные названий книг Библии
        PreparedStatement   namebooks = null;
        ResultSet           rnamebooks    = null;
        
        //переменные статистики глав Библии
        PreparedStatement   pchapters = null;
        ResultSet           rschapters    = null;
        
      //переменные статистики стихов Библии
        PreparedStatement   ppoems = null;
        ResultSet           rspoems    = null;
        
        Properties connInfo = new Properties();
        
        connInfo.put("characterEncoding","UTF8");
        connInfo.put("user", "root");
        connInfo.put("password", "zxasqw12");
        
        //вводим нужный язык Библии
        Scanner in = new Scanner(System.in);
        while(true) {
        	System.out.println("Пожалуйста, введите один язык Библии из 10 (ru,en,de,es,fr,pt,zh,gr,it,tr) и нажмите <enter>:\n");
        	
        	String lang = in.nextLine();
        
        //достаем точный индекс из массива  
        Class cl = Class.forName("biblegenertor.names");
        try {
		Field outext = cl.getDeclaredField(lang + "names");
		String[] strfield = (String[])outext.get(cl);
		System.out.println(strfield[0]);
        } catch (NoSuchFieldException e) {
        System.out.println("Вы ввели неверный параметр. Работа программы завершена");
        System.exit(0);
        }
        //////////////////////////////////
		
        conn  = DriverManager.getConnection("jdbc:mysql://localhost/bible_"+lang+"?", connInfo);
        
        File dir = new File(lang);
        File dir2 = new File(lang+"/bible");
        dir.mkdir();
        dir2.mkdir();
        
        //генерация суперкласса коллекции для хранения Библии
        FileWriter fstreamsuperclass = new FileWriter(lang+"/bible/"+lang+"superobject.java");
        BufferedWriter outfstreamsuperclass = new BufferedWriter(fstreamsuperclass);
        
        outfstreamsuperclass.write("package "+lang+".bible;\n\n");
        outfstreamsuperclass.write("import java.util.LinkedHashMap;\n\n");
        outfstreamsuperclass.write("public class "+lang+"superobject {\n\n");
        outfstreamsuperclass.write("public Integer bible;\n");
        outfstreamsuperclass.write("public Integer chapter;\n");
        outfstreamsuperclass.write("public Integer poem;\n");
        outfstreamsuperclass.write("public String poemtext;\n\n");
        outfstreamsuperclass.write("public static LinkedHashMap<String, "+lang+"superobject> biblemap;\n");
        outfstreamsuperclass.write("static {\n");
        outfstreamsuperclass.write("\tbiblemap = new LinkedHashMap<String, "+lang+"superobject>();\n");
        outfstreamsuperclass.write("}\n\n");
        outfstreamsuperclass.write(lang+"superobject(Integer bible, Integer chapter, Integer poem, String poemtext) {\n");
        outfstreamsuperclass.write("\tthis.bible = bible != null ? bible : 1;\n");
        outfstreamsuperclass.write("\tthis.chapter = chapter != null ? chapter : 1;\n");
        outfstreamsuperclass.write("\tthis.poem = poem != null ? poem : 1;\n");
        outfstreamsuperclass.write("\tthis.poemtext = poemtext != null ? poemtext : \"poemtext\";\n");
        outfstreamsuperclass.write("}\n\n");
        outfstreamsuperclass.write("}");
        
        outfstreamsuperclass.close();
        
        
        //System.exit(0);

        //генерация класса статистики библии
        FileWriter fstreamproperties = new FileWriter(lang+"/bible/"+lang+"bibleproperties.java");
        BufferedWriter outproperties = new BufferedWriter(fstreamproperties);
        
        outproperties.write("package "+lang+".bible;\n\n");
        outproperties.write("import java.util.Properties;\n\n");
        outproperties.write("public class "+lang+"bibleproperties {\n\n");
        
        //генерируем статический список книг Библии
        outproperties.write("public static String "+lang+"biblenames[] = {\n");
        
        if(lang == "ru") {
        	namebooks = conn.prepareStatement("SELECT idshort as id, shortname as biblename FROM bible_"+lang);
        } else {
        	namebooks = conn.prepareStatement("SELECT idbible as id, biblename FROM "+lang+"bible");
        }
        if (namebooks.execute()) {
        	rnamebooks = namebooks.getResultSet();
        	while(rnamebooks.next()) {
        		if(rnamebooks.getInt("id") != 66 ) {
        			outproperties.write("\t\"" + rnamebooks.getString("biblename")  + "\",\n");
        		} else {
        			outproperties.write("\t\"" + rnamebooks.getString("biblename") + "\"\n");
        		}
        	}
        	System.out.println("Генерация книг завершена");
        }
        
        outproperties.write("};\n\n");
        ////////////////////////////////////////////
        
        outproperties.write("public static Properties "+lang+"biblechapters;\n");
        outproperties.write("static {\n");
        outproperties.write("\t"+lang+"biblechapters = new Properties();\n");
        
        String poemstatistics = "public static Properties "+lang+"chapterpoems;\n";
        poemstatistics += "static {\n";
        poemstatistics += "\t"+lang+"chapterpoems = new Properties();\n";
        
        for(int i=1; i<=66; i++) {
        	
        	pchapters = conn.prepareStatement("SELECT MAX(chapter) as maxchapter FROM "+lang+"text WHERE bible="+i);
        	if (pchapters.execute()) {
        		
        		rschapters = pchapters.getResultSet();
        		while (rschapters.next()) {
        			
        			int maxchapter = rschapters.getInt("maxchapter");
        			outproperties.write("\t"+lang+"biblechapters.put(\""+lang+"bible"+i+"\","+maxchapter+");\n");
        			
        			for (int ich = 1; ich<=maxchapter; ich++) {
        				ppoems = conn.prepareStatement("SELECT MAX(poem) as maxpoem FROM "+lang+"text WHERE bible="+i+" AND chapter="+ich);
        				if (ppoems.execute()) {
        					rspoems = ppoems.getResultSet();
        					if (rspoems.first()) {
        						int maxpoemvalue = rspoems.getInt("maxpoem");
        						poemstatistics += "\t"+lang+"chapterpoems.put(\""+lang+"bible"+i+"_chapter"+ich+"\","+maxpoemvalue+");\n";
        					}

        				}
        				
        			}
        			
        			
        		}
        		
        	}
        	
        }
        
        System.out.println("Генерация статистики глав и стихов завершена");
        
        outproperties.write("}\n\n");
        poemstatistics += "}\n\n";
        outproperties.write(poemstatistics);
        outproperties.write("}");
        outproperties.close();
        
        for(int i=1; i<=66; i++) {
        	
       	pstmt = conn.prepareStatement("SELECT * FROM "+lang+"text WHERE bible = " + i);
        
        FileWriter fstream = new FileWriter(lang+"/bible/"+lang+"bible" + i + ".java");
        BufferedWriter out = new BufferedWriter(fstream);
        
        
        out.write("package "+lang+".bible;\n\n");
       	out.write("public class "+lang+"bible" + i + " {\n\n");
        out.write("public "+lang+"bible" + i + "() {\n\n");
        
        if(i != 19) {
        
        out.write("putmapbible" + i + "();\n\n");
        out.write("}\n\n");
        out.write("private void putmapbible" + i + "() {\n\n");
        
        if(pstmt.execute()) {
            rs = pstmt.getResultSet(); 
            while (rs.next()) {
            	String bible = rs.getString("bible");
            	String chapter = rs.getString("chapter");
            	String poem = rs.getString("poem");
            	String poemtext = rs.getString("poemtext");
            	String putformap = 	lang+"superobject.biblemap.put(\"b" + bible + "_" + chapter + "_" + poem + 
            						"\", new "+lang+"superobject("+ bible + "," + chapter + "," + poem + ",\"" +  poemtext.replace("\"", "\\\"") + "\"));\n"; 
            	out.write(putformap);
            }
            
        }
        
        
        } else {
        	
       	out.write("putmapbible" + i + "_1();\n");
       	out.write("putmapbible" + i + "_2();\n\n");
        out.write("}\n\n");
        out.write("private void putmapbible" + i + "_1() {\n\n");
        
        if(pstmt.execute()) {
            rs = pstmt.getResultSet();  
            while (rs.next()) {
            	if(rs.getString("chapter").contains("76") && Integer.parseInt(rs.getString("poem")) == 1) {
            			out.write("\n}\n\n");
            			out.write("private void putmapbible" + i + "_2() {\n\n");
            	}
            	String chapter = rs.getString("chapter");
            	String bible = rs.getString("bible");
            	String poem = rs.getString("poem");
            	String poemtext = rs.getString("poemtext");
            	String putformap = 	lang+"superobject.biblemap.put(\"b" + bible + "_" + chapter + "_" + poem + 
            						"\", new "+lang+"superobject("+ bible + "," + chapter + "," + poem + ",\"" +  poemtext.replace("\"", "\\\"") + "\"));\n"; 
            	out.write(putformap);
            }
            
        }
        	
        }
            
        out.write("\n}\n\n");
        out.write("}");
        out.close();
        
        System.out.println("Сгенерирован файл " + lang+"/bible/"+lang+"bible" + i + ".java");
        
        }
        
        
        
        	} 
        
        //in.close();
        
        
        
        }
	
	

    }



