package packagem;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Git{
	
  private static String readAll(Reader rd) throws IOException {
          StringBuilder sb = new StringBuilder();
          int cp;
          while ((cp = rd.read()) != -1) {
             sb.append((char) cp);
          }
          return sb.toString();
       }

 

 public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
    InputStream is = new URL(url).openStream();
    try(BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
       String jsonText = readAll(rd);
       return new JSONArray(jsonText);
     } finally {
       is.close();
     }
 }

 

 public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
    InputStream is = new URL(url).openStream();
    try(BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
       String jsonText = readAll(rd);
       return new JSONObject(jsonText);
     } finally {
       is.close();
     }
 }
 
public static List<Object> retrieveTickID() throws JSONException, IOException {
      String projName ="TAJO";
      ArrayList<Object> v = new ArrayList<>();
      Integer j = 0;
      Integer i = 0; 
      Integer total = 1;
      //Get JSON API for closed bugs w/ AV in the project
      do {
         //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
         j = i + 1000;
         String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                + projName + "%22AND%22issueType%22=%22New%20Feature%22AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                + i.toString() + "&maxResults=" + j.toString();
         JSONObject json = readJsonFromUrl(url);
         JSONArray issues = json.getJSONArray("issues");
         total = json.getInt("total");
         for (; i < total && i < j; i++) {
            //Iterate through each bug
            v.add(issues.getJSONObject(i%1000).get("key"));
         } 
      } while (i < total);
      return v;
}

	
 public static List<String> subAddEmpty(int mese2,String anno2) {
	 String mesevuoto = "";
	 ArrayList<String> v =new ArrayList<>();
	 for(int j = mese2; j >= 1; j-- ) {
			if(Integer.toString(j).length() == 1) {
				mesevuoto=anno2+"-0"+Integer.toString(j);
			}else {
				mesevuoto=anno2+"-"+Integer.toString(j);
			}
			v.add(mesevuoto);							
		}
	 return v;
 }
 public static List<String> subAddEmpty1(int mese1,String anno2){
	 String mesevuoto="";
	 ArrayList<String> v =new ArrayList<>();
	 for(int z = mese1 ; z <= 12 ; z++ ) {
			if(Integer.toString(z).length() == 1) {
				mesevuoto=anno2+"-0"+Integer.toString(z);
			}else {
				mesevuoto=anno2+"-"+Integer.toString(z);
			}
			v.add(mesevuoto);			
		}
	 return v;
 }

 public static List<String> addEmptyDate(String date1,String date2){
	ArrayList<String> v = new ArrayList<>();
	String anno1 = date1.substring(0,4);
	String anno2 = date2.substring(0,4);
	Integer mese1 = Integer.parseInt(date1.substring(5, 7));
	Integer mese2 = Integer.parseInt(date2.substring(5, 7));
	String mesevuoto = "";
	if(anno1.compareTo(anno2) == 0) {
		mese1++;
		Integer diff =Math.abs(mese1-mese2);
		if(diff.compareTo(0) != 0) {
			while(diff!=0) {
				if(Integer.toString(mese1).length()==1) {
					mesevuoto=anno1+"-0"+Integer.toString(mese1);
				}else {
					mesevuoto=anno1+"-"+Integer.toString(mese1);
				}
				v.add(mesevuoto);
				diff--;
				mese1++;
			}
		}
	}else {
		mese2--;
		ArrayList<String> m1=(ArrayList<String>) subAddEmpty(mese2,anno2);
		v.addAll(m1);
	
		mese1++;
		ArrayList<String> m2=(ArrayList<String>) subAddEmpty1(mese1,anno1);
		v.addAll(m2);
		
  }
return v;
}

public static int writeFile(FileWriter fileWriter,int numeroCommit,String data1,String data2,int contaCommit) throws IOException {
	if(contaCommit != 0 && data1.compareTo(data2)!=0) {
		fileWriter.append(data1.substring(0,7));
		fileWriter.append(",");
		fileWriter.append(Integer.toString(numeroCommit));
		fileWriter.append("\n");
		numeroCommit=0;
		ArrayList<String> vuoto = (ArrayList<String>) addEmptyDate(data2, data1);
		for(int y = 0; y<vuoto.size();y++) {
			fileWriter.append(vuoto.get(y));
			fileWriter.append(",");
			fileWriter.append("0");
			fileWriter.append("\n");
		}	
	}
	return numeroCommit;
}
public static void reduceComplexity(int[] commit ,Boolean[] prima,String com,JSONArray json,int k,FileWriter fileWriter,String[] data1) throws JSONException, IOException { 
	String data2="";
	//0 numeroCommit 1 contaCommit
	if(prima[0] || prima[1]) {
		data2=json.getJSONObject(k).getJSONObject(com).getJSONObject("committer").getString("date").substring(0,7);
		commit[0]=writeFile(fileWriter,commit[0],data1[0],data2,commit[1]);
		
		commit[0]++;
		commit[1]++;
		data1[0]=json.getJSONObject(k).getJSONObject(com).getJSONObject("committer").getString("date").substring(0,7);
	}
}
public static void main(String[] args) throws IOException, JSONException, InterruptedException {
	int k=0;
	int i=1;
	int j=0;
	int[] commit= new int[2];
	commit[0]=0;
	commit[1]=0;
	Logger logger = Logger.getAnonymousLogger();
	String c="C:";
	String filePath="\\Users\\gabri\\OneDrive\\Desktop\\DatiTicket.csv";
	Boolean[] prima=new Boolean[2];
	String[] data1=new String[1];
	String url1 = "https://api.github.com/repos/apache/tajo/commits?page=";
	String url2 = "&per_page=100";
	ArrayList<Object> v=(ArrayList<Object>) retrieveTickID();
	String com="commit";
	try(FileWriter fileWriter = new FileWriter(c+filePath)){
		fileWriter.append("Anno-Mese, Commit\n");
		for(; ;i++) {
			//Scorro le varie pagine
			JSONArray json = readJsonArrayFromUrl(url1+i+url2);
			Integer l = json.length();
			if(l!= 0) { // Verifico che la pagina contiene almeno un committ
				for(k=0 ; k< l ; k++) { // Questo scorre i vari committ    			
					for(j=0; j <v.size() ; j++) {// Questo scorre i vari ticket da analizzare
						prima[0]=json.getJSONObject(k).getJSONObject(com).getString("message").contains(v.get(j).toString()+":");
						prima[1]=json.getJSONObject(k).getJSONObject(com).getString("message").contains("["+v.get(j).toString()+"]");
						reduceComplexity(commit ,prima,com,json,k,fileWriter,data1);
					}
				}
			}else {
				fileWriter.flush();
				return;
			}
		}
	}catch (IOException e) {
		logger.info("Errore");

	}
}
}