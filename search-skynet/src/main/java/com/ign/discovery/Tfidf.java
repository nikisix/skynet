package com.ign.discovery;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Tfidf {
	static HashMap<String,Double>  tf = new HashMap<String,Double> ();
	static HashMap <String,Double> df = new HashMap<String,Double> ();
	
	//term_freq = number of tweets in a given hash / total number of tweets in a given 
bucket
	//ex (current_bucket.count("#LOL")/current_bucket.total) / 
(allBuckets.count("#LOL")/allBuckets.total)

	private static HashMap<String,Double> calcFreq(String[] bucket){
		HashMap <String,Double>freq = new HashMap<String,Double>();
		
		for(String s : bucket){
			if(freq.keySet().contains(s)){
				freq.put(s, freq.get(s)+1);
			} else {
				freq.put(s, 1.0);
			}
		}
		for(String key : freq.keySet()){
			freq.put(key,(Double) (freq.get(key)/bucket.length));
		}
		return freq; 
	}
	
	private static HashMap <String,Double> calcTF(String[] currentBucket){
		tf = calcFreq(currentBucket);
		return tf;
	}
	
	private static HashMap <String,Double> calcDF(String[] allBuckets){
		df = calcFreq(allBuckets);
		return df;
	}

//	public static String tfidfToJsonWithDictionary(String [] currentBucket, String[] 
allBuckets){
//		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		calcTF(currentBucket);
//		calcDF(allBuckets);
//		HashMap <String,Double> results = new HashMap <String,Double>();
//		double num=0,den=0;
//		
//		for(String s : tf.keySet()){
//			try{
//				num = tf.get(s);
//			} catch(NullPointerException ex){
//				System.err.println("tf.get( " + s + " failed");
//			}
//			try{
//				den = df.get(s);
//			} catch(NullPointerException ex){
//				System.err.println("df.get( " + s + " failed");
//			}
//			results.put(s,num/den); //change this line
////			num/(alpha*english_languge_freq + (1-alpha)*all_buckets_freq)
////			System.out.println(s + " " + results.get(s));
//		}
//		
////		return results;
////		byValue cmp = new byValue();
//		TreeMap <String,Double> sortedResults = new TreeMap<String, Double>();
//		sortedResults.putAll(results);
//		results.clear();
//		SortedSet<Entry<String, Double>> sset = 
entriesSortedByValues(sortedResults);
//		
//		
//		LinkedHashMap <String,Double>linkedHash = new 
LinkedHashMap<String,Double>();
//		Iterator <Entry<String, Double>>iter = sset.iterator();
//		while(iter.hasNext()){
//			Entry<String,Double> curEntry = iter.next();
//			linkedHash.put(curEntry.getKey(),curEntry.getValue());
//		}
//		
//		return gson.toJson(linkedHash);
//	}
	
	public static String tfidfToJson(String [] currentBucket, String[] allBuckets){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		calcTF(currentBucket);
		calcDF(allBuckets);
		HashMap <String,Double> results = new HashMap <String,Double>();
		double num=0,den=0;
		
		for(String s : tf.keySet()){
			try{
				num = tf.get(s);
			} catch(NullPointerException ex){
				System.err.println("tf.get( " + s + " failed");
			}
			try{
				den = df.get(s);
			} catch(NullPointerException ex){
				System.err.println("df.get( " + s + " failed");
			}
			results.put(s,num/den); //change this line
//			num/(alpha*english_languge_freq + (1-alpha)*all_buckets_freq)
//			System.out.println(s + " " + results.get(s));
		}
		
//		return results;
//		byValue cmp = new byValue();
		TreeMap <String,Double> sortedResults = new TreeMap<String, Double>();
		sortedResults.putAll(results);
		results.clear();
		SortedSet<Entry<String, Double>> sset = 
entriesSortedByValues(sortedResults);
		
		
		LinkedHashMap <String,Double>linkedHash = new 
LinkedHashMap<String,Double>();
		Iterator <Entry<String, Double>>iter = sset.iterator();
		while(iter.hasNext()){
			Entry<String,Double> curEntry = iter.next();
			linkedHash.put(curEntry.getKey(),curEntry.getValue());
		}
		
		return gson.toJson(linkedHash);
	}
	
	public static SortedSet<Entry<String, Double>> tfidf(String [] currentBucket, 
String[] allBuckets){
		calcTF(currentBucket);
		calcDF(allBuckets);
		HashMap <String,Double> results = new HashMap <String,Double>();
		double num=0,den=0;
		
		for(String s : tf.keySet()){
			try{
				num = tf.get(s);
			} catch(NullPointerException ex){
				System.err.println("tf.get( " + s + " failed");
			}
			try{
				den = df.get(s);
			} catch(NullPointerException ex){
				System.err.println("df.get( " + s + " failed");
			}
			results.put(s,num/den); //change this line
//			num/(alpha*english_languge_freq + (1-alpha)*all_buckets_freq)
//			System.out.println(s + " " + results.get(s));
		}
		
//		return results;
//		byValue cmp = new byValue();
		TreeMap <String,Double> sortedResults = new TreeMap<String, Double>();
		sortedResults.putAll(results);
		results.clear();
		SortedSet<Entry<String, Double>> sset = 
entriesSortedByValues(sortedResults);
		return sset;
	}
	
	public static void main(String[] args) {
		String tweet = "Suppose we have a set of English text documents and wish to 
determine which document is most relevant to the query the brown cow. A simple way to start 
out is by eliminating documents that do not contain all three words the, brown, and cow, but 
this still leaves many documents. To further distinguish them, we might count the number of 
times each term occurs in each document and sum them all together; the number of times a 
term occurs in a document is called its term frequency. However, because the term the is so 
common, this will tend to incorrectly emphasize documents which happen to use the word the 
more frequently, without giving enough weight to the more meaningful terms brown and cow. 
Also the term the is not a good keyword to distinguish relevant and non-relevant documents 
and terms. On the contrary, the words brown and cow that occur rarely are good keywords to 
distinguish relevant documents from the non-relevant documents. Hence an inverse document 
frequency factor is incorporated which diminishes the weight of terms that occur very 
frequently in the collection and increases the weight of terms that occur rarely.";
//		String tweet = "Suppose we have a set of English text document and wish";
		tweet = tweet.replaceAll("[^A-Za-z\\s]", "");
		String doc =   "Suppose we have a set of English text documents and wish to 
determine which document is most relevant to the query the brown cow. A simple way to start 
out is by eliminating documents that do not contain all three words the, brown, and cow, but 
this still leaves many documents. To further distinguish them, we might count the number of 
times each term occurs in each document and sum them all together; the number of times a 
term occurs in a document is called its term frequency. However, because the term the is so 
common, this will tend to incorrectly emphasize documents which happen to use the word the 
more frequently, without giving enough weight to the more meaningful terms brown and cow. 
Also the term the is not a good keyword to distinguish relevant and non-relevant documents 
and terms. On the contrary, the words brown and cow that occur rarely are good keywords to 
distinguish relevant documents from the non-relevant documents. Hence an inverse document 
frequency factor is incorporated which diminishes the weight of terms that occur very 
frequently in the collection and increases the weight of terms that occur rarely. The term 
count in the given document is simply the number of times a given term appears in that 
document. This count is usually normalized to prevent a bias towards longer documents (which 
may have a higher term count regardless of the actual importance of that term in the 
document) to give a measure of the importance of the term t within the particular document 
d. Thus we have the term frequency tf(t,d), defined in the simplest case as the occurrence 
count of a term in a document. (Many variants have been suggested; see e.g. Manning, 
Raghavan and Schütze, p.118.) The inverse document frequency is a measure of the general 
importance of the term (obtained by dividing the total number of documents by the number of 
documents containing the term, and then taking the logarithm of that quotient).";
//		String doc =   "Suppose we have a set of English text document and wish to 
determine which document is most relevant";
		doc = doc.replaceAll("[^A-Za-z\\s]", "");
		
//		for(String s : doc.split(" ")){
//			System.out.println(s);
//		}

		String jsonString = tfidfToJson(tweet.split(" "),doc.split(" "));
		System.out.println(jsonString);
//		SortedSet <Entry<String,Double>> sset = tfidf(tweet.split(" "),doc.split(" 
"));
//		Iterator <Entry<String, Double>> iter = sset.iterator();
//		Entry <String,Double>e;
		
//		while(iter.hasNext()){
//			e = iter.next();
//			System.out.println(e.getKey() + " " + e.getValue()); 	
//		}
	}

	static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> 
entriesSortedByValues(Map<K,V> map) {
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
	        new Comparator<Map.Entry<K,V>>() {
	            @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
	                int res = -e1.getValue().compareTo(e2.getValue());
	                return res != 0 ? res : 1; // Special fix to preserve items with 
equal values
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
}

//class byValue implements Comparator<Map.Entry<String,Double>> {
//    public int compare(Map.Entry<String,Double> e1, Map.Entry<String,Double> e2) {
//        if (e1.getValue() < e2.getValue()){
//            return 1;
//        } else if (e1.getValue() == e2.getValue()) {
//            return 0;
//        } else {
//            return -1;
//        }
//    }
//}
//class Mycompare implements Comparator <String>{
//	Map <String,Double> base;
//	
//	Mycompare(HashMap <String,Double>Base){
//		base = Base;
//	}
//	
//    public int compare(String s1,String s2)
//    {
//        Double d1=base.get(s1);
//        Double d2=base.get(s2);
////        if(d1 == null)
////        	System.err.println("value with key is null. key = " + s1);
////        if(d2 == null)
////        	System.err.println("value with key is null. key = " + s2);
//
//        if(d1==d2)  return 0;
//        if(d1>d2)	return 1;
//        else		return -1;
//    }
//}
