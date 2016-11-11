import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Antifraud {
	//The key is the id and the value is the id's adjacency list in the form of a HashSet to avoid duplicates
	static HashMap<Integer,HashSet<Integer>> hm;
    static File file1,file2,file3,file4,file5; 
	static boolean flag;
	
	public static void main(String[] args) {
		// Date for logging purpose
		if(args.length!=6){
			System.out.println("Need 6 arguments: 5 files and 1 flag for graph updation");
			return;
		}
		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss:SSS");
		Date dateobj = new Date();
		System.out.println("Start Time:\t" + df.format(dateobj));
		file1 = new File(args[0]);
		file2 = new File(args[1]);
		file3 = new File(args[2]);
		file4 = new File(args[3]);
		file5 = new File(args[4]);
        flag = Boolean.parseBoolean(args[5]);
		
		//Builds the initial graph
		buildGraph();
		dateobj = new Date();
		System.out.println("Graph Build Finish Time:\t" + df.format(dateobj));
		
		//Classifies the stream file transactions and updates the graph for future transactions.
		//Thus the next time the same transaction ids come, they will be 1st degree friends
		classifyTransaction();
		dateobj = new Date();
		System.out.println("Classification Finish Time:\t" + df.format(dateobj));
	}
    
	//Builds the initial Graph
	public static void buildGraph(){
		hm = new HashMap<Integer,HashSet<Integer>>();
		
		Scanner kbd = null;
		try {
			FileInputStream inputStream = new FileInputStream(file1);
			kbd = new Scanner(inputStream);
			//Ignore carriage return and only use line feed as new line identifier
			kbd = kbd.useDelimiter("\n");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String[] inp;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		@SuppressWarnings("unused")
		Date dt;
		int id1,id2;
		@SuppressWarnings("unused")
		float amount;
		@SuppressWarnings("unused")
		String comment;
		HashSet<Integer> hs;
		
		//Skip the header
		if(kbd.hasNext()) kbd.next();
		
		//use to track the line number in the file for error logging
		int i=0;
		
		while(kbd.hasNext()){
			i++;
			inp = kbd.next().split(",",5);
			
			try{
				//Parse all the columns even though not required for basic record validation
				dt = sdf.parse(inp[0].trim());
				id1 = Integer.parseInt(inp[1].trim());
				id2 = Integer.parseInt(inp[2].trim());
				amount = Float.parseFloat(inp[3].trim());
				comment = inp[4].trim();
				
				//Add id2 as id1's friend
				if(hm.containsKey(id1)){
					//if id1 already has friends
					hm.get(id1).add(id2);
				}
				else{
					//if id1 doesn't have any friends yet
					hs = new HashSet<Integer>();
					hs.add(id2);
					hm.put(id1, hs);
				}
				//Add id1 as id2's friend
				if(hm.containsKey(id2)){
					//if id2 already has friends
					hm.get(id2).add(id1);
				}
				else{
					//if id2 doesn't have any friends yet
					hs = new HashSet<Integer>();
					hs.add(id1);
					hm.put(id2, hs);
				}
			}catch(Exception e){
				//Catch parsing exceptions. Erroneous records will be rejected.
				System.out.println(i);
				e.printStackTrace();
				System.out.println(Arrays.toString(inp));
				continue;
			}
		}
		kbd.close();
	}
	
	public static void classifyTransaction(){
		FileWriter out1 = null;
		FileWriter out2 = null;
		FileWriter out3 = null;
		Scanner kbd = null;
				
		try {
			file3.createNewFile();
			file4.createNewFile();
			file5.createNewFile();
			out1 = new FileWriter(file3);
			out2 = new FileWriter(file4);
			out3 = new FileWriter(file5);
			FileInputStream inputStream = new FileInputStream(file2);
			kbd = new Scanner(inputStream);
			//Ignore carriage return and only use line feed as new line identifier
			kbd = kbd.useDelimiter("\n");
		} catch (IOException e1) {
			//Catch any file errors
			e1.printStackTrace();
		}

		String[] inp;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		@SuppressWarnings("unused")
		Date dt;
		int id1,id2;
		@SuppressWarnings("unused")
		float amount;
		@SuppressWarnings("unused")
		String comment;
		
		//skip header
		if(kbd.hasNext()) kbd.next();
		//Track line number in the file for error logging
		int i=0;
		
		while(kbd.hasNext()){
			i++;
			inp = kbd.next().split(",",5);
			try{
				//Parse all the columns even though not required for basic record validation
				dt = sdf.parse(inp[0].trim());
				id1 = Integer.parseInt(inp[1].trim());
				id2 = Integer.parseInt(inp[2].trim());
				amount = Float.parseFloat(inp[3].trim());
				comment = inp[4].trim();
				
				//Classifies the current transaction
				classify(id1,id2,out1,out2,out3);
			}catch(Exception e){
				//Catch parsing exceptions. Erroneous records will be rejected.
				System.out.println(i);
				e.printStackTrace();
				System.out.println(Arrays.toString(inp));
				continue;
			}
		}
		kbd.close();
		try {
			out1.flush();
			out1.close();
			out2.flush();
			out2.close();
			out3.flush();
			out3.close();
			System.out.println(file3.getAbsolutePath());
			System.out.println(file4.getAbsolutePath());
			System.out.println(file5.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void classify(int id1, int id2, FileWriter out1, FileWriter out2, FileWriter out3) throws IOException{
		HashSet<Integer> hs1,hs2,hs1_queue,hs2_queue;
		HashSet<Integer> hs_id1,hs_id2; 
		
		if(hm.containsKey(id1) && hm.containsKey(id2)){
			hs_id1 = hm.get(id1);
			hs_id2 = hm.get(id2);
			if(hs_id1.contains(id2)) {
				//1st Degree
				//A -> B
				out1.write("trusted\n");
				out2.write("trusted\n");
				out3.write("trusted\n");
				return;
			}
			
			//Not 1st Degree
			out1.write("unverified\n");
			hs1 = new HashSet<Integer>();
			hs1_queue = new HashSet<Integer>();
			hs1.addAll(hs_id1);
			for(int id : hs1){
				hs1_queue.addAll(hm.get(id));
				if(hs1_queue.contains(id2)){
					//2nd Degree
					//A -> C -> B
					if(flag){
						hs_id1.add(id2);
						hs_id2.add(id1);
					}
					out2.write("trusted\n");
					out3.write("trusted\n");
					return;
				}
			}
			
			//Not 2nd Degree
			out2.write("unverified\n");
			hs2 = new HashSet<Integer>();
			hs2_queue = new HashSet<Integer>();
			hs2.addAll(hs_id2);
			for(int id : hs2){
				hs2_queue.addAll(hm.get(id));
			}
			hs1.clear();
			hs2.clear();
			hs1.addAll(hs1_queue);
			hs2.addAll(hs2_queue);
			hs1_queue.clear();
			hs2_queue.clear();
			int size=0;
			for(int id : hs1){
				hs1_queue.addAll(hm.get(id));
				size = hs1_queue.size();
				hs1_queue.removeAll(hs2);
				if(size!=hs1_queue.size()){
					//3rd Degree
					//A -> C -> D -> B
					if(flag){
						hs_id1.add(id2);
						hs_id2.add(id1);
					}
					out3.write("trusted\n");
					return;
				}
			}
			
			//Not 3rd Degree
			for(int id : hs2){
				hs2_queue.addAll(hm.get(id));
			}
			size = hs1_queue.size();
			hs1_queue.removeAll(hs2_queue);
			if(size!=hs1_queue.size()){
				//4th Degree
				//A -> C -> D -> E -> B
				if(flag){
					hs_id1.add(id2);
					hs_id2.add(id1);
				}
				out3.write("trusted\n");
				return;
			}
			
			//Not 4th Degree
			out3.write("unverified\n");
			return;
		}
		else {
			//Either of the 2 ids is a New Customer
			out1.write("unverified\n");
			out2.write("unverified\n");
			out3.write("unverified\n");
			return;
		}
	}
}
