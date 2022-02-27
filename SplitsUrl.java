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

        Pattern pattern = Pattern.compile("#CCCCCC\">(\\d\\d)/(\\d\\d)/(\\d\\d\\d\\d)</TD><TD align=\"center\" style=\"padding: 4px; border-bottom: 1px solid #CCCCCC\">(\\d) for (\\d)");

        Matcher matcher = pattern.matcher(buff);
        boolean matches = matcher.matches();
        String splitStr = "";
        // int count1 = 0;
        while(matcher.find()) {
            // count1++;
            // System.out.println("found: " + count + " : "
            //         // + matcher.start() + " - " + matcher.end() + " " + buff.substring(matcher.start(), matcher.end())
            //          + " " + matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + " " + matcher.group(4)+ " " + matcher.group(5));
            //{"key": "NVDA_2021", "symbol": "NVDA", "jump": 4, "year": 2010, "month": 7, "day": 20 },
            int year = Integer.parseInt(matcher.group(3));
            if (year >= 2000) {
              int mon = Integer.parseInt(matcher.group(1));
              int day = Integer.parseInt(matcher.group(2));
      
              if (stockCount > 0)
                splitStr += ",\n";  
              splitStr += "{\"key\": \"" + sym + "_" + matcher.group(3) + "\", " + "\"symbol\": \"" + sym + "\", ";
              splitStr += "\"year\": " + year + ", ";
              splitStr += "\"month\": " + mon + ", ";
              splitStr += "\"day\": " + day + "}";
            }
        }
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
        FileOutputStream fs= new FileOutputStream("C://Users/eli/OneDrive/Documents/React/splits.txt");
        BufferedWriter splitsWrite = new BufferedWriter(new OutputStreamWriter(fs));
        splitsWrite.write ("[\n");
        
        FileReader reader = new FileReader ("C://Users/eli/OneDrive/Documents/React/stock_list.txt");
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        int stockCount = 0;
        
        while ((line = bufferedReader.readLine()) != null) {
          System.out.println(line);
          if (line.indexOf('(') == -1) {
            
        // String splits = getSplitsFromUrl ("AMZN");
            String splits = getSplitsFromUrl (line, stockCount);
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