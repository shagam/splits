/**
 * Url
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileReader;
import java.io.IOException;

// import com.allen_sauer.gwt.log.client.Log;
// import com.google.gwt.storage.client;
// import com.google.gwt.user.client.Cookies;



public class SplitsUrl {

  // public static String readStoredItem(final String key) { 
  //   if (Storage.isSupported()) {
  //       String item = Storage.getLocalStorageIfSupported().getItem(key);
  //       item = item == null ? "" : item;
  //       // Log.debug("Returning value:" + item + ", from local storage with key:" + key);
  //       return item;
  //   } else {
  //       // get from cookies
  //       String cookie = Cookies.getCookie(key);
  //       // Log.debug("Returning value:" + cookie + ", from cookie with key:" + key);
  //       return cookie;
  //   }
  // }

    // separte code to build also for non split records
    static String buildSplitStr (String sym, Double jump, int year, int mon, int day) {
      String splitStr = "";
      String yearStr = ""+ year;
      if (year == 0)
        yearStr = "0000"; // easier for human read
      splitStr += "{\"key\": \"" + sym + "_" + yearStr + "\", ";
      splitStr +=  "\"symbol\": \"" + sym + "\", ";
      splitStr += "\"jump\": " + jump + ", "; 
      splitStr += "\"year\": " + year + ", ";
      splitStr += "\"month\": " + mon + ", ";
      splitStr += "\"day\": " + day + "}";
      return splitStr;
    }


  static String getSplitsFromUrl (String sym, int stockCount) { 


    try {

      URL u = new URL ("https://www.stocksplithistory.com/?symbol="+ sym);
      u.openConnection();
      HttpURLConnection hr = (HttpURLConnection) u.openConnection();
      // System.out.println(hr.getResponseCode());
      if (hr.getResponseCode() == 200) {
        InputStream im=hr.getInputStream(); 
        // StringBuffer sb=new StringBuffer();
        BufferedReader br=new BufferedReader (new InputStreamReader(im));

        
        String buff = "";
        String line=br.readLine();
        int count = 0;
        while (line != null) {
          // System.out.println (line);
          buff += line;
          // bw.write(line);
          // bw.newLine();
          line = br.readLine();
          count ++;
        }
        // System.out.print (buff);

        //#CCCCCC">06/02/1998</TD><TD align="center" style="padding: 4px; border-bottom: 1px solid #CCCCCC">2 for 1
        //#CCCCCC">01/05/1999</TD><TD align="center" style="padding: 4px; border-bottom: 1px solid #CCCCCC">3 for 1
        //#CCCCCC">09/02/1999</TD><TD align="center" style="padding: 4px; border-bottom: 1px solid #CCCCCC">2 for 1
        //#CCCCCC">08/31/2020</TD><TD align="center" style="padding: 4px; border-bottom: 1px solid #CCCCCC">4 for 1</TD></TR>

        //"</TD><TD align=\"center\" style=\"padding: 4px; border-bottom: 1px solid #CCCCCC\">"
        //#CCCCCC">06/02/1998</TD><TD align="center" style="padding: 4px; border-bottom: 1px solid #CCCCCC">2 for 1
        //</TD></TR><TR><TD align=

        //#CCCCCC">01/21/2010</TD><TD align="center" style="padding: 4px; border-bottom: 1px solid #CCCCCC">50 for 1

        Pattern pattern = Pattern.compile("#CCCCCC\">(\\d\\d)/(\\d\\d)/(\\d\\d\\d\\d)</TD><TD align=\"center\" style=\"padding: 4px; border-bottom: 1px solid #CCCCCC\">(\\d*) for (\\d*)");

        Matcher matcher = pattern.matcher(buff);
        // boolean matches = matcher.matches();
        String splitStr = "";
        int matchCount = 0;
    
        // if (matches > 0) {
        while(matcher.find()) {
            System.out.println("lines: " + count + " : " +  " sym: " + sym
                    // + matcher.start() + " - " + matcher.end() + " " + buff.substring(matcher.start(), matcher.end())
                     + " " + matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + " " + matcher.group(4)+ " " + matcher.group(5));
            //{"key": "NVDA_2021", "symbol": "NVDA", "jump": 4, "year": 2010, "month": 7, "day": 20 },
            int year = Integer.parseInt(matcher.group(3));
            if (year >= 2000) {
              try {
              int mon = Integer.parseInt(matcher.group(1));
              int day = Integer.parseInt(matcher.group(2));
   
                Double cnt4 = Double.parseDouble(matcher.group(4)); // jump factor
                Double cnt5 = Double.parseDouble(matcher.group(5)); // jump factor

              Double jump = cnt4 / cnt5;
              jump = Math.round (jump * 1000) / 1000.0;
              
              if (jump != 1) {  // omit jump == 1, which is not a real split
                if (stockCount > 0)
                  splitStr += ",\n";
                  
                splitStr += buildSplitStr (sym, jump, year, mon, day); 
                matchCount++; 
              }
            } catch (NumberFormatException e) {e.printStackTrace();};
            }
        }

        if (matchCount == 0) {
          if (stockCount > 0)
            splitStr += ",\n";
          splitStr += buildSplitStr (sym, (double)0, 0, 0, 0); // year 0 splits will be ignored by stock-chart
        }
      // }
        // if (splitStr!= "")
        //   splitStr +=  "\n";
        return splitStr;
      }
      // System.out
    } catch (Exception e) {
      System.out.println(e);}
    return null;
    // System.out.println("hello");
  }
        // System.out.print (count);

    public static void main(String[] args) {

      try {
        // create output file
        FileOutputStream fs= new FileOutputStream("../stocksplithistory.json");
        BufferedWriter splitsWrite = new BufferedWriter(new OutputStreamWriter(fs));
        splitsWrite.write ("[\n");
        
        // read input
        FileReader reader = new FileReader ("./stock_list.txt");
        BufferedReader bufferedReader = new BufferedReader(reader);
        String stockSymbol;
        int stockCount = 0;
        
        while ((stockSymbol = bufferedReader.readLine()) != null) {
          // System.out.println(line);
          if (stockSymbol.indexOf('(') == -1) {
            
            // String splits_ = getSplitsFromUrl ("BRK.B",1);
            String splits = getSplitsFromUrl (stockSymbol, stockCount);
            if (splits != "") {
              System.out.println (splits);
              if (stockCount > 0)
                splitsWrite.write (splits);
              else
                splitsWrite.write (splits);
              stockCount++;
            }           
          } 
        }
        reader.close();
        splitsWrite.write("\n]\n");
        splitsWrite.close();

      }catch (IOException e) {
        e.printStackTrace();
      } 
    }


  
}