package in.dobro;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class supergenerator {
	
	private static Class<?> cl;

	static String[] langs50 = {"af","am","ara","be","bg","bn","cs","da","de","du","en","es","et","fa","fi","fr","gr","heb","hin","hr","hu","hy","is","it","jap","ka","kk","ko","la","lt","lv","mk","no","pl","pt","ro","ru","sk","sq","sr","sv","sw","tam","th","tl","tr","uk","ur","vi","zh"};

	
	static Connection             conn  = null;        
    
	//переменные генерации классов Библии
	static PreparedStatement   pstmt = null;
	static ResultSet            rs    = null;

    //переменные названий книг Библии
    static PreparedStatement   namebooks = null;
    static PreparedStatement   updatetable = null;
    static PreparedStatement   updatetable2 = null;
    static ResultSet           rnamebooks    = null;
    
    //переменные статистики глав Библии
    static PreparedStatement   pchapters = null;
    static ResultSet           rschapters    = null;
    
  //переменные статистики стихов Библии
    static PreparedStatement   ppoems = null;
    static ResultSet           rspoems    = null;
    
    static Properties connInfo;
    static {
    connInfo = new Properties();
    connInfo.put("characterEncoding","UTF8");
    connInfo.put("user", "root");
    connInfo.put("password", "zxasqw12");
	}
    
    
    
	protected static void generation(String lang) throws SQLException, IOException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		printbiblename(lang);
		
        conn  = DriverManager.getConnection("jdbc:mysql://localhost/bible_"+lang+"?", connInfo);
        
        File dir = new File(lang);
        File dir2 = new File(lang+"/bible");
        dir.mkdir();
        dir2.mkdir();

        //генерация класса статистики библии
        FileWriter fstreamproperties = new FileWriter(lang+"/bible/"+lang+"bibleproperties.java");
        BufferedWriter outproperties = new BufferedWriter(fstreamproperties);
        
        outproperties.write("package "+lang+".bible;\n\n");
        outproperties.write("import java.util.Properties;\n\n");
        outproperties.write("public class "+lang+"bibleproperties {\n\n");
        
        //генерируем статический список книг Библии
        outproperties.write("public static String "+lang+"biblenames[] = {\n");
        
        updatetable = conn.prepareStatement("UPDATE " + lang + "text SET poem=poem + 0");
        updatetable.execute();
        updatetable2 = conn.prepareStatement(" ALTER TABLE " + lang + "text CHANGE COLUMN poem poem INT UNSIGNED");
        updatetable2.execute();
       	namebooks = conn.prepareStatement("SELECT idbible as id, biblename FROM "+lang+"bible");

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
        out.write("import java.util.Properties;\n\n"); 
       	out.write("public class "+lang+"bible" + i + " {\n\n");
 
        out.write("public static Properties "+lang+"bibletext"+i+";\n\n");
        out.write("static {\n");
        out.write("\t"+lang+"bibletext"+i+" = new Properties();\n");
        
        if(pstmt.execute()) {
            rs = pstmt.getResultSet(); 
            while (rs.next()) {
            	String bible = rs.getString("bible");
            	String chapter = rs.getString("chapter");
            	String poem = rs.getInt("poem")+"";
            	String poemtext = rs.getString("poemtext");
            	String putformap = 	"\t"+lang+"bibletext"+i+".put(\"b" + bible + "_" + chapter + "_" + poem + "\", \"" +  poemtext.replace("\"", "\\\"") + "\");\n"; 
            	out.write(putformap);
            }
            
        }
        
            
        out.write("\n}\n\n");
        out.write("}");
        out.close();
        
        System.out.println("Сгенерирован файл " + lang+"/bible/"+lang+"bible" + i + ".java");
        
        }
        

	}



	private static void printbiblename(String lang) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		// достаем точный индекс из массива
		cl = Class.forName("in.dobro.names");
		try {
			Field outext = cl.getDeclaredField(lang + "names");
			String[] strfield = (String[]) outext.get(cl);
			System.out.println(strfield[0]);
		} catch (NoSuchFieldException e) {
			System.out.println("Вы ввели неверный параметр. Работа программы завершена");
			System.exit(0);
		}
	}
	
}
