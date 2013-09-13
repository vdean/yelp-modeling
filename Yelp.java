package Yelp;

/*
 Example code based on code from Nicholas Smith at http://imnes.blogspot.com/2011/01/how-to-use-yelp-v2-from-java-including.html
 For a more complete example (how to integrate with GSON, etc) see the blog post above.
 */

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

public class Yelp {

  OAuthService service;
  Token accessToken;
  Set<String> stopWords;
  BufferedWriter review_file;
  Map<String, String> review_cache;

  /**
   * Setup the Yelp API OAuth credentials.
   *
   * OAuth credentials are available from the developer site, under Manage API access (version 2 API).
   *
   * @param consumerKey Consumer key
   * @param consumerSecret Consumer secret
   * @param token Token
   * @param tokenSecret Token secret
   */
  public Yelp(String consumerKey, String consumerSecret, String token, String tokenSecret) {
    this.service = new ServiceBuilder().provider(YelpApi2.class).apiKey(consumerKey).apiSecret(consumerSecret).build();
    this.accessToken = new Token(token, tokenSecret);
    stopWords = new HashSet<String>(); 
    String[] words = "a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your".split(",");
    for (int i = 0; i < words.length; i++) {
      stopWords.add(words[i]);
    }
    review_cache = new HashMap<String, String>();
    try {
      BufferedReader reader = new BufferedReader(new FileReader("/Users/victoria/Desktop/school work/Probability:Math Modeling/yelp_reviews.txt"));
      String line;
      while ((line = reader.readLine()) != null) {
        String[] fields = line.split("\\|");
        //System.out.println("Read " + line.substring(0, 20) + " " + fields.length);
        if (fields.length == 2) {
          String response = fields[1];
          if (response.endsWith("\n")) {
            response = response.substring(0, response.length()-1);
          }
          review_cache.put(fields[0], response);
        }
      }
      System.out.println("Read " + review_cache.size() + " review entries");
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      review_file = new BufferedWriter(new FileWriter("/Users/victoria/Desktop/school work/Probability:Math Modeling/yelp_reviews.txt", true));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Search with term and location.
   *
   * @param term Search term
   * @param latitude Latitude
   * @param longitude Longitude
   * @return JSON string response
   */
  public String search(String term, double latitude, double longitude) {
    OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
    request.addQuerystringParameter("term", term);
    request.addQuerystringParameter("ll", latitude + "," + longitude);
    this.service.signRequest(this.accessToken, request);
    Response response = request.send();
    return response.getBody();
  }
   public String searchCity(String term, String city, int limit) {
    OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
    request.addQuerystringParameter("term", term);
    request.addQuerystringParameter("location", city);
    request.addQuerystringParameter("limit", "" + limit);

    this.service.signRequest(this.accessToken, request);
    Response response = request.send();
    return response.getBody();
  }
  public String getBusiness(String id) {
    OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/business/" + id);
    this.service.signRequest(this.accessToken, request);
    Response response = request.send();
    return response.getBody();
  }
  
  class Review {
    int num_stars;
    String review_text;
  };
  
  public void writeReviewEntry(String id, String response) {
    response = response.replace('|', ' ');
    review_cache.put(id, response);
    String line = id + "|" + response + "\n";
    try {
      review_file.write(line);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void getReviews(Vector<String> ids, Vector<Review> reviews) {
    for (int i = 0; i < ids.size(); i++) {
      String id = ids.get(i);
      String response;
      if (review_cache.containsKey(id)) {
        response = review_cache.get(id);
        //System.out.println("Cache hit for " + id);
      } else {
        System.out.println("Getting reviews for " + id);
        response = this.getBusiness(id);
        writeReviewEntry(id, response);
        System.out.println("Response: " + response.length() + "\n" + response);
      }
      Map<String, String> flattened = parseResponse(response);
      int index = 0;
      while (true) {
        String k1 = "reviews[" + index + "].rating";
        String k2 = "reviews[" + index + "].excerpt";
        if (!flattened.containsKey(k1) && !flattened.containsKey(k2)) {
          //System.out.println("Exiting for " + k1);
          break;
        } else if(!flattened.containsKey(k1) || !flattened.containsKey(k2)){
          System.err.println("Skipping for " + k1);
          index++;
          continue;
        }
        //System.out.println("Adding review for " + k1);
        Review r = new Review();
        r.review_text = flattened.get(k2).toLowerCase();
        r.num_stars = Integer.parseInt(flattened.get(k1));
        reviews.add(r);
        //System.out.println("Got review " + r.num_stars + ": " + r.review_text);
        index++;
      } 
    }  
  }

  class Distribution {
    float distribution_probability;
    int total_word_count;
    Map<String, Integer> count;
    Distribution() { count = new TreeMap<String, Integer>(); }
    
    void addDocument(Set<String> stopWords, String text) {
      distribution_probability++;
      StringTokenizer st = new StringTokenizer(text, " \t\n\r\f,.;:'\"!@#$%^&*()?/-<>=+–_");
      while (st.hasMoreTokens()) {
        String w = st.nextToken();
        if (!stopWords.contains(w)) {
          total_word_count++;
          if (count.containsKey(w)) {
            count.put(w, count.get(w) + 1);
          } else {
            count.put(w, new Integer(1));
          }
        }
      }
    }
    void print() {
      Set<String> words = count.keySet();
      Iterator<String> it = words.iterator();
      while(it.hasNext()) {
        String w = (String)it.next();
        System.out.println(w + " = " + count.get(w) + " " + this.getWordProbability(w));
      }
    }
    
    double getWordProbability(String w) {
      double divisor = total_word_count;
      if (count.containsKey(w)) {
        return count.get(w) / divisor;
      } else {
        // For rare words that don't occur in training data
        return 0.5 / divisor;
      }
    }
    
    double likelihood (Distribution globalModel, Set<String> stopWords, String text) {
      StringTokenizer st = new StringTokenizer(text, " \t\n\r\f,.;:'\"!@#$%^&*()?/-<>=+–_");
      double result = distribution_probability;
      while (st.hasMoreTokens()) {
        String w = st.nextToken();
        if (!stopWords.contains(w)) {
          result *= getWordProbability(w)/globalModel.getWordProbability(w);
        }
      }
      return result;
    }
  };
  
  static Map<String, String> parseResponse(String response) {
    JSONDecoder decoder = new JSONDecoder(response);
    JSONObject result = (JSONObject)decoder.decode();
    Map<String, String> flattened = new TreeMap<String, String>();
    result.flattenTo(flattened, "");
    return flattened;
  }
    
  static void addBusinessIds(Vector<String> ids, String response) {
    Map<String, String> flattened = parseResponse(response);
    int i = 0;
    while (true) {
      String k = "businesses[" + i + "].id";
      if (!flattened.containsKey(k)) {
        break;
      }
      String id = flattened.get(k);
      id = id.substring(1, id.length()-1);
      ids.add(id);
      //System.out.println("Got id " + id);
      i++;
    }      
  }
  
  static int indexFromStars(int num_stars) {
    return (num_stars >= 4) ? 2 : 1;
  }
  
  void buildModels (Distribution[] models, List<Review> reviews) {
    for (int i = 0; i < models.length; i++) {
      models[i] = new Distribution();
    }
    for (int i = 0; i < reviews.size(); i++) {
      Review r = reviews.get(i);
      models[indexFromStars(r.num_stars)].addDocument(stopWords, r.review_text);
      models[0].addDocument(stopWords, r.review_text);
    }
    if (reviews.size() > 0) {
      // Normalize document count to a probability
      for (int i = 0; i < models.length; i++) { 
        models[i].distribution_probability /= reviews.size();
      }
    }
  }
  
  void classify (Distribution[] models, int trainingSize, List<Review> reviews) {
    int num_correct = 0;
    for (int i = 0; i < reviews.size(); i++) {
      Review r = reviews.get(i);
      double max_likelihood = 0;
      int max_index = -1;
      for (int d = 1; d < models.length; d++) {
        double likelihood = models[d].likelihood(models[0], stopWords, r.review_text);
        //System.out.println("Likelihood for " + d + " = " + likelihood);
        if (likelihood > max_likelihood) {
          max_index = d;
          max_likelihood = likelihood;
        }
      }
      int expected = indexFromStars(r.num_stars);
      if (max_index == expected) {
          num_correct++;
      }
      //System.out.println("Classify stars: " + r.num_stars + " exp: " + expected + " predicted: " + max_index + " likelihood: " + max_likelihood);
    }
    double percent = (num_correct * 1.0) / reviews.size() * 100;
    System.out.println("Training reviews: " + trainingSize);
    System.out.println(num_correct + " correct out of " + reviews.size() + " " + percent + "%");
  }
  
  void dumpInterestingWords(Distribution[] models) {
    Map<Double, String> sorted = new TreeMap<Double, String>();
    Set<String> words2 = models[0].count.keySet();
    Iterator<String> it = words2.iterator();
    while (it.hasNext()) {
      String w = (String) it.next();
      sorted.put(models[2].getWordProbability(w)/models[1].getWordProbability(w),w);
    }
    Iterator it2 = sorted.entrySet().iterator();
    while (it2.hasNext()) {
      Map.Entry kv = (Map.Entry) it2.next();
      System.out.printf("%-20s: %.4f\n",kv.getValue(),kv.getKey());
    }
  }

  public static void main(String[] args) {
    // Update tokens here from Yelp developers site, Manage API access.
    String consumerKey = "piw-_wbiWBdgr6jDgPC-TQ";
    String consumerSecret = "aitPjddsXFfsaFCmxY-fnWUKlhI";
    String token = "7aipJDTuLYK6fncSBzgCCkMYWRQGJqKB";
    String tokenSecret = "VOR8mzNxRHJ5b_I6P0jX92OQVTM";

    Yelp yelp = new Yelp(consumerKey, consumerSecret, token, tokenSecret);
    //String response = yelp.search("restaurants", 30.361471, -87.164326);
    Vector<String> ids = new Vector<String>();
    if (false) {
      String[] cities = ("Palo Alto,San Francisco,Menlo Park,Mountain View,New York,Atlanta,Chicago,San Jose,Santa Cruz,San Diego,La Jolla,Los Gatos,Santa Clara,Fremont,Boston,Cambridge,Seattle,Portland,Tucson,Dallas,Austin,Houston,Pasadena,Miami,New Orleans,Detroit,Toronto,Vancouver,Olympia,Las Vegas,Salt Lake City,Provo,El Paso,Memphis,Baltimore,Nashville,Denver,Louisville,Milwaukee,Oklahoma City,Albuquerque,Tucson,Fresno,Sacramento,Long Beach,Kansas City,Mesa,Virginia Beach,Colorado Springs,Omaha,Raleigh,Cleveland,Tulsa,Honolulu,Oakland,Minneapolis,Wichita,Arlington,Bakersfield,New Orleans,Anaheim,Tampa,Aurora,Santa Ana,St. Louis,Pittsburgh,Corpus Christi,Riverside,Cincinnati,Lexington,Anchorage,Stockton,Toledo,Saint Paul,Newark,Greensboro,Buffalo,Plano,Lincoln,Henderson,Fort Wayne,Jersey City,St. Petersburg,Chula Vista,Norfolk,Orlando,Chandler,Laredo,Madison,Winston-Salem,Lubbock,Baton Rouge,Durham,Garland,Glendale,Reno,Hialeah,Chesapeake,Scottsdale,North Las Vegas,Irving,Birmingham,Rochester,San Bernadino,Spokane,Gilbert,Arlington,Montgomery,Boise,Richmond,Des Moines,Modesto,Fayetteville,Shreveport,Akron,Tacoma,Aurora,Oxnard,Fontana,Yonkers,Augusta,Mobile,Little Rock,Moreno Valley,Glendale,Amarillo,Huntington Beach,Columbus,Grand Rapids,Tallahassee,Worcester,Newport News,Huntsville,Knoxville,Providence,Santa Clarita,Grand Prairie,Brownsville,Jackson,Overland Park,Garden Grove,Santa Rosa,Chattanooga,Oceanside,Fort Lauderdale,Rancho Cucamonga,Port Saint Lucie,Ontario,Vancouver,Springfield,Lancaster,Eugene").split(",");
      //String[] cities = ("Palo Alto").split(",");
      for (int i = 0; i < cities.length; i++) {
        String response = yelp.searchCity("restaurants", cities[i], 20);
        addBusinessIds(ids, response);
      }
    } else {
      for (String k:yelp.review_cache.keySet()) {
        ids.add(k);
      }
    }
    int[] trainingSizes = {100, 250, 500, 1000, 2500, 5000, 7000, 10000, 11250};
    for (int it2 = 0; it2 < trainingSizes.length; it2++) {
      System.out.println("Got " + ids.size() + " restaurants");
      Vector<Review> reviews = new Vector<Review>();
      yelp.getReviews(ids, reviews);
      System.out.println("Got " + reviews.size() + " reviews");
      int split = reviews.size() / 2;
      
      List<Review> training_reviews = reviews.subList(0, Math.min(trainingSizes[it2],reviews.size() - reviews.size() / 10));
      List<Review> test_reviews = reviews.subList(reviews.size() - reviews.size() / 10, reviews.size());

      Distribution[] models = new Distribution[3];

      yelp.buildModels(models, training_reviews);
      if (it2 == trainingSizes.length - 1) {
        yelp.dumpInterestingWords(models);
      }
      for (int i = 0; i < models.length; i++) {
        //System.out.println("------ distribution" + i);
        //models[i].print();
      }
      yelp.classify(models, training_reviews.size(), test_reviews);
      if (false) {
        String response = yelp.getBusiness("flounders-chowder-house-gulf-breeze");

        System.out.println(response.length());
        System.out.println(response);
        JSONDecoder decoder = new JSONDecoder(response);
        JSONObject result = (JSONObject) decoder.decode();

        Map<String, String> flattened = new TreeMap<String, String>();
        result.flattenTo(flattened, "");

        Set<String> keys = flattened.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
          String key = (String) it.next();
          System.out.println(key + " = " + flattened.get(key));
        }

        System.out.println("ToJson: " + flattened.toString());
      }
    }
    try {
      yelp.review_file.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}